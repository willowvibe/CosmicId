package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests for [AspectCalculator] — verifies the five major Western aspects
 * (conjunction, sextile, square, trine, opposition) are detected with the
 * correct orb and type for known planetary configurations.
 *
 * Reference: 2024-Apr-08 Great North American Eclipse is a Sun-Moon conjunction
 * (both within 0.1°). 2020-Dec-21 Great Conjunction: Jupiter and Saturn were
 * 0.1° apart (sextile-orb of conjunction).
 */
class AspectCalculatorTest {

    private val astronomy = AstronomicalCalculator()
    private val calc = AspectCalculator(astronomy)

    @Test
    fun `synthetic conjunction within orb is detected`() {
        val longitudes = mapOf<CelestialBody, Double>(
            CelestialBody.MERCURY to 100.0,
            CelestialBody.VENUS to 102.5,  // 2.5° away — within 8° conjunction orb
        )
        // Use a known JD (2000-01-01 12:00 UT = JD 2451545.0)
        val aspects = calc.computeAspects(2451545.0, longitudes)
        val conjunction = aspects.firstOrNull { it.type == AspectType.CONJUNCTION }
        assertNotNull("Expected conjunction aspect", conjunction)
        assertEquals(2.5, conjunction!!.orb, 0.001)
    }

    @Test
    fun `120 degree separation yields trine`() {
        val longitudes = mapOf<CelestialBody, Double>(
            CelestialBody.JUPITER to 0.0,
            CelestialBody.MARS to 122.0,  // 122° = 2° orb of 120° trine
        )
        val aspects = calc.computeAspects(2451545.0, longitudes)
        val trine = aspects.firstOrNull { it.type == AspectType.TRINE }
        assertNotNull("Expected trine", trine)
        assertEquals(2.0, trine!!.orb, 0.001)
    }

    @Test
    fun `180 degree separation yields opposition`() {
        val longitudes = mapOf<CelestialBody, Double>(
            CelestialBody.SUN to 0.0,
            CelestialBody.MOON to 178.5,  // 1.5° from 180° opposition
        )
        val aspects = calc.computeAspects(2451545.0, longitudes)
        val opp = aspects.firstOrNull { it.type == AspectType.OPPOSITION }
        assertNotNull("Expected opposition", opp)
        assertEquals(1.5, opp!!.orb, 0.001)
    }

    @Test
    fun `90 degree separation yields square`() {
        val longitudes = mapOf<CelestialBody, Double>(
            CelestialBody.VENUS to 50.0,
            CelestialBody.SATURN to 138.0,  // 88° = 2° from 90° square
        )
        val aspects = calc.computeAspects(2451545.0, longitudes)
        val square = aspects.firstOrNull { it.type == AspectType.SQUARE }
        assertNotNull("Expected square", square)
        assertEquals(2.0, square!!.orb, 0.001)
    }

    @Test
    fun `60 degree separation yields sextile`() {
        val longitudes = mapOf<CelestialBody, Double>(
            CelestialBody.MERCURY to 0.0,
            CelestialBody.MOON to 60.0,  // exact sextile
        )
        val aspects = calc.computeAspects(2451545.0, longitudes)
        val sextile = aspects.firstOrNull { it.type == AspectType.SEXTILE }
        assertNotNull("Expected sextile", sextile)
        assertEquals(0.0, sextile!!.orb, 0.001)
    }

    @Test
    fun `no aspect when planets are out of orb`() {
        val longitudes = mapOf<CelestialBody, Double>(
            CelestialBody.MERCURY to 0.0,
            CelestialBody.VENUS to 30.0,  // 30° — not in any orb (closest is 8° conjunction)
        )
        val aspects = calc.computeAspects(2451545.0, longitudes)
        assertTrue("No aspects expected, got $aspects", aspects.isEmpty())
    }

    @Test
    fun `aspects are sorted by orb ascending`() {
        val longitudes = mapOf<CelestialBody, Double>(
            CelestialBody.SUN to 0.0,
            CelestialBody.MOON to 1.0,    // 1° from conjunction
            CelestialBody.MERCURY to 90.0,
            CelestialBody.VENUS to 94.0,  // 4° from square
        )
        val aspects = calc.computeAspects(2451545.0, longitudes)
        assertTrue(aspects.size >= 2)
        // Verify monotonic non-decreasing orb order.
        for (i in 1 until aspects.size) {
            assertTrue(
                "Aspects must be sorted by orb: ${aspects[i-1]} -> ${aspects[i]}",
                aspects[i - 1].orb <= aspects[i].orb + 0.0001,
            )
        }
    }

    @Test
    fun `displayLabel includes planet names and orb`() {
        val a = Aspect(
            planet1 = CelestialBody.SUN,
            planet2 = CelestialBody.MOON,
            type = AspectType.CONJUNCTION,
            exactDegree = 0.0,
            orb = 1.5,
            applying = true,
        )
        val label = a.displayLabel()
        assertTrue("contains Sun: $label", label.contains("Sun"))
        assertTrue("contains Moon: $label", label.contains("Moon"))
        assertTrue("contains Conjunction: $label", label.contains("Conjunction"))
        assertTrue("contains 1.5°: $label", label.contains("1.5°"))
    }
}
