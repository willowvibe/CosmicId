package com.willowvibe.agereveal.domain

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Calculates the visual moon phase for a given date.
 * Uses the elongation (Moon longitude - Sun longitude) to determine phase.
 */
@Singleton
class MoonPhaseCalculator @Inject constructor() {

    /**
     * Compute moon phase from Sun and Moon tropical longitudes.
     * @return [MoonPhase] with name, illumination fraction (0.0–1.0), and age in days.
     */
    fun calculate(sunLongitude: Double, moonLongitude: Double): MoonPhase {
        val elongation = norm360(moonLongitude - sunLongitude)
        // Synodic month = 29.53059 days
        val ageDays = (elongation / 360.0) * 29.53059
        // Illumination fraction: 0 = new moon, 0.5 = quarter, 1.0 = full moon
        val illumination = (1.0 - kotlin.math.cos(Math.toRadians(elongation))) / 2.0

        val (name, waxing) = when {
            elongation < 22.5 -> "New Moon" to true
            elongation < 67.5 -> "Waxing Crescent" to true
            elongation < 112.5 -> "First Quarter" to true
            elongation < 157.5 -> "Waxing Gibbous" to true
            elongation < 202.5 -> "Full Moon" to true
            elongation < 247.5 -> "Waning Gibbous" to false
            elongation < 292.5 -> "Last Quarter" to false
            elongation < 337.5 -> "Waning Crescent" to false
            else -> "New Moon" to true
        }

        return MoonPhase(
            name = name,
            waxing = waxing,
            illuminationFraction = illumination.coerceIn(0.0, 1.0),
            ageDays = ageDays,
            elongationDegrees = elongation,
        )
    }

    private fun norm360(x: Double): Double = ((x % 360.0) + 360.0) % 360.0
}

data class MoonPhase(
    val name: String,
    val waxing: Boolean,
    val illuminationFraction: Double,
    val ageDays: Double,
    val elongationDegrees: Double,
)
