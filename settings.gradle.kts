pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    
}
rootProject.name = "KmpWebClient"


include(":library")
include(":android")
include(":jvm")

