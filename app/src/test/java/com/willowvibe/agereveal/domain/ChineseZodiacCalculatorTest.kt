package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [ChineseZodiacCalculator] — focused coverage of the
 * Lunar-New-Year cutoff behavior and the 60-cycle stem-branch output.
 */
class ChineseZodiacCalculatorTest {

    private lateinit var calculator: ChineseZodiacCalculator

    @Before
    fun setUp() {
        calculator = ChineseZodiacCalculator()
    }

    // ---------------------------------------------------------------------------
    // Lunar New Year cutoff
    // ---------------------------------------------------------------------------

    @Test
    fun `date after CNY belongs to current year`() {
        // 2000-02-15 is after 2000 CNY (Feb 5). Should be Dragon (year 2000).
        assertEquals("🐉 Dragon", calculator.getZodiac(LocalDate.of(2000, 2, 15)))
    }

    @Test
    fun `date before CNY belongs to previous year`() {
        // 2000-01-25 is before 2000 CNY (Feb 5). Should be Rabbit (year 1999).
        // 1999 is Rabbit in the 12-year cycle.
        assertEquals("🐇 Rabbit", calculator.getZodiac(LocalDate.of(2000, 1, 25)))
    }

    @Test
    fun `chinese year shifts back before CNY`() {
        assertEquals(2000, calculator.getChineseYear(LocalDate.of(2000, 2, 15)))
        assertEquals(1999, calculator.getChineseYear(LocalDate.of(2000, 1, 25)))
    }

    // ---------------------------------------------------------------------------
    // Stem-branch + element
    // ---------------------------------------------------------------------------

    @Test
    fun `stem branch contains stem and branch`() {
        val stemBranch = calculator.getStemBranch(LocalDate.of(2000, 2, 15))
        // 2000 = 庚辰 (Geng-Chen), Metal-Dragon.
        // Format is "Geng-Chen / Metal-Dragon"
        assertEquals("庚 Geng-辰 Chen / Metal-Dragon", stemBranch)
    }

    @Test
    fun `stem branch for 2024 is Yang Wood Dragon`() {
        // 2024 = 甲辰 (Jia-Chen), Wood-Dragon.
        assertEquals("甲 Jia-辰 Chen / Wood-Dragon", calculator.getStemBranch(LocalDate.of(2024, 2, 15)))
    }

    // ---------------------------------------------------------------------------
    // 12-year cycle wraps correctly
    // ---------------------------------------------------------------------------

    @Test
    fun `12-year cycle is consistent`() {
        // 2000 Dragon, 2012 Dragon (2000 + 12)
        assertEquals("🐉 Dragon", calculator.getZodiac(LocalDate.of(2012, 2, 15)))
        // 1988 Dragon (2000 - 12). Use March to be safely after 1988 CNY (Feb 17).
        assertEquals("🐉 Dragon", calculator.getZodiac(LocalDate.of(1988, 6, 1)))
    }
}
