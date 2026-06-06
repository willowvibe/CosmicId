package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.BirthChart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Unit tests for [BirthChart.compute] — verifies the assembled chart
 * contains all expected fields and that the new birth-moon-phase wiring
 * (Phase 6.5+) produces a valid [MoonPhase].
 */
class BirthChartTest {

    @Test
    fun `compute produces chart with all primary fields populated`() {
        val chart = BirthChart.compute(
            birthDate = LocalDate.of(1990, 6, 15),
            birthTime = LocalTime.of(12, 0),
            zoneOffset = ZoneOffset.UTC,
        )
        assertNotNull("Snapshot should be present", chart.snapshot)
        assertNotNull("Western sign", chart.westernSign)
        assertNotNull("Rashi", chart.rashi)
        assertNotNull("Moon sign", chart.westernMoonSign)
        assertNotNull("Nakshatra", chart.nakshatra)
        assertNotNull("Tithi", chart.tithi)
        assertNotNull("Chinese zodiac", chart.chineseZodiac)
        assertNotNull("Stem-branch", chart.chineseStemBranch)
        assertNotNull("Dasha info", chart.dashaInfo)
        assertNotNull("BaZi info", chart.baZiInfo)
    }

    @Test
    fun `compute wires MoonPhaseCalculator — BUG-086`() {
        val chart = BirthChart.compute(
            birthDate = LocalDate.of(1990, 6, 15),
            birthTime = LocalTime.of(12, 0),
            zoneOffset = ZoneOffset.UTC,
        )
        val phase = chart.birthMoonPhase
        assertNotNull("Birth moon phase should not be null (BUG-086)", phase)
        // The phase name should be one of the 8 standard moon phases
        assertTrue(
            "Moon phase name '${phase!!.name}' should be a standard phase",
            phase.name in setOf(
                "New Moon", "Waxing Crescent", "First Quarter", "Waxing Gibbous",
                "Full Moon", "Waning Gibbous", "Last Quarter", "Waning Crescent",
            ),
        )
    }

    @Test
    fun `birth moon phase illumination is between 0 and 1`() {
        val chart = BirthChart.compute(
            birthDate = LocalDate.of(2000, 1, 1),
            birthTime = LocalTime.of(0, 0),
            zoneOffset = ZoneOffset.UTC,
        )
        val phase = chart.birthMoonPhase!!
        assertTrue(
            "Illumination ${phase.illuminationFraction} should be in [0, 1]",
            phase.illuminationFraction in 0.0..1.0,
        )
    }

    @Test
    fun `birth moon phase age is within synodic month`() {
        val chart = BirthChart.compute(
            birthDate = LocalDate.of(2000, 1, 1),
            birthTime = LocalTime.of(0, 0),
            zoneOffset = ZoneOffset.UTC,
        )
        val phase = chart.birthMoonPhase!!
        // Synodic month ≈ 29.53 days
        assertTrue(
            "Age ${phase.ageDays} should be in [0, 29.53]",
            phase.ageDays in 0.0..29.53,
        )
    }

    @Test
    fun `chart summary contains key fields`() {
        val chart = BirthChart.compute(
            birthDate = LocalDate.of(1990, 6, 15),
            birthTime = LocalTime.of(12, 0),
            zoneOffset = ZoneOffset.UTC,
        )
        val summary = chart.toSummary()
        assertTrue("Summary should mention Sun: $summary", summary.contains("Sun:"))
        assertTrue("Summary should mention Moon: $summary", summary.contains("Moon:"))
        assertTrue("Summary should mention Dasha: $summary", summary.contains("Dasha:"))
    }

    @Test
    fun `jd from chart matches astronomy Julian Day`() {
        val chart = BirthChart.compute(
            birthDate = LocalDate.of(2000, 1, 1),
            birthTime = LocalTime.of(12, 0),
            zoneOffset = ZoneOffset.UTC,
        )
        // J2000.0 = 2451545.0
        assertEquals(2451545.0, chart.jd, 0.01)
    }
}
