package gradle

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Assertions.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.io.path.createTempDirectory

@DisplayName("PR Build Files Validation - Extended Scenarios (new file)")
class PRBuildFilesValidationExtendedTest {

    private fun tempDir(): Path = createTempDirectory("pr-build-validate-")
    private fun tempDir(): Path = createTempDirectory("pr-build-validate-")

    // Helper to robustly locate and invoke gradle.PRBuildFilesValidation.validate(dir)
    private fun invokeValidate(dir: Path): Any? {
        val candidates = listOf("gradle.PRBuildFilesValidation", "gradle.PRBuildFilesValidationKt")
        for (name in candidates) {
            val clazz = try {
                Class.forName(name)
            } catch (_: Throwable) {
                continue
            }
            val methods = clazz.methods.filter { it.name == "validate" && it.parameterCount == 1 }
            for (m in methods) {
                val paramType = m.parameterTypes[0]
                val arg: Any? = when {
                    paramType.isAssignableFrom(Path::class.java) -> dir
                    paramType.isAssignableFrom(java.io.File::class.java) -> dir.toFile()
                    paramType.isAssignableFrom(String::class.java) -> dir.toString()
                    else -> null
                }
                if (arg != null) {
                    val target = if (java.lang.reflect.Modifier.isStatic(m.modifiers)) null
                    else runCatching { clazz.getField("INSTANCE").get(null) }.getOrNull()
                        ?: runCatching { clazz.getDeclaredConstructor().newInstance() }.getOrNull()
                    try {
                        return m.invoke(target, arg)
                    } catch (_: Throwable) {
                        // try next
                    }
                }
            }
        }
        throw IllegalStateException("Could not locate gradle.PRBuildFilesValidation.validate(dir)")
    }

    @Nested
    @DisplayName("Happy paths")
    inner class HappyPaths {
        @Test
        fun `valid minimal build gradle kts passes`() {
            val dir = tempDir()
            Files.writeString(dir.resolve("settings.gradle.kts"), "rootProject.name = \"sample\"")
            Files.writeString(
                dir.resolve("build.gradle.kts"), """
                plugins { kotlin("jvm") version "1.9.23" }
                repositories { mavenCentral() }
                dependencies { testImplementation(kotlin("test")) }
            """.trimIndent()
            )

            val result = try {
                val clazz = Class.forName("gradle.PRBuildFilesValidation")
                val method = clazz.methods.firstOrNull { it.name == "validate" }
                    ?: throw IllegalStateException("Missing validate()")
                method.invoke(null, dir)
            } catch (e: Throwable) {
                e
            }
            assertFalse(
                result is Throwable,
                "Validation should not throw for valid minimal project."
            )
        }
    }

    @Nested
    @DisplayName("Failure paths")
    inner class FailurePaths {
        @Test
        fun `fails when forbidden plugin is applied`() {
            val dir = tempDir()
            Files.writeString(
                dir.resolve("settings.gradle.kts"),
                "rootProject.name = \"forbidden\""
            )
            Files.writeString(
                dir.resolve("build.gradle.kts"), """
                plugins { id("forbidden.plugin") version "0.1.0" }
                repositories { mavenCentral() }
            """.trimIndent()
            )

            val outcome = try {
                val clazz = Class.forName("gradle.PRBuildFilesValidation")
                val method = clazz.methods.firstOrNull { it.name == "validate" }
                    ?: throw IllegalStateException("Missing validate()")
                method.invoke(null, dir)
            } catch (e: Throwable) {
                e
            }

            if (outcome is Throwable) {
                assertTrue(outcome.message?.contains("forbidden", ignoreCase = true) == true)
            } else {
                val isValidProp = outcome::class.members.firstOrNull { it.name == "isValid" }
                val violationsProp =
                    outcome::class.members.firstOrNull { it.name == "violations" || it.name == "messages" }
                if (isValidProp \ != null) {
                    assertEquals(false, isValidProp.call(outcome) as? Boolean)
                } else if (violationsProp \ != null) {
                    val v = violationsProp.call(outcome)?.toString() ?: ""
                    assertTrue(v.contains("forbidden", ignoreCase = true))
                } else {
                    fail("Validation did not fail or expose failure details.")
                }
            }
        }
    }
}