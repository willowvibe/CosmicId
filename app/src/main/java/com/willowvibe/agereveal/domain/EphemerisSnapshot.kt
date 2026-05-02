package com.willowvibe.agereveal.domain

/**
 * Immutable snapshot of ephemeris values for a single birth date-time.
 * Computing Julian Day and trigonometric series once avoids redundant work
 * when multiple astrology fields are derived from the same moment.
 */
data class EphemerisSnapshot(
    val jd: Double,
    val tropicalSunLongitude: Double,
    val siderealSunLongitude: Double,
    val tropicalMoonLongitude: Double,
    val siderealMoonLongitude: Double,
    val ayanamsa: Double,
    val tithi: Int = 0,
)
