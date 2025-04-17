import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}


@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        // suppresses compiler warning: [EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING] 'expect'/'actual' classes (including interfaces, objects, annotations, enums, and 'actual' typealiases) are in Beta.
        freeCompilerArgs.add("-Xexpect-actual-classes")

        // avoid "variable has been optimised out" in debugging mode
        if (System.getProperty("idea.debugger.dispatch.addr") != null) {
            freeCompilerArgs.add("-Xdebug")
        }
    }


    jvmToolchain(11)

    jvm {
//        withJava() // due to a bug in IntelliJ currently does not work
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    androidTarget {

    }

    js(IR) {
        moduleName = "kmpwebclient"
        binaries.library()

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

        nodejs {
            testTask {
                useMocha {
                    timeout = "20s" // Mocha times out after 2 s, which is too short for some tests
                }
            }
        }
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


    val coroutinesVersion: String by project
    val ktorVersion: String by project

    val klfVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":web-client-api"))

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-auth:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

                implementation("net.codinux.log:klf:$klfVersion")
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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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



ext["customArtifactId"] = "kmp-web-client"

if (File(projectDir, "../gradle/scripts/publish-dankito.gradle.kts").exists()) {
    apply(from = "../gradle/scripts/publish-dankito.gradle.kts")
}
