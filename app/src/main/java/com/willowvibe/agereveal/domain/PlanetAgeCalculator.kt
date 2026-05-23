package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Calculates age on other planets given Earth years.
 * Uses orbital periods relative to Earth years.
 */
@Singleton
class PlanetAgeCalculator @Inject constructor() {

    /** Earth orbital period = 1.0 year (reference). */
    private val earthPeriod = 1.0

    /** Orbital periods in Earth years. */
    private val orbitalPeriods = mapOf(
        CelestialBody.MERCURY to 0.2408,
        CelestialBody.VENUS to 0.6152,
        CelestialBody.MARS to 1.8809,
        CelestialBody.JUPITER to 11.8626,
        CelestialBody.SATURN to 29.4571,
        CelestialBody.URANUS to 84.0205,
        CelestialBody.NEPTUNE to 164.8,
        CelestialBody.PLUTO to 248.0,
    )

    /**
     * Calculate planetary ages for a given number of Earth years lived.
     * Returns a list sorted by planet distance from the Sun.
     */
    fun calculatePlanetAges(earthYears: Double): List<PlanetAge> {
        return orbitalPeriods.map { (celestialBody, period) ->
            val age = earthYears / period
            PlanetAge(
                planet = celestialBody,
                ageYears = age,
                orbitalPeriod = period,
            )
        }
    }

    /** Format a planet age for display. E.g. "14.3" or "0.4". */
    fun formatPlanetAge(age: Double): String = when {
        age < 0.1 -> "%.2f".format(age)
        age < 1.0 -> "%.1f".format(age)
        age < 10.0 -> "%.1f".format(age)
        else -> "%,d".format(age.roundToInt())
    }

    /** Generate a shareable sentence. E.g. "On Mars, John is only 14 years old. 🚀" */
    fun shareSentence(name: String, planet: CelestialBody, ageYears: Double): String {
        val formatted = formatPlanetAge(ageYears)
        val verb = if (name == "You") "are" else "is"
        val noun = if (ageYears == 1.0) "year" else "years"
        return "On ${planet.displayName}, $name $verb only $formatted $noun old. ${planet.emoji}"
    }
}

/**
 * Age on a specific celestial body.
 * @property planet The celestial body (e.g., Mars, Jupiter)
 * @property ageYears Age in planet years
 * @property orbitalPeriod Orbital period in Earth years
 */
data class PlanetAge(
    val planet: CelestialBody,
    val ageYears: Double,
    val orbitalPeriod: Double,
)
