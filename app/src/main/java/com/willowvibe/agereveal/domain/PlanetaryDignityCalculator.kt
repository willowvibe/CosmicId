package com.willowvibe.agereveal.domain

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vedic planetary dignity (avastha) calculator.
 *
 * In Vedic astrology every planet has a specific strength (shad-bala) depending on
 * which sign it occupies. The four primary dignity states are:
 *
 * - **Exalted (Uchcha)** — maximum strength at a precise degree in a specific sign.
 * - **Own House (Swakshetra)** — strong, in a sign the planet rules.
 * - **Moolatrikona** — second-best strength; a favoured degree-range inside the
 *   planet's own sign (e.g. 0°–10° Aries for Mars).
 * - **Debilitated (Neecha)** — minimum strength at the opposite sign from exaltation.
 * - **Neutral** — none of the above; the planet operates at average strength.
 *
 * Because the existing ephemeris is accurate to ~±2° (sign-level), this
 * calculator works at sign-level for Own / Debilitated and adds a proximity
 * hint when a planet is close to its exact exaltation degree.
 */
@Singleton
class PlanetaryDignityCalculator @Inject constructor() {

    /**
     * Returns the dignity state of each planet for the given birth chart.
     *
     * @param positions List of (planet name, tropical longitude in degrees).
     *        Expected planet names: Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn.
     * @return List of (planet name, dignity label) pairs.
     */
    fun computeDignities(positions: List<Pair<String, Double>>): List<PlanetaryDignity> {
        return positions.map { (name, longitude) ->
            val signIndex = ((longitude / 30.0).toInt() % 12 + 12) % 12  // 0=Aries … 11=Pisces
            val degreeInSign = longitude % 30.0
            computeDignity(name, signIndex, degreeInSign)
        }
    }

    private fun computeDignity(planetName: String, signIndex: Int, degreeInSign: Double): PlanetaryDignity {
        val planet = PlanetConfig.entries.find { it.name.equals(planetName, ignoreCase = true) }
            ?: return PlanetaryDignity(planetName, Dignity.NEUTRAL, "")

        val inOwnSign = planet.ownSigns.contains(signIndex)
        val inDebilitationSign = planet.debilitationSign == signIndex
        val inExaltationSign = planet.exaltationSign == signIndex

        return when {
            // Debilitation trumps everything else
            inDebilitationSign -> {
                PlanetaryDignity(planetName, Dignity.DEBILITATED, "")
            }
            // Exaltation — sign match + proximity hint
            inExaltationSign -> {
                val diff = kotlin.math.abs(degreeInSign - planet.exaltationDegree)
                val proximity = when {
                    diff < 3.0 -> " near exact"
                    diff < 8.0 -> " strong"
                    else -> ""
                }
                PlanetaryDignity(planetName, Dignity.EXALTED, proximity)
            }
            // Moolatrikona check (must be in own sign AND within the moolatrikona range)
            inOwnSign && planet.moolatrikonaSign == signIndex -> {
                val (startDeg, endDeg) = planet.moolatrikonaRange
                if (degreeInSign in startDeg..endDeg) {
                    PlanetaryDignity(planetName, Dignity.MOOLATRIKONA, "")
                } else {
                    PlanetaryDignity(planetName, Dignity.OWN_HOUSE, "")
                }
            }
            // Own house (but outside moolatrikona)
            inOwnSign -> PlanetaryDignity(planetName, Dignity.OWN_HOUSE, "")
            // Neutral fallback
            else -> PlanetaryDignity(planetName, Dignity.NEUTRAL, "")
        }
    }

    /**
     * Immutable description of a planet's dignity.
     *
     * @property planetName e.g. "Jupiter"
     * @property dignity The primary dignity category
     * @property proximityHint Additional qualifier when near exaltation degree, e.g. " near exact"
     */
    data class PlanetaryDignity(
        val planetName: String,
        val dignity: Dignity,
        val proximityHint: String = "",
    ) {
        /** Human-readable label for UI display, e.g. "Jupiter — Exalted" or "Saturn — Own House". */
        fun displayLabel(): String = "$planetName — ${dignity.label}$proximityHint"
    }

    enum class Dignity(val label: String) {
        EXALTED("Exalted"),
        MOOLATRIKONA("Moolatrikona"),
        OWN_HOUSE("Own House"),
        NEUTRAL("Neutral"),
        DEBILITATED("Debilitated"),
    }

    /**
     * Configuration for each classical planet's dignity relationships.
     *
     * @property ownSigns Sign indices (0=Aries) where the planet rules.
     * @property exaltationSign Sign index of maximum strength.
     * @property exaltationDegree Exact degree within that sign (0–30).
     * @property debilitationSign Sign index of minimum strength (180° opposite exaltation).
     * @property moolatrikonaSign Which own-sign contains the moolatrikona range.
     * @property moolatrikonaRange (startDeg, endDeg) within that sign.
     */
    private enum class PlanetConfig(
        val ownSigns: List<Int>,
        val exaltationSign: Int,
        val exaltationDegree: Double,
        val debilitationSign: Int,
        val moolatrikonaSign: Int,
        val moolatrikonaRange: Pair<Double, Double>,
    ) {
        Sun(
            ownSigns = listOf(4),                 // Leo
            exaltationSign = 0,                    // Aries
            exaltationDegree = 10.0,
            debilitationSign = 6,                  // Libra
            moolatrikonaSign = 4,                  // Leo
            moolatrikonaRange = 0.0 to 20.0,
        ),
        Moon(
            ownSigns = listOf(3),                  // Cancer
            exaltationSign = 1,                    // Taurus
            exaltationDegree = 3.0,
            debilitationSign = 7,                  // Scorpio
            moolatrikonaSign = 3,                  // Cancer
            moolatrikonaRange = 0.0 to 30.0,       // Whole sign is moolatrikona for Moon
        ),
        Mercury(
            ownSigns = listOf(2, 5),               // Gemini, Virgo
            exaltationSign = 5,                    // Virgo
            exaltationDegree = 15.0,
            debilitationSign = 11,                 // Pisces
            moolatrikonaSign = 2,                  // Gemini
            moolatrikonaRange = 0.0 to 20.0,
        ),
        Venus(
            ownSigns = listOf(1, 6),               // Taurus, Libra
            exaltationSign = 11,                   // Pisces
            exaltationDegree = 27.0,
            debilitationSign = 5,                  // Virgo
            moolatrikonaSign = 1,                  // Taurus
            moolatrikonaRange = 0.0 to 15.0,
        ),
        Mars(
            ownSigns = listOf(0, 7),               // Aries, Scorpio
            exaltationSign = 9,                    // Capricorn
            exaltationDegree = 28.0,
            debilitationSign = 3,                  // Cancer
            moolatrikonaSign = 0,                  // Aries
            moolatrikonaRange = 0.0 to 12.0,
        ),
        Jupiter(
            ownSigns = listOf(8, 11),              // Sagittarius, Pisces
            exaltationSign = 4,                    // Cancer
            exaltationDegree = 5.0,
            debilitationSign = 9,                  // Capricorn
            moolatrikonaSign = 8,                  // Sagittarius
            moolatrikonaRange = 0.0 to 10.0,
        ),
        Saturn(
            ownSigns = listOf(9, 10),              // Capricorn, Aquarius
            exaltationSign = 6,                    // Libra
            exaltationDegree = 20.0,
            debilitationSign = 0,                  // Aries
            moolatrikonaSign = 10,                 // Aquarius
            moolatrikonaRange = 0.0 to 20.0,
        ),
    }
}
