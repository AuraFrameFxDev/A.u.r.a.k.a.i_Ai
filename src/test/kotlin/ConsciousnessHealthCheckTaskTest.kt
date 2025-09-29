import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

@DisplayName("consciousnessHealthCheck task (HEAD variant)")
class ConsciousnessHealthCheckTaskTest : GradleTestkitBase() {

    private fun scriptWithSubprojects(androidComposeEnabled: Boolean): String {
        // Simulate two subprojects:
        // - app (android application with Hilt, Compose (toggle), and KSP)
        // - core (android library without Compose)
        // We won't apply real Android plugins to keep it light; we stub plugins registry and flags.
        // The task logic only queries plugin presence via names; we'll emulate via fake plugin application.
        val composePluginId = "org.jetbrains.kotlin.plugin.compose"

        return """
            import org.gradle.api.Project
            import org.gradle.kotlin.dsl.*

            open class FakePlugin(val id: String) : org.gradle.api.Plugin<Project> {
                override fun apply(target: Project) {
                    // no-op
                }
            }

            allprojects {
                plugins.apply(FakePlugin("com.android.application"))
                if (${androidComposeEnabled}) {
                    plugins.apply(FakePlugin("$composePluginId"))
                }
                plugins.apply(FakePlugin("com.google.dagger.hilt.android"))
                plugins.apply(FakePlugin("com.google.devtools.ksp"))
            }

            // A library subproject without compose
            includeSubproject(":core") {
                plugins.apply(FakePlugin("com.android.library"))
                // no compose
            }

            // HEAD variant code:
            class ModuleReport(
                val name: String,
                val type: String,
                val hasHilt: Boolean,
                val hasCompose: Boolean,
                val hasKsp: Boolean
            )

            fun Project.collectModuleReports(): List<ModuleReport> = subprojects.map { sp ->
                val plugins = sp.plugins
                ModuleReport(
                    name = sp.name,
                    type = when {
                        plugins.hasPlugin("com.android.application") -> "android-app"
                        plugins.hasPlugin("com.android.library") -> "android-lib"
                        plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> "kotlin-jvm"
                        else -> "other"
                    },
                    hasHilt = plugins.hasPlugin("com.google.dagger.hilt.android"),
                    hasCompose = plugins.findPlugin("org.jetbrains.kotlin.plugin.compose") \!= null,
                    hasKsp = plugins.hasPlugin("com.google.devtools.ksp")
                )
            }

            tasks.register("consciousnessHealthCheck") {
                group = "genesis"
                description = "Detailed system health report"
                doLast {
                    val reports = collectModuleReports()
                    println("=== Genesis Protocol Health Report ===")
                    println("📦 Total Modules: ${'$'}{reports.size}")
                    println("🤖 Android Apps: ${'$'}{reports.count { it.type == "android-app" }}")
                    println("📚 Android Libraries: ${'$'}{reports.count { it.type == "android-lib" }}")
                    println("☕ Kotlin JVM: ${'$'}{reports.count { it.type == "kotlin-jvm" }}")
                    println("\n=== Plugin Usage ===")
                    println("💉 Hilt: ${'$'}{reports.count { it.hasHilt }} modules")
                    println("🎨 Compose: ${'$'}{reports.count { it.hasCompose }} modules")
                    println("🔧 KSP: ${'$'}{reports.count { it.hasKsp }} modules")

                    val missingCompose = reports.filter { it.type.startsWith("android-") && \!it.hasCompose }
                    if (missingCompose.isNotEmpty()) {
                        println("\n⚠️  Android modules without Compose:")
                        missingCompose.forEach { println("   • ${'$'}{it.name}") }
                    } else {
                        println("\n✅ All Android modules have Compose enabled")
                    }
                }
            }

            tasks.withType<Test> {
                useJUnitPlatform()
            }

            // Infrastructure to create subprojects physically
            fun includeSubproject(path: String, config: Project.() -> Unit) {
                val name = path.removePrefix(":")
                val dir = projectDir.resolve(name)
                dir.mkdirs()
                // Provide minimal build script for the subproject
                dir.resolve("build.gradle.kts").writeText("plugins {}")
                settings.gradle.kts.appendText("\ninclude(\"$path\")\n")
                gradle.rootProject {
                    project(path).config()
                }
            }
        """.trimIndent()
    }

    @Test
    @DisplayName("reports missing Compose modules when some Android modules lack Compose")
    fun reportsMissingCompose() {
        val root = withTempProject { dir ->
            writeBuildFile(dir, scriptWithSubprojects(androidComposeEnabled = false))
        }
        val result = run(root, "consciousnessHealthCheck")
        assertTaskSuccess(result, ":consciousnessHealthCheck")
        assertOutputContains(
            result,
            "=== Genesis Protocol Health Report ===",
            "📦 Total Modules:",
            "🤖 Android Apps:",
            "📚 Android Libraries:",
            "💉 Hilt:",
            "🎨 Compose:",
            "🔧 KSP:",
            "⚠️  Android modules without Compose:",
            "• core"
        )
    }

    @Test
    @DisplayName("confirms all Android modules have Compose when enabled everywhere")
    fun allModulesHaveCompose() {
        val root = withTempProject { dir ->
            writeBuildFile(dir, scriptWithSubprojects(androidComposeEnabled = true))
        }
        val result = run(root, "consciousnessHealthCheck")
        assertTaskSuccess(result, ":consciousnessHealthCheck")
        assertOutputContains(result, "✅ All Android modules have Compose enabled")
    }
}