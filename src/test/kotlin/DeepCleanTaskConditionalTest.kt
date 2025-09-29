import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFailsWith

@DisplayName("deepClean task conditional registration")
class DeepCleanTaskConditionalTest : GradleTestkitBase() {

    private val script = """
        if (file("nuclear-clean.gradle.kts").exists()) {
            apply(from = "nuclear-clean.gradle.kts")
            if (tasks.findByName("nuclearClean") \!= null) {
                tasks.register("deepClean") {
                    group = "build"
                    description = "Nuclear clean + standard clean"
                    dependsOn("nuclearClean")
                    doLast {
                        println("🚀 Deep clean completed. Run: ./gradlew build --refresh-dependencies")
                    }
                }
            }
        }

        allprojects {
            tasks.withType<Test> {
                useJUnitPlatform()
            }
        }
    """.trimIndent()

    @Test
    @DisplayName("deepClean is not available when nuclear-clean.gradle.kts is absent")
    fun deepCleanAbsentWhenScriptMissing() {
        val root = withTempProject { dir ->
            // Do NOT create nuclear-clean.gradle.kts
            writeBuildFile(dir, script)
        }
        // Querying the task graph should not show deepClean
        val result = run(root, "tasks", "--all")
        // Avoid asserting failure; just ensure not present
        val output = result.output
        assert(\!output.contains("deepClean - Nuclear clean + standard clean")) {
            "deepClean should not be registered when nuclear-clean.gradle.kts is missing."
        }
    }

    @Test
    @DisplayName("deepClean is available and runs when nuclear-clean.gradle.kts defines nuclearClean")
    fun deepCleanPresentWhenScriptExists() {
        val root = withTempProject { dir ->
            // Create a minimal nuclear-clean script that registers nuclearClean
            File(dir, "nuclear-clean.gradle.kts").writeText(
                "tasks.register(\"nuclearClean\") { doLast { println(\"nuclear clean executed\") } }"
            )
            writeBuildFile(dir, script)
        }
        val result = run(root, "deepClean")
        assertTaskSuccess(result, ":deepClean")
        // Ensure both nuclearClean and deepClean messaging occur
        assertOutputContains(result, "nuclear clean executed", "Deep clean completed")
    }
}