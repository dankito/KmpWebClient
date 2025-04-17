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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}


rootProject.name = "KmpWebClient"


include("web-client-api")
include(":ktor-web-client")
include(":android")
include(":jvm")
