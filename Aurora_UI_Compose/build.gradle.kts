plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.aurora.ui"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
        buildConfigField("String", "AURORA_ENGINE_ID", "\"webview\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":design"))
    implementation(project(":motion"))
    implementation(project(":browser"))
    implementation(project(":data"))
    implementation(project(":home"))
    implementation(project(":engine:api"))
    implementation(project(":engine:webview"))
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(project(":ui:focus"))
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    debugImplementation("androidx.compose.ui:ui-tooling")
}
