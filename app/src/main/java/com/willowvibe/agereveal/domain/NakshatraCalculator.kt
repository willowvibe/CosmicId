package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
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
    private val metadata: NakshatraMetadata,
) {

    private val nakshatraArc = 360.0 / 27.0  // 13°20'

    fun getNakshatra(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String {
        val details = getNakshatraDetails(birthDate, birthTime, zoneOffset)
        val posInNakshatra = details.positionInNakshatra
        // Within 1° of a nakshatra boundary — Moon moves ~13°/day, so 1° ≈ 2 hours of travel
        return if (posInNakshatra < 1.0 || posInNakshatra > (nakshatraArc - 1.0)) {
            "${details.name} ⚠ Cusp"
        } else {
            details.name
        }
    }

    /** Returns the nakshatra with its pada (quarter), e.g. "Rohini — 2nd Pada". */
    fun getNakshatraWithPada(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): String {
        val details = getNakshatraDetails(birthDate, birthTime, zoneOffset)
        return "${details.name} — ${details.padaName()}"
    }

    /**
     * Full metadata for the Moon's current nakshatra — used by DetailsUnlockScreen
     * and by the Dasha calculator (which needs [NakshatraData.lord] to seed Vimshottari).
     */
    fun getNakshatraDetails(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): NakshatraDetails {
        val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)
        val longitude = snapshot.siderealMoonLongitude
        val nakshatraIndex = ((longitude / nakshatraArc).toInt() % 27 + 27) % 27
        val posInNakshatra = longitude % nakshatraArc
        val padaIndex = (posInNakshatra / (nakshatraArc / 4.0)).toInt().coerceIn(0, 3)
        val data = metadata.forIndex(nakshatraIndex)
        return NakshatraDetails(
            data = data,
            padaIndex = padaIndex,
            positionInNakshatra = posInNakshatra,
            siderealMoonLongitude = longitude,
        )
    }
}

/**
 * Result of resolving a Moon's position to its nakshatra — data + calculated padas
 * + the raw longitude and position within the mansion. The [positionInNakshatra]
 * is exposed so the UI can draw a progress bar.
 */
data class NakshatraDetails(
    val data: NakshatraData,
    val padaIndex: Int,
    val positionInNakshatra: Double,
    val siderealMoonLongitude: Double,
) {
    val name: String get() = data.name
    val lord: CelestialBody get() = data.lord

    /** Display name of the current pada (quarter of the nakshatra). */
    fun padaName(): String = when (padaIndex) {
        0 -> "1st Pada"
        1 -> "2nd Pada"
        2 -> "3rd Pada"
        else -> "4th Pada"
    }
}
