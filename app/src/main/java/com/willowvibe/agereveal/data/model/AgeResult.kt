package com.willowvibe.agereveal.data.model

import com.willowvibe.agereveal.domain.DashaInfo
import com.willowvibe.agereveal.domain.NakshatraData
import com.willowvibe.agereveal.domain.NavamsaChart
import com.willowvibe.agereveal.domain.Aspect
import java.time.LocalDate
import java.time.LocalTime

/**
 * Immutable result produced by [com.willowvibe.agereveal.domain.AgeCalculator].
 * All derived values are computed once and stored here to avoid re-calculation on recomposition.
 */
data class AgeResult(
    val name: String = "",             // Optional name for display purposes
    val birthDate: LocalDate,
    val birthTime: LocalTime? = null,  // Optional time of birth for precise astrology

    // Exact age components
    val years: Int,
    val months: Int,
    val days: Int,

    // Aggregate totals
    val totalDays: Long,
    val totalHours: Long,
    val totalMinutes: Long,
    val totalSeconds: Long,          // updated every second via ticker flow

    // Birthday countdown
    val nextBirthdayDate: LocalDate,
    val daysToNextBirthday: Long,

    // Day-of-week facts
    val dayOfWeekBorn: String,       // e.g. "THURSDAY"
    val dayOfWeekNextBirthday: String,

    // Milestone days (unlockable)
    val milestones: List<Milestone> = emptyList(),

    // Zodiac & Vedic (unlockable)
    val westernZodiac: String = "",
    val westernMoonSign: String = "",
    val rashi: String = "",
    val rashiLord: String = "",
    val approximateAscendant: String = "",
    val tithi: String = "",
    val nakshatra: String = "",
    val nakshatraPada: String = "",
    val chineseZodiac: String = "",
    val chineseStemBranch: String = "",

    // Planetary positions summary
    val planetPositions: List<Pair<String, String>> = emptyList(),

    // Planetary dignities (Vedic avastha)
    val planetDignities: List<com.willowvibe.agereveal.domain.PlanetaryDignityCalculator.PlanetaryDignity> = emptyList(),

    // Vimshottari Dasha (unlockable) — Phase 6.5: now structured; summary() preserved as back-compat
    val dashaDetail: DashaInfo? = null,

    // Ba Zi (Four Pillars) — Year + Month (unlockable)
    val baZiInfo: String = "",

    // Lunar birthday (unlockable)
    val lunarBirthday: String = "",

    // Fun fact (unlockable)
    val estimatedHeartbeats: Long = 0L,

    // Global age percentile (unlockable)
    val globalPercentile: String = "",
    val sharedBirthDateEstimate: String = "",

    // Parallel universe birth contexts (unlockable)
    val parallelUniverses: List<com.willowvibe.agereveal.domain.ParallelUniverseGenerator.UniverseContext> = emptyList(),

    // Phase 6.5 — Vedic UI surfacing (BUG-068 vehicle). All populated when includeUnlocked = true.
    val nakshatraMetadata: NakshatraData? = null,    // from NakshatraMetadata.forLongitude(siderealMoonLon)
    val navamsaChart: NavamsaChart? = null, // from DivisionalChartCalculator.getNavamsaChart(planetLongitudes)
    val planetaryAspects: List<Aspect> = emptyList(),// from AspectCalculator.computeAspects(jd, planetLongitudes)
    val tropicalAscendant: String? = null,           // from ZodiacCalculator.getTropicalAscendantSign(...)

    // Precision indicator
    val isExact: Boolean = birthTime != null,  // True if time of birth is provided
) {
    /**
     * Back-compat: derived from [dashaDetail] when present, else empty string.
     * The original `dashaInfo: String = ""` constructor parameter has been
     * promoted to a computed property so existing string-based consumers
     * (share card, any `result.dashaInfo` reader) keep working unchanged.
     */
    val dashaInfo: String
        get() = dashaDetail?.summary() ?: ""
}

/**
 * A single life-day milestone, e.g. the 10,000th day alive.
 */
data class Milestone(
    val targetDays: Int,
    val date: LocalDate,
    val isPast: Boolean,
    val daysAway: Long,             // negative when in the past
)
