package com.willowvibe.agereveal.domain

/**
 * Generates retro block-art ASCII representations of numbers.
 *
 * Each digit is a 5×5 grid using Unicode block characters (█ and space).
 * Digits are joined with one-column spacing for a monospace terminal aesthetic.
 */
object AsciiArtGenerator {

    private val patterns = mapOf(
        '0' to listOf(
            " ███ ",
            "█   █",
            "█   █",
            "█   █",
            " ███ ",
        ),
        '1' to listOf(
            "  █  ",
            " ██  ",
            "  █  ",
            "  █  ",
            " ███ ",
        ),
        '2' to listOf(
            " ███ ",
            "    █",
            " ███ ",
            "█    ",
            "█████",
        ),
        '3' to listOf(
            " ███ ",
            "    █",
            " ███ ",
            "    █",
            " ███ ",
        ),
        '4' to listOf(
            "█   █",
            "█   █",
            "█████",
            "    █",
            "    █",
        ),
        '5' to listOf(
            "█████",
            "█    ",
            " ███ ",
            "    █",
            " ███ ",
        ),
        '6' to listOf(
            " ███ ",
            "█    ",
            "████ ",
            "█   █",
            " ███ ",
        ),
        '7' to listOf(
            "█████",
            "    █",
            "   █ ",
            "  █  ",
            " █   ",
        ),
        '8' to listOf(
            " ███ ",
            "█   █",
            " ███ ",
            "█   █",
            " ███ ",
        ),
        '9' to listOf(
            " ███ ",
            "█   █",
            " ████",
            "    █",
            " ███ ",
        ),
        ',' to listOf(
            "     ",
            "     ",
            "     ",
            "  █  ",
            " █   ",
        ),
    )

    /**
     * Render [number] as block-art ASCII with an optional caption underneath.
     * Returns a plain-text string ready for clipboard or terminal sharing.
     */
    fun render(number: Long, caption: String = "SECONDS ALIVE"): String {
        val digits = number.toString().toCharArray()
        val rows = Array(5) { StringBuilder() }

        for ((index, digit) in digits.withIndex()) {
            val pat = patterns[digit] ?: patterns['0']!!
            for (r in 0..4) {
                rows[r].append(pat[r])
                if (index < digits.lastIndex) rows[r].append(" ") // 1-col gap
            }
        }

        val lines = rows.map { it.toString() }.toMutableList()
        lines.add("")
        lines.add(caption)
        return lines.joinToString("\n")
    }
}
