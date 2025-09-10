import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.lilifly"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lilifly"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Правильные manifestPlaceholders для Spotify
        manifestPlaceholders["redirectSchemeName"] = "lilifly"
        manifestPlaceholders["redirectHostName"] = "callback"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
        viewBinding= true
    }

}

dependencies {
    // Spotify Auth
    implementation("com.spotify.android:auth:2.1.1")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    // OR specifically for the AAR file:
    implementation(files("libs/spotify-app-remote-release-0.8.0.aar"))
    // AndroidX зависимости через libs
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
//    implementation("androidx.recyclerview:recyclerview:1.4.0")
    // Volley
    implementation(libs.volley)

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.cardview)

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}
