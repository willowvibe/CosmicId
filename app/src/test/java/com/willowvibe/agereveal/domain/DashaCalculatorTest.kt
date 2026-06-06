package com.willowvibe.agereveal.domain

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DashaCalculatorTest {

    private lateinit var calculator: DashaCalculator

    @Before
    fun setUp() {
        calculator = DashaCalculator(AstronomicalCalculator())
    }

    // -------------------------------------------------------------------------
    // Format & content
    // -------------------------------------------------------------------------

    @Test
    fun `dasha info contains Mahadasha keyword`() {
        val info = calculator.getDashaInfo(LocalDate.of(1990, 6, 15))
        assertTrue("Expected 'Mahadasha' in: $info", info.contains("Mahadasha"))
    }

    @Test
    fun `dasha info contains Antardasha keyword`() {
        val info = calculator.getDashaInfo(LocalDate.of(1990, 6, 15))
        assertTrue("Expected 'Antardasha' in: $info", info.contains("Antardasha"))
    }

    @Test
    fun `dasha info lists known lords only`() {
        val info = calculator.getDashaInfo(LocalDate.of(1990, 6, 15))
        val knownLords = listOf("Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury")
        val parts = info.split(" · ", " ")
        for (part in parts) {
            if (part in setOf("Mahadasha", "Antardasha", "Pratyantar", "")) continue
            assertTrue("Unknown lord token '$part' in: $info", knownLords.any { part.contains(it) })
        }
    }

    // -------------------------------------------------------------------------
    // Determinism & stability
    // -------------------------------------------------------------------------

    @Test
    fun `same date produces identical result`() {
        val d1 = LocalDate.of(1990, 6, 15)
        val r1 = calculator.getDashaInfo(d1)
        val r2 = calculator.getDashaInfo(d1)
        assertTrue("Results should be identical: '$r1' vs '$r2'", r1 == r2)
    }

    @Test
    fun `result changes when today is far in the future`() {
        val birth = LocalDate.of(1990, 6, 15)
        val now1 = LocalDate.of(2020, 1, 1)
        val now2 = LocalDate.of(2050, 1, 1)
        val r1 = calculator.getDashaInfo(birth, today = now1)
        val r2 = calculator.getDashaInfo(birth, today = now2)
        assertTrue("Dasha should change over 30 years: '$r1' vs '$r2'", r1 != r2)
    }

    // -------------------------------------------------------------------------
    // No crash across full year sweep
    // -------------------------------------------------------------------------

    @Test
    fun `no exception thrown for any day in a full year`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.getDashaInfo(start.plusDays(i.toLong()))
        }
    }

    @Test
    fun `no exception thrown for edge dates`() {
        // Feb 29 leap year
        calculator.getDashaInfo(LocalDate.of(2000, 2, 29))
        // Very old date
        calculator.getDashaInfo(LocalDate.of(1900, 1, 1))
        // Recent date
        calculator.getDashaInfo(LocalDate.of(2024, 6, 1))
    }

    // -------------------------------------------------------------------------
    // Cycle completeness — every lord should appear as Mahadasha somewhere
    // -------------------------------------------------------------------------

    @Test
    fun `all nine dasha lords appear as Mahadasha across 120 years`() {
        val birth = LocalDate.of(1900, 1, 1)
        val seen = mutableSetOf<String>()
        // Sample every 5 years across a 120-year span
        for (years in 0 until 120 step 5) {
            val today = birth.plusYears(years.toLong())
            val info = calculator.getDashaInfo(birth, today = today)
            val mahadasha = info.substringBefore(" Mahadasha").trim()
            seen.add(mahadasha)
        }
        val expected = setOf("Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury")
        assertTrue("Expected all 9 lords as Mahadasha, missing: ${expected - seen}", seen.containsAll(expected))
    }

    // -------------------------------------------------------------------------
    // Structured form (Phase 6.5: Pratyantar Dasha added)
    // -------------------------------------------------------------------------

    @Test
    fun `structured DashaInfo contains Mahadasha Antardasha and Pratyantar`() {
        val info = calculator.getDashaDetail(LocalDate.of(1990, 6, 15), today = LocalDate.of(2020, 1, 1))
        assertTrue("Mahadasha has a lord", info.mahadasha.lord.isNotBlank())
        assertTrue("Antardasha has a lord", info.antardasha.lord.isNotBlank())
        assertTrue("Pratyantar has a lord", info.pratyantar.lord.isNotBlank())
    }

    @Test
    fun `Mahadasha years elapsed plus remaining equals total`() {
        val info = calculator.getDashaDetail(LocalDate.of(1990, 6, 15), today = LocalDate.of(2020, 1, 1))
        val maha = info.mahadasha
        assertTrue(
            "Mahadasha elapsed (${maha.yearsElapsed}) + remaining (${maha.yearsRemaining}) should ≈ total (${maha.totalYears}); got diff ${kotlin.math.abs((maha.yearsElapsed + maha.yearsRemaining) - maha.totalYears)}",
            kotlin.math.abs((maha.yearsElapsed + maha.yearsRemaining) - maha.totalYears) < 0.01,
        )
    }

    @Test
    fun `Pratyantar is a sub-period of the Antardasha`() {
        val info = calculator.getDashaDetail(LocalDate.of(1990, 6, 15), today = LocalDate.of(2020, 1, 1))
        // Pratyantar total years = Antardasha total years * (sub-lord fraction of 120y).
        // Sanity check: pratyantar total < antardasha total (because sub-lord fraction < 1).
        assertTrue(
            "Pratyantar (${info.pratyantar.totalYears}y) should be < Antardasha (${info.antardasha.totalYears}y)",
            info.pratyantar.totalYears < info.antardasha.totalYears,
        )
    }

    @Test
    fun `DashaInfo summary contains all three level names`() {
        val info = calculator.getDashaDetail(LocalDate.of(1990, 6, 15), today = LocalDate.of(2020, 1, 1))
        val s = info.summary()
        assertTrue("Summary contains Mahadasha: $s", s.contains("Mahadasha"))
        assertTrue("Summary contains Antardasha: $s", s.contains("Antardasha"))
        assertTrue("Summary contains Pratyantar: $s", s.contains("Pratyantar"))
    }
}
