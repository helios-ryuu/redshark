import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.helios.redshark"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.helios.redshark"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties["GOOGLE_WEB_CLIENT_ID"] ?: ""}\"")
        buildConfigField("String", "CLOUDFLARE_R2_ACCOUNT_ID",
            "\"${localProperties["CLOUDFLARE_R2_ACCOUNT_ID"] ?: ""}\"")
        buildConfigField("String", "CLOUDFLARE_R2_ACCESS_KEY_ID",
            "\"${localProperties["CLOUDFLARE_R2_ACCESS_KEY_ID"] ?: ""}\"")
        buildConfigField("String", "CLOUDFLARE_R2_SECRET_ACCESS_KEY",
            "\"${localProperties["CLOUDFLARE_R2_SECRET_ACCESS_KEY"] ?: ""}\"")
        buildConfigField("String", "CLOUDFLARE_R2_BUCKET",
            "\"${localProperties["CLOUDFLARE_R2_BUCKET"] ?: ""}\"")
        buildConfigField("String", "CLOUDFLARE_R2_ENDPOINT",
            "\"${localProperties["CLOUDFLARE_R2_ENDPOINT"] ?: ""}\"")
        buildConfigField("String", "CLOUDFLARE_R2_PUBLIC_BASE_URL",
            "\"${localProperties["CLOUDFLARE_R2_PUBLIC_BASE_URL"] ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Google Sign-In (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.googleid)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Logging
    implementation(libs.timber)

    // OkHttp
    implementation(libs.okhttp)

    // Firestore
    implementation(libs.firebase.firestore)

    // Image loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
