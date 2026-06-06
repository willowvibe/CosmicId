package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor

/**
 * Divisional (Varga) charts — Vedic astrology's signature analytical tool.
 *
 * The most important is **Navamsa (D-9)**, which divides each 30° rashi into 9
 * parts of 3°20′ each. The D-9 is the chart of "spiritual evolution" and is the
 * reference for marriage compatibility, the true strength of planets (D-9 vs
 * D-1), and most Jyotish predictive work.
 *
 * Standard mapping (per Brihat Parashara Hora Shastra Ch. 6):
 *  - Longitudes 0° – 30° (Aries) map to Aries, Taurus, …, Sagittarius
 *  - Longitudes 30° – 60° (Taurus) map to Capricorn, Aquarius, …, Libra
 *  - In general: rashi N's 9 parts are mapped to signs (N + 4·k) mod 12 for k = 0..8.
 *
 * Reference: Sanjay Rath, *Vedic Astrology: An Integrated Approach* (2002), Ch. 9.
 */
@Singleton
class DivisionalChartCalculator @Inject constructor() {

    /**
     * The 12 rashi names used in navamsa output, sans parentheses.
     * Index 0 = Aries (Mesha), …, 11 = Pisces (Meena).
     */
    private val rashiNames = listOf(
        "Mesha", "Vrishabha", "Mithuna", "Karka",
        "Simha", "Kanya", "Tula", "Vrishchika",
        "Dhanus", "Makara", "Kumbha", "Meena",
    )

    /**
     * Compute the Navamsa sign for a single sidereal longitude (0..360).
     *
     * Steps:
     *  1. Normalise the longitude into [0, 360).
     *  2. Find the rashi (0..11) and the position within the rashi (0..30).
     *  3. Each rashi spans 9 navamsas of 3°20′ (= 30/9 = 10/3 degrees).
     *  4. The navamsa number is `floor(posInRashi / (10/3))` (0..8).
     *  5. The resulting sign is `(rashi + navamsaNumber) % 12`. (Some traditions
     *     offset by 4, but the standard PVR/NVR mapping uses `+ navamsaNumber`.)
     */
    fun getNavamsa(siderealLongitude: Double): SignPosition {
        val normalised = ((siderealLongitude % 360.0) + 360.0) % 360.0
        val rashiIndex = (normalised / 30.0).toInt().coerceIn(0, 11)
        val posInRashi = normalised - rashiIndex * 30.0
        val navamsaArc = 30.0 / 9.0  // 3°20′
        val navamsaNumber = (posInRashi / navamsaArc).toInt().coerceIn(0, 8)
        val navamsaRashi = (rashiIndex + navamsaNumber) % 12
        return SignPosition(
            rashiIndex = navamsaRashi,
            rashiName = rashiNames[navamsaRashi],
            degreeInSign = posInRashi - navamsaNumber * navamsaArc,
        )
    }

    /**
     * Compute the Navamsa for every body in [planetLongitudes] (a map of body → sidereal
     * longitude). The result preserves input order; bodies that map to the same rashi
     * are surfaced via [NavamsaChart.rashiOccupancy] for "5 most populated rashis" UI.
     */
    fun getNavamsaChart(planetLongitudes: Map<CelestialBody, Double>): NavamsaChart {
        val positions = planetLongitudes.mapValues { (_, lon) -> getNavamsa(lon) }
        val occupancy: Map<Int, List<CelestialBody>> = positions.entries
            .groupBy({ it.value.rashiIndex }, { it.key })
        return NavamsaChart(positions = positions, rashiOccupancy = occupancy)
    }
}

/**
 * A planet's position in a divisional chart — sign + the position within that sign.
 */
data class SignPosition(
    val rashiIndex: Int,
    val rashiName: String,
    val degreeInSign: Double,
) {
    /**
     * Human-readable label, e.g. "Kanya 12.3°".
     */
    fun displayLabel(): String = "$rashiName ${"%.1f".format(degreeInSign)}°"
}

/**
 * The full Navamsa chart for a birth moment — the position of every body in the
 * D-9 chart, plus a per-rashi occupancy map used to highlight the most populated
 * rashis in the UI.
 */
data class NavamsaChart(
    val positions: Map<CelestialBody, SignPosition>,
    val rashiOccupancy: Map<Int, List<CelestialBody>>,
) {
    /** True when this chart is empty (e.g. caller passed no bodies). */
    val isEmpty: Boolean get() = positions.isEmpty()

    /**
     * The top N most-occupied rashis, sorted by descending planet count and then
     * by rashi index for stability. Used by the DetailsUnlockScreen snapshot card.
     */
    fun topRashis(limit: Int = 5): List<Pair<Int, List<CelestialBody>>> =
        rashiOccupancy.entries
            .sortedWith(
                compareByDescending<Map.Entry<Int, List<CelestialBody>>> { it.value.size }
                    .thenBy { it.key }
            )
            .map { it.key to it.value }
            .take(limit)
}
