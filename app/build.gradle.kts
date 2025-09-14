import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.gms.google-services") version "4.4.3" apply false
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
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment)

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")


    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

}
