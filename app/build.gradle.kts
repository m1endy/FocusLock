plugins {
    id("com.android.application") version "8.7.0"
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

android {
    namespace = "com.focuslock.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.focuslock.app"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
