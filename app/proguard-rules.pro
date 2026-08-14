# Compose ships its own consumer R8 rules; no blanket keep needed.

# WebView @JavascriptInterface bridges are invoked by WebView via reflection;
# AGP's default rules keep the annotated methods, keep the bridge classes too.
-keep class com.aurora.engine.webview.WebViewLoginStorage$LoginBridge { *; }
-keep class com.aurora.engine.webview.WebViewBlobBridge { *; }

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

# Browser state models (persisted app state)
-keep class com.aurora.browser.state.** { *; }
-keep class com.aurora.data.model.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Focus engine
-keep class com.aurora.ui.focus.** { *; }

# Retrofit/OkHttp if used
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
