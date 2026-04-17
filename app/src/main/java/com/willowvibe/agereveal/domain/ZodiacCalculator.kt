package com.willowvibe.agereveal.domain

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Computes Western (tropical) zodiac, Vedic Rashi (sidereal Sun sign), and Chinese zodiac.
 *
 * Western zodiac uses the standard tropical date cutoffs.
 * Rashi is computed from the Sun's sidereal ecliptic longitude with Lahiri ayanamsa
 * applied — not a Western→Vedic name swap.
 */
@Singleton
class ZodiacCalculator @Inject constructor(
    private val astronomy: AstronomicalCalculator,
) {

    fun getWesternZodiac(month: Int, day: Int): String = when {
        (month == 3 && day >= 21) || (month == 4 && day <= 19)   -> "Aries ♈"
        (month == 4 && day >= 20) || (month == 5 && day <= 20)   -> "Taurus ♉"
        (month == 5 && day >= 21) || (month == 6 && day <= 20)   -> "Gemini ♊"
        (month == 6 && day >= 21) || (month == 7 && day <= 22)   -> "Cancer ♋"
        (month == 7 && day >= 23) || (month == 8 && day <= 22)   -> "Leo ♌"
        (month == 8 && day >= 23) || (month == 9 && day <= 22)   -> "Virgo ♍"
        (month == 9 && day >= 23) || (month == 10 && day <= 22)  -> "Libra ♎"
        (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Scorpio ♏"
        (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Sagittarius ♐"
        (month == 12 && day >= 22) || (month == 1 && day <= 19)  -> "Capricorn ♑"
        (month == 1 && day >= 20) || (month == 2 && day <= 18)   -> "Aquarius ♒"
        else                                                     -> "Pisces ♓"
    }

    /** Vedic Rashi derived from the Sun's sidereal ecliptic longitude (12 × 30° signs). */
    fun getRashi(birthDate: LocalDate): String {
        val longitude = astronomy.siderealSunLongitude(birthDate)
        val index = ((longitude / 30.0).toInt() % 12 + 12) % 12
        return rashiOrder[index]
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

    // ---------------------------------------------------------------------------
    // Chinese Zodiac (12-year cycle, anchored to 1900 = Rat)
    // ---------------------------------------------------------------------------

    private val chineseZodiacCycle = listOf(
        "🐀 Rat", "🐂 Ox", "🐯 Tiger", "🐇 Rabbit",
        "🐉 Dragon", "🐍 Snake", "🐴 Horse", "🐏 Goat",
        "🐒 Monkey", "🐓 Rooster", "🐕 Dog", "🐖 Pig",
    )

    fun getChineseZodiac(year: Int): String {
        val index = ((year - 1900) % 12 + 12) % 12
        return chineseZodiacCycle[index]
    }
}
