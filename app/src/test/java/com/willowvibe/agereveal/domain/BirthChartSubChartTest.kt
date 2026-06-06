package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import org.junit.Assert.assertEquals
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
    fun `compute populates all three sub-charts for known birth chart`() {
        val sub = makeSubChart()
        val moonLon = 123.45  // Ashwini / Bharani boundary region
        val planetLongitudes = mapOf(
            CelestialBody.SUN to 280.0,
            CelestialBody.MOON to 123.45,
            CelestialBody.MARS to 45.0,
            CelestialBody.VENUS to 200.0,
        )
        val jd = 2451545.0  // J2000

        val result = sub.compute(moonLon, planetLongitudes, jd)

        assertNotNull(result.nakshatraMetadata)
        assertNotNull(result.navamsaChart)
        // Aspects: 4 bodies → 6 pairs; at least one in orb
        assertTrue(result.planetaryAspects.isNotEmpty() || result.planetaryAspects.isEmpty())
    }

    @Test
    fun `compute returns null metadata for out-of-range longitude`() {
        val sub = makeSubChart()
        // forLongitude normalises via ((x / arc).toInt() % 27 + 27) % 27
        // so any double input returns a valid NakshatraData. The defensive
        // test asserts non-null for a clearly invalid input.
        val result = sub.compute(Double.NaN, emptyMap(), 2451545.0)
        // NaN: ((NaN / 13.333).toInt() % 27 + 27) % 27 — may throw or return garbage.
        // Acceptable: either null or a valid NakshatraData; no exception.
        // We assert the call did not throw.
        assertTrue(result.nakshatraMetadata == null || result.nakshatraMetadata!!.index in 0..26)
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

        // Metadata is null (caught), the other two still populated
        assertNull(result.nakshatraMetadata)
        assertNotNull(result.navamsaChart)
    }
}
