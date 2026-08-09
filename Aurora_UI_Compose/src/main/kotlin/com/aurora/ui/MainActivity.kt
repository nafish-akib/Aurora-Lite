package com.aurora.ui

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.aurora.data.DataService
import com.aurora.data.storage.UsbStateReceiver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuroraNavigationIntents {
    private val _urls = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val urls = _urls.asSharedFlow()

    fun openUrl(url: String) {
        _urls.tryEmit(url)
    }
}

object AuroraAppLifecycle {
    private val _foreground = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)
    val foreground = _foreground.asSharedFlow()

    private val _trimMemory = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val trimMemory = _trimMemory.asSharedFlow()

    fun setForeground(isForeground: Boolean) {
        _foreground.tryEmit(isForeground)
    }

    fun trimMemory(level: Int) {
        _trimMemory.tryEmit(level)
    }
}

class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_CODE_NOTIFICATIONS = 1001
    }

    private val usbReceiver = UsbStateReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installGlobalCrashHandler()
        DataService.initialize(applicationContext)
        EngineFactory.create(applicationContext).initialize(applicationContext)
        enableEdgeToEdge()
        requestNeededPermissions()
        val initialUrl = intent.webUrl()
        setContent {
            AuroraApp(initialUrl = initialUrl)
        }
    }

    private fun installGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("AuroraCrash", "FATAL: Uncaught exception on thread ${thread.name}", throwable)
            try {
                val ctx = applicationContext
                val file = java.io.File(ctx.cacheDir, "aurora_crash.log")
                val sw = java.io.StringWriter()
                java.io.PrintWriter(sw).use { throwable.printStackTrace(it) }
                val entry = "${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())} | ${thread.name} | ${throwable.javaClass.name}: ${throwable.message}\n${sw}\n---\n"
                file.appendText(entry)
            } catch (_: Exception) { }
            try {
                val ctx = applicationContext
                val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                if (intent != null) ctx.startActivity(intent)
            } catch (_: Exception) { }
            defaultHandler?.uncaughtException(thread, throwable)
            try {
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(10)
            } catch (_: Exception) { }
        }
    }

    fun checkPreviousCrash(): String? {
        return try {
            val file = java.io.File(applicationContext.cacheDir, "aurora_crash.log")
            if (file.exists() && file.length() > 0) {
                val content = file.readText().take(2000)
                file.delete()
                content
            } else null
        } catch (_: Exception) { null }
    }

    private fun requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATIONS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            Log.d("AuroraPerms", "POST_NOTIFICATIONS granted=$granted")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.webUrl()?.let { AuroraNavigationIntents.openUrl(it) }
    }

    override fun onPause() {
        try { unregisterReceiver(usbReceiver) } catch (_: Exception) {}
        super.onPause()
        AuroraAppLifecycle.setForeground(false)
        Log.d("AuroraLifecycle", "MainActivity.onPaused engine=${AuroraEngineConfig.displayName}")
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (KeyBridge.isKeyboardOpen) return super.dispatchKeyEvent(event)
        if (event.action != KeyEvent.ACTION_DOWN) return super.dispatchKeyEvent(event)
        val direction = when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> "UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "RIGHT"
            else -> null
        }
        if (direction != null) {
            val handled = KeyBridge.onDpad?.invoke(direction) ?: false
            return if (handled) true else super.dispatchKeyEvent(event)
        }
        if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || event.keyCode == KeyEvent.KEYCODE_ENTER) {
            val handled = KeyBridge.onSelect?.invoke() ?: false
            return if (handled) true else super.dispatchKeyEvent(event)
        }
        if (event.keyCode == KeyEvent.KEYCODE_VOICE_ASSIST || event.keyCode == KeyEvent.KEYCODE_SEARCH) {
            KeyBridge.onVoice?.invoke()
            return true
        }
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            KeyBridge.onBack?.invoke()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(usbReceiver, UsbStateReceiver.createFilter())
        AuroraAppLifecycle.setForeground(true)
        Log.d("AuroraLifecycle", "MainActivity.onResumed engine=${AuroraEngineConfig.displayName}")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AuroraAppLifecycle.trimMemory(level)
        Log.d("AuroraLifecycle", "MainActivity.onTrimMemory level=$level")
    }
}

private fun Intent?.webUrl(): String? {
    if (this?.action != Intent.ACTION_VIEW) return null
    val uri = data ?: return null
    val scheme = uri.scheme?.lowercase() ?: return null
    return if (scheme == "http" || scheme == "https") uri.toString() else null
}