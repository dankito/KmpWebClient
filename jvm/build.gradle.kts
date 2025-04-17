plugins {
    kotlin("jvm")
    application
}


dependencies {
    implementation(project(":ktor-web-client"))

    implementation("net.codinux.log:kmp-log:1.1.2")
    implementation("net.codinux.util:stopwatch:1.5.0")

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