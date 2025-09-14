plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.storm.tornadoai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.storm.tornadoai"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        // Add your Bing key in local.properties as: BING_KEY=xxxxxxxx
        buildConfigField("String", "BING_KEY", "\"${project.findProperty("BING_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug { isMinifyEnabled = false }
    }

    buildFeatures { viewBinding = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

    // Networking / parsing
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jsoup:jsoup:1.17.2")

    // JSON (for Bing response)
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // SQLite helper
    implementation("androidx.sqlite:sqlite:2.4.0")
}