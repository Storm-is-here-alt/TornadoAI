plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.storm.tornadoai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.storm.tornadoai"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"

        buildConfigField("String", "SD_DB_PATH",
            "\"/storage/6F3A-4D77/TornadoAI/corpus.db\"")
        buildConfigField("String", "NEWS_SOURCES_PATH",
            "\"/storage/6F3A-4D77/TornadoAI/News_sources.txt\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    // Required for AGP 8.x + our buildConfigField use
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    // Make AGP target Java 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("androidx.sqlite:sqlite:2.4.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    implementation("cz.adaptech.tesseract4android:tesseract4android:4.9.0")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
}

