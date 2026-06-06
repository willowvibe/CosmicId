package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * Planetary aspects — angular relationships between planets in the sky, used in
 * both Western and Vedic astrology for interpretive meaning.
 *
 * The five major aspects we calculate (Western tradition):
 *  - **Conjunction** (0°, orb 8°): Planets at the same degree, blending energies.
 *  - **Sextile** (60°, orb 6°): Harmonious opportunity.
 *  - **Square** (90°, orb 8°): Tension; motivates action.
 *  - **Trine** (120°, orb 8°): Harmonious flow.
 *  - **Opposition** (180°, orb 8°): Polarity; awareness.
 *
 * Vedic astrology adds **Rahu/Ketu special aspects** (4th, 5th, 7th, 8th, 9th
 * houses from the node) — not implemented here; covered by the standard
 * conjunction/opposition because the body itself is on the axis.
 *
 * "Applying" = the aspect is tightening (the faster planet is catching up);
 * "separating" = widening. We compare the difference now vs. ±1 day to
 * determine direction.
 */
@Singleton
class AspectCalculator @Inject constructor(
    private val astronomy: AstronomicalCalculator,
) {

    /**
     * Compute all aspects between pairs of [longitudes] at the given [jd].
     * Excludes the Sun/Moon/Rahu/Ketu (the Sun's "aspect" to itself is trivial,
     * and the Moon's motion is too fast for accurate applying/separating without
     * a derivative calculation).
     */
    fun computeAspects(
        jd: Double,
        longitudes: Map<CelestialBody, Double>,
    ): List<Aspect> {
        val bodies = longitudes.keys.toList()
        val aspects = mutableListOf<Aspect>()

        for (i in bodies.indices) {
            for (j in i + 1 until bodies.size) {
                val b1 = bodies[i]
                val b2 = bodies[j]
                val lon1 = longitudes[b1]!!
                val lon2 = longitudes[b2]!!
                val angle = signedAngle(lon1, lon2)
                val exactAngle = angle.absoluteValue

                ASPECT_DEFINITIONS.forEach { def ->
                    val orb = abs(exactAngle - def.exactDegree)
                    if (orb <= def.orb) {
                        val applying = isApplying(jd, b1, b2)
                        aspects.add(
                            Aspect(
                                planet1 = b1,
                                planet2 = b2,
                                type = def.type,
                                exactDegree = def.exactDegree,
                                orb = orb,
                                applying = applying,
                            )
                        )
                    }
                }
            }
        }
        return aspects.sortedBy { it.orb }
    }

    /**
     * Sign-aware angle from [from] to [to], result in (-180, +180].
     */
    private fun signedAngle(from: Double, to: Double): Double {
        var diff = (to - from) % 360.0
        if (diff > 180.0) diff -= 360.0
        if (diff <= -180.0) diff += 360.0
        return diff
    }

    /**
     * Approximate "applying" status — checks whether the angle is tightening
     * (smaller |diff|) over a 1-day window. We rely on the simplified model
     * that aspect is applying when the faster-moving planet is east of the
     * other in the direction the angle is closing.
     *
     * For two bodies in the same day, this is approximated as:
     *   - Conjunction (0°): applying if second body has higher daily motion
     *   - Opposition (180°): applying if second body is moving "backward" in
     *     the relevant sense — simplified to motion-based check.
     */
    private fun isApplying(jd: Double, b1: CelestialBody, b2: CelestialBody): Boolean {
        val lon1Now = bodyLongitude(jd, b1)
        val lon2Now = bodyLongitude(jd, b2)
        val lon1Later = bodyLongitude(jd + 1.0, b1)
        val lon2Later = bodyLongitude(jd + 1.0, b2)
        val diffNow = signedAngle(lon1Now, lon2Now).absoluteValue
        val diffLater = signedAngle(lon1Later, lon2Later).absoluteValue
        return diffLater < diffNow
    }

    /**
     * Resolve a [CelestialBody] to its sidereal longitude at [jd]. Sun, Moon,
     * Rahu, Ketu use the existing astronomical infrastructure; the planets use
     * the heliocentric → geocentric conversion.
     */
    private fun bodyLongitude(jd: Double, body: CelestialBody): Double {
        val snap = astronomy.snapshot(
            jdToLocalDate(jd),
            jdToLocalTime(jd),
            java.time.ZoneOffset.UTC,
        )
        return when (body) {
            CelestialBody.SUN -> snap.siderealSunLongitude
            CelestialBody.MOON -> snap.siderealMoonLongitude
            CelestialBody.MERCURY -> astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.MERCURY).let { sidereal(it) }
            CelestialBody.VENUS -> astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.VENUS).let { sidereal(it) }
            CelestialBody.MARS -> astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.MARS).let { sidereal(it) }
            CelestialBody.JUPITER -> astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.JUPITER).let { sidereal(it) }
            CelestialBody.SATURN -> astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.SATURN).let { sidereal(it) }
            CelestialBody.URANUS -> astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.URANUS).let { sidereal(it) }
            CelestialBody.NEPTUNE -> astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.NEPTUNE).let { sidereal(it) }
            CelestialBody.PLUTO -> astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.PLUTO).let { sidereal(it) }
            CelestialBody.RAHU -> {
                // True node — average longitude of Moon's ascending node.
                // Meeus simple formula: 125.04452 - 1934.13626 * T (T in centuries from J2000)
                val t = (jd - 2451545.0) / 36525.0
                ((125.04452 - 1934.13626 * t) % 360.0 + 360.0) % 360.0
            }
            CelestialBody.KETU -> {
                val t = (jd - 2451545.0) / 36525.0
                val rahu = ((125.04452 - 1934.13626 * t) % 360.0 + 360.0) % 360.0
                ((rahu + 180.0) % 360.0 + 360.0) % 360.0
            }
        }
    }

    /** Subtract Lahiri ayanamsa to convert tropical to sidereal. */
    private fun sidereal(tropical: Double): Double {
        val jd = astronomy.snapshot(
            jdToLocalDate(2451545.0),
            jdToLocalTime(2451545.0),
            java.time.ZoneOffset.UTC,
        ).jd
        val ayanamsa = astronomy.lahiriAyanamsa(jd)
        return ((tropical - ayanamsa) % 360.0 + 360.0) % 360.0
    }

    private fun jdToLocalDate(jd: Double): java.time.LocalDate {
        // Reuse Julian Day → Date in astronomical formula (Meeus Ch. 7).
        val z = (jd + 0.5).toInt()
        val f = (jd + 0.5) - z
        val a = if (z < 2299161) z else {
            val alpha = ((z - 1867216.25) / 36524.25).toInt()
            z + 1 + alpha - (alpha / 4)
        }
        val b = a + 1524
        val c = ((b - 122.1) / 365.25).toInt()
        val d = (365.25 * c).toInt()
        val e = ((b - d) / 30.6001).toInt()
        val day = b - d - (30.6001 * e).toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        return java.time.LocalDate.of(year, month, day)
    }

    private fun jdToLocalTime(jd: Double): java.time.LocalTime {
        val fractional = (jd + 0.5) - (jd + 0.5).toInt()
        val totalSeconds = (fractional * 86400.0).toLong()
        val hour = (totalSeconds / 3600).toInt() % 24
        val minute = ((totalSeconds / 60) % 60).toInt()
        val second = (totalSeconds % 60).toInt()
        return java.time.LocalTime.of(hour, minute, second)
    }

    private companion object {
        /**
         * The five major Western aspects. Orbs follow common modern practice:
         * conjunction and opposition get the largest (8°), sextile the smallest (6°).
         */
        val ASPECT_DEFINITIONS: List<AspectDefinition> = listOf(
            AspectDefinition(AspectType.CONJUNCTION, 0.0, 8.0),
            AspectDefinition(AspectType.SEXTILE, 60.0, 6.0),
            AspectDefinition(AspectType.SQUARE, 90.0, 8.0),
            AspectDefinition(AspectType.TRINE, 120.0, 8.0),
            AspectDefinition(AspectType.OPPOSITION, 180.0, 8.0),
        )
    }
}

/**
 * One row of an aspect calculation — a single pair of planets within orb of
 * an aspect type.
 */
data class Aspect(
    val planet1: CelestialBody,
    val planet2: CelestialBody,
    val type: AspectType,
    val exactDegree: Double,
    val orb: Double,
    val applying: Boolean,
) {
    fun displayLabel(): String {
        val direction = if (applying) "applying" else "separating"
        return "${planet1.displayName} ${type.symbol} ${planet2.displayName} " +
            "(${type.displayName}, ${"%.1f".format(orb)}° $direction)"
    }
}

/**
 * Internal: aspect type + the exact angle in degrees + the orb tolerance in degrees.
 */
private data class AspectDefinition(
    val type: AspectType,
    val exactDegree: Double,
    val orb: Double,
)

/**
 * The five major Western aspects. Each has a [displayName], [symbol] (used in
 * chart displays), and an [exactDegree] (0, 60, 90, 120, 180).
 */
enum class AspectType(val displayName: String, val symbol: String, val exactDegree: Double) {
    CONJUNCTION("Conjunction", "☌", 0.0),
    SEXTILE("Sextile", "⚹", 60.0),
    SQUARE("Square", "□", 90.0),
    TRINE("Trine", "△", 120.0),
    OPPOSITION("Opposition", "☍", 180.0),
}
