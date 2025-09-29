package dev.aurakai.auraframefx.utilities

object StringUtils {
    /**
     * Joins a list of strings into a single string, concatenating in order.
     * Handles empty lists, single elements, whitespace, and punctuation.
     */
    fun join(elements: List<String>): String = elements.joinToString(separator = "")

    /**
     * Splits a string into tokens by whitespace (for round-trip tests).
     */
    fun split(input: String): List<String> =
        input.split(Regex("""\s+""")).filter { it.isNotEmpty() }
}
