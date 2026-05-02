package com.willowvibe.agereveal.data.model

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

    // Vimshottari Dasha (unlockable)
    val dashaInfo: String = "",

    // Ba Zi (Four Pillars) — Year + Month (unlockable)
    val baZiInfo: String = "",

    // Fun fact (unlockable)
    val estimatedHeartbeats: Long = 0L,

    // Precision indicator
    val isExact: Boolean = birthTime != null,  // True if time of birth is provided
)

/**
 * A single life-day milestone, e.g. the 10,000th day alive.
 */
data class Milestone(
    val targetDays: Int,
    val date: LocalDate,
    val isPast: Boolean,
    val daysAway: Long,             // negative when in the past
)
