package dev.aurakai.auraframefx.romtools

/**
 * ROM Tools Configuration - Centralized and Type-Safe
 *
 * Replaces BuildConfig fields with compile-time constants
 * More maintainable than BuildConfig generation
 */
object RomToolsConfig {

    /** Whether the ROM tools functionality is enabled. */
    const val ROM_TOOLS_ENABLED: Boolean = true

    /** The list of supported Android versions for ROM manipulation. */
    val SUPPORTED_ANDROID_VERSIONS: List<Int> = listOf(13, 14, 15, 16)

    /** The list of supported CPU architectures. */
    val SUPPORTED_ARCHITECTURES: List<String> = listOf(
        "arm64-v8a",
        "armeabi-v7a",
        "x86_64"
    )

    /** The timeout for ROM modification operations, in milliseconds. */
    const val ROM_OPERATION_TIMEOUT_MS: Long = 30_000L

    /** The maximum size of a ROM file, in bytes. */
    const val MAX_ROM_FILE_SIZE: Long = 8L * 1024 * 1024 * 1024 // 8GB

    /** The list of supported ROM file formats. */
    val SUPPORTED_ROM_FORMATS: List<String> = listOf(
        "img", "zip", "tar", "gz", "xz", "7z"
    )

    /** Whether live ROM editing is enabled. */
    const val LIVE_ROM_EDITING_ENABLED: Boolean = true

    /** Whether to automatically create a backup before modifying a ROM. */
    const val AUTO_BACKUP_ENABLED: Boolean = true

    /** The list of supported checksum algorithms for ROM verification. */
    val CHECKSUM_ALGORITHMS: List<String> = listOf(
        "SHA-256", "SHA-512", "MD5"
    )
}
