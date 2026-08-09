# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Browser state models (used via copy/reflection)
-keep class com.aurora.browser.state.** { *; }
-keep class com.aurora.data.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# DataStore (serialized via preferences)
-keepclassmembers class com.aurora.data.preferences.SessionPreferences {
    <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Focus engine
-keep class com.aurora.ui.focus.** { *; }

# Engine callbacks (interface methods)
-keep class com.aurora.engine.BrowserCallbacks { *; }
-keep class com.aurora.engine.BrowserSession { *; }
-keep class com.aurora.engine.BrowserEngine { *; }

# WebView engine — @JavascriptInterface bridges are invoked by WebView via
# reflection; AGP's default rules keep the annotated methods, this keeps
# the enclosing bridge classes intact as well.
-keep class com.aurora.engine.webview.WebViewLoginStorage$LoginBridge { *; }

# WebView renderer crash recovery uses the recovery callback through WebView;
# keep session client implementations intact.
-keep class com.aurora.engine.webview.WebViewBrowserSession { *; }

# Retrofit/OkHttp if used
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
