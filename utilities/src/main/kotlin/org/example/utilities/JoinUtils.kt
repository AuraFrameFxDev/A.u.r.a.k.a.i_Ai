package dev.aurakai.auraframefx.utilities

import java.util.LinkedList

object JoinUtils {
    /**
     * Joins LinkedList elements into a single string separated by a single space.
     * Preserves whitespace-only and empty elements, handles large lists safely.
     */
    fun join(elements: LinkedList<String>): String = elements.joinToString(separator = " ") { it }
}
