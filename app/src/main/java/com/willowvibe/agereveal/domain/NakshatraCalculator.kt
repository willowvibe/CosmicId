package com.willowvibe.agereveal.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Approximate nakshatra (birth star) calculator.
 *
 * The 27 nakshatras divide the 360° ecliptic into 13°20' segments.
 * A precise calculation requires the moon's sidereal longitude at the time of birth,
 * which needs time-of-birth and ephemeris data.
 *
 * This implementation uses a calendar-day approximation based on the birth date only,
 * which is accurate enough for a casual curiosity app. For production precision, integrate
 * the Swiss Ephemeris library or a backend ephemeris API.
 */
@Singleton
class NakshatraCalculator @Inject constructor() {

    // Each nakshatra spans approximately 13.33 days of the lunar month.
    // We anchor to a known nakshatra start date and cycle through 27.
    // Anchor: 2000-01-06 ≈ Ashwini (nakshatra 0) — rough alignment.
    private val anchorDate: LocalDate = LocalDate.of(2000, 1, 6)
    private val nakshatrasPerCycle = 27
    private val daysPerNakshatra = 27.32 / nakshatrasPerCycle // ≈ 1.012 days per nakshatra segment

    private val nakshatraNames = listOf(
        "Ashwini (अश्विनी)",     "Bharani (भरणी)",      "Krittika (कृत्तिका)",
        "Rohini (रोहिणी)",       "Mrigashira (मृगशिरा)", "Ardra (आर्द्रा)",
        "Punarvasu (पुनर्वसु)",  "Pushya (पुष्य)",       "Ashlesha (आश्लेषा)",
        "Magha (मघा)",           "Purva Phalguni (पूर्व फाल्गुनी)", "Uttara Phalguni (उत्तर फाल्गुनी)",
        "Hasta (हस्त)",          "Chitra (चित्रा)",      "Swati (स्वाति)",
        "Vishakha (विशाखा)",     "Anuradha (अनुराधा)",   "Jyeshtha (ज्येष्ठा)",
        "Moola (मूला)",          "Purva Ashadha (पूर्वाषाढ़a)", "Uttara Ashadha (उत्तराषाढ़a)",
        "Shravana (श्रवण)",      "Dhanishtha (धनिष्ठा)", "Shatabhisha (शतभिषा)",
        "Purva Bhadrapada (पूर्व भाद्रपद)", "Uttara Bhadrapada (उत्तर भाद्रपद)", "Revati (रेवती)",
    )

    fun getNakshatra(birthDate: LocalDate): String {
        val daysSinceAnchor = ChronoUnit.DAYS.between(anchorDate, birthDate)
        val lunarCycleDays = 29.53
        val positionInCycle = ((daysSinceAnchor % lunarCycleDays) + lunarCycleDays) % lunarCycleDays
        val index = (positionInCycle / (lunarCycleDays / nakshatrasPerCycle)).toInt() % nakshatrasPerCycle
        return nakshatraNames[index]
    }
}
