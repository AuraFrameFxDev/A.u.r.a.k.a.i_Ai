// In settings.gradle.kts

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
    repositories {
        google() // Google must be first for AGP 9.x
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
        // Removed duplicate Foojay resolver
        id("org.jetbrains.kotlin.jvm") apply false
        id("com.android.application") apply false
        id("com.android.library") apply false
        id("com.google.devtools.ksp") apply false
        id("com.google.gms.google-services") apply false
        id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") apply false
        id("org.lsposed.lsparanoid") apply false
        id("com.google.firebase.crashlytics") apply false
        id("org.openapi.generator") apply false
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
        id("com.google.dagger.hilt.android") version "2.57.1" apply false // Updated Hilt plugin version
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // Add local Maven repository for custom JARs
        maven {
            name = "localRepo"
            url = uri("$rootDir/local-repo")
        }
    }
}

rootProject.name = "ReGenesis"
include(":app")
include(":core-module")
include(":feature-module")
include(":datavein-oracle-native")
include(":oracle-drive-integration")
include(":secure-comm")
include(":sandbox-ui")
include(":collab-canvas")
include(":colorblendr")
include(":romtools")
include(":module-a")
include(":module-b")
include(":module-c")
include(":module-d")
include(":module-e")
include(":module-f")
include(":benchmark")
include(":screenshot-tests")
include(":jvm-test")
include(":list")
include(":utilities")
includeBuild("build-logic")
