

plugins {
    kotlin("multiplatform")
}


kotlin {
    jvmToolchain(11)

    jvm()


    js {
        binaries.library()

        browser()

        nodejs()
    }


    linuxX64()
    mingwX64()

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    watchosArm64()
    watchosSimulatorArm64()
    tvosArm64()
    tvosSimulatorArm64()

    applyDefaultHierarchyTemplate()


    val ktorVersion: String by project

    sourceSets {
        commonMain.dependencies {

        }
    }
}


if (File(projectDir, "../gradle/scripts/publish-dankito.gradle.kts").exists()) {
    apply(from = "../gradle/scripts/publish-dankito.gradle.kts")
}