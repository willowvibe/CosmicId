package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [DailyFortuneGenerator] — verifies the fortune is
 * deterministic for the same (date, birthDate) pair, the message pool
 * doesn't crash, and the entertainment disclaimer (BUG-088) is surfaced.
 */
class DailyFortuneGeneratorTest {

    private lateinit var generator: DailyFortuneGenerator

    @Before
    fun setUp() {
        val astronomy = AstronomicalCalculator()
        generator = DailyFortuneGenerator(
            astronomicalCalculator = astronomy,
            moonPhaseCalculator = MoonPhaseCalculator(),
            zodiacCalculator = ZodiacCalculator(astronomy),
        )
    }

    @Test
    fun `fortune contains required fields`() {
        val fortune = generator.generate(LocalDate.of(1990, 6, 15), today = LocalDate.of(2026, 6, 5))
        assertNotNull(fortune.headline)
        assertNotNull(fortune.body)
        assertNotNull(fortune.emoji)
        assertNotNull(fortune.moonPhase)
        assertNotNull(fortune.sunSign)
        assertNotNull(fortune.stemBranch)
        assertTrue("Lucky number in 1..99", fortune.luckyNumber in 1..99)
        assertNotNull(fortune.luckyColor)
    }

    @Test
    fun `fortune is deterministic for the same date pair — BUG-056 regression guard`() {
        val a = generator.generate(LocalDate.of(1990, 6, 15), today = LocalDate.of(2026, 6, 5))
        val b = generator.generate(LocalDate.of(1990, 6, 15), today = LocalDate.of(2026, 6, 5))
        assertEquals(a, b)
    }

    @Test
    fun `fortune is entertainment-flagged — BUG-088`() {
        val fortune = generator.generate(LocalDate.of(1990, 6, 15), today = LocalDate.of(2026, 6, 5))
        assertTrue("Fortune should be marked as entertainment", fortune.isEntertainment)
        assertEquals(
            DailyFortuneGenerator.DEFAULT_DISCLAIMER,
            fortune.disclaimer,
        )
        assertTrue(
            "Disclaimer should mention entertainment: ${fortune.disclaimer}",
            fortune.disclaimer.contains("entertainment", ignoreCase = true),
        )
    }

    @Test
    fun `lucky number is in 1 to 99 range`() {
        // Sweep a few dates to make sure the formula doesn't escape its range
        for (i in 0..30) {
            val today = LocalDate.of(2026, 1, 1).plusDays(i.toLong())
            val fortune = generator.generate(LocalDate.of(1990, 6, 15), today = today)
            assertTrue("Lucky number out of range: ${fortune.luckyNumber}", fortune.luckyNumber in 1..99)
        }
    }

    @Test
    fun `no exception thrown for any day in a year sweep`() {
        val start = LocalDate.of(2026, 1, 1)
        for (i in 0..364) {
            generator.generate(LocalDate.of(1990, 6, 15), today = start.plusDays(i.toLong()))
        }
    }
}
