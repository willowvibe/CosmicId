package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bundles three Phase 6.5 sub-calculators that the engine ships but that
 * [AgeCalculator] does not directly inject. Exists to keep [AgeCalculator]'s
 * constructor short (one new dep instead of three) and the test surface tight.
 *
 * Dasha is *not* in this wrapper because [AgeCalculator] already injects
 * [DashaCalculator] directly (the `dashaDetail` field is computed inline in
 * `AgeCalculator.calculate()`).
 *
 * Each sub-calculation is wrapped in `runCatching { ... }.getOrNull()` so a
 * single failure does not kill the others. The method never throws.
 */
@Singleton
class BirthChartSubChart @Inject constructor(
    private val nakshatraMetadata: NakshatraMetadata,
    private val divisionalChartCalculator: DivisionalChartCalculator,
    private val aspectCalculator: AspectCalculator,
) {
    /**
     * Container for the three sub-chart outputs. All three are nullable so a
     * single sub-calculator failure does not lose the others.
     */
    data class SubCharts(
        val nakshatraMetadata: NakshatraData?,
        val navamsaChart: NavamsaChart?,
        val planetaryAspects: List<Aspect>,
    )

    /**
     * Compute the three sub-charts for a given birth moment.
     *
     * @param siderealMoonLongitude Moon's sidereal longitude (degrees, 0-360). Used
     *        to look up the birth Nakshatra's metadata.
     * @param planetLongitudes Map of celestial body → sidereal longitude. Used for
     *        Navamsa D-9 calculation and planetary aspects.
     * @param jd Julian Day of the birth moment (UTC). Used by the aspect
     *        calculator to determine "applying" vs "separating" direction.
     */
    fun compute(
        siderealMoonLongitude: Double,
        planetLongitudes: Map<CelestialBody, Double>,
        jd: Double,
    ): SubCharts {
        val metadata = runCatching {
            nakshatraMetadata.forLongitude(siderealMoonLongitude)
        }.getOrNull()
        val navamsa = runCatching {
            divisionalChartCalculator.getNavamsaChart(planetLongitudes)
        }.getOrNull()
        val aspects = runCatching {
            aspectCalculator.computeAspects(jd, planetLongitudes)
        }.getOrNull() ?: emptyList()
        return SubCharts(metadata, navamsa, aspects)
    }
}
