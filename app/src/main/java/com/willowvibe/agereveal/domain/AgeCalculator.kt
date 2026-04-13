package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.Milestone
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core age calculation engine.
 * Uses [java.time] exclusively — never Calendar or Date.
 * All calculations are deterministic and testable with no Android dependency.
 */
@Singleton
class AgeCalculator @Inject constructor(
    private val zodiacCalculator: ZodiacCalculator,
    private val nakshatraCalculator: NakshatraCalculator,
) {

    /**
     * Compute an [AgeResult] for a given [birthDate] as of [today].
     *
     * @param includeUnlocked When true, populates zodiac / Vedic / heartbeats fields.
     *                        Pass false initially; pass true after rewarded ad is watched.
     */
    fun calculate(
        birthDate: LocalDate,
        today: LocalDate = LocalDate.now(),
        totalSecondsOverride: Long = -1L,
        includeUnlocked: Boolean = false,
    ): AgeResult {
        require(!birthDate.isAfter(today)) { "Birth date cannot be in the future" }

        val period = Period.between(birthDate, today)
        val totalDays = ChronoUnit.DAYS.between(birthDate, today)
        val totalHours = totalDays * 24
        val totalMinutes = totalHours * 60
        val totalSeconds = if (totalSecondsOverride >= 0) totalSecondsOverride
                           else ChronoUnit.SECONDS.between(birthDate.atStartOfDay(), today.atStartOfDay())

        // Next birthday
        var nextBirthday = birthDate.withYear(today.year)
        if (!nextBirthday.isAfter(today)) nextBirthday = nextBirthday.plusYears(1)
        val daysToNextBirthday = ChronoUnit.DAYS.between(today, nextBirthday)

        return AgeResult(
            birthDate = birthDate,
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            totalHours = totalHours,
            totalMinutes = totalMinutes,
            totalSeconds = totalSeconds,
            nextBirthdayDate = nextBirthday,
            daysToNextBirthday = daysToNextBirthday,
            dayOfWeekBorn = birthDate.dayOfWeek.name,
            dayOfWeekNextBirthday = nextBirthday.dayOfWeek.name,
            milestones = if (includeUnlocked) getMilestones(birthDate, today) else emptyList(),
            westernZodiac = if (includeUnlocked) zodiacCalculator.getWesternZodiac(birthDate.monthValue, birthDate.dayOfMonth) else "",
            rashi = if (includeUnlocked) zodiacCalculator.getRashi(birthDate.monthValue, birthDate.dayOfMonth) else "",
            nakshatra = if (includeUnlocked) nakshatraCalculator.getNakshatra(birthDate) else "",
            chineseZodiac = if (includeUnlocked) zodiacCalculator.getChineseZodiac(birthDate.year) else "",
            estimatedHeartbeats = if (includeUnlocked) estimateHeartbeats(totalMinutes) else 0L,
        )
    }

    // ---------------------------------------------------------------------------
    // Milestone days (from build plan: 1000, 5000, 10000, 15000, 20000, 25000)
    // ---------------------------------------------------------------------------

    fun getMilestones(birthDate: LocalDate, today: LocalDate = LocalDate.now()): List<Milestone> {
        val milestoneTargets = listOf(1_000, 5_000, 10_000, 15_000, 20_000, 25_000)
        return milestoneTargets.map { target ->
            val date = birthDate.plusDays(target.toLong())
            Milestone(
                targetDays = target,
                date = date,
                isPast = !date.isAfter(today),
                daysAway = ChronoUnit.DAYS.between(today, date),
            )
        }
    }

    // ---------------------------------------------------------------------------
    // Heartbeats: average ~72 BPM
    // ---------------------------------------------------------------------------

    private fun estimateHeartbeats(totalMinutes: Long): Long = totalMinutes * 72L
}
