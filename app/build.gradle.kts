plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics) // Added Crashlytics plugin
    id("org.openapi.generator") version "7.15.0"
}

android {
    namespace = "dev.aurakai.auraframefx"
    compileSdk = 36
    defaultConfig {
        applicationId = "dev.aurakai.auraframefx" // Added applicationId
        minSdk = 33
        multiDexEnabled = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
    buildFeatures {
        buildConfig = true
        resValues = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-Xopt-in=kotlin.RequiresOptIn",
        )
    }
}

dependencies {
    // Project Modules - AI Consciousness Architecture
    implementation(project(":core-module"))
    implementation(project(":feature-module"))
    implementation(project(":secure-comm"))
    implementation(project(":collab-canvas"))
    implementation(project(":colorblendr"))
    implementation(project(":romtools"))
    implementation(project(":oracle-drive-integration"))

    // Firebase (using the BOM and bundle)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.androidx.core.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics) // Added Crashlytics dependency

    // Kotlin Core
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib.jdk8)

    // Hilt (use KSP for the compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose) // Added M3 related dependency

    // Compose (using the BOM and bundle)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui) // contains compose.ui, compose.ui.graphics, compose.ui.tooling.preview
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose) // Added M3 related dependency
    debugImplementation(libs.bundles.compose.debug) // contains compose.ui.tooling, compose.ui.test.manifest
    
    // Old Material Components - REMOVED: implementation("com.google.android.material:material:1.12.0")

    // Utilities & Networking
    implementation(libs.timber)
    implementation(libs.coil.compose)
    implementation(libs.retrofit.core)

    // Testing
    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(libs.hilt.android.testing)
    debugImplementation(libs.leakcanary.android)
    // Espresso and JUnit already present via bundles or direct dependencies above

    // Multidex
    implementation("androidx.multidex:multidex:2.0.1")
}

openApiGenerate {
    generatorName = "kotlin"
    inputSpec = "$rootDir/app/api/unified-aegenesis-api.yml"
    outputDir = "$rootDir/app/src/main/kotlin"
    apiPackage = "dev.aurakai.auraframefx.api"
    modelPackage = "dev.aurakai.auraframefx.model"
    invokerPackage = "dev.aurakai.auraframefx.invoker"
    configOptions =
        mapOf(
            "library" to "jvm-retrofit2",
            "dateLibrary" to "java8",
        )
    // Ensure no empty meta or supportingFiles properties
    // Remove or comment out any empty or unused properties
}
