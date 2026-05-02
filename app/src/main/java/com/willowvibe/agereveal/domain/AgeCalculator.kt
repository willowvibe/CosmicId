package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.GeoLocation
import com.willowvibe.agereveal.data.model.Milestone
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.ZoneOffset
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
    private val dashaCalculator: DashaCalculator,
    private val baZiCalculator: BaZiCalculator,
    private val lunarConverter: LunarCalendarConverter,
) {

    /**
     * Compute an [AgeResult] for a given [birthDate] and [birthTime] as of [today].
     *
     * @param birthTime Optional time of birth for precise Nakshatra/Rashi calculations
     * @param includeUnlocked When true, populates zodiac / Vedic / heartbeats fields.
     *                        Pass false initially; pass true after rewarded ad is watched.
     */
    fun calculate(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        today: LocalDate = LocalDate.now(),
        totalSecondsOverride: Long = -1L,
        includeUnlocked: Boolean = false,
        zoneOffset: ZoneOffset? = null,
        location: GeoLocation? = null,
    ): AgeResult {
        require(!birthDate.isAfter(today)) { "Birth date cannot be in the future" }

        // Use birth time if provided for precise calculations, otherwise assume midnight
        val birthDateTime = birthTime?.let { bt -> birthDate.atTime(bt) } ?: birthDate.atStartOfDay()
        val todayDateTime = today.atStartOfDay()

        val period = Period.between(birthDate, today)
        val totalDays = ChronoUnit.DAYS.between(birthDate, today)
        val totalHours = totalDays * 24
        val totalMinutes = totalHours * 60
        val totalSeconds = if (totalSecondsOverride >= 0) totalSecondsOverride
        else ChronoUnit.SECONDS.between(birthDateTime, todayDateTime)

        // Next birthday — use yearSafeBirthday to handle Feb 29 in non-leap years
        var nextBirthday = yearSafeBirthday(birthDate, today.year)
        if (nextBirthday.isBefore(today)) nextBirthday = yearSafeBirthday(birthDate, today.year + 1)
        val daysToNextBirthday = ChronoUnit.DAYS.between(today, nextBirthday)

        return AgeResult(
            birthDate = birthDate,
            birthTime = birthTime,
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
            westernZodiac = if (includeUnlocked) zodiacCalculator.getWesternZodiac(birthDate, birthTime, zoneOffset) else "",
            westernMoonSign = if (includeUnlocked) zodiacCalculator.getWesternMoonSign(birthDate, birthTime, zoneOffset) else "",
            rashi = if (includeUnlocked) zodiacCalculator.getRashi(birthDate, birthTime, zoneOffset) else "",
            rashiLord = if (includeUnlocked) zodiacCalculator.getRashiLord(birthDate, birthTime, zoneOffset) else "",
            approximateAscendant = if (includeUnlocked) zodiacCalculator.getApproximateAscendant(birthDate, birthTime, zoneOffset, location) else "",
            tithi = if (includeUnlocked) zodiacCalculator.getTithi(birthDate, birthTime, zoneOffset) else "",
            nakshatra = if (includeUnlocked) nakshatraCalculator.getNakshatra(birthDate, birthTime, zoneOffset) else "",
            nakshatraPada = if (includeUnlocked) nakshatraCalculator.getNakshatraWithPada(birthDate, birthTime, zoneOffset) else "",
            chineseZodiac = if (includeUnlocked) zodiacCalculator.getChineseZodiac(birthDate) else "",
            chineseStemBranch = if (includeUnlocked) zodiacCalculator.getChineseStemBranch(birthDate) else "",
            planetPositions = if (includeUnlocked) zodiacCalculator.getPlanetPositions(birthDate, birthTime, zoneOffset) else emptyList(),
            dashaInfo = if (includeUnlocked) dashaCalculator.getDashaInfo(birthDate, birthTime, zoneOffset) else "",
            baZiInfo = if (includeUnlocked) baZiCalculator.getBaZiSummary(birthDate) else "",
            lunarBirthday = if (includeUnlocked) lunarConverter.toLunarString(birthDate) else "",
            estimatedHeartbeats = if (includeUnlocked) estimateHeartbeats(totalMinutes) else 0L,
            isExact = birthTime != null,
        )
    }

    // ---------------------------------------------------------------------------
    // Milestone days (from build plan: 1000, 5000, 10000, 15000, 20000, 25000)
    // ---------------------------------------------------------------------------

    fun getMilestones(birthDate: LocalDate, today: LocalDate = LocalDate.now()): List<Milestone> {
        val milestoneTargets =
            listOf(500, 1_000, 2_000, 3_000, 5_000, 7_000, 10_000, 12_500, 15_000, 20_000, 25_000, 30_000)
        return milestoneTargets.map { target ->
            val date = birthDate.plusDays(target.toLong())
            Milestone(
                targetDays = target,
                date = date,
                isPast = date.isBefore(today),   // strictly before; today's milestone is not "past"
                daysAway = ChronoUnit.DAYS.between(today, date),
            )
        }
    }

    // ---------------------------------------------------------------------------
    // Heartbeats: average ~72 BPM
    // ---------------------------------------------------------------------------

    private fun estimateHeartbeats(totalMinutes: Long): Long = totalMinutes * 72L

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    /**
     * Returns [birthDate] adjusted to [year], safely handling Feb 29 birthdays
     * in non-leap years by mapping to Mar 1 (matches Australian / common convention).
     * Note: `LocalDate.withYear` silently clamps Feb 29 to Feb 28; we explicitly override
     * that behaviour so the birthday still falls on a post-Feb-28 date in non-leap years.
     */
    private fun yearSafeBirthday(birthDate: LocalDate, year: Int): LocalDate {
        if (birthDate.monthValue == 2 && birthDate.dayOfMonth == 29 && !java.time.Year.isLeap(year.toLong())) {
            return LocalDate.of(year, 3, 1)
        }
        return birthDate.withYear(year)
    }
}
