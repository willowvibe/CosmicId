package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.GeoLocation
import com.willowvibe.agereveal.data.model.Milestone
import com.willowvibe.agereveal.domain.model.CelestialBody
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
    private val percentileCalculator: AgePercentileCalculator,
    private val parallelUniverseGenerator: ParallelUniverseGenerator,
    private val planetaryDignityCalculator: PlanetaryDignityCalculator,
    private val birthChartSubChart: BirthChartSubChart, // Phase E
    private val astronomicalCalculator: AstronomicalCalculator, // Phase E — for jd
) {

    /**
     * Compute an [AgeResult] for a given [birthDate] and [birthTime] as of [today].
     *
     * @param birthTime Optional time of birth for precise Nakshatra/Rashi calculations
     * @param includeUnlocked When true, populates zodiac / Vedic / heartbeats fields.
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
        val percentileResult = if (includeUnlocked) percentileCalculator.calculate(period.years) else null

        val (planetLongitudes, jd) = if (includeUnlocked) {
            computePlanetLongitudesAndJd(birthDate, birthTime, zoneOffset)
        } else emptyMap<CelestialBody, Double>() to 0.0

        val snapshot = if (includeUnlocked) {
            astronomicalCalculator.snapshot(birthDate, birthTime, zoneOffset)
        } else null

        val subCharts = if (includeUnlocked && snapshot != null) {
            birthChartSubChart.compute(
                siderealMoonLongitude = snapshot.siderealMoonLongitude,
                planetLongitudes = planetLongitudes,
                jd = jd,
            )
        } else null

        val dashaDetail = if (includeUnlocked) {
            runCatching { dashaCalculator.getDashaDetail(birthDate, birthTime, zoneOffset) }
                .getOrNull()
        } else null

        val tropicalAscendant = if (includeUnlocked && location != null) {
            runCatching {
                zodiacCalculator.getTropicalAscendantSign(birthDate, birthTime, zoneOffset, location)
            }.getOrNull()
        } else null

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
            planetDignities = if (includeUnlocked) {
                val longitudes = zodiacCalculator.getPlanetLongitudes(birthDate, birthTime, zoneOffset)
                planetaryDignityCalculator.computeDignities(longitudes)
            } else emptyList(),
            dashaDetail = dashaDetail,
            baZiInfo = if (includeUnlocked) baZiCalculator.getBaZiSummary(birthDate) else "",
            lunarBirthday = if (includeUnlocked) lunarConverter.toLunarString(birthDate) else "",
            estimatedHeartbeats = if (includeUnlocked) estimateHeartbeats(totalMinutes) else 0L,
            globalPercentile = percentileResult?.percentileText ?: "",
            sharedBirthDateEstimate = percentileResult?.sharedBirthDateEstimate ?: "",
            parallelUniverses = if (includeUnlocked) parallelUniverseGenerator.generate(birthDate, today) else emptyList(),
            // Phase E fields
            nakshatraMetadata = subCharts?.nakshatraMetadata,
            navamsaChart = subCharts?.navamsaChart,
            planetaryAspects = subCharts?.planetaryAspects ?: emptyList(),
            tropicalAscendant = tropicalAscendant,
            isExact = birthTime != null,
        )
    }

    // ---------------------------------------------------------------------------
    // Phase E helper: compute planet longitudes + JD once, used by sub-chart trio
    // ---------------------------------------------------------------------------

    /**
     * Build a map of celestial body → sidereal longitude for the 10 bodies we
     * surface (Sun, Moon, Mercury..Pluto). Returns the JD alongside so callers
     * can pass it to [com.willowvibe.agereveal.domain.AspectCalculator].
     *
     * Mirrors the loop in [com.willowvibe.agereveal.domain.model.BirthChart.compute].
     * Rahu/Ketu are excluded — not needed for aspects or navamsa today.
     */
    private fun computePlanetLongitudesAndJd(
        birthDate: LocalDate,
        birthTime: LocalTime?,
        zoneOffset: ZoneOffset?,
    ): Pair<Map<CelestialBody, Double>, Double> {
        val localDateTime = birthTime?.let { bt -> birthDate.atTime(bt) } ?: birthDate.atStartOfDay()
        val utDateTime = zoneOffset?.let {
            localDateTime.atOffset(it).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
        } ?: localDateTime
        val jd = astronomicalCalculator.julianDay(utDateTime)
        val snap = astronomicalCalculator.snapshot(birthDate, birthTime, zoneOffset)
        val ayanamsa = snap.ayanamsa

        val longitudes = mutableMapOf<CelestialBody, Double>()
        for (body in CelestialBody.all) {
            when (body) {
                CelestialBody.SUN -> longitudes[body] = snap.siderealSunLongitude
                CelestialBody.MOON -> longitudes[body] = snap.siderealMoonLongitude
                CelestialBody.RAHU, CelestialBody.KETU -> {
                    // Lunar nodes not used by sub-chart trio; skip.
                }
                else -> {
                    val planet = when (body) {
                        CelestialBody.MERCURY -> AstronomicalCalculator.Planet.MERCURY
                        CelestialBody.VENUS -> AstronomicalCalculator.Planet.VENUS
                        CelestialBody.MARS -> AstronomicalCalculator.Planet.MARS
                        CelestialBody.JUPITER -> AstronomicalCalculator.Planet.JUPITER
                        CelestialBody.SATURN -> AstronomicalCalculator.Planet.SATURN
                        CelestialBody.URANUS -> AstronomicalCalculator.Planet.URANUS
                        CelestialBody.NEPTUNE -> AstronomicalCalculator.Planet.NEPTUNE
                        CelestialBody.PLUTO -> AstronomicalCalculator.Planet.PLUTO
                        else -> error("Unhandled body: $body")
                    }
                    val tropical = astronomicalCalculator.planetLongitude(jd, planet)
                    longitudes[body] = ((tropical - ayanamsa) % 360.0 + 360.0) % 360.0
                }
            }
        }
        return longitudes to jd
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
