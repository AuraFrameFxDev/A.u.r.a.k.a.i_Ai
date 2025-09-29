import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("printXposedApiJar task")
class PrintXposedApiJarTaskTest : GradleTestkitBase() {

    private val script = """
        tasks.register("printXposedApiJar") {
            doLast {
                val libsDir = rootProject.projectDir.resolve("Libs")
                val apiJar = libsDir.resolve("api-82.jar")
                val sourcesJar = libsDir.resolve("api-82-sources.jar")
                println("Xposed API JAR: ${'$'}{apiJar.absolutePath}")
                println("Xposed Sources JAR: ${'$'}{sourcesJar.absolutePath}")
            }
        }

        allprojects {
            tasks.withType<Test> {
                useJUnitPlatform()
            }
        }
    """.trimIndent()

    @Test
    @DisplayName("prints absolute paths to Xposed API jars under Libs/")
    fun printsJarPaths() {
        val root = withTempProject { dir ->
            // Ensure Libs directory exists to reflect realistic absolute paths
            File(dir, "Libs").mkdirs()
            writeBuildFile(dir, script)
        }

        val result = run(root, "printXposedApiJar")
        assertTaskSuccess(result, ":printXposedApiJar")
        assertOutputContains(result, "Xposed API JAR:", "Xposed Sources JAR:")
    }
}