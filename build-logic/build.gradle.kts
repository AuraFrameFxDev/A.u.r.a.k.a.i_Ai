plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.1.3")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
}

gradlePlugin {
    plugins {
        create("androidBase") {
            id = "plugins.android-base"
            implementationClass = "plugins.AndroidBasePlugin"
        }
        create("kotlinJvm") {
            id = "plugins.kotlin-jvm"
            implementationClass = "plugins.KotlinJvmPlugin"
        }
        create("agentFusion") {
            id = "plugins.agent-fusion"
            implementationClass = "plugins.AgentFusionPlugin"
        }
    }
}

kotlin {
    jvmToolchain(23)
}
