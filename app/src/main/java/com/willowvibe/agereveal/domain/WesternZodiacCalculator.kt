package com.willowvibe.agereveal.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Western (tropical) zodiac calculations.
 *
 * Owns the 12-sign name list and computes tropical zodiac signs (Sun and Moon)
 * from the Sun/Moon's ecliptic longitude with cusp detection at the ±1°
 * boundaries.
 *
 * See Meeus *Astronomical Algorithms* Ch. 12 (coordinate transforms) for the
 * underlying positions; sign-index math is trivial `floor(longitude / 30)`.
 */
@Singleton
class WesternZodiacCalculator @Inject constructor(
    private val astronomy: AstronomicalCalculator,
) {

    val signNames: List<String> = listOf(
        "Aries ♈", "Taurus ♉", "Gemini ♊", "Cancer ♋",
        "Leo ♌", "Virgo ♍", "Libra ♎", "Scorpio ♏",
        "Sagittarius ♐", "Capricorn ♑", "Aquarius ♒", "Pisces ♓",
    )

    /** Western (tropical) sign index (0=Aries, 11=Pisces) from the Sun's ecliptic longitude. */
    fun getSignIndex(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): Int {
        val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)
        return ((snapshot.tropicalSunLongitude / 30.0).toInt() % 12 + 12) % 12
    }

    /** Get just the sign name (without emoji) for display. */
    fun getSignName(index: Int): String = signNames[index].split(" ").first()

    /**
     * Western (tropical) zodiac from the Sun's ecliptic longitude with cusp
     * detection (⚠) when the Sun is within 1° of a sign boundary.
     */
    fun getZodiac(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String {
        val index = getSignIndex(birthDate, birthTime, zoneOffset)
        val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)
        val posInSign = snapshot.tropicalSunLongitude % 30.0
        val name = signNames[index]
        // Sun moves ~1°/day, so cusp = ±1 day of sign change
        return if (posInSign < 1.0 || posInSign > 29.0) "$name ⚠ Cusp" else name
    }

    /**
     * Western Moon sign from the Moon's tropical ecliptic longitude, with cusp
     * detection. The Moon moves ~13°/day, so a 1° cusp window is ~2 hours.
     */
    fun getMoonSign(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String {
        val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)
        val longitude = snapshot.tropicalMoonLongitude
        val index = ((longitude / 30.0).toInt() % 12 + 12) % 12
        val name = signNames[index]
        val posInSign = longitude % 30.0
        return if (posInSign < 1.0 || posInSign > 29.0) "$name ⚠ Cusp" else name
    }
}
