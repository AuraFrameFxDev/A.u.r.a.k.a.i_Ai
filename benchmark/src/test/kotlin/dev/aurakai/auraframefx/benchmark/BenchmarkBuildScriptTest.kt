package dev.aurakai.auraframefx.benchmark

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BenchmarkBuildScriptTest {
    @TempDir
    lateinit var testProjectDir: File

    private lateinit var settingsFile: File
    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        // Prepare an isolated Gradle project that includes the benchmark module build script under test.
        settingsFile = File(testProjectDir, "settings.gradle.kts").apply {
            writeText(
                """
                rootProject.name = \"benchmark-testkit-sandbox\"
                pluginManagement {
                  repositories {
                    gradlePluginPortal()
                    google()
                    mavenCentral()
                  }
                }
                dependencyResolutionManagement {
                  repositories {
                    google()
                    mavenCentral()
                  }
                }
                """.trimIndent()
            )
        }
        // Copy the real build.gradle.kts into temp project for resolvability checks.
        val benchmarkDir = File(testProjectDir, "benchmark").apply { mkdirs() }
        buildFile = File(benchmarkDir, "build.gradle.kts").apply {
            val repoBuild = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
                .map { File(it, "benchmark/build.gradle.kts") }
                .firstOrNull { it.exists() }
                ?: error(
                    "Could not locate benchmark/build.gradle.kts by walking up from ${
                        System.getProperty(
                            "user.dir"
                        )
                    }"
                )
            writeText(repoBuild.readText())
        }
        // Copy the version catalog the synthetic settings.gradle expects
        val repoCatalog = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .map { File(it, "gradle/libs.versions.toml") }
            .firstOrNull { it.exists() }
            ?: error(
                "Could not locate gradle/libs.versions.toml by walking up from ${
                    System.getProperty(
                        "user.dir"
                    )
                }"
            )
        File(testProjectDir, "gradle").mkdirs()
        File(testProjectDir, "gradle/libs.versions.toml").writeText(repoCatalog.readText())
        // Provide a minimal gradle.properties to avoid daemon/user env noise
        File(testProjectDir, "gradle.properties").writeText(
            "org.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8"
        )
    }

    private fun gradle(vararg args: String): BuildResult {
        return GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(*args, "--stacktrace")
            .withPluginClasspath()
            .build()
    }

    @Test
    @Order(1)
    fun `build script should not contain unresolved merge conflict markers`() {
        val content = buildFile.readText()
        assertFalse(content.contains("<<<<<<<"), "Found start-of-conflict marker <<<<<<< in build.gradle.kts")
        assertFalse(content.contains("======="), "Found conflict separator ======= in build.gradle.kts")
        assertFalse(content.contains(">>>>>>>"), "Found end-of-conflict marker >>>>>>> in build.gradle.kts")
        val suspicious = listOf("Processing module:", "} }", "android.defaultConfig.targetSdk")
        suspicious.forEach { token ->
            assertFalse(content.trim().endsWith(token), "Build file appears to end with suspicious trailing content: $token")
        }
    }

    @Test
    @Order(2)
    fun `tasks listing should succeed to ensure script is syntactically valid`() {
        buildFile.readText()
        val androidPluginApplied =
            Regex("id\\(\\" com \\.android\\.library\\"\\)|alias\\(libs\\.plugins\\.android\\.library\\)").containsMatchIn(content)
        Assumptions.assumeTrue(
            System.getenv("ALLOW_PLUGIN_RESOLUTION") == "true" || !androidPluginApplied,
            "Skipping ':tasks' invocation because Android plugin resolution would be required. Set ALLOW_PLUGIN_RESOLUTION=true to enable."
        )
        val result = GradleRunner.create()
            .withProjectDir(benchmarkDir())
            .withArguments("tasks", "--all", "--stacktrace")
            .withPluginClasspath()
            .build()
        assertTrue(result.output.contains("BUILD SUCCESSFUL"), "Gradle ':tasks' did not complete successfully.")
    }

    @Test
    @Order(3)
    fun `custom task benchmarkModuleStatus should be registered and print expected lines`() {
        val result = GradleRunner.create()
            .withProjectDir(benchmarkDir())
            .withArguments("benchmarkModuleStatus", "--stacktrace")
            .withPluginClasspath()
            .build()
        assertTrue(
            result.output.contains("📊 BENCHMARK MODULE - Genesis Protocol Status: ACTIVE"),
            "Expected status banner not found in output."
        )
        assertTrue(
            result.output.contains("Namespace:"),
            "Expected namespace line not found in output."
        )
        assertTrue(
            result.output.contains("Java Version:"),
            "Expected Java version line not found in output."
        )
        assertTrue(result.output.contains("Min SDK:"), "Expected min SDK line not found in output.")
        assertTrue(
            result.output.contains("Features: Performance Benchmarking"),
            "Expected features line not found in output."
        )
    }

    @Test
    @Order(4)
    fun `build script should configure JUnit 5 platform and testing dependencies`() {
        val content = buildFile.readText()
        assertTrue(
            content.contains("useJUnitPlatform()"),
            "Expected useJUnitPlatform() configuration not found in build.gradle.kts"
        )
        val expectedDeps =
            listOf("testImplementation", "junit.platform.launcher", "junit.jupiter.engine")
        expectedDeps.forEach { key ->
            assertTrue(content.contains(key), "Expected testing dependency reference '$key' not found.")
        }
    }

    @Test
    @Order(5)
    fun `android test options and build types should be declared in script text`() {
        val content = buildFile.readText()
        assertTrue(content.contains("isIncludeAndroidResources = true"), "Expected isIncludeAndroidResources = true")
        assertTrue(content.contains("isReturnDefaultValues = true"), "Expected isReturnDefaultValues = true")
        assertTrue(content.contains("animationsDisabled = true"), "Expected animationsDisabled = true")
        assertTrue(
            Regex("buildTypes\\s*\\{[\\s\\S]*maybeCreate\\(\\" benchmark \\
            "\\)"
        ).containsMatchIn(content), "Expected custom 'benchmark' build type declaration.")
        assertTrue(content.contains("compose = true"), "Expected compose = true")
        assertTrue(content.contains("buildConfig = true"), "Expected buildConfig = true")
    }

    @Test
    @Order(6)
    fun `plugins block should not mix alias and id for the android library plugin simultaneously`() {
        val content = buildFile.readText()
        val hasAlias =
            Regex("alias\\(libs\\.plugins\\.android\\.library\\)").containsMatchIn(content)
        val hasId = Regex("id\\(\\" com \\.android\\.library\\"\\)").containsMatchIn(content)
        assertFalse(hasAlias && hasId, "Build script appears to apply the Android Library plugin twice (alias and id). Resolve to one.")
        assertTrue(hasAlias || hasId, "Android Library plugin must be applied via alias or id.")
    }

    private fun benchmarkDir(): File = File(testProjectDir, "benchmark")
}
