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
            if (part == "Mahadasha" || part == "Antardasha") continue
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
}
