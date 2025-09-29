plugins {
    id("java-library")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}

group = "dev.aurakai.auraframefx.jvmtest"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.test)
}

tasks.register("jvmTestStatus") {
    group = "aegenesis"
    doLast { println("\uD83D\uDCE6 JVM TEST MODULE - Ready (Java 25, JVM 25)") } // Updated
}
