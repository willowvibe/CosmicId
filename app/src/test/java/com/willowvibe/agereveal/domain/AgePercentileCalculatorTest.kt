package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AgePercentileCalculatorTest {

    private lateinit var calculator: AgePercentileCalculator

    @Before
    fun setUp() {
        calculator = AgePercentileCalculator()
    }

    @Test
    fun `newborn has 0 percentile`() {
        val result = calculator.calculate(0)
        assertTrue(result.percentileText.contains("0%"))
    }

    @Test
    fun `age 30 returns approximately 43 percent`() {
        val pct = calculator.computePercentile(30)
        assertEquals(43.4, pct, 0.1)
    }

    @Test
    fun `age 50 returns approximately 64 percent`() {
        val pct = calculator.computePercentile(50)
        assertEquals(64.3, pct, 0.1)
    }

    @Test
    fun `age 70 returns approximately 81 percent`() {
        val pct = calculator.computePercentile(70)
        assertEquals(80.8, pct, 0.1)
    }

    @Test
    fun `age 23 interpolates between 20 and 25 brackets`() {
        val pct = calculator.computePercentile(23)
        // Linear interpolation between 30.3 (age 20) and 37.1 (age 25)
        // fraction = 3/5 = 0.6
        // expected = 30.3 + 0.6 * (37.1 - 30.3) = 30.3 + 4.08 = 34.38
        assertEquals(34.38, pct, 0.01)
    }

    @Test
    fun `negative age returns 0`() {
        assertEquals(0.0, calculator.computePercentile(-5), 0.0)
    }

    @Test
    fun `very high age caps at last data point`() {
        val pct = calculator.computePercentile(120)
        assertEquals(97.0, pct, 0.0)
    }

    @Test
    fun `result contains percentile text and shared birth date estimate`() {
        val result = calculator.calculate(25)
        assertTrue("Expected percentile text", result.percentileText.isNotEmpty())
        assertTrue("Expected shared birth date", result.sharedBirthDateEstimate.isNotEmpty())
        assertTrue("Expected '~22M' in estimate", result.sharedBirthDateEstimate.contains("22"))
    }
}
