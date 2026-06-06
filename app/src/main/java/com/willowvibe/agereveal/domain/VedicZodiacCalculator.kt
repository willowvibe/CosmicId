package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.data.model.GeoLocation
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vedic (sidereal) zodiac calculations — Rashi, Rashi Lord, Tithi, and
 * approximate / exact Ascendant (Lagna).
 *
 * Rashi and Tithi derive from the sidereal Sun/Moon longitudes (Lahiri
 * ayanamsa is applied by [AstronomicalCalculator]). Lagna uses the observer
 * latitude + longitude when [location] is provided; otherwise falls back to
 * the equatorial approximation (which is off by 1–2 signs for mid-latitude
 * users — the UI labels such results as *Approximate*).
 */
@Singleton
class VedicZodiacCalculator @Inject constructor(
    private val astronomy: AstronomicalCalculator,
) {

    /** 12 Vedic Rashis in sidereal order, with Devanagari + emoji-free display names. */
    val rashiNames: List<String> = listOf(
        "Mesha (मेष)",
        "Vrishabha (वृषभ)",
        "Mithuna (मिथुन)",
        "Karka (कर्क)",
        "Simha (सिंह)",
        "Kanya (कन्या)",
        "Tula (तुला)",
        "Vrishchika (वृश्चिक)",
        "Dhanus (धनु)",
        "Makara (मकर)",
        "Kumbha (कुम्भ)",
        "Meena (मीन)",
    )

    /** Traditional graha (ruling planet) of each Rashi. */
    val rashiLords: List<String> = listOf(
        "Mars", "Venus", "Mercury", "Moon",
        "Sun", "Mercury", "Venus", "Mars",
        "Jupiter", "Saturn", "Saturn", "Jupiter",
    )

    /** Names of the 30 Tithis (1–15 Shukla Paksha, 16–30 Krishna Paksha). */
    val tithiNames: List<String> = listOf(
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima",
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Amavasya",
    )

    /** Vedic Rashi derived from the Sun's sidereal ecliptic longitude. */
    fun getRashi(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String {
        val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)
        val longitude = snapshot.siderealSunLongitude
        val index = ((longitude / 30.0).toInt() % 12 + 12) % 12
        val name = rashiNames[index]
        val posInSign = longitude % 30.0
        return if (posInSign < 1.0 || posInSign > 29.0) "$name ⚠ Cusp" else name
    }

    /** Returns the ruling planet (graha) of the Vedic Rashi for the given birth date. */
    fun getRashiLord(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String {
        val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)
        val longitude = snapshot.siderealSunLongitude
        val index = ((longitude / 30.0).toInt() % 12 + 12) % 12
        return rashiLords[index]
    }

    /**
     * Tithi (lunar day, 1–30) for the given birth date-time. 1–15 = Shukla
     * Paksha (waxing), 16–30 = Krishna Paksha (waning). Uses tropical
     * Moon–Sun elongation; tithi is a geometric concept independent of
     * ayanamsa.
     */
    fun getTithi(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String {
        val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)
        val index = (snapshot.tithi - 1).coerceIn(0, 29)
        val name = tithiNames[index]
        val paksha = if (snapshot.tithi <= 15) "Shukla" else "Krishna"
        return "$name ($paksha Paksha)"
    }

    /**
     * Vedic Lagna (Ascendant) — exact when [location] is provided, approximate
     * otherwise. Without a location, uses Greenwich sidereal time at 0°
     * latitude (equatorial ascendant) which can be off by 1-2 signs for
     * mid-latitude users. With a location, computes the true ecliptic
     * ascendant using observer latitude + longitude and Local Sidereal Time.
     */
    fun getApproximateAscendant(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
        location: GeoLocation? = null,
    ): String {
        val localDateTime = birthTime?.let { bt -> birthDate.atTime(bt) } ?: birthDate.atTime(12, 0)
        val utDateTime = zoneOffset?.let {
            localDateTime.atOffset(it).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
        } ?: localDateTime
        val jd = astronomy.julianDay(utDateTime)

        val tropicalAsc = if (location != null) {
            astronomy.exactAscendantLongitude(jd, location.latitude, location.longitude)
        } else {
            astronomy.approximateAscendantLongitude(jd)
        }
        val siderealAsc = norm360(tropicalAsc - astronomy.lahiriAyanamsa(jd))
        val index = ((siderealAsc / 30.0).toInt() % 12 + 12) % 12
        val name = rashiNames[index]
        val posInSign = siderealAsc % 30.0
        return if (posInSign < 1.0 || posInSign > 29.0) "$name ⚠ Cusp" else name
    }

    private fun norm360(x: Double): Double = ((x % 360.0) + 360.0) % 360.0
}
