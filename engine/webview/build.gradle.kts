plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aurora.engine.webview"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":engine:api"))
    implementation(project(":data"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}