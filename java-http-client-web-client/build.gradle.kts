plugins {
    kotlin("jvm")
}


kotlin {
    jvmToolchain(11)
}


val coroutinesVersion: String by project
val kotlinxSerializationVersion: String by project

val assertKVersion: String by project

dependencies {
    implementation(project(":web-client-api"))
    
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")


    testImplementation(kotlin("test"))

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("com.willowtreeapps.assertk:assertk:$assertKVersion")
}

tasks.test {
    useJUnitPlatform()
}


if (File(projectDir, "../gradle/scripts/publish-dankito.gradle.kts").exists()) {
    apply(from = "../gradle/scripts/publish-dankito.gradle.kts")
}