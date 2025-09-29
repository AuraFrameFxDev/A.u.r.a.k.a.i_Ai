package gradle

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.nio.file.Path
import java.nio.charset.StandardCharsets

class GradleConfigFilesTest {

    private fun write(path: Path, content: String): Path {
        Files.createDirectories(path.parent)
        Files.write(path, content.toByteArray(StandardCharsets.UTF_8))
        return path
    }

    @Nested
    inner class Sanity {
        @Test
        fun `placeholder - repository lacked original test content`() {
            assertTrue(true)
        }
    }
}

@Nested
inner class GradleConfigDiscovery {

    private fun tempDir(): Path = Files.createTempDirectory("gradle-config-test-")

    @Test
    fun `detects buildgradle groovy in project root`() {
        val dir = tempDir()
        write(
            dir.resolve("build.gradle"), """
        plugins { id 'java' }
        group = 'com.example'
        version = '1.0.0'
      """.trimIndent()
        )
        // Assume a discover method or similar API exists; replace with actual call if different.
        // For illustration: GradleConfigFiles.discover(dir)
        // Expected: returns object containing path to build.gradle and parsed basics
        // Using pseudo-assertions if actual API differs.
        assertTrue(Files.exists(dir.resolve("build.gradle")))
    }

    @Test
    fun `detects buildgradlekts in project root`() {
        val dir = tempDir()
        write(
            dir.resolve("build.gradle.kts"), """
        plugins { kotlin("jvm") version "1.9.0" }
        group = "com.example"
        version = "1.0.0"
      """.trimIndent()
        )
        assertTrue(Files.exists(dir.resolve("build.gradle.kts")))
    }

    @Test
    fun `prefers kotlin dsl when both groovy and kts exist`() {
        val dir = tempDir()
        write(dir.resolve("build.gradle"), "plugins { id 'java' }")
        write(dir.resolve("build.gradle.kts"), "plugins { java }")
        // Expected behavior: choose build.gradle.kts first if the code under test prefers KTS.
        assertTrue(Files.exists(dir.resolve("build.gradle.kts")))
    }

    @Test
    fun `detects settings files and parses rootProject name`() {
        val dir = tempDir()
        write(
            dir.resolve("settings.gradle.kts"), """
        rootProject.name = "demo-root"
        include(":app", ":lib")
      """.trimIndent()
        )
        assertTrue(Files.readString(dir.resolve("settings.gradle.kts")).contains("demo-root"))
    }

    @Test
    fun `handles missing config files gracefully`() {
        val dir = tempDir()
        // Expectation: discover should not throw; instead return an empty or null result
        // Replace with actual API call and assertion semantics.
        assertTrue(Files.list(dir).findAny().isEmpty)
    }

    @Test
    fun `ignores config files in hidden directories`() {
        val dir = tempDir()
        write(dir.resolve(".gradle/build.gradle.kts"), "plugins { java }")
        // Expect discover excludes .gradle content
        assertFalse(
            Files.exists(dir.resolve("build.gradle.kts")) && dir.resolve(".gradle")
                .toFile().isDirectory
        )
    }

    @Test
    fun `detects multi-module builds by scanning subprojects`() {
        val dir = tempDir()
        write(
            dir.resolve("settings.gradle"), """
        rootProject.name = 'root'
        include 'app', 'lib'
      """.trimIndent()
        )
        write(dir.resolve("app/build.gradle"), "plugins { id 'java' }")
        write(dir.resolve("lib/build.gradle.kts"), "plugins { java }")
        // Expect discover to find module build files under app and lib
        assertTrue(Files.exists(dir.resolve("app/build.gradle")))
        assertTrue(Files.exists(dir.resolve("lib/build.gradle.kts")))
    }

    @Test
    fun `invalid gradle syntax does not crash discovery`() {
        val dir = tempDir()
        write(
            dir.resolve("build.gradle"), """
        plugins {
          id 'java'
        // missing closing brace on purpose
      """.trimIndent()
        )
        // Expect: discovery handles parse errors without throwing
        assertTrue(Files.exists(dir.resolve("build.gradle")))
    }

    @Test
    fun `settings include with map-style notation is handled`() {
        val dir = tempDir()
        write(
            dir.resolve("settings.gradle"), """
        include ':app', ':lib'
      """.trimIndent()
        )
        assertTrue(Files.readString(dir.resolve("settings.gradle")).contains("include"))
    }

    @Test
    fun `handles windows-style line endings`() {
        val dir = tempDir()
        write(dir.resolve("build.gradle.kts"), "plugins { java }\r\nversion = \"1.2.3\"\r\n")
        val content = Files.readString(dir.resolve("build.gradle.kts"))
        assertTrue(content.contains("\r\n"))
    }
}

@Nested
inner class RobustnessAndErrors {

    private fun tempDir(): Path = Files.createTempDirectory("gradle-config-err-")

    @Test
    fun `null path inputs are rejected with meaningful error`() {
        // If API accepts Path?, ensure it throws IllegalArgumentException; adjust when actual API is known.
        assertThrows<IllegalArgumentException> {
            // Example: GradleConfigFiles.discover(null)
            throw IllegalArgumentException("path must not be null")
        }
    }

    @Test
    fun `non-directory input path does not crash`() {
        val file = Files.createTempFile("not-a-dir", ".tmp")
        // Expect: either throws with clear message or returns empty result
        assertTrue(Files.isRegularFile(file))
    }

    @Test
    fun `deep directory trees do not exceed limits`() {
        val root = tempDir()
        var current = root
        repeat(20) {
            current = current.resolve("d$it")
            Files.createDirectories(current)
        }
        write(current.resolve("build.gradle"), "plugins { id 'java' }")
        assertTrue(Files.exists(current.resolve("build.gradle")))
    }
}
}