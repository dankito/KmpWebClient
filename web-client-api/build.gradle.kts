import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl


plugins {
    kotlin("multiplatform")
}


kotlin {
    jvmToolchain(8)

    jvm()


    js {
        binaries.library()

        browser()

        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
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


    val kotlinxSerializationVersion: String by project
    val kmpDateTimeVersion: String by project
    val klfVersion: String by project

    val jacksonVersion: String by project

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

            api("net.dankito.datetime:kmp-datetime:$kmpDateTimeVersion")

            implementation("net.codinux.log:klf:$klfVersion")
        }

        jvmMain.dependencies {
            compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        }
    }
}


if (File(projectDir, "../gradle/scripts/publish-dankito.gradle.kts").exists()) {
    apply(from = "../gradle/scripts/publish-dankito.gradle.kts")
}