package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.data.model.GeoLocation
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin facade over [WesternZodiacCalculator], [VedicZodiacCalculator],
 * [ChineseZodiacCalculator], and [PlanetaryCalculator].
 *
 * Preserves the public API of the original 369-line god-class so that
 * existing call sites ([AgeCalculator], [BirthChart.compute],
 * [ZodiacCompatibilityCalculator], [BaZiCalculator],
 * [VedicCompatibilityCalculator], [DailyFortuneGenerator], and the test
 * suite) don't need to change. All heavy lifting lives in the four
 * focused calculators.
 */
@Singleton
class ZodiacCalculator @Inject constructor(
    private val astronomy: AstronomicalCalculator,
    private val western: WesternZodiacCalculator,
    private val vedic: VedicZodiacCalculator,
    private val chinese: ChineseZodiacCalculator,
    private val planetary: PlanetaryCalculator,
) {

    /**
     * Convenience constructor for tests and one-off code that wants a
     * pre-wired [ZodiacCalculator] without dealing with Hilt. Production
     * code should let Hilt inject the default constructor.
     */
    constructor(astronomy: AstronomicalCalculator) : this(
        astronomy = astronomy,
        western = WesternZodiacCalculator(astronomy),
        vedic = VedicZodiacCalculator(astronomy),
        chinese = ChineseZodiacCalculator(),
        planetary = PlanetaryCalculator(astronomy, WesternZodiacCalculator(astronomy)),
    )

    // ---------------------------------------------------------------------------
    // Western (tropical) — delegates to WesternZodiacCalculator
    // ---------------------------------------------------------------------------

    /** Returns the western (tropical) zodiac sign index (0=Aries, …, 11=Pisces) from the Sun's ecliptic longitude. */
    fun getWesternSignIndex(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): Int = western.getSignIndex(birthDate, birthTime, zoneOffset)

    /** Get the full western zodiac names list for programmatic access. */
    fun getWesternSignNames(): List<String> = western.signNames

    /** Get just the sign name without emoji for display. */
    fun getWesternSignName(index: Int): String = western.getSignName(index)

    /** Western (tropical) zodiac from the Sun's ecliptic longitude with cusp detection. */
    fun getWesternZodiac(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String = western.getZodiac(birthDate, birthTime, zoneOffset)

    /** Western Moon sign from the Moon's tropical ecliptic longitude with cusp detection. */
    fun getWesternMoonSign(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String = western.getMoonSign(birthDate, birthTime, zoneOffset)

    // ---------------------------------------------------------------------------
    // Vedic (sidereal) — delegates to VedicZodiacCalculator
    // ---------------------------------------------------------------------------

    /** Vedic Rashi derived from the Sun's sidereal ecliptic longitude (12 × 30° signs). */
    fun getRashi(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String = vedic.getRashi(birthDate, birthTime, zoneOffset)

    /** Returns the ruling planet (graha) of the Vedic Rashi for the given birth date. */
    fun getRashiLord(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String = vedic.getRashiLord(birthDate, birthTime, zoneOffset)

    /** Tithi (lunar day, 1–30) for the given birth date-time. */
    fun getTithi(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String = vedic.getTithi(birthDate, birthTime, zoneOffset)

    /** Vedic Lagna (Ascendant) — exact when [location] is provided, approximate otherwise. */
    fun getApproximateAscendant(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
        location: GeoLocation? = null,
    ): String = vedic.getApproximateAscendant(birthDate, birthTime, zoneOffset, location)

    /** Tropical (Western) ascendant sign name. Mirrors `BirthChart.tropicalAscendant`. */
    fun getTropicalAscendantSign(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
        location: GeoLocation? = null,
    ): String = vedic.getTropicalAscendantSign(birthDate, birthTime, zoneOffset, location)

    // ---------------------------------------------------------------------------
    // Planets — delegates to PlanetaryCalculator
    // ---------------------------------------------------------------------------

    /** Raw geocentric tropical ecliptic longitudes for the classical planets. */
    fun getPlanetLongitudes(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): List<Pair<String, Double>> = planetary.getPlanetLongitudes(birthDate, birthTime, zoneOffset)

    /** Planet positions summary — geocentric tropical zodiac sign for each planet. */
    fun getPlanetPositions(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): List<Pair<String, String>> = planetary.getPlanetPositions(birthDate, birthTime, zoneOffset)

    // ---------------------------------------------------------------------------
    // Chinese Zodiac — delegates to ChineseZodiacCalculator
    // ---------------------------------------------------------------------------

    /** Returns the Chinese zodiac animal for [date], respecting the CNY cutoff. */
    fun getChineseZodiac(date: LocalDate): String = chinese.getZodiac(date)

    /** Returns the Chinese calendar year for [date], shifting back by one before CNY. */
    fun getChineseYear(date: LocalDate): Int = chinese.getChineseYear(date)

    /** Returns the full Chinese stem-branch with element, e.g. "Jia-Chen / Wood-Dragon". */
    fun getChineseStemBranch(date: LocalDate): String = chinese.getStemBranch(date)
}
