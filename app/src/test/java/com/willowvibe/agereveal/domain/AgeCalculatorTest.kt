package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AgeCalculatorTest {

    private lateinit var calculator: AgeCalculator

    @Before
    fun setUp() {
        val astronomy = AstronomicalCalculator()
        val zodiac = ZodiacCalculator(astronomy)
        val nakshatra = NakshatraCalculator(astronomy)
        calculator = AgeCalculator(zodiac, nakshatra)
    }

    // -------------------------------------------------------------------------
    // Basic age components
    // -------------------------------------------------------------------------

    @Test
    fun `age components on exact birthday are years=N months=0 days=0`() {
        val birth = LocalDate.of(1990, 6, 15)
        val today = LocalDate.of(2025, 6, 15)
        val result = calculator.calculate(birth, today = today)
        assertEquals(35, result.years)
        assertEquals(0, result.months)
        assertEquals(0, result.days)
    }

    @Test
    fun `age components one day after birthday`() {
        val birth = LocalDate.of(1990, 6, 15)
        val today = LocalDate.of(2025, 6, 16)
        val result = calculator.calculate(birth, today = today)
        assertEquals(35, result.years)
        assertEquals(0, result.months)
        assertEquals(1, result.days)
    }

    @Test
    fun `total days is correct`() {
        val birth = LocalDate.of(2000, 1, 1)
        val today = LocalDate.of(2000, 1, 11)
        val result = calculator.calculate(birth, today = today)
        assertEquals(10L, result.totalDays)
    }

    @Test
    fun `total hours is 24x total days`() {
        val birth = LocalDate.of(2000, 1, 1)
        val today = LocalDate.of(2000, 1, 6)
        val result = calculator.calculate(birth, today = today)
        assertEquals(5L * 24L, result.totalHours)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `future birth date throws IllegalArgumentException`() {
        calculator.calculate(LocalDate.now().plusDays(1))
    }

    // -------------------------------------------------------------------------
    // Next birthday
    // -------------------------------------------------------------------------

    @Test
    fun `days to next birthday is 0 on the birthday`() {
        val birth = LocalDate.of(1990, 8, 20)
        val today = LocalDate.of(2025, 8, 20)
        val result = calculator.calculate(birth, today = today)
        assertEquals(0L, result.daysToNextBirthday)
    }

    @Test
    fun `days to next birthday is 365 the day after birthday in non-leap year`() {
        val birth = LocalDate.of(1990, 6, 15)
        val today = LocalDate.of(2025, 6, 16) // 2025 not a leap year
        val result = calculator.calculate(birth, today = today)
        assertEquals(364L, result.daysToNextBirthday)
    }

    @Test
    fun `next birthday for feb 29 in non-leap year is march 1`() {
        val birth = LocalDate.of(2000, 2, 29)
        val today = LocalDate.of(2025, 3, 2) // day after the safe birthday (Mar 1)
        val result = calculator.calculate(birth, today = today)
        // Next birthday should be Mar 1, 2026
        assertEquals(LocalDate.of(2026, 3, 1), result.nextBirthdayDate)
    }

    @Test
    fun `next birthday for feb 29 before safe date is march 1 same year`() {
        val birth = LocalDate.of(2000, 2, 29)
        val today = LocalDate.of(2025, 2, 28)
        val result = calculator.calculate(birth, today = today)
        assertEquals(LocalDate.of(2025, 3, 1), result.nextBirthdayDate)
    }

    // -------------------------------------------------------------------------
    // Milestones
    // -------------------------------------------------------------------------

    @Test
    fun `milestone count matches expected targets`() {
        val milestones = calculator.getMilestones(LocalDate.of(2000, 1, 1))
        val expectedTargets = listOf(500, 1_000, 2_000, 3_000, 5_000, 7_000,
            10_000, 12_500, 15_000, 20_000, 25_000, 30_000)
        assertEquals(expectedTargets.size, milestones.size)
        assertEquals(expectedTargets, milestones.map { it.targetDays })
    }

    @Test
    fun `milestone date is birthdate plus target days`() {
        val birth = LocalDate.of(2000, 1, 1)
        val milestones = calculator.getMilestones(birth)
        milestones.forEach { m ->
            assertEquals(birth.plusDays(m.targetDays.toLong()), m.date)
        }
    }

    @Test
    fun `past milestone has isPast=true and negative daysAway`() {
        val birth = LocalDate.of(2000, 1, 1) // 1000th day = 2002-09-27
        val today = LocalDate.of(2025, 1, 1) // well past all early milestones
        val milestones = calculator.getMilestones(birth, today)
        val thousandth = milestones.first { it.targetDays == 1_000 }
        assertTrue(thousandth.isPast)
        assertTrue(thousandth.daysAway < 0)
    }

    @Test
    fun `future milestone has isPast=false and positive daysAway`() {
        val birth = LocalDate.of(2000, 1, 1)
        val today = LocalDate.of(2000, 1, 2) // only 1 day old
        val milestones = calculator.getMilestones(birth, today)
        milestones.forEach { m ->
            assertTrue("${m.targetDays} should be in future", !m.isPast)
            assertTrue(m.daysAway > 0)
        }
    }

    // -------------------------------------------------------------------------
    // Heartbeats
    // -------------------------------------------------------------------------

    @Test
    fun `estimated heartbeats is totalMinutes times 72`() {
        val birth = LocalDate.of(2000, 1, 1)
        val today = LocalDate.of(2000, 1, 2) // 1 day = 1440 minutes
        val result = calculator.calculate(birth, today = today, includeUnlocked = true)
        assertEquals(1440L * 72L, result.estimatedHeartbeats)
    }

    // -------------------------------------------------------------------------
    // Result fields
    // -------------------------------------------------------------------------

    @Test
    fun `dayOfWeekBorn is not blank`() {
        val result = calculator.calculate(LocalDate.of(1990, 6, 15))
        assertNotNull(result.dayOfWeekBorn)
        assertTrue(result.dayOfWeekBorn.isNotBlank())
    }

    @Test
    fun `isExact is false when no birth time supplied`() {
        val result = calculator.calculate(LocalDate.of(1990, 6, 15))
        assertTrue(!result.isExact)
    }
}
