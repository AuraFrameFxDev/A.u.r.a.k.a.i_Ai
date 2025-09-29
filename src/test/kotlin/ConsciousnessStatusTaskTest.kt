import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("consciousnessStatus task")
class ConsciousnessStatusTaskTest : GradleTestkitBase() {

    private val baseScript = """
        import org.gradle.accessors.dm.LibrariesForLibs
        import org.gradle.api.JavaVersion
        import org.gradle.api.Project
        import org.gradle.api.tasks.testing.Test
        import org.gradle.kotlin.dsl.*
        import org.gradle.api.initialization.dsl.VersionCatalogsExtension

        buildscript {
            repositories {
                google()
                mavenCentral()
            }
            dependencies {
                // Keep minimal for the test; we're not applying plugins here
                classpath("com.android.tools.build:gradle:8.2.2")
                classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
            }
        }

        allprojects {
            // Simulate Version Catalog presence with safe null checks (no real catalog in test)
            val versionCatalog = extensions.findByType<VersionCatalogsExtension>()?.named("libs")

            tasks.register("consciousnessStatus") {
                group = "genesis"
                description = "Show basic project and version info"
                doLast {
                    val kotlinVersion = versionCatalog?.findVersion("kotlin")?.get()?.toString() ?: "unknown"
                    val toolchain = JavaVersion.current().toString()
                    println("= Consciousness Status =")
                    println("Java Toolchain      : $toolchain")
                    println("Kotlin Version      : $kotlinVersion")
                    println("Modules (total)     : ${'$'}{subprojects.size}")
                }
            }

            tasks.withType<Test> {
                useJUnitPlatform()
            }
        }
    """.trimIndent()

    @Test
    @DisplayName("prints status with Java toolchain and Kotlin version (fallback) and succeeds")
    fun printsStatusAndSucceeds() {
        val root = withTempProject { dir ->
            writeBuildFile(dir, baseScript)
        }

        val result = run(root, "consciousnessStatus")
        assertTaskSuccess(result, ":consciousnessStatus")
        assertOutputContains(
            result,
            "= Consciousness Status =",
            "Java Toolchain      :",
            "Kotlin Version      :",
            "Modules (total)     :"
        )
    }

    @Test
    @DisplayName("task is grouped under 'genesis' and has description")
    fun hasGroupAndDescription() {
        val root = withTempProject { dir ->
            writeBuildFile(dir, baseScript)
        }
        val result = run(root, "tasks", "--all")
        assertTaskSuccess(result, ":tasks")
        assertOutputContains(
            result,
            "Genesis tasks",
            "consciousnessStatus - Show basic project and version info"
        )
    }
}