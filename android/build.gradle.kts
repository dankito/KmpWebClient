plugins {
    id("com.android.application")
    kotlin("android")
}

group = "me.ganymed"
version = "1.0-SNAPSHOT"

repositories {
    google()
}

dependencies {
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
}

android {
    namespace = "me.ganymed.android"
    compileSdk = 32
    defaultConfig {
        applicationId = "me.ganymed.android"
        minSdk = 24
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}