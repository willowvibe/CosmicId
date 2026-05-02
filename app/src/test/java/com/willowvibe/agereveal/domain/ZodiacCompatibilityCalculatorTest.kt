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
    fun `same chinese year scores 88`() {
        // Both Rat (1900) — not a self-punishment sign
        val result = calculator.calculate(
            LocalDate.of(1900, 5, 1),
            LocalDate.of(1900, 9, 1),
        )
        assertEquals(88, result.chineseScore)
    }

    @Test
    fun `same chinese self punishment year scores 42`() {
        // Both Dragon (2000) — Dragon has self-punishment
        val result = calculator.calculate(
            LocalDate.of(2000, 5, 1),
            LocalDate.of(2000, 9, 1),
        )
        assertEquals(42, result.chineseScore)
    }

    @Test
    fun `chinese trine group scores 95`() {
        // Rat(0), Dragon(4), Monkey(8) are trine (index % 4 == 0)
        val result = calculator.calculate(
            LocalDate.of(1900, 3, 1),  // Rat year
            LocalDate.of(2000, 5, 1),  // Dragon year
        )
        assertEquals(95, result.chineseScore)
    }

    @Test
    fun `chinese six harmonies scores 92`() {
        // Rat(0) and Ox(1) are 六合
        val result = calculator.calculate(
            LocalDate.of(1900, 3, 1),  // Rat year
            LocalDate.of(1901, 3, 1),  // Ox year
        )
        assertEquals(92, result.chineseScore)
    }

    @Test
    fun `chinese clash scores 35`() {
        // Rat(0) and Horse(6) are opposite (diff=6)
        val result = calculator.calculate(
            LocalDate.of(1900, 3, 1),  // Rat year
            LocalDate.of(1918, 3, 1),  // Horse year
        )
        assertEquals(35, result.chineseScore)
    }

    @Test
    fun `chinese harm scores 45`() {
        // Rat(0) and Goat(7) are 相害
        val result = calculator.calculate(
            LocalDate.of(1900, 3, 1),  // Rat year
            LocalDate.of(1919, 3, 1),  // Goat year
        )
        assertEquals(45, result.chineseScore)
    }

    @Test
    fun `chinese punishment scores 40`() {
        // Tiger(2) and Snake(5) are 相刑
        val result = calculator.calculate(
            LocalDate.of(1902, 3, 1),  // Tiger year
            LocalDate.of(1905, 3, 1),  // Snake year
        )
        assertEquals(40, result.chineseScore)
    }

    @Test
    fun `chinese self punishment scores 42`() {
        // Dragon(4) with Dragon(4) has self-punishment
        val result = calculator.calculate(
            LocalDate.of(1904, 3, 1),  // Dragon year
            LocalDate.of(1916, 3, 1),  // Dragon year
        )
        assertEquals(42, result.chineseScore)
    }

    @Test
    fun `chinese relationship label is present`() {
        val result = calculator.calculate(
            LocalDate.of(1900, 3, 1),
            LocalDate.of(1901, 3, 1),
        )
        assertTrue("Label should not be blank: ${result.chineseRelationshipLabel}",
            result.chineseRelationshipLabel.isNotBlank())
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
        // Aries + Leo: western trine = 95
        // Rat(1900) + Dragon(2000): Chinese trine = 95
        // overall = 95 → "Cosmic Soulmates"
        val result = calculator.calculate(
            LocalDate.of(1900, 4, 1),  // Aries, Rat year (after CNY Jan 31)
            LocalDate.of(2000, 8, 1),  // Leo, Dragon year (after CNY Feb 5)
        )
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
