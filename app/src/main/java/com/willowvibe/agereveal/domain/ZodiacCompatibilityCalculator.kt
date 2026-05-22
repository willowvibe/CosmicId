package com.willowvibe.agereveal.domain

import java.time.LocalDate
import java.time.Period
import javax.inject.Inject
import javax.inject.Singleton

data class AgeInfo(
    val years: Int,
    val months: Int,
    val days: Int,
)

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
    val relationshipType: RelationshipType = RelationshipType.Romantic,
    val personAAge: AgeInfo? = null,
    val personBAge: AgeInfo? = null,
    val ageGapLabel: String = "",
    val chineseRelationshipLabel: String = "",
)

@Singleton
class ZodiacCompatibilityCalculator @Inject constructor(
    private val zodiacCalculator: ZodiacCalculator,
) {

    fun calculate(
        dateA: LocalDate,
        dateB: LocalDate,
        nameA: String = "",
        nameB: String = "",
        relationshipType: RelationshipType = RelationshipType.Romantic,
    ): CompatibilityResult {
        val westernA = zodiacCalculator.getWesternZodiac(dateA)
        val westernB = zodiacCalculator.getWesternZodiac(dateB)
        val chineseA = zodiacCalculator.getChineseZodiac(dateA)
        val chineseB = zodiacCalculator.getChineseZodiac(dateB)
        val indexA = zodiacCalculator.getWesternSignIndex(dateA)
        val indexB = zodiacCalculator.getWesternSignIndex(dateB)
        val elementA = getWesternElementByIndex(indexA)
        val elementB = getWesternElementByIndex(indexB)
        val westernScore = westernCompatibilityScoreByIndex(indexA, indexB)
        val chineseScore = chineseCompatibilityScore(dateA, dateB)
        val chineseLabel = chineseRelationshipLabel(dateA, dateB)
        val overallScore = when (relationshipType) {
            RelationshipType.Romantic -> (westernScore * 0.5 + chineseScore * 0.5).toInt()
            RelationshipType.Sibling -> (westernScore * 0.4 + chineseScore * 0.6).toInt()
            RelationshipType.Friendship -> (westernScore * 0.6 + chineseScore * 0.4).toInt()
            RelationshipType.Regular -> (westernScore * 0.5 + chineseScore * 0.5).toInt()
        }
        val today = LocalDate.now()
        val ageA = Period.between(dateA, today)
        val ageB = Period.between(dateB, today)
        val ageGapLabel = computeAgeGapLabel(
            nameA.ifEmpty { "Person A" },
            nameB.ifEmpty { "Person B" },
            dateA, dateB,
        )
        return CompatibilityResult(
            personAWestern = westernA,
            personBWestern = westernB,
            personAElement = elementA,
            personBElement = elementB,
            personAChinese = chineseA,
            personBChinese = chineseB,
            westernScore = westernScore,
            chineseScore = chineseScore,
            overallScore = overallScore.coerceIn(0, 100),
            headline = getHeadline(overallScore.coerceIn(0, 100), relationshipType),
            description = getDescription(elementA, elementB, relationshipType),
            nameA = nameA,
            nameB = nameB,
            relationshipType = relationshipType,
            personAAge = AgeInfo(ageA.years, ageA.months, ageA.days),
            personBAge = AgeInfo(ageB.years, ageB.months, ageB.days),
            ageGapLabel = ageGapLabel,
            chineseRelationshipLabel = chineseLabel,
        )
    }

    private fun getWesternElementByIndex(index: Int): String = when (index % 4) {
        0 -> "Fire"
        1 -> "Earth"
        2 -> "Air"
        else -> "Water"
    }

    private fun westernCompatibilityScoreByIndex(signA: Int, signB: Int): Int {
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
        return chineseCompatibilityMatrix(indexA, indexB)
    }

    private fun chineseRelationshipLabel(dateA: LocalDate, dateB: LocalDate): String {
        val yearA = zodiacCalculator.getChineseYear(dateA)
        val yearB = zodiacCalculator.getChineseYear(dateB)
        val indexA = ((yearA - 1900) % 12 + 12) % 12
        val indexB = ((yearB - 1900) % 12 + 12) % 12
        return chineseRelationshipName(indexA, indexB)
    }

    /**
     * Full 12×12 Chinese zodiac compatibility matrix.
     *
     * Relationships in priority order:
     * 1. 六合 (Six Harmonies) — best pairs: 92
     * 2. 三合 (Trine) — same element group: 95
     * 3. Same sign: 88 (certain signs have self-punishment: 42)
     * 4. 相冲 (Clash) — opposite signs: 35
     * 5. 相害 (Harm) — six harm pairs: 45
     * 6. 相刑 (Punishment) — triple groups & Rat-Rabbit: 40
     * 7. Default diff-based: adjacent 75, diff 2/10 → 70, diff 3/9 → 65, diff 4/8 → 60, diff 5/7 → 55
     */
    private fun chineseCompatibilityMatrix(a: Int, b: Int): Int {
        if (a == b) {
            // Self-punishment for Dragon(4), Horse(6), Rooster(9), Pig(11)
            return if (a in setOf(4, 6, 9, 11)) 42 else 88
        }

        val pair = setOf(a, b)
        val diff = minOf(kotlin.math.abs(a - b), 12 - kotlin.math.abs(a - b))

        // 六合 (Six Harmonies) — best-matched pairs
        val sixHarmonies = setOf(
            setOf(0, 1), setOf(2, 11), setOf(3, 10),
            setOf(4, 9), setOf(5, 8), setOf(6, 7),
        )
        if (pair in sixHarmonies) return 92

        // 三合 (Trine) — same element group
        if (a % 4 == b % 4) return 95

        // 相冲 (Clash) — opposite signs
        if (diff == 6) return 35

        // 相刑 (Punishment) — triple groups + Rat-Rabbit. Checked before Harm
        // because punishment is the more severe relationship when a pair belongs
        // to both categories (e.g. Tiger-Snake).
        val punishmentPairs = setOf(
            setOf(2, 5), setOf(2, 8), setOf(5, 8),   // Tiger-Snake-Monkey
            setOf(1, 7), setOf(1, 10), setOf(7, 10), // Ox-Goat-Dog
            setOf(0, 3),                               // Rat-Rabbit
        )
        if (pair in punishmentPairs) return 40

        // 相害 (Harm) — six harmful pairs
        val harmPairs = setOf(
            setOf(0, 7), setOf(1, 6), setOf(2, 5),
            setOf(3, 4), setOf(8, 11), setOf(9, 10),
        )
        if (pair in harmPairs) return 45

        return when (diff) {
            1, 11 -> 75
            2, 10 -> 70
            3, 9 -> 65
            4, 8 -> 60
            5, 7 -> 55
            else -> 60
        }
    }

    /** Human-readable label for the Chinese zodiac relationship between two indices. */
    private fun chineseRelationshipName(a: Int, b: Int): String {
        if (a == b) {
            return if (a in setOf(4, 6, 9, 11)) "Self-Punishment" else "Same Sign"
        }

        val pair = setOf(a, b)
        val diff = minOf(kotlin.math.abs(a - b), 12 - kotlin.math.abs(a - b))

        val sixHarmonies = setOf(
            setOf(0, 1), setOf(2, 11), setOf(3, 10),
            setOf(4, 9), setOf(5, 8), setOf(6, 7),
        )
        if (pair in sixHarmonies) return "六合 Harmony"

        if (a % 4 == b % 4) return "三合 Trine"

        if (diff == 6) return "相冲 Clash"

        val punishmentPairs = setOf(
            setOf(2, 5), setOf(2, 8), setOf(5, 8),
            setOf(1, 7), setOf(1, 10), setOf(7, 10),
            setOf(0, 3),
        )
        if (pair in punishmentPairs) return "相刑 Punishment"

        val harmPairs = setOf(
            setOf(0, 7), setOf(1, 6), setOf(2, 5),
            setOf(3, 4), setOf(8, 11), setOf(9, 10),
        )
        if (pair in harmPairs) return "相害 Harm"

        return when (diff) {
            1, 11 -> "Adjacent"
            2, 10 -> "Neutral"
            3, 9 -> "Distant"
            4, 8 -> "Tense"
            5, 7 -> "Challenging"
            else -> "Neutral"
        }
    }

    private fun getHeadline(score: Int, type: RelationshipType): String = when (type) {
        RelationshipType.Romantic -> when {
            score >= 90 -> "Cosmic Soulmates ✨"
            score >= 80 -> "Deeply Compatible 💫"
            score >= 70 -> "Strong Connection 🌟"
            score >= 60 -> "Good Match 💛"
            score >= 50 -> "Balanced Pair ⚖️"
            score >= 40 -> "Growth Partnership 🌱"
            else -> "Dynamic Tension ⚡"
        }
        RelationshipType.Sibling -> when {
            score >= 90 -> "Soul Siblings ✨"
            score >= 80 -> "Deeply Bonded 💫"
            score >= 70 -> "Strong Kinship 🌟"
            score >= 60 -> "Family Harmony 💛"
            score >= 50 -> "Balanced Siblings ⚖️"
            score >= 40 -> "Learning Together 🌱"
            else -> "Contrasting Spirits ⚡"
        }
        RelationshipType.Friendship -> when {
            score >= 90 -> "Kindred Spirits ✨"
            score >= 80 -> "True Friends 💫"
            score >= 70 -> "Strong Friendship 🌟"
            score >= 60 -> "Good Companions 💛"
            score >= 50 -> "Balanced Buddies ⚖️"
            score >= 40 -> "Growing Together 🌱"
            else -> "Dynamic Duo ⚡"
        }
        RelationshipType.Regular -> when {
            score >= 90 -> "Cosmic Alignment ✨"
            score >= 80 -> "Deep Harmony 💫"
            score >= 70 -> "Strong Resonance 🌟"
            score >= 60 -> "Good Fit 💛"
            score >= 50 -> "Balanced Pair ⚖️"
            score >= 40 -> "Mutual Growth 🌱"
            else -> "Dynamic Contrast ⚡"
        }
    }

    private fun getDescription(elementA: String, elementB: String, type: RelationshipType): String {
        val pair = setOf(elementA, elementB)
        return when (type) {
            RelationshipType.Romantic -> partnerDescription(pair, elementA, elementB)
            RelationshipType.Sibling -> siblingDescription(pair, elementA, elementB)
            RelationshipType.Friendship -> friendDescription(pair, elementA, elementB)
            RelationshipType.Regular -> regularDescription(pair, elementA, elementB)
        }
    }

    private fun partnerDescription(pair: Set<String>, elementA: String, elementB: String): String = when {
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

    private fun siblingDescription(pair: Set<String>, elementA: String, elementB: String): String = when {
        pair == setOf("Fire", "Air") ->
            "One sibling sparks ideas, the other fans the flames — a lively household full of energy and laughter."
        pair == setOf("Earth", "Water") ->
            "A nurturing sibling bond — one offers steady support while the other brings emotional depth."
        elementA == elementB && elementA == "Fire" ->
            "Two fiery siblings — competitive but fiercely protective of each other."
        elementA == elementB && elementA == "Earth" ->
            "Reliable and loyal siblings who always have each other's backs through thick and thin."
        elementA == elementB && elementA == "Air" ->
            "Curious and communicative siblings who love debating ideas and exploring the world together."
        elementA == elementB && elementA == "Water" ->
            "Deeply intuitive siblings who understand each other without words — a profound emotional bond."
        pair == setOf("Fire", "Water") ->
            "Contrasting energies create a dynamic sibling relationship — passion meets patience."
        pair == setOf("Fire", "Earth") ->
            "One sibling dreams big, the other keeps things practical — a complementary team."
        pair == setOf("Air", "Water") ->
            "Imagination meets feeling — siblings who create beautiful memories together."
        else ->
            "Grounded and thoughtful siblings — one brings ideas, the other brings structure."
    }

    private fun friendDescription(pair: Set<String>, elementA: String, elementB: String): String = when {
        pair == setOf("Fire", "Air") ->
            "An unstoppable friend duo — Fire brings boldness, Air brings fresh perspectives. Adventure awaits."
        pair == setOf("Earth", "Water") ->
            "A comforting friendship — Earth keeps things real while Water understands your feelings deeply."
        elementA == elementB && elementA == "Fire" ->
            "Two fire spirits as friends — always ready for spontaneity, excitement, and mutual encouragement."
        elementA == elementB && elementA == "Earth" ->
            "Steadfast friends who show up consistently — loyalty and trust define this bond."
        elementA == elementB && elementA == "Air" ->
            "Intellectual besties — endless conversations, shared curiosity, and mental stimulation."
        elementA == elementB && elementA == "Water" ->
            "Emotionally in-sync friends who offer genuine empathy and a safe space for vulnerability."
        pair == setOf("Fire", "Water") ->
            "A friendship of contrasts — one motivates action, the other brings reflection. Both grow."
        pair == setOf("Fire", "Earth") ->
            "The dreamer and the planner — friends who balance ambition with grounded support."
        pair == setOf("Air", "Water") ->
            "One friend lifts you with ideas, the other holds you with understanding — a beautiful balance."
        else ->
            "Creative and stable friends — you bring out the best in each other's strengths."
    }

    private fun regularDescription(pair: Set<String>, elementA: String, elementB: String): String = when {
        pair == setOf("Fire", "Air") ->
            "Fire and Air create momentum together — energetic collaboration and mutual inspiration."
        pair == setOf("Earth", "Water") ->
            "Earth and Water blend naturally — stable support meets emotional awareness."
        elementA == elementB && elementA == "Fire" ->
            "Two Fire energies amplify each other — bold, passionate, and action-oriented together."
        elementA == elementB && elementA == "Earth" ->
            "Shared Earth brings consistency, dependability, and a solid foundation to this connection."
        elementA == elementB && elementA == "Air" ->
            "Two Air minds think alike — intellectual exchange and forward-looking perspectives."
        elementA == elementB && elementA == "Water" ->
            "Two Water souls resonate — empathy, intuition, and emotional depth in harmony."
        pair == setOf("Fire", "Water") ->
            "Opposing forces create a balanced dynamic — energy and calm in productive tension."
        pair == setOf("Fire", "Earth") ->
            "Fire inspires action, Earth provides structure — a complementary pairing."
        pair == setOf("Air", "Water") ->
            "Ideas meet intuition — a connection that bridges thought and feeling."
        else ->
            "Creative vision meets practical grounding — a well-rounded dynamic."
    }

    private fun computeAgeGapLabel(nameA: String, nameB: String, dateA: LocalDate, dateB: LocalDate): String {
        if (dateA == dateB) return "Both are the same age ✦"
        val (olderName, olderDate, youngerDate) = if (dateA.isBefore(dateB)) {
            Triple(nameA, dateA, dateB)
        } else {
            Triple(nameB, dateB, dateA)
        }
        val gap = Period.between(olderDate, youngerDate)
        val parts = mutableListOf<String>()
        if (gap.years > 0) parts.add("${gap.years} year${if (gap.years == 1) "" else "s"}")
        if (gap.months > 0) parts.add("${gap.months} month${if (gap.months == 1) "" else "s"}")
        if (gap.days > 0) parts.add("${gap.days} day${if (gap.days == 1) "" else "s"}")
        val gapString = parts.joinToString(", ")
        return "$olderName is older by $gapString"
    }
}
