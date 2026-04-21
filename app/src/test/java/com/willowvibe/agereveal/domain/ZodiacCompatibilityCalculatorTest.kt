package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ZodiacCompatibilityCalculatorTest {

    private lateinit var calculator: ZodiacCompatibilityCalculator
    private lateinit var zodiacCalculator: ZodiacCalculator

    @Before
    fun setUp() {
        zodiacCalculator = ZodiacCalculator(AstronomicalCalculator())
        calculator = ZodiacCompatibilityCalculator(zodiacCalculator)
    }

    // -------------------------------------------------------------------------
    // Result completeness
    // -------------------------------------------------------------------------

    @Test
    fun `calculate returns non-null result`() {
        val result = calculator.calculate(
            LocalDate.of(1990, 6, 15),
            LocalDate.of(1992, 3, 10),
        )
        assertNotNull(result)
    }

    @Test
    fun `result preserves provided names`() {
        val result = calculator.calculate(
            LocalDate.of(1990, 6, 15),
            LocalDate.of(1992, 3, 10),
            nameA = "Alice",
            nameB = "Bob",
        )
        assertEquals("Alice", result.nameA)
        assertEquals("Bob", result.nameB)
    }

    @Test
    fun `overall score is between 0 and 100`() {
        val result = calculator.calculate(
            LocalDate.of(1990, 6, 15),
            LocalDate.of(1992, 3, 10),
        )
        assertTrue("Score out of range: ${result.overallScore}",
            result.overallScore in 0..100)
    }

    @Test
    fun `western and chinese scores are between 0 and 100`() {
        val result = calculator.calculate(
            LocalDate.of(1990, 6, 15),
            LocalDate.of(1992, 3, 10),
        )
        assertTrue(result.westernScore in 0..100)
        assertTrue(result.chineseScore in 0..100)
    }

    @Test
    fun `overall score is average of western and chinese`() {
        val result = calculator.calculate(
            LocalDate.of(1990, 6, 15),
            LocalDate.of(1992, 3, 10),
        )
        val expected = (result.westernScore * 0.5 + result.chineseScore * 0.5).toInt()
        assertEquals(expected, result.overallScore)
    }

    // -------------------------------------------------------------------------
    // Western compatibility scores
    // -------------------------------------------------------------------------

    @Test
    fun `same western sign scores 85`() {
        // Both Gemini (May 21 – Jun 20)
        val result = calculator.calculate(
            LocalDate.of(1990, 5, 25),
            LocalDate.of(1992, 6, 10),
        )
        assertEquals(85, result.westernScore)
    }

    @Test
    fun `trine signs score 95`() {
        // Aries (index 0) and Leo (index 4) — diff = 4 → trine
        val result = calculator.calculate(
            LocalDate.of(1990, 4, 1),  // Aries
            LocalDate.of(1992, 8, 1),  // Leo
        )
        assertEquals(95, result.westernScore)
    }

    // -------------------------------------------------------------------------
    // Chinese compatibility scores
    // -------------------------------------------------------------------------

    @Test
    fun `same chinese year scores 85`() {
        // Both Dragon (2000)
        val result = calculator.calculate(
            LocalDate.of(2000, 5, 1),
            LocalDate.of(2000, 9, 1),
        )
        assertEquals(85, result.chineseScore)
    }

    @Test
    fun `chinese trine group scores 92`() {
        // Rat(0), Dragon(4), Monkey(8) are trine (index % 4 == 0)
        // Rat 1900 (after CNY) and Dragon 2000 (after CNY)
        val result = calculator.calculate(
            LocalDate.of(1900, 3, 1),  // Rat year
            LocalDate.of(2000, 5, 1),  // Dragon year
        )
        assertEquals(92, result.chineseScore)
    }

    // -------------------------------------------------------------------------
    // Headline
    // -------------------------------------------------------------------------

    @Test
    fun `headline is not blank`() {
        val result = calculator.calculate(
            LocalDate.of(1990, 6, 15),
            LocalDate.of(1992, 3, 10),
        )
        assertTrue(result.headline.isNotBlank())
    }

    @Test
    fun `score 90+ gives cosmic soulmates headline`() {
        // Aries + Leo: westernScore = 95, use Chinese same year for high combined score
        val result = calculator.calculate(
            LocalDate.of(2000, 4, 1),  // Aries, Dragon
            LocalDate.of(2000, 8, 1),  // Leo, Dragon
        )
        // westernScore=95, chineseScore=85 → overall=90 → "Cosmic Soulmates"
        assertTrue("Unexpected headline: ${result.headline}",
            result.headline.contains("Cosmic Soulmates"))
    }

    // -------------------------------------------------------------------------
    // Description
    // -------------------------------------------------------------------------

    @Test
    fun `description is not blank`() {
        val result = calculator.calculate(
            LocalDate.of(1990, 6, 15),
            LocalDate.of(1992, 3, 10),
        )
        assertTrue(result.description.isNotBlank())
    }

    @Test
    fun `fire+air pair produces specific description`() {
        val result = calculator.calculate(
            LocalDate.of(1990, 4, 1),  // Aries — Fire
            LocalDate.of(1992, 2, 1),  // Aquarius — Air
        )
        assertTrue("Expected Fire+Air description, got: ${result.description}",
            result.description.contains("Fire") && result.description.contains("Air"))
    }
}
