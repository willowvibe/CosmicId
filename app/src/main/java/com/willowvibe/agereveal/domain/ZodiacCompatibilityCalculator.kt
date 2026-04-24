package com.willowvibe.agereveal.domain

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class CompatibilityResult(
    val personAWestern: String,
    val personBWestern: String,
    val personAElement: String,
    val personBElement: String,
    val personAChinese: String,
    val personBChinese: String,
    val westernScore: Int,
    val chineseScore: Int,
    val overallScore: Int,
    val headline: String,
    val description: String,
    val nameA: String = "",
    val nameB: String = "",
)

@Singleton
class ZodiacCompatibilityCalculator @Inject constructor(
    private val zodiacCalculator: ZodiacCalculator,
) {

    fun calculate(dateA: LocalDate, dateB: LocalDate, nameA: String = "", nameB: String = ""): CompatibilityResult {
        val westernA = zodiacCalculator.getWesternZodiac(dateA)
        val westernB = zodiacCalculator.getWesternZodiac(dateB)
        val chineseA = zodiacCalculator.getChineseZodiac(dateA)
        val chineseB = zodiacCalculator.getChineseZodiac(dateB)
        val elementA = getWesternElement(dateA.monthValue, dateA.dayOfMonth)
        val elementB = getWesternElement(dateB.monthValue, dateB.dayOfMonth)
        val westernScore = westernCompatibilityScore(dateA, dateB)
        val chineseScore = chineseCompatibilityScore(dateA, dateB)
        val overallScore = (westernScore * 0.5 + chineseScore * 0.5).toInt()
        return CompatibilityResult(
            personAWestern = westernA,
            personBWestern = westernB,
            personAElement = elementA,
            personBElement = elementB,
            personAChinese = chineseA,
            personBChinese = chineseB,
            westernScore = westernScore,
            chineseScore = chineseScore,
            overallScore = overallScore,
            headline = getHeadline(overallScore),
            description = getDescription(elementA, elementB),
            nameA = nameA,
            nameB = nameB,
        )
    }

    private fun getSignIndex(month: Int, day: Int): Int = when {
        (month == 3 && day >= 21) || (month == 4 && day <= 19) -> 0  // Aries    – Fire
        (month == 4 && day >= 20) || (month == 5 && day <= 20) -> 1  // Taurus   – Earth
        (month == 5 && day >= 21) || (month == 6 && day <= 20) -> 2  // Gemini   – Air
        (month == 6 && day >= 21) || (month == 7 && day <= 22) -> 3  // Cancer   – Water
        (month == 7 && day >= 23) || (month == 8 && day <= 22) -> 4  // Leo      – Fire
        (month == 8 && day >= 23) || (month == 9 && day <= 22) -> 5  // Virgo    – Earth
        (month == 9 && day >= 23) || (month == 10 && day <= 22) -> 6  // Libra    – Air
        (month == 10 && day >= 23) || (month == 11 && day <= 21) -> 7  // Scorpio  – Water
        (month == 11 && day >= 22) || (month == 12 && day <= 21) -> 8  // Sagittarius – Fire
        (month == 12 && day >= 22) || (month == 1 && day <= 19) -> 9  // Capricorn – Earth
        (month == 1 && day >= 20) || (month == 2 && day <= 18) -> 10 // Aquarius – Air
        else -> 11 // Pisces   – Water
    }

    private fun getWesternElement(month: Int, day: Int): String = when (getSignIndex(month, day) % 4) {
        0 -> "Fire"
        1 -> "Earth"
        2 -> "Air"
        else -> "Water"
    }

    private fun westernCompatibilityScore(a: LocalDate, b: LocalDate): Int {
        val signA = getSignIndex(a.monthValue, a.dayOfMonth)
        val signB = getSignIndex(b.monthValue, b.dayOfMonth)
        val diff = kotlin.math.abs(signA - signB)
        return when (minOf(diff, 12 - diff)) {
            0 -> 85  // Same sign
            4, 8 -> 95  // Trine (120°) — most harmonious
            2, 10 -> 75 // Sextile (60°) — compatible
            3, 9 -> 55  // Square (90°) — challenging but dynamic
            6 -> 48  // Opposition (180°) — magnetic tension
            1, 11 -> 65 // Semi-sextile
            else -> 60
        }
    }

    private fun chineseCompatibilityScore(dateA: LocalDate, dateB: LocalDate): Int {
        val yearA = zodiacCalculator.getChineseYear(dateA)
        val yearB = zodiacCalculator.getChineseYear(dateB)
        val indexA = ((yearA - 1900) % 12 + 12) % 12
        val indexB = ((yearB - 1900) % 12 + 12) % 12
        if (indexA == indexB) return 85
        // Trine groups: {0,4,8}, {1,5,9}, {2,6,10}, {3,7,11}
        if (indexA % 4 == indexB % 4) return 92
        val diff = minOf(kotlin.math.abs(indexA - indexB), 12 - kotlin.math.abs(indexA - indexB))
        return when (diff) {
            6 -> 38  // Direct clash
            4, 8 -> 62  // Neutral-compatible
            else -> 70
        }
    }

    private fun getHeadline(score: Int): String = when {
        score >= 90 -> "Cosmic Soulmates ✨"
        score >= 80 -> "Deeply Compatible 💫"
        score >= 70 -> "Strong Connection 🌟"
        score >= 60 -> "Good Match 💛"
        score >= 50 -> "Balanced Pair ⚖️"
        score >= 40 -> "Growth Partnership 🌱"
        else -> "Dynamic Tension ⚡"
    }

    private fun getDescription(elementA: String, elementB: String): String {
        val pair = setOf(elementA, elementB)
        return when {
            pair == setOf("Fire", "Air") ->
                "Fire and Air fuel each other — an energetic, passionate connection full of ideas and adventure."

            pair == setOf("Earth", "Water") ->
                "Earth and Water nourish each other — a deeply grounded and emotionally fulfilling bond."

            elementA == elementB && elementA == "Fire" ->
                "Two Fire signs ignite together — intense chemistry, passion, and shared drive."

            elementA == elementB && elementA == "Earth" ->
                "Two Earth signs build steadily — reliable, practical, and deeply loyal."

            elementA == elementB && elementA == "Air" ->
                "Two Air signs connect intellectually — brilliant conversations and shared ideals."

            elementA == elementB && elementA == "Water" ->
                "Two Water signs flow together — deep empathy and emotional understanding."

            pair == setOf("Fire", "Water") ->
                "Fire and Water challenge each other — intense attraction with opposing energies requiring balance."

            pair == setOf("Fire", "Earth") ->
                "Fire and Earth complement each other — passion tempered by practicality."

            pair == setOf("Air", "Water") ->
                "Air and Water bring dreams to life — imagination meets deep feeling."

            else ->
                "Air and Earth balance each other — creative thinking grounded in stability."
        }
    }
}
