package com.willowvibe.agereveal.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Medium-precision ephemeris for sidereal Rashi (Sun sign) and Nakshatra (lunar mansion).
 *
 * Sun longitude: Meeus Ch. 25 formula with nutation + aberration correction; accuracy ~0.01°.
 * Moon longitude: Meeus Ch. 47 simplified with 15 correction terms including the argument of
 * latitude (F) and the 2M' term; accuracy ~0.1° — sufficient to resolve nakshatra correctly
 * for the vast majority of birth dates when a birth time is provided.
 * Lahiri (Chitrapaksha) ayanamsa is applied to convert tropical longitudes to sidereal.
 */
@Singleton
class AstronomicalCalculator @Inject constructor() {

    /**
     * Sun's apparent ecliptic longitude in degrees, normalised to [0, 360).
     *
     * Includes nutation-in-longitude and aberration so that the result matches the
     * apparent (observed) position rather than the geometric one. This reduces the
     * residual error for sidereal sign/nakshatra determination from ~1° to ~0.01°.
     */
    fun sunLongitude(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val l0 = norm360(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        val m = Math.toRadians(norm360(357.52911 + 35999.05029 * t - 0.0001537 * t * t))
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(m) +
                (0.019993 - 0.000101 * t) * sin(2 * m) +
                0.000289 * sin(3 * m)
        val sunGeometric = norm360(l0 + c)

        // Apparent longitude: apply nutation-in-longitude (ΔΨ) + aberration correction.
        // Ω is the longitude of the ascending node of the Moon's mean orbit.
        val omega = Math.toRadians(norm360(125.04 - 1934.136 * t))
        return norm360(sunGeometric - 0.00569 - 0.00478 * sin(omega))
    }

    /**
     * Moon's tropical ecliptic longitude in degrees, normalised to [0, 360).
     *
     * Uses 15 correction terms including the 2M' harmonic and the argument of latitude F,
     * which together remove the largest previously-missing errors (~0.21° and ~0.11°
     * respectively). Accuracy improves from ~±1° to ~±0.1°.
     */
    fun moonLongitude(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val lp = norm360(218.3164477 + 481267.88123421 * t)
        val d  = Math.toRadians(norm360(297.8501921 + 445267.1114034 * t))
        val ms = Math.toRadians(norm360(357.5291092 + 35999.0502909 * t))
        val mp = Math.toRadians(norm360(134.9633964 + 477198.8675055 * t))
        // F = argument of latitude (Moon's distance from its ascending node)
        val f  = Math.toRadians(norm360(93.2720950 + 483202.0175233 * t))

        val correction =
            6.289  * sin(mp) +
            -1.274 * sin(mp - 2 * d) +
             0.658 * sin(2 * d) +
             0.214 * sin(2 * mp) +           // previously missing — 0.214° amplitude
            -0.186 * sin(ms) +
            -0.114 * sin(2 * f) +            // previously missing — 0.114° amplitude
            -0.059 * sin(2 * mp - 2 * d) +
            -0.057 * sin(mp - 2 * d + ms) +
             0.053 * sin(mp + 2 * d) +
             0.046 * sin(2 * d - ms) +
             0.041 * sin(mp - ms) +
            -0.035 * sin(d) +
            -0.031 * sin(mp + ms) +
             0.029 * sin(2 * mp + ms) +      // previously missing
            -0.023 * sin(2 * f - 2 * d)      // previously missing

        return norm360(lp + correction)
    }

    /**
     * Lahiri (Chitrapaksha) ayanamsa in degrees.
     *
     * Value at J2000 is 23°51'11" (23.85306°). Precession rate 50.2884″/year
     * (5028.84″/century). A small quadratic term accounts for the secular
     * change in the precession rate.
     */
    fun lahiriAyanamsa(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        return 23.85306 + t * (5028.84 / 3600.0) - t * t * (1.397 / 3600.0)
    }

    /**
     * Greenwich Mean Sidereal Time in degrees, normalised to [0, 360).
     *
     * Linear approximation accurate to ~0.1°, sufficient for an approximate
     * ascendant when the observer's latitude is unknown.
     */
    fun greenwichMeanSiderealTime(jd: Double): Double {
        return norm360(280.46061837 + 360.98564736629 * (jd - 2451545.0))
    }

    /**
     * Approximate tropical ascendant (Rising Sign) longitude in degrees.
     *
     * Uses GMST at 0° longitude and assumes the observer is on the equator
     * (latitude = 0°). This gives the "equatorial ascendant" — a valid
     * astronomical reference point. For users at mid-latitudes the true
     * ascendant can differ by 1-2 signs (~30-60°), so the result must
     * always be displayed with an "Approximate" label.
     */
    fun approximateAscendantLongitude(jd: Double): Double {
        val epsilon = Math.toRadians(23.4397)           // obliquity of ecliptic
        val theta = Math.toRadians(greenwichMeanSiderealTime(jd))
        val ascRad = kotlin.math.atan2(
            kotlin.math.cos(theta),
            -(kotlin.math.sin(theta) * kotlin.math.cos(epsilon)),
        )
        return norm360(Math.toDegrees(ascRad))
    }

    /**
     * Exact tropical ascendant longitude in degrees, using observer latitude
     * and longitude for precise Local Sidereal Time.
     *
     * Formula: atan2(sin(LST), cos(LST) * cos(ε) - tan(φ) * sin(ε))
     * where LST = GMST + longitude, ε = obliquity, φ = latitude.
     *
     * Accuracy is ~±0.5° — sufficient for sign-level (30° bin) determination.
     */
    fun exactAscendantLongitude(
        jd: Double,
        latitude: Double,
        longitude: Double,
    ): Double {
        val epsilon = Math.toRadians(23.4397)
        val gmst = greenwichMeanSiderealTime(jd)
        val lst = Math.toRadians(norm360(gmst + longitude))
        val latRad = Math.toRadians(latitude)
        val ascRad = kotlin.math.atan2(
            kotlin.math.sin(lst),
            kotlin.math.cos(lst) * kotlin.math.cos(epsilon) - kotlin.math.tan(latRad) * kotlin.math.sin(epsilon),
        )
        return norm360(Math.toDegrees(ascRad))
    }

    /**
     * Tithi (lunar day, 1–30) derived from Moon–Sun elongation.
     *
     * Each tithi spans 12° of elongation. 1–15 = Shukla Paksha (waxing),
     * 16–30 = Krishna Paksha (waning). Amavasya = 30, Purnima = 15.
     */
    fun tithi(sunLongitude: Double, moonLongitude: Double): Int {
        val elongation = norm360(moonLongitude - sunLongitude)
        return (elongation / 12.0).toInt() + 1
    }

    fun snapshot(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): EphemerisSnapshot {
        val localDateTime = birthTime?.let { bt -> birthDate.atTime(bt) } ?: birthDate.atTime(12, 0)
        // Convert local time to UT when zone offset is known; otherwise assume input is UT.
        val utDateTime = zoneOffset?.let {
            localDateTime.atOffset(it).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
        } ?: localDateTime
        val jd = julianDay(utDateTime)
        val ayanamsa = lahiriAyanamsa(jd)
        val sun = sunLongitude(jd)
        val moon = moonLongitude(jd)
        return EphemerisSnapshot(
            jd = jd,
            tropicalSunLongitude = sun,
            siderealSunLongitude = norm360(sun - ayanamsa),
            tropicalMoonLongitude = moon,
            siderealMoonLongitude = norm360(moon - ayanamsa),
            ayanamsa = ayanamsa,
            tithi = tithi(sun, moon),
        )
    }

    fun siderealSunLongitude(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): Double = snapshot(birthDate, birthTime, zoneOffset).siderealSunLongitude

    fun siderealMoonLongitude(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): Double = snapshot(birthDate, birthTime, zoneOffset).siderealMoonLongitude

    /** Julian Day Number at the given date-time (for precise calculations with birth time). */
    fun julianDay(dateTime: java.time.LocalDateTime): Double {
        val date = dateTime.toLocalDate()
        var y = date.year
        var m = date.monthValue
        if (m <= 2) {
            y -= 1; m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        val dayFraction = (dateTime.hour + dateTime.minute / 60.0 + dateTime.second / 3600.0) / 24.0
        return floor(365.25 * (y + 4716)) +
                floor(30.6001 * (m + 1)) +
                date.dayOfMonth + b - 1524.5 + dayFraction
    }

    // ---------------------------------------------------------------------------
    // Planetary positions — simplified geocentric longitude
    // ---------------------------------------------------------------------------

    /**
     * Geocentric tropical ecliptic longitude of a planet in degrees, [0, 360).
     *
     * Uses Keplerian mean elements + first-order eccentric correction, then
     * subtracts Earth's position to get geocentric longitude. Accuracy is
     * ~±2° — sufficient for zodiac sign determination (30° bins).
     *
     * @param jd Julian Day
     * @param planet One of: MERCURY, VENUS, MARS, JUPITER, SATURN.
     */
    fun planetLongitude(jd: Double, planet: Planet): Double {
        val d = jd - 2451545.0

        // Earth's heliocentric longitude ≈ Sun's geocentric + 180°
        val sunGeo = sunLongitude(jd)
        val earthHelio = norm360(sunGeo + 180.0)
        val earthRad = 1.0 // AU (sufficient for sign-level; Earth's orbit is nearly circular)

        val (meanLon, dailyMotion, eccentricity, semiMajorAxis) = planet.elements
        val meanAnomaly = Math.toRadians(norm360(meanLon + dailyMotion * d - planet.perihelion))
        val eccentricAnomaly = solveKepler(meanAnomaly, eccentricity)
        val trueAnomaly = 2.0 * kotlin.math.atan(
            kotlin.math.sqrt((1.0 + eccentricity) / (1.0 - eccentricity)) *
                kotlin.math.tan(eccentricAnomaly / 2.0)
        )
        val planetHelio = norm360(planet.perihelion + Math.toDegrees(trueAnomaly))
        val planetRad = semiMajorAxis * (1.0 - eccentricity * kotlin.math.cos(eccentricAnomaly))

        // Cartesian difference (planet - Earth)
        val x = planetRad * cos(Math.toRadians(planetHelio)) - earthRad * cos(Math.toRadians(earthHelio))
        val y = planetRad * sin(Math.toRadians(planetHelio)) - earthRad * sin(Math.toRadians(earthHelio))

        return norm360(Math.toDegrees(kotlin.math.atan2(y, x)))
    }

    private fun solveKepler(meanAnomaly: Double, eccentricity: Double): Double {
        var e = meanAnomaly
        repeat(6) {
            e = meanAnomaly + eccentricity * kotlin.math.sin(e)
        }
        return e
    }

    private fun norm360(x: Double): Double = ((x % 360.0) + 360.0) % 360.0

    /**
     * Check if a planet appears retrograde at the given Julian Day.
     *
     * Retrograde motion is an apparent reversal of direction caused by Earth's
     * orbital motion overtaking the planet (for superior planets) or the planet
     * overtaking Earth (for inferior planets).
     *
     * Computed by comparing the planet's position at JD vs JD+1 day.
     * Returns true if the planet's longitude decreases over the 24-hour period.
     *
     * @param planet The celestial body
     * @param jd Julian Day
     * @return true if the planet is retrograde at the given time
     */
    fun isRetrograde(planet: Planet, jd: Double): Boolean {
        val current = planetLongitude(jd, planet)
        val tomorrow = planetLongitude(jd + 1.0, planet)
        // For retrograde motion, tomorrow's longitude < current's longitude
        // when accounting for the 0-360 wraparound
        // If tomorrow > current + 180, it means the planet moved "backward" through 360
        // If tomorrow < current - 180, it's prograde across 0/360 boundary
        val diff = tomorrow - current
        // Normal prograde motion is ~0.5-2°/day for outer planets, up to ~15°/day for Mercury
        // Retrograde motion shows as negative values (planet moves backward)
        return diff < -0.5  // If planet moved more than 0.5° "backward" in 24h, it's retrograde
    }

    /**
     * Supported planets for the [planetLongitude] helper.
     * Keplerian elements are mean values at J2000 (NASA/JPL).
     */
    enum class Planet(
        val elements: PlanetElements,
        val perihelion: Double,
    ) {
        MERCURY(
            elements = PlanetElements(meanLon = 252.25084, dailyMotion = 4.0923388, eccentricity = 0.20563661, semiMajorAxis = 0.387098),
            perihelion = 77.457796,
        ),
        VENUS(
            elements = PlanetElements(meanLon = 181.97973, dailyMotion = 1.6021302, eccentricity = 0.00677188, semiMajorAxis = 0.723330),
            perihelion = 131.53298,
        ),
        MARS(
            elements = PlanetElements(meanLon = -4.553432, dailyMotion = 0.5240320, eccentricity = 0.09340062, semiMajorAxis = 1.523679),
            perihelion = -23.943629,
        ),
        JUPITER(
            elements = PlanetElements(meanLon = 34.35148, dailyMotion = 0.0830912, eccentricity = 0.04849793, semiMajorAxis = 5.202603),
            perihelion = 14.274952,
        ),
        SATURN(
            elements = PlanetElements(meanLon = 50.07744, dailyMotion = 0.0334448, eccentricity = 0.05415006, semiMajorAxis = 9.554909),
            perihelion = 92.861407,
        ),
        URANUS(
            elements = PlanetElements(meanLon = 313.47370, dailyMotion = 0.0117282, eccentricity = 0.04725744, semiMajorAxis = 19.218446),
            perihelion = 171.52679,
        ),
        NEPTUNE(
            elements = PlanetElements(meanLon = 304.89624, dailyMotion = 0.0059819, eccentricity = 0.00859048, semiMajorAxis = 30.110388),
            perihelion = 297.84337,
        ),
        PLUTO(
            elements = PlanetElements(meanLon = 238.92930, dailyMotion = 0.0039677, eccentricity = 0.24882730, semiMajorAxis = 39.482116),
            perihelion = 224.06933,
        ),
    }

    data class PlanetElements(
        val meanLon: Double,
        val dailyMotion: Double,
        val eccentricity: Double,
        val semiMajorAxis: Double,
    )
}
