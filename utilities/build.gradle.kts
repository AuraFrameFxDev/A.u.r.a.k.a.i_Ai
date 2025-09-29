plugins {
    alias(libs.plugins.android.library)

    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.aurakai.auraframefx.utilities"
    compileSdk = 36
    compileSdkPreview = "CANARY"
    defaultConfig {
        minSdk = 33
    }
    buildFeatures {
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            freeCompilerArgs.addAll(
                "-Xjvm-default=all",
                "-Xopt-in=kotlin.RequiresOptIn"
            )
        }
    }
}

dependencies {
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Hilt testing
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)

    // Module dependencies
    api(project(":list"))

    // Utilities
    implementation(libs.commons.io)
    implementation(libs.commons.compress)
    implementation(libs.xz)

    // YukiHook & LSPosed dependencies are now provided by genesis.android.xposed convention plugin

    // Force newer AndroidX versions to override Hilt's old dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)

    // Testing dependencies
    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)
}

// MOVED to root level and Updated
tasks.register("utilitiesStatus") {
    group = "aegenesis"
    description = "Checks the status of the Utilities module"
    doLast {
        println("📦 UTILITIES MODULE - Ready (Java 24, JVM 24)") // Updated
    }
}

// MOVED to root level - Added standard test configuration for JUnit 5
tasks.withType<Test> {
    useJUnitPlatform()
}
