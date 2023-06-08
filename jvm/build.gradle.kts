plugins {
    kotlin("jvm")
    application
}


dependencies {
    implementation(project(":kmp-web-client"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}