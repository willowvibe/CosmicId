package com.willowvibe.agereveal.domain

/**
 * Immutable snapshot of ephemeris values for a single birth date-time.
 * Computing Julian Day and trigonometric series once avoids redundant work
 * when multiple astrology fields are derived from the same moment.
 *
 * @property jd Julian Day Number of the birth moment (UT).
 * @property tropicalSunLongitude Apparent (nutation + aberration corrected) Sun longitude
 *           in degrees, [0, 360).
 * @property siderealSunLongitude Tropical minus Lahiri ayanamsa.
 * @property tropicalMoonLongitude Moon's apparent ecliptic longitude (Meeus Ch. 47).
 * @property siderealMoonLongitude Moon's sidereal longitude.
 * @property ayanamsa Lahiri (Chitrapaksha) ayanamsa in degrees.
 * @property tithi Lunar day 1–30 derived from Moon–Sun elongation.
 * @property nutationLongitude ΔΨ in arcseconds (IAU 2000B).
 * @property nutationObliquity Δε in arcseconds (IAU 2000B).
 * @property trueObliquity True obliquity of the ecliptic ε = ε0 + Δε in degrees.
 */
data class EphemerisSnapshot(
    val jd: Double,
    val tropicalSunLongitude: Double,
    val siderealSunLongitude: Double,
    val tropicalMoonLongitude: Double,
    val siderealMoonLongitude: Double,
    val ayanamsa: Double,
    val tithi: Int = 0,
    val nutationLongitude: Double = 0.0,
    val nutationObliquity: Double = 0.0,
    val trueObliquity: Double = 23.4397,
)
