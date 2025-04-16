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
    group = "net.dankito.web.client"
    version = "1.0.0-SNAPSHOT"


    ext["sourceCodeRepositoryBaseUrl"] = "github.com/dankito/KmpWebClient"

    ext["projectDescription"] = "Easy to set up Kotlin Multiplatform Web Client based on Ktor with convenience functions like authorization and ignoring certificates"

    repositories {
        mavenCentral()
        google()
    }
}