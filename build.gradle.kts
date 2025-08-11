buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
    }
}


allprojects {
    group = "net.dankito.web"
    version = "1.6.1-SNAPSHOT"


    ext["sourceCodeRepositoryBaseUrl"] = "github.com/dankito/KmpWebClient"

    ext["projectDescription"] = "Easy to set up Kotlin Multiplatform Web Client based on Ktor with convenience functions like authorization and ignoring certificates"

    repositories {
        mavenCentral()
        google()
    }
}