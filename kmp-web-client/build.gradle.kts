import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}


@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    // Enable the default target hierarchy:
    targetHierarchy.default()

    jvm {
        jvmToolchain(11)
//        withJava() // due to a bug in IntelliJ currently does not work
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    android {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js(IR) {
        moduleName = "kmpwebclient"
//        binaries.executable()

        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    useFirefoxHeadless()
                }
            }
        }

        nodejs()
    }


    linuxX64()
    mingwX64()


    ios {
        binaries {
            framework {
                baseName = "kmp-web-client"
            }
        }
    }
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    watchos()
    watchosSimulatorArm64()
    tvos()
    tvosSimulatorArm64()


    val coroutinesVersion: String by project
    val ktorVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-auth:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
            }
        }


        val javaCommonMain by creating {
            dependsOn(commonMain)

            dependencies {
                compileOnly("io.ktor:ktor-client-cio:$ktorVersion")
                compileOnly("io.ktor:ktor-client-okhttp:$ktorVersion")
                compileOnly("io.ktor:ktor-client-apache:$ktorVersion")
                compileOnly("io.ktor:ktor-client-java:$ktorVersion")
                compileOnly("io.ktor:ktor-client-jetty:$ktorVersion")
                compileOnly("io.ktor:ktor-client-android:$ktorVersion")
            }
        }

        val jvmMain by getting {
            dependsOn(javaCommonMain)

            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        val jvmTest by getting

        val androidMain by getting {
            dependsOn(javaCommonMain)

            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
            }
        }

        val nativeMain by getting {
            dependencies {
                // yes, i know, Kotlin/Native has no compileOnly, but we need this dependency to configure engine and to make intention clear
                compileOnly("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }

        val linuxMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                // yes, i know, Kotlin/Native has no compileOnly, but we need this dependency to configure engine and to make intention clear
                compileOnly("io.ktor:ktor-client-curl:$ktorVersion")
            }
        }

        val mingwMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-winhttp:$ktorVersion")
                // yes, i know, Kotlin/Native has no compileOnly, but we need this dependency to configure engine and to make intention clear
                compileOnly("io.ktor:ktor-client-curl:$ktorVersion")
            }
        }

        val appleMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
    }
}


android {
    namespace = "net.dankito.web.client"

    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }

    lint {
        abortOnError = false
    }

    testOptions {
        unitTests {
            // Otherwise we get this exception in tests:
            // Method e in android.util.Log not mocked. See https://developer.android.com/r/studio-ui/build/not-mocked for details.
            isReturnDefaultValues = true
        }
    }
}
