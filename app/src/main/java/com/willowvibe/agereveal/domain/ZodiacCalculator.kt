package com.willowvibe.agereveal.domain

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Computes Western zodiac, Vedic Rashi, and Chinese zodiac from a birth date.
 * All logic is lookup-table based — no external libraries needed.
 * Based on date logic defined in the build plan.
 */
@Singleton
class ZodiacCalculator @Inject constructor() {

    // ---------------------------------------------------------------------------
    // Western Zodiac (sun sign by month + day)
    // ---------------------------------------------------------------------------

    fun getWesternZodiac(month: Int, day: Int): String = when {
        (month == 3 && day >= 21) || (month == 4 && day <= 19)  -> "Aries ♈"
        (month == 4 && day >= 20) || (month == 5 && day <= 20)  -> "Taurus ♉"
        (month == 5 && day >= 21) || (month == 6 && day <= 20)  -> "Gemini ♊"
        (month == 6 && day >= 21) || (month == 7 && day <= 22)  -> "Cancer ♋"
        (month == 7 && day >= 23) || (month == 8 && day <= 22)  -> "Leo ♌"
        (month == 8 && day >= 23) || (month == 9 && day <= 22)  -> "Virgo ♍"
        (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Libra ♎"
        (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Scorpio ♏"
        (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Sagittarius ♐"
        (month == 12 && day >= 22) || (month == 1 && day <= 19)  -> "Capricorn ♑"
        (month == 1 && day >= 20) || (month == 2 && day <= 18)  -> "Aquarius ♒"
        else                                                      -> "Pisces ♓"
    }

    // ---------------------------------------------------------------------------
    // Vedic Rashi (approximate — mirrors Western sun sign in name but uses sidereal)
    // For a production-grade app, use a proper sidereal calculation (ayanamsa offset ~23°).
    // This table gives a close approximation suitable for a casual app.
    // ---------------------------------------------------------------------------

    private val rashiByZodiac: Map<String, String> = mapOf(
        "Aries ♈"       to "Mesha (मेष)",
        "Taurus ♉"      to "Vrishabha (वृषभ)",
        "Gemini ♊"      to "Mithuna (मिथुन)",
        "Cancer ♋"      to "Karka (कर्क)",
        "Leo ♌"         to "Simha (सिंह)",
        "Virgo ♍"       to "Kanya (कन्या)",
        "Libra ♎"       to "Tula (तुला)",
        "Scorpio ♏"     to "Vrishchika (वृश्चिक)",
        "Sagittarius ♐" to "Dhanus (धनु)",
        "Capricorn ♑"   to "Makara (मकर)",
        "Aquarius ♒"    to "Kumbha (कुम्भ)",
        "Pisces ♓"      to "Meena (मीन)",
    )

    fun getRashi(month: Int, day: Int): String {
        val western = getWesternZodiac(month, day)
        return rashiByZodiac[western] ?: "Unknown"
    }

    // ---------------------------------------------------------------------------
    // Chinese Zodiac (12-year cycle, anchored to 1900 = Rat)
    // ---------------------------------------------------------------------------

    private val chineseZodiacCycle = listOf(
        "🐀 Rat", "🐂 Ox", "🐯 Tiger", "🐇 Rabbit",
        "🐉 Dragon", "🐍 Snake", "🐴 Horse", "🐏 Goat",
        "🐒 Monkey", "🐓 Rooster", "🐕 Dog", "🐖 Pig",
    )

    fun getChineseZodiac(year: Int): String {
        val index = ((year - 1900) % 12 + 12) % 12   // handles years before 1900 gracefully
        return chineseZodiacCycle[index]
    }
}
