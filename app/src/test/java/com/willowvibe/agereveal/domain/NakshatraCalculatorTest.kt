package com.willowvibe.agereveal.domain

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class NakshatraCalculatorTest {

    private lateinit var calculator: NakshatraCalculator

    private val allNakshatras = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni",
        "Uttara Phalguni", "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha",
        "Jyeshtha", "Moola", "Purva Ashadha", "Uttara Ashadha", "Shravana",
        "Dhanishtha", "Shatabhisha", "Purva Bhadrapada", "Uttara Bhadrapada", "Revati",
    )

    @Before
    fun setUp() {
        calculator = NakshatraCalculator(AstronomicalCalculator())
    }

    @Test
    fun `returns a non-empty string`() {
        val result = calculator.getNakshatra(LocalDate.of(1990, 6, 15))
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `result starts with a known nakshatra name`() {
        val result = calculator.getNakshatra(LocalDate.of(1990, 6, 15))
        val base = result.removeSuffix(" ⚠ Cusp")
        assertTrue("Unknown nakshatra: $result",
            allNakshatras.any { base.startsWith(it) })
    }

    @Test
    fun `result with birth time starts with a known nakshatra name`() {
        val result = calculator.getNakshatra(LocalDate.of(1990, 6, 15), LocalTime.of(14, 30))
        val base = result.removeSuffix(" ⚠ Cusp")
        assertTrue("Unknown nakshatra: $result",
            allNakshatras.any { base.startsWith(it) })
    }

    @Test
    fun `no exception thrown for any day in a full year`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.getNakshatra(start.plusDays(i.toLong()))
        }
    }

    @Test
    fun `no exception thrown for every hour of a single day`() {
        val date = LocalDate.of(1990, 6, 15)
        for (hour in 0..23) {
            calculator.getNakshatra(date, LocalTime.of(hour, 0))
        }
    }

    @Test
    fun `cusp flag appended as suffix when present`() {
        // Run every day of a year and verify the cusp suffix (if present) is the only addition
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            val result = calculator.getNakshatra(start.plusDays(i.toLong()))
            val hasCusp = result.endsWith(" ⚠ Cusp")
            val base = if (hasCusp) result.removeSuffix(" ⚠ Cusp") else result
            assertTrue("Invalid nakshatra format: $result",
                allNakshatras.any { base.startsWith(it) })
        }
    }
}
