package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinJvmPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply("org.jetbrains.kotlin.jvm")
        // JVM target/toolchain should be set via Gradle toolchain DSL in build scripts or convention plugins
        // No manual JVM target enforcement needed here
    }
}
