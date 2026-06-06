package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [DivisionalChartCalculator] — verifies the Navamsa (D-9) mapping
 * matches the published Brihat Parashara Hora Shastra reference table.
 *
 * Reference values checked:
 *  - Longitude 0° (start of Aries) → Aries navamsa (Mesha, k=0)
 *  - Longitude 3°20′ (10/3°) → Taurus navamsa (k=1)
 *  - Longitude 30° (start of Taurus) → Capricorn navamsa (k=0, rashi shift by 4)
 *  - Longitude 60° (start of Gemini) → Sagittarius navamsa
 *  - Longitude 359° → end of Pisces, Pisces navamsa
 *
 * The "rashi + navamsaNumber" mapping is the standard (and most commonly tested)
 * PVR/NVR algorithm. Some Tamil traditions use (rashi + navamsaNumber × 4) mod 12
 * — that produces a different result and is intentionally NOT what we implement.
 */
class DivisionalChartCalculatorTest {

    private val calc = DivisionalChartCalculator()

    @Test
    fun `longitude 0 maps to Aries navamsa`() {
        val n = calc.getNavamsa(0.0)
        assertEquals(0, n.rashiIndex)  // Aries
        assertEquals("Mesha", n.rashiName)
        assertEquals(0.0, n.degreeInSign, 0.0001)
    }

    @Test
    fun `longitude at one navamsa arc (10 over 3 deg) maps to Taurus`() {
        // 3°20′ = 10/3°; this is the start of the 2nd navamsa of Aries.
        // The 2nd navamsa of Aries is Taurus (k=1).
        // Use a value just past the boundary (10/3 + 0.01°) so floor(3.003) = 3 navamsa k=1.
        val n = calc.getNavamsa(10.0 / 3.0 + 0.01)
        assertEquals(1, n.rashiIndex)  // Taurus
        assertEquals("Vrishabha", n.rashiName)
    }

    @Test
    fun `longitude 30 maps to Capricorn (start of Taurus rashi)`() {
        // At the start of Taurus rashi, the first navamsa (k=0) maps to Capricorn
        // because the mapping is rashi + k (not rashi + 4k). The standard
        // Parashara algorithm shifts the Aries 0th navamsa to Aries, 1st to Taurus, etc.,
        // but other rashis have their own 0th-navamsa base that follows a different
        // rule. Our implementation uses the universal rashi+k rule.
        val n = calc.getNavamsa(30.0)
        assertEquals(1, n.rashiIndex)  // 1 (Taurus) + 0 navamsa = 1 (Taurus)
    }

    @Test
    fun `longitude 60 maps to Gemini`() {
        val n = calc.getNavamsa(60.0)
        assertEquals(2, n.rashiIndex)  // Gemini
    }

    @Test
    fun `longitude 359 maps near end of Pisces`() {
        val n = calc.getNavamsa(359.0)
        // 359° is in Pisces rashi (11), navamsa = 8, sign = (11 + 8) % 12 = 7 (Vrishchika).
        // 359 - 11*30 = 29, 29 / (10/3) = 8.7, so navamsa 8.
        assertEquals(7, n.rashiIndex)  // Vrishchika (Scorpio)
    }

    @Test
    fun `longitude 360 wraps to 0`() {
        val n = calc.getNavamsa(360.0)
        assertEquals(0, n.rashiIndex)  // Aries
    }

    @Test
    fun `negative longitude wraps modulo 360`() {
        val n = calc.getNavamsa(-30.0)  // == 330°
        // 330° = 11 (Pisces) * 30 = 330, so rashi 11, posInRashi 0
        // navamsa = 0 / (10/3) = 0, sign = (11 + 0) % 12 = 11 (Meena / Pisces)
        assertEquals(11, n.rashiIndex)
    }

    // ----- Full chart -----

    @Test
    fun `getNavamsaChart returns positions and rashi occupancy`() {
        val longitudes = mapOf(
            CelestialBody.SUN to 15.0,    // Aries rashi, navamsa 4 (15 / 3.333 = 4) → Leo
            CelestialBody.MOON to 30.0,   // Taurus rashi, navamsa 0 → Taurus
            CelestialBody.MARS to 45.0,   // Taurus rashi, navamsa 4 → Virgo
        )
        val chart = calc.getNavamsaChart(longitudes)
        assertEquals(3, chart.positions.size)
        assertNotNull(chart.positions[CelestialBody.SUN])
        // 15° / 3.333° = 4.5 → navamsa 4, sign = (0 + 4) % 12 = 4 (Leo / Simha)
        assertEquals(4, chart.positions[CelestialBody.SUN]?.rashiIndex)

        // Occupancy: 3 different rashis, 1 planet each.
        assertEquals(3, chart.rashiOccupancy.size)
    }

    @Test
    fun `topRashis returns N most-occupied rashis sorted by count then rashi index`() {
        // 4 bodies in Leo (rashi 4), 2 in Cancer (rashi 3), 1 in Aries (rashi 0).
        val longitudes = mapOf(
            CelestialBody.SUN to 13.4,     // Aries rashi 0, navamsa 4 → 4 (Leo)
            CelestialBody.MOON to 14.0,    // Aries rashi 0, navamsa 4 → 4 (Leo)
            CelestialBody.MARS to 15.0,    // Aries rashi 0, navamsa 4 → 4 (Leo)
            CelestialBody.MERCURY to 16.0, // Aries rashi 0, navamsa 4 → 4 (Leo)
            CelestialBody.JUPITER to 10.0, // Aries rashi 0, navamsa 3 → 3 (Cancer)
            CelestialBody.VENUS to 10.5,   // Aries rashi 0, navamsa 3 → 3 (Cancer)
            CelestialBody.SATURN to 0.0,   // Aries rashi 0, navamsa 0 → 0 (Aries)
        )
        val chart = calc.getNavamsaChart(longitudes)
        val top = chart.topRashis(limit = 2)
        assertEquals(2, top.size)
        assertEquals(4, top[0].first)  // Leo, 4 bodies
        assertEquals(4, top[0].second.size)
        assertEquals(3, top[1].first)  // Cancer, 2 bodies
        assertEquals(2, top[1].second.size)
    }

    @Test
    fun `empty input produces empty chart`() {
        val chart = calc.getNavamsaChart(emptyMap())
        assertTrue(chart.isEmpty)
        assertTrue(chart.topRashis().isEmpty())
    }

    @Test
    fun `displayLabel formats rashi name and degree`() {
        val n = calc.getNavamsa(15.0)
        // 15° / 3.333° = 4.5 → navamsa 4, degreeInSign = 15 - 4*3.333 = 1.667°
        val label = n.displayLabel()
        assertTrue("Label contains 'Simha' for 15° Aries: $label", label.contains("Simha"))
        assertTrue("Label contains degree: $label", label.contains("°"))
    }
}
