package com.willowvibe.agereveal.domain

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects generational cohort from birth year.
 * Each generation has a name, emoji, and year range.
 */
@Singleton
class GenerationCalculator @Inject constructor() {

    fun getGeneration(birthYear: Int): Generation = when (birthYear) {
        in 1928..1945 -> Generation(
            name = "Silent Generation",
            shortName = "Silent Gen",
            emoji = "👴",
            startYear = 1928,
            endYear = 1945,
            description = "Born between 1928 and 1945. Grew up during the Great Depression and WWII.",
        )
        in 1946..1964 -> Generation(
            name = "Baby Boomers",
            shortName = "Boomer",
            emoji = "📄",
            startYear = 1946,
            endYear = 1964,
            description = "Born between 1946 and 1964. Post-WWII economic boom generation.",
        )
        in 1965..1980 -> Generation(
            name = "Generation X",
            shortName = "Gen X",
            emoji = "💿",
            startYear = 1965,
            endYear = 1980,
            description = "Born between 1965 and 1980. Latchkey kids of the MTV era.",
        )
        in 1981..1996 -> Generation(
            name = "Millennials",
            shortName = "Millennial",
            emoji = "📠",
            startYear = 1981,
            endYear = 1996,
            description = "Born between 1981 and 1996. Digital natives who remember dial-up.",
        )
        in 1997..2012 -> Generation(
            name = "Generation Z",
            shortName = "Gen Z",
            emoji = "👾",
            startYear = 1997,
            endYear = 2012,
            description = "Born between 1997 and 2012. True digital natives.",
        )
        else -> Generation(
            name = "Generation Alpha",
            shortName = "Gen Alpha",
            emoji = "🤖",
            startYear = 2013,
            endYear = 2025,
            description = "Born from 2013 onward. Growing up with tablets and AI.",
        )
    }

    /** Format a shareable badge text for the given generation and seconds survived. */
    fun badgeText(generation: Generation, totalSeconds: Long): String {
        val secLabel = when {
            totalSeconds >= 1_000_000_000 -> "%.1f billion".format(totalSeconds / 1_000_000_000.0)
            totalSeconds >= 1_000_000 -> "%.1f million".format(totalSeconds / 1_000_000.0)
            else -> "%,d".format(totalSeconds)
        }
        return "Certified ${generation.shortName} ${generation.emoji} · $secLabel seconds survived · ${generation.startYear}–${generation.endYear} cohort"
    }
}

data class Generation(
    val name: String,
    val shortName: String,
    val emoji: String,
    val startYear: Int,
    val endYear: Int,
    val description: String,
)
