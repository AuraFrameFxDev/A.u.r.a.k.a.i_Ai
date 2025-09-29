import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties
import java.util.stream.Stream
import kotlin.streams.toList
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

private val CONFIG_ROOT: Path = Paths.get("config")
private val JSON_VALIDATOR: Json = Json { ignoreUnknownKeys = true; isLenient = true }
private val PLACEHOLDER_TOKENS = listOf("TODO", "FIXME", "REPLACE_ME", "CHANGEME")

private fun collectConfigFiles(vararg extensions: String): List<Path> {
    if (\!Files.isDirectory(CONFIG_ROOT)) return emptyList()
    val normalized = extensions.map { it.lowercase() }
    return Files.walk(CONFIG_ROOT).use { stream ->
        stream.filter { Files.isRegularFile(it) && (normalized.isEmpty() || normalized.any { ext -> it.fileName.toString().lowercase().endsWith(".$ext") }) }
            .sorted()
            .toList()
    }
}

private fun readContent(path: Path): String = Files.readString(path)

private fun ensureNoPlaceholderTokens(path: Path, content: String) {
    PLACEHOLDER_TOKENS.forEach { token ->
        assertFalse(content.contains(token), "Config file $path should not contain placeholder token '$token'")
    }
}

private fun ensureNoWindowsLineEndings(path: Path, content: String) {
    assertFalse(content.contains('\r'), "Config file $path should use LF line endings")
}

private fun ensureEndsWithNewline(path: Path, content: String) {
    assertTrue(content.endsWith("\n"), "Config file $path should end with a newline character")
}

private fun parseYamlIfPossible(path: Path, content: String) {
    val yamlClass = runCatching { Class.forName("org.yaml.snakeyaml.Yaml") }.getOrNull()
    assumeTrue(yamlClass \!= null, "SnakeYAML not on classpath; skipping strict YAML validation for $path")
    val yamlInstance = yamlClass\!\!.getDeclaredConstructor().newInstance()
    val loadMethod = yamlClass.getMethod("load", String::class.java)
    runCatching { loadMethod.invoke(yamlInstance, content) }
        .onFailure { throwable ->
            val rootCause = generateSequence(throwable) { it.cause }.last()
            fail("YAML parsing failed for $path: ${rootCause.message}")
        }
}

// Testing stack: JUnit 5 (Jupiter) + kotlin.test assertions + kotlinx.serialization for JSON validation.
@DisplayName("Configuration file integrity (JUnit 5 + kotlin.test)")
class ConfigFilesTest {

    @Test
    fun `config directory should exist`() {
        assertTrue(Files.isDirectory(CONFIG_ROOT), "Expected configuration directory at $CONFIG_ROOT")
    }

    @Test
    fun `config directory should contain at least one config file`() {
        val files = collectConfigFiles()
        assertTrue(files.isNotEmpty(), "Expected at least one configuration file under $CONFIG_ROOT")
    }

    @Nested
    @DisplayName("Common configuration hygiene")
    inner class CommonConfigTests {
        @ParameterizedTest(name = "{0}")
        @MethodSource("allFiles")
        fun `config files should be well formed text`(path: Path) {
            val content = readContent(path)
            assertTrue(content.isNotBlank(), "Config file $path should not be empty")
            ensureEndsWithNewline(path, content)
            ensureNoWindowsLineEndings(path, content)
            ensureNoPlaceholderTokens(path, content)
        }
    }

    @Nested
    @DisplayName("JSON configuration")
    inner class JsonConfigTests {
        @ParameterizedTest(name = "{0}")
        @MethodSource("jsonFiles")
        fun `json files should parse successfully`(path: Path) {
            val content = readContent(path)
            assertTrue(content.isNotBlank(), "JSON config $path should not be empty")
            assertDoesNotThrow({ JSON_VALIDATOR.parseToJsonElement(content) }) {
                "Failed to parse JSON config $path"
            }
        }
    }

    @Nested
    @DisplayName("YAML configuration")
    inner class YamlConfigTests {
        @ParameterizedTest(name = "{0}")
        @MethodSource("yamlFiles")
        fun `yaml files should parse when SnakeYAML is on classpath`(path: Path) {
            val content = readContent(path)
            assertTrue(content.isNotBlank(), "YAML config $path should not be empty")
            parseYamlIfPossible(path, content)
        }
    }

    @Nested
    @DisplayName("Properties configuration")
    inner class PropertiesConfigTests {
        @ParameterizedTest(name = "{0}")
        @MethodSource("propertiesFiles")
        fun `properties files should load using java util Properties`(path: Path) {
            val props = Properties()
            Files.newInputStream(path).use(props::load)
            assertTrue(props.isNotEmpty(), "Properties config $path should declare at least one property")
        }
    }

    companion object {
        @JvmStatic
        fun allFiles(): Stream<Path> = collectConfigFiles().stream()

        @JvmStatic
        fun jsonFiles(): Stream<Path> = collectConfigFiles("json").stream()

        @JvmStatic
        fun yamlFiles(): Stream<Path> = collectConfigFiles("yml", "yaml").stream()

        @JvmStatic
        fun propertiesFiles(): Stream<Path> = collectConfigFiles("properties", "props").stream()
    }
}