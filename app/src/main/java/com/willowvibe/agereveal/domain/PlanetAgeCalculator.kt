package com.willowvibe.agereveal.domain

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
    private val periods = mapOf(
        Planet.MERCURY to 0.2408,
        Planet.VENUS to 0.6152,
        Planet.MARS to 1.8809,
        Planet.JUPITER to 11.8626,
        Planet.SATURN to 29.4571,
        Planet.URANUS to 84.0205,
        Planet.NEPTUNE to 164.8,
        Planet.PLUTO to 248.0,
    )

    /**
     * Calculate planetary ages for a given number of Earth years lived.
     * Returns a list sorted by planet distance from the Sun.
     */
    fun calculatePlanetAges(earthYears: Double): List<PlanetAge> {
        return periods.map { (planet, period) ->
            val age = earthYears / period
            PlanetAge(
                planet = planet,
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
    fun shareSentence(name: String, planet: Planet, ageYears: Double): String {
        val formatted = formatPlanetAge(ageYears)
        return "On ${planet.displayName}, $name is only $formatted ${if (ageYears > 1.0) "years" else "year"} old. ${planet.emoji}"
    }
}

enum class Planet(
    val displayName: String,
    val emoji: String,
) {
    MERCURY("Mercury", "🫁"),
    VENUS("Venus", "💋"),
    MARS("Mars", "🚀"),
    JUPITER("Jupiter", "🥇"),
    SATURN("Saturn", "🟂"),
    URANUS("Uranus", "🧊"),
    NEPTUNE("Neptune", "🐠"),
    PLUTO("Pluto", "🥽"),
}

data class PlanetAge(
    val planet: Planet,
    val ageYears: Double,
    val orbitalPeriod: Double,
)
