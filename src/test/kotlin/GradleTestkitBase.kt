import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

open class GradleTestkitBase {
    /**
     * Creates a temporary Gradle project directory and invokes a caller-provided setup callback to populate it.
     *
     * The directory is marked for deletion on JVM exit and is initialized with a minimal
     * settings.gradle.kts that sets a stable root project name ("testkit-root") to produce deterministic output.
     *
     * @param setup A lambda invoked with the temporary project root where callers should create project files (for example via writeBuildFile). Defaults to a no-op.
     * @return The temporary project root directory as a File.
     */
    protected fun withTempProject(setup: (root: File) -> Unit = {}): File {
        val dir = createTempDirectory("gradle-testkit-").toFile()
        dir.deleteOnExit()
        // Copy the repository's root build files into the temp dir for realistic execution.
        // Use minimal settings.gradle to make it a valid build.
        File(dir, "settings.gradle.kts").writeText(
            // Keep project name stable for deterministic output
            "rootProject.name = \"testkit-root\"\n"
        )
        // Write a lightweight build that applies the real root build.gradle.kts using 'apply from'
        // Instead of that (which can break with relative paths), we embed a trimmed test driver build that
        // includes the specific snippets to test: tasks and configuration affected by the diff.
        // This avoids referencing the whole repo and keeps tests hermetic.
        // The test-specific build script will be provided by callers via writeBuildFile().
        setup(dir)
        return dir
    }

    /**
     * Writes the provided Kotlin build script content to `build.gradle.kts` in the given project root.
     *
     * Overwrites any existing `build.gradle.kts` file in `root`.
     *
     * @param root Directory to place the build file (expected to be a temporary test project root).
     * @param content The build.gradle.kts source to write.
     */
    protected fun writeBuildFile(root: File, content: String) {
        File(root, "build.gradle.kts").writeText(content)
    }

    /**
     * Runs a Gradle build against the given temporary project directory and returns the BuildResult.
     *
     * The provided `args` are passed to the Gradle invocation (`, "--stacktrace"` is appended).
     * If `expectSuccess` is true the method calls `build()`; otherwise it calls `buildAndFail()`.
     *
     * @param root The project directory to execute the build in.
     * @param args Gradle CLI arguments (tasks and flags) to pass to the runner.
     * @param expectSuccess When true, expect the build to succeed; when false, expect it to fail.
     * @return The Gradle TestKit BuildResult produced by the run.
     *
     * Note: This uses Gradle TestKit's `withPluginClasspath()` and therefore requires `gradle-test-kit` on the test classpath.
     */
    protected fun run(root: File, vararg args: String, expectSuccess: Boolean = true): BuildResult {
        val runner = GradleRunner.create()
            .withProjectDir(root)
            .withPluginClasspath() // requires gradle-test-kit on test classpath
            .withArguments(*args, "--stacktrace")
        return if (expectSuccess) runner.build() else runner.buildAndFail()
    }

    /**
     * Asserts that the specified task exists in the given BuildResult and completed successfully.
     *
     * The check requires the task to be present in the result and its outcome to be either
     * [TaskOutcome.SUCCESS] or [TaskOutcome.UP_TO_DATE]. Fails the test with a clear message
     * if the task is missing or did not succeed.
     *
     * @param result The Gradle BuildResult to inspect.
     * @param taskPath The fully qualified task path (e.g. ":module:taskName") to verify.
     */
    protected fun assertTaskSuccess(result: BuildResult, taskPath: String) {
        val t = result.task(taskPath)
        assertNotNull(t, "Expected task $taskPath to be present in the result.")
        assertTrue(
            t.outcome == TaskOutcome.SUCCESS || t.outcome == TaskOutcome.UP_TO_DATE,
            "Task $taskPath should succeed."
        )
    }

    /**
     * Asserts that the given Gradle build result's console output contains all provided snippets.
     *
     * Each snippet is checked independently; the assertion fails if any snippet is not found.
     *
     * @param result The Gradle BuildResult whose output will be searched.
     * @param snippets One or more string fragments that must be present in the output.
     */
    protected fun assertOutputContains(result: BuildResult, vararg snippets: String) {
        val out = result.output
        for (s in snippets) {
            assertContains(out, s, "Expected console output to contain: $s")
        }
    }
}