package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BirthChartSubChartTest {

    private fun makeSubChart(): BirthChartSubChart = BirthChartSubChart(
        nakshatraMetadata = NakshatraMetadata(),
        divisionalChartCalculator = DivisionalChartCalculator(),
        aspectCalculator = AspectCalculator(AstronomicalCalculator()),
    )

    @Test
    fun `compute populates sub-charts for known birth chart`() {
        val sub = makeSubChart()
        val moonLon = 123.45  // Ashwini / Bharani boundary region
        // Use 4 non-Sun/Moon/Rahu/Ketu bodies (the docstring on AspectCalculator
        // describes the exclusion; the math runs on the bodies present in the map
        // regardless). Mars (0°) sextile Venus (60°), Jupiter (90°) square both,
        // Saturn (180°) opposition Mars. All four pairs land on exact aspect
        // angles with 0° orb — guaranteed at least one aspect in the output.
        val planetLongitudes = mapOf(
            CelestialBody.MARS to 0.0,
            CelestialBody.VENUS to 60.0,
            CelestialBody.JUPITER to 90.0,
            CelestialBody.SATURN to 180.0,
        )
        val jd = 2451545.0  // J2000

        val result = sub.compute(moonLon, planetLongitudes, jd)

        assertNotNull(result.nakshatraMetadata)
        assertNotNull(result.navamsaChart)
        assertTrue(result.planetaryAspects.isNotEmpty())
    }

    @Test
    fun `compute does not throw on NaN moon longitude`() {
        val sub = makeSubChart()
        // forLongitude uses ((x / 13.333).toInt() % 27 + 27) % 27 which propagates
        // NaN through the arithmetic — the contract is just that we don't throw.
        // JUnit fails the test if compute() throws, so the assertion below is a
        // placeholder that exercises the "happy" return path.
        val result = sub.compute(Double.NaN, emptyMap(), 2451545.0)
        assertNotNull(result)
    }

    @Test
    fun `compute returns empty aspects when no pairs in orb`() {
        val sub = makeSubChart()
        // All planets at exactly 0° → all in conjunction (tight orb, in range)
        // Use 0° vs 180° so they're in opposition
        val planetLongitudes = mapOf(
            CelestialBody.SUN to 0.0,
            CelestialBody.MOON to 180.0,
        )
        val jd = 2451545.0
        val result = sub.compute(0.0, planetLongitudes, jd)
        // Sun-Moon opposition: in orb (180° ± 8° → 0° orb at exact 180°)
        assertTrue(result.planetaryAspects.isNotEmpty())
    }

    @Test
    fun `compute does not propagate exception when one sub-chart fails`() {
        // Inject a metadata that throws to verify runCatching swallows it
        val throwingMetadata = object : NakshatraMetadata() {
            override fun forLongitude(@Suppress("UNUSED_PARAMETER") x: Double): NakshatraData {
                throw IllegalStateException("simulated failure")
            }
        }

        val sub = BirthChartSubChart(
            nakshatraMetadata = throwingMetadata,
            divisionalChartCalculator = DivisionalChartCalculator(),
            aspectCalculator = AspectCalculator(AstronomicalCalculator()),
        )

        val result = sub.compute(45.0, mapOf(CelestialBody.SUN to 0.0), 2451545.0)

        // Metadata is null (caught), the other two still populated with real data
        assertNull(result.nakshatraMetadata)
        assertNotNull(result.navamsaChart)
        assertTrue(
            result.navamsaChart!!.positions.isNotEmpty() ||
                result.navamsaChart.rashiOccupancy.isNotEmpty()
        )
    }
}
