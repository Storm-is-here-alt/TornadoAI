dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Needed for MainActivity.kt (OkHttp usage)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // If you use coroutines anywhere (safe to include)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}