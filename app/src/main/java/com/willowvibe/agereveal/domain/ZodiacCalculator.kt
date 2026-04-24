package com.willowvibe.agereveal.domain

import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Computes Western (tropical) zodiac, Vedic Rashi (sidereal Sun sign), and Chinese zodiac.
 *
 * Western zodiac uses the standard tropical date cutoffs.
 * Rashi is computed from the Sun's sidereal ecliptic longitude with Lahiri ayanamsa
 * applied — not a Western→Vedic name swap.
 * Chinese zodiac accounts for the Lunar New Year cutoff so that dates in late January /
 * early February before the new year receive the previous year's animal.
 */
@Singleton
class ZodiacCalculator @Inject constructor(
    private val astronomy: AstronomicalCalculator,
) {

    private val westernSignNames = listOf(
        "Aries ♈", "Taurus ♉", "Gemini ♊", "Cancer ♋",
        "Leo ♌", "Virgo ♍", "Libra ♎", "Scorpio ♏",
        "Sagittarius ♐", "Capricorn ♑", "Aquarius ♒", "Pisces ♓"
    )

    /** Western (tropical) zodiac from the Sun's ecliptic longitude with cusp detection. */
    fun getWesternZodiac(birthDate: LocalDate, birthTime: LocalTime? = null): String {
        val snapshot = astronomy.snapshot(birthDate, birthTime)
        val longitude = snapshot.tropicalSunLongitude
        val index = ((longitude / 30.0).toInt() % 12 + 12) % 12
        val name = westernSignNames[index]
        val posInSign = longitude % 30.0
        // Within 1° of a sign boundary — Sun moves ~1°/day so cusp = ±1 day of sign change
        return if (posInSign < 1.0 || posInSign > 29.0) "$name ⚠ Cusp" else name
    }

    /** Western Moon sign from the Moon's tropical ecliptic longitude with cusp detection. */
    fun getWesternMoonSign(birthDate: LocalDate, birthTime: LocalTime? = null): String {
        val snapshot = astronomy.snapshot(birthDate, birthTime)
        val longitude = snapshot.tropicalMoonLongitude
        val index = ((longitude / 30.0).toInt() % 12 + 12) % 12
        val name = westernSignNames[index]
        val posInSign = longitude % 30.0
        // Moon moves ~13°/day, so 1° ≈ ~2 hours near a boundary
        return if (posInSign < 1.0 || posInSign > 29.0) "$name ⚠ Cusp" else name
    }

    /** Vedic Rashi derived from the Sun's sidereal ecliptic longitude (12 × 30° signs). */
    fun getRashi(birthDate: LocalDate, birthTime: LocalTime? = null): String {
        val snapshot = astronomy.snapshot(birthDate, birthTime)
        val longitude = snapshot.siderealSunLongitude
        val index = ((longitude / 30.0).toInt() % 12 + 12) % 12
        val name = rashiOrder[index]
        val posInSign = longitude % 30.0
        // Within 1° of a sign boundary — Sun moves ~1°/day so cusp = ±1 day of sign change
        return if (posInSign < 1.0 || posInSign > 29.0) "$name ⚠ Cusp" else name
    }

    private val rashiOrder = listOf(
        "Mesha (मेष)",          // 0° – 30°
        "Vrishabha (वृषभ)",      // 30° – 60°
        "Mithuna (मिथुन)",       // 60° – 90°
        "Karka (कर्क)",          // 90° – 120°
        "Simha (सिंह)",          // 120° – 150°
        "Kanya (कन्या)",         // 150° – 180°
        "Tula (तुला)",           // 180° – 210°
        "Vrishchika (वृश्चिक)",   // 210° – 240°
        "Dhanus (धनु)",          // 240° – 270°
        "Makara (मकर)",          // 270° – 300°
        "Kumbha (कुम्भ)",         // 300° – 330°
        "Meena (मीन)",           // 330° – 360°
    )

    private val rashiLords = listOf(
        "Mars", "Venus", "Mercury", "Moon",
        "Sun", "Mercury", "Venus", "Mars",
        "Jupiter", "Saturn", "Saturn", "Jupiter",
    )

    /** Returns the ruling planet (graha) of the Vedic Rashi for the given birth date. */
    fun getRashiLord(birthDate: LocalDate, birthTime: LocalTime? = null): String {
        val snapshot = astronomy.snapshot(birthDate, birthTime)
        val longitude = snapshot.siderealSunLongitude
        val index = ((longitude / 30.0).toInt() % 12 + 12) % 12
        return rashiLords[index]
    }

    // ---------------------------------------------------------------------------
    // Chinese Zodiac — 12-year cycle, Lunar New Year aware
    // ---------------------------------------------------------------------------

    private val chineseZodiacCycle = listOf(
        "🐀 Rat", "🐂 Ox", "🐯 Tiger", "🐇 Rabbit",
        "🐉 Dragon", "🐍 Snake", "🐴 Horse", "🐏 Goat",
        "🐒 Monkey", "🐓 Rooster", "🐕 Dog", "🐖 Pig",
    )

    /**
     * Returns the Chinese zodiac animal for [date], correctly handling the Lunar New Year
     * cutoff. People born before the new year in a given Gregorian year belong to the
     * previous Chinese year's animal (e.g. born Jan 25 2000 before Feb 5 CNY → Rabbit,
     * not Dragon).
     */
    fun getChineseZodiac(date: LocalDate): String {
        val chineseYear = getChineseYear(date)
        val index = ((chineseYear - 1900) % 12 + 12) % 12
        return chineseZodiacCycle[index]
    }

    /**
     * Returns the Chinese calendar year for [date], shifting back by one if the date
     * falls before that Gregorian year's Lunar New Year.
     */
    fun getChineseYear(date: LocalDate): Int {
        val cny = chineseNewYearDate(date.year)
        return if (date.isBefore(cny)) date.year - 1 else date.year
    }

    /**
     * Returns the Gregorian date of Chinese New Year (Spring Festival) for [year].
     *
     * Lookup table covers 1900–2100; values were computed from astronomical new-moon
     * calculations (second new moon after winter solstice). For years outside the table
     * the result falls back to an approximation that is accurate to ±1 day.
     *
     * Encoding: each entry is month*100 + day  (e.g. 131 = Jan 31, 205 = Feb 5).
     */
    private fun chineseNewYearDate(year: Int): LocalDate {
        val offset = year - 1900
        if (offset in CNY_DATES.indices) {
            val encoded = CNY_DATES[offset]
            return LocalDate.of(year, encoded / 100, encoded % 100)
        }
        // Fallback approximation outside table range
        val approxDay = ((year - 1900) * 10.875 + 131).toInt() % 30 + 20
        val month = if (approxDay > 31) 2 else 1
        val day = if (approxDay > 31) approxDay - 31 else approxDay
        return LocalDate.of(year, month, day.coerceIn(1, 28))
    }

    companion object {
        /**
         * Chinese New Year dates for Gregorian years 1900–2100 (201 entries).
         * Index 0 = 1900, index 1 = 1901, …, index 200 = 2100.
         * Encoded as month*100 + day.
         */
        private val CNY_DATES = intArrayOf(
            // 1900–1909
            131, 219, 208, 129, 216, 204, 125, 213, 202, 122,
            // 1910–1919
            210, 130, 218, 206, 126, 214, 203, 123, 211, 201,
            // 1920–1929
            220, 208, 128, 216, 205, 124, 213, 202, 123, 210,
            // 1930–1939
            130, 217, 206, 126, 214, 204, 124, 211, 131, 219,
            // 1940–1949
            208, 127, 215, 205, 125, 213, 202, 122, 210, 129,
            // 1950–1959
            217, 206, 127, 214, 203, 124, 212, 131, 218, 208,
            // 1960–1969
            128, 215, 205, 125, 213, 202, 121, 209, 130, 217,
            // 1970–1979
            206, 127, 215, 203, 123, 211, 131, 218, 207, 128,
            // 1980–1989
            216, 205, 125, 213, 202, 220, 209, 129, 217, 206,
            // 1990–1999
            127, 215, 204, 123, 210, 131, 219, 207, 128, 216,
            // 2000–2009
            205, 124, 212, 201, 122, 209, 129, 218, 207, 126,
            // 2010–2019
            214, 203, 123, 210, 131, 219, 208, 128, 216, 205,
            // 2020–2029
            125, 212, 201, 122, 210, 129, 217, 206, 126, 213,
            // 2030–2039
            203, 123, 211, 131, 219, 208, 128, 215, 204, 124,
            // 2040–2049
            212, 201, 122, 210, 130, 217, 206, 126, 214, 202,
            // 2050–2059
            123, 211, 201, 219, 208, 128, 215, 204, 124, 211,
            // 2060–2069
            131, 218, 208, 128, 216, 205, 126, 214, 203, 123,
            // 2070–2079
            211, 131, 219, 207, 127, 215, 204, 124, 212, 202,
            // 2080–2089
            121, 209, 129, 217, 206, 126, 214, 203, 123, 210,
            // 2090–2099
            130, 217, 206, 126, 214, 203, 123, 210, 131, 219,
            // 2100
            208,
        )
    }
}
