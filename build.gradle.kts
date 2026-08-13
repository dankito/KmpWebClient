buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}


allprojects {
    group = "net.dankito.web"
    version = "1.9.0-SNAPSHOT"


    ext["sourceCodeRepositoryBaseUrl"] = "github.com/dankito/KmpWebClient"

    ext["projectDescription"] = "Easy to set up Kotlin Multiplatform Web Client based on Ktor with convenience functions like authorization and ignoring certificates"

    repositories {
        mavenCentral()
        google()
    }
}