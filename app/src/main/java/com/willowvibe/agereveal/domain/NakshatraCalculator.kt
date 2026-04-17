package com.willowvibe.agereveal.domain

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Nakshatra (lunar mansion) derived from the Moon's sidereal ecliptic longitude.
 *
 * The 27 nakshatras divide 360° of ecliptic into equal 13°20' segments (Lahiri system).
 * Without a birth time, the Moon position is computed at noon UT of the birth date,
 * which can be off by up to half a nakshatra either way (the Moon moves ~13°/day).
 * For exact Nakshatra, birth time + location are required.
 */
@Singleton
class NakshatraCalculator @Inject constructor(
    private val astronomy: AstronomicalCalculator,
) {

    private val nakshatraArc = 360.0 / 27.0  // 13°20'

    private val nakshatraNames = listOf(
        "Ashwini (अश्विनी)",     "Bharani (भरणी)",      "Krittika (कृत्तिका)",
        "Rohini (रोहिणी)",        "Mrigashira (मृगशिरा)", "Ardra (आर्द्रा)",
        "Punarvasu (पुनर्वसु)",   "Pushya (पुष्य)",       "Ashlesha (आश्लेषा)",
        "Magha (मघा)",           "Purva Phalguni (पूर्व फाल्गुनी)", "Uttara Phalguni (उत्तर फाल्गुनी)",
        "Hasta (हस्त)",          "Chitra (चित्रा)",      "Swati (स्वाति)",
        "Vishakha (विशाखा)",     "Anuradha (अनुराधा)",   "Jyeshtha (ज्येष्ठा)",
        "Moola (मूला)",          "Purva Ashadha (पूर्वाषाढ़ा)", "Uttara Ashadha (उत्तराषाढ़ा)",
        "Shravana (श्रवण)",       "Dhanishtha (धनिष्ठा)", "Shatabhisha (शतभिषा)",
        "Purva Bhadrapada (पूर्व भाद्रपद)", "Uttara Bhadrapada (उत्तर भाद्रपद)", "Revati (रेवती)",
    )

    fun getNakshatra(birthDate: LocalDate): String {
        val longitude = astronomy.siderealMoonLongitude(birthDate)
        val index = ((longitude / nakshatraArc).toInt() % 27 + 27) % 27
        return nakshatraNames[index]
    }
}
