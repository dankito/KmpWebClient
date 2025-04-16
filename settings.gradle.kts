pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }


    val kotlinVersion: String by settings

    plugins {
        kotlin("multiplatform") version kotlinVersion apply false
    }
}


rootProject.name = "KmpWebClient"


include(":kmp-web-client")
include(":android")
include(":jvm")

