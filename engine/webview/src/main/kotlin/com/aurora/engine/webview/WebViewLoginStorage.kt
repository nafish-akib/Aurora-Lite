package com.aurora.engine.webview

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.aurora.data.security.EncryptedPrefs
import com.aurora.engine.LoginStorage
import org.json.JSONArray
import org.json.JSONObject

/**
 * Password vault for the WebView engine. Since Android WebView has no
 * autofill-style delegate (unlike GeckoView's Autofill.Delegate), capture is
 * performed by a small JS probe injected on every page load which reports
 * submitted login forms through a @JavascriptInterface bridge. Pre-fill of
 * saved credentials is intentionally not implemented in v1 — parity gap,
 * documented in the lite report.
 */
class WebViewLoginStorage(context: Context) : LoginStorage {

    private val savedLogins = mutableListOf<LoginStorage.SavedLogin>()
    private val securePrefs by lazy { EncryptedPrefs(context.applicationContext, "aurora_logins") }

    override var saveOnCapture: Boolean = false
        set(value) {
            field = value
            securePrefs.put("_consent", value.toString())
        }

    init {
        loadFromStorage()
        try {
            saveOnCapture = securePrefs.get("_consent")?.toBoolean() ?: false
        } catch (_: Exception) { }
    }

    override fun saveLogin(origin: String, username: String, password: String) {
        val existing = savedLogins.indexOfFirst { it.origin == origin && it.username == username }
        if (existing >= 0) savedLogins[existing] = LoginStorage.SavedLogin(origin, username, password)
        else savedLogins.add(LoginStorage.SavedLogin(origin, username, password))
        persistToStorage()
        Log.d("AuroraLogin", "WebView: Saved for $origin ($username)")
    }

    override fun findLogins(origin: String): List<LoginStorage.SavedLogin> =
        if (origin.isBlank()) savedLogins.toList() else savedLogins.filter { it.origin == origin }

    override fun getAllLogins(): List<LoginStorage.SavedLogin> = savedLogins.toList()

    override fun deleteLogin(origin: String, username: String) {
        savedLogins.removeAll { it.origin == origin && it.username == username }
        persistToStorage()
    }

    override fun clearAll() {
        savedLogins.clear()
        securePrefs.clear()
        Log.d("AuroraLogin", "WebView: Cleared")
    }

    /** Registers the JS bridge on the given WebView (call once per view). */
    fun install(webView: WebView) {
        try {
            webView.addJavascriptInterface(LoginBridge(this), "AuroraLoginBridge")
            Log.d("AuroraLogin", "WebView login probe installed")
        } catch (e: Exception) {
            Log.w("AuroraLogin", "WebView login probe unavailable: ${e.message}")
        }
    }

    /** Injects the capture probe; call on every onPageFinished. */
    fun injectCaptureScript(webView: WebView) {
        try {
            webView.evaluateJavascript(CAPTURE_SCRIPT, null)
        } catch (e: Exception) {
            Log.w("AuroraLogin", "WebView capture script injection failed: ${e.message}")
        }
    }

    private fun persistToStorage() {
        try {
            val array = JSONArray()
            savedLogins.forEach {
                val obj = JSONObject()
                obj.put("origin", it.origin)
                obj.put("username", it.username)
                obj.put("password", it.password)
                array.put(obj)
            }
            securePrefs.put("logins", array.toString())
        } catch (_: Exception) { }
    }

    private fun loadFromStorage() {
        try {
            val json = securePrefs.get("logins") ?: return
            val array = JSONArray(json)
            savedLogins.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                savedLogins.add(
                    LoginStorage.SavedLogin(
                        obj.getString("origin"),
                        obj.getString("username"),
                        obj.getString("password")
                    )
                )
            }
        } catch (_: Exception) { }
    }

    private class LoginBridge(
        private val storage: WebViewLoginStorage
    ) {
        @JavascriptInterface
        fun submit(payload: String) {
            if (!storage.saveOnCapture) return
            try {
                val obj = JSONObject(payload)
                val username = obj.optString("u", "")
                val password = obj.optString("p", "")
                val url = storage.lastCapturedUrl.substringBefore("?").substringBefore("#")
                if (username.isNotBlank() && password.isNotBlank() && url.isNotBlank()) {
                    storage.saveLogin(url, username, password)
                }
            } catch (e: Exception) {
                Log.w("AuroraLogin", "WebView login capture parse failed: ${e.message}")
            }
        }
    }

    @Volatile
    var lastCapturedUrl: String = ""
        internal set

    companion object {
        private const val CAPTURE_SCRIPT = """(function() {
  if (window.__auroraLoginProbe__) return;
  window.__auroraLoginProbe__ = true;
  function findUserField(form) {
    var inputs = form.querySelectorAll('input');
    for (var i = 0; i < inputs.length; i++) {
      var inp = inputs[i];
      var type = (inp.type || 'text').toLowerCase();
      if (type === 'text' || type === 'email' || type === 'tel' || type === 'search') {
        var hint = (inp.name + ' ' + inp.id + ' ' + (inp.autocomplete || '')).toLowerCase();
        if (/user|login|email|account|username/i.test(hint)) return inp;
      }
    }
    return form.querySelector('input[type="email"], input[type="tel"], input[type="text"]');
  }
  function capture(form) {
    var pass = '';
    var inputs = form.querySelectorAll('input');
    for (var i = 0; i < inputs.length; i++) {
      var inp = inputs[i];
      var type = (inp.type || 'text').toLowerCase();
      if (type === 'password') { pass = inp.value; break; }
    }
    if (!pass) return;
    var userField = findUserField(form);
    var username = userField ? userField.value : '';
    if (!username) return;
    try { window.AuroraLoginBridge.submit(JSON.stringify({ u: username, p: pass })); } catch (e) {}
  }
  document.addEventListener('submit', function(e) {
    var f = e.target;
    if (f && f.tagName === 'FORM') capture(f);
  }, true);
})();"""
    }
}