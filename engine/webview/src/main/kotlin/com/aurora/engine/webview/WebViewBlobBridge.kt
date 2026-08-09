package com.aurora.engine.webview

import android.content.Context
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class WebViewBlobBridge(
    private val appContext: Context,
    private val onDownload: (url: String, fileName: String, mimeType: String, savedFilePath: String) -> Unit
) {
    private val outputDir = appContext.cacheDir.resolve("blobs").also { it.mkdirs() }

    @JavascriptInterface
    fun onBlobDownload(dataUrl: String, mimeType: String, fileName: String) {
        try {
            val base64 = dataUrl.substringAfter("base64,").trim()
            val decoded = Base64.decode(base64, Base64.DEFAULT)
            val safeName = fileName.ifBlank { "download" }
            var outFile = File(outputDir, safeName)
            if (outFile.exists()) {
                outFile = File(outputDir, "${UUID.randomUUID()}_${safeName}")
            }
            FileOutputStream(outFile).use { it.write(decoded) }
            Log.d("AuroraWebView", "Blob download saved: ${outFile.absolutePath} (${decoded.size} bytes)")
            onDownload(outFile.toURI().toString(), safeName, mimeType.ifBlank { "application/octet-stream" }, outFile.absolutePath)
        } catch (e: Exception) {
            Log.w("AuroraWebView", "Blob download failed", e)
        }
    }

    companion object {
        val INJECT_SCRIPT = """
(function(){if(window._auroraBlobInjected)return;window._auroraBlobInjected=!0;document.addEventListener('click',function(e){var a=e.target;while(a&&a.tagName!=='A')a=a.parentElement;if(!a)return;var h=a.getAttribute('href');if(!h||h.indexOf('blob:')!==0)return;e.preventDefault();e.stopPropagation();fetch(h).then(function(r){return r.blob()}).then(function(bl){var fr=new FileReader();fr.onload=function(){window.AuroraBlob.onBlobDownload(fr.result,bl.type||'',a.getAttribute('download')||'')};fr.readAsDataURL(bl)}).catch(function(){console.log('Aurora: blob fetch failed, opening directly');window.open(h)})},!0)})();
        """.trimIndent()
    }
}
