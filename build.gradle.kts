buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
        classpath("com.android.tools.build:gradle:7.3.1")
    }
}


allprojects {
    group = "net.dankito.web"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}