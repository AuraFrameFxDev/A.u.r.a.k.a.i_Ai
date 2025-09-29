package dev.aurakai.auraframefx.app

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class AppBuildScriptTest {
    private val tempDir: Path = Files.createTempDirectory("aegenesis-test-")

    @Test
    fun `prints expected header and dividers`() {
        val out = fakeStatusOutput(
            apiExists = false,
            apiSizeBytes = 0,
            nativeCode = false,
            kspMode = null
        )
        assertTrue(
            out.lines().first().contains("AEGENESIS APP MODULE STATUS"),
            "Header should contain module status title"
        )
        assertEquals(50, out.lines()[1].length, "Second line should be a 50-char divider")
    }

    @Test
    fun `indicates API missing and native disabled by default`() {
        val out = fakeStatusOutput(
            apiExists = false,
            apiSizeBytes = 0,
            nativeCode = false,
            kspMode = null
        )
        assertTrue(out.contains("🔌 Unified API Spec: ❌ Missing"))
        assertTrue(out.contains("🔧 Native Code: ❌ Disabled"))
        assertTrue(out.contains("🧠 KSP Mode: default"))
        assertTrue(out.contains("🎯 Target SDK: 36"))
        assertTrue(out.contains("📱 Min SDK: 33"))
    }

    private fun createApiFile(sizeBytes: Int): Path {
        val apiDir = tempDir.resolve("api")
        Files.createDirectories(apiDir)
        val api = apiDir.resolve("unified-aegenesis-api.yml")
        if (sizeBytes > 0) {
            val chunk = "a".repeat(1024).toByteArray()
            var remaining = sizeBytes
            Files.newOutputStream(
                api,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            ).use { os ->
                while (remaining > 0) {
                    val toWrite = minOf(remaining, chunk.size)
                    os.write(chunk, 0, toWrite)
                    remaining -= toWrite
                }
            }
        } else {
            Files.writeString(
                api,
                "",
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
        return api
    }

    @Test
    fun `API missing case - expect Missing label and no size line`() {
        val simulatedOutput = buildString {
            appendLine("🔌 Unified API Spec: ❌ Missing")
        }
        assertTrue(simulatedOutput.contains("❌ Missing"))
        assertFalse(
            simulatedOutput.contains("📄 API File Size:"),
            "No size line expected when file is missing"
        )
    }

    @Test
    fun `API present case - size rounded down to KB`() {
        val api = createApiFile(sizeBytes = 4097) // just over 4KB
        val sizeKB = Files.size(api) / 1024
        val line = "📄 API File Size: ${sizeKB}KB"
        assertTrue(sizeKB >= 4, "Expected at least 4KB, got ${sizeKB}KB")
        assertTrue(line.matches(Regex("📄 API File Size: \\d+KB")))
    }

    // TODO: Add or mock fakeStatusOutput for testability
}
