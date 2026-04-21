package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ZodiacCalculatorTest {

    private lateinit var calculator: ZodiacCalculator

    @Before
    fun setUp() {
        calculator = ZodiacCalculator(AstronomicalCalculator())
    }

    // -------------------------------------------------------------------------
    // Western zodiac (date-based boundary checks)
    // -------------------------------------------------------------------------

    @Test fun `aries starts march 21`()   { assertEquals("Aries ♈",       calculator.getWesternZodiac(3, 21)) }
    @Test fun `aries ends april 19`()     { assertEquals("Aries ♈",       calculator.getWesternZodiac(4, 19)) }
    @Test fun `taurus starts april 20`()  { assertEquals("Taurus ♉",      calculator.getWesternZodiac(4, 20)) }
    @Test fun `taurus ends may 20`()      { assertEquals("Taurus ♉",      calculator.getWesternZodiac(5, 20)) }
    @Test fun `gemini starts may 21`()    { assertEquals("Gemini ♊",      calculator.getWesternZodiac(5, 21)) }
    @Test fun `cancer starts june 21`()   { assertEquals("Cancer ♋",      calculator.getWesternZodiac(6, 21)) }
    @Test fun `leo starts july 23`()      { assertEquals("Leo ♌",         calculator.getWesternZodiac(7, 23)) }
    @Test fun `virgo starts aug 23`()     { assertEquals("Virgo ♍",       calculator.getWesternZodiac(8, 23)) }
    @Test fun `libra starts sep 23`()     { assertEquals("Libra ♎",       calculator.getWesternZodiac(9, 23)) }
    @Test fun `scorpio starts oct 23`()   { assertEquals("Scorpio ♏",     calculator.getWesternZodiac(10, 23)) }
    @Test fun `sagittarius starts nov 22`(){ assertEquals("Sagittarius ♐", calculator.getWesternZodiac(11, 22)) }
    @Test fun `capricorn starts dec 22`() { assertEquals("Capricorn ♑",   calculator.getWesternZodiac(12, 22)) }
    @Test fun `aquarius starts jan 20`()  { assertEquals("Aquarius ♒",    calculator.getWesternZodiac(1, 20)) }
    @Test fun `pisces starts feb 19`()    { assertEquals("Pisces ♓",      calculator.getWesternZodiac(2, 19)) }
    @Test fun `pisces ends march 20`()    { assertEquals("Pisces ♓",      calculator.getWesternZodiac(3, 20)) }

    // -------------------------------------------------------------------------
    // Chinese zodiac — Lunar New Year boundary (BUG-004 regression)
    // -------------------------------------------------------------------------

    @Test
    fun `born before CNY 2000 is Rabbit not Dragon`() {
        // CNY 2000 = Feb 5 — Jan 25 is still the Year of the Rabbit (1999)
        val sign = calculator.getChineseZodiac(LocalDate.of(2000, 1, 25))
        assertTrue("Expected Rabbit, got: $sign", sign.contains("Rabbit"))
    }

    @Test
    fun `born on CNY 2000 is Dragon`() {
        val sign = calculator.getChineseZodiac(LocalDate.of(2000, 2, 5))
        assertTrue("Expected Dragon, got: $sign", sign.contains("Dragon"))
    }

    @Test
    fun `born after CNY 2000 is Dragon`() {
        val sign = calculator.getChineseZodiac(LocalDate.of(2000, 3, 1))
        assertTrue("Expected Dragon, got: $sign", sign.contains("Dragon"))
    }

    @Test
    fun `born before CNY 2019 is Dog not Pig`() {
        // CNY 2019 = Feb 5 — Jan 1 2019 should still be Dog (2018)
        val sign = calculator.getChineseZodiac(LocalDate.of(2019, 1, 1))
        assertTrue("Expected Dog, got: $sign", sign.contains("Dog"))
    }

    @Test
    fun `born after CNY 2019 is Pig`() {
        val sign = calculator.getChineseZodiac(LocalDate.of(2019, 2, 6))
        assertTrue("Expected Pig, got: $sign", sign.contains("Pig"))
    }

    @Test
    fun `year 1900 march is Rat`() {
        // 1900 is year 0 in the cycle — Rat
        val sign = calculator.getChineseZodiac(LocalDate.of(1900, 3, 1))
        assertTrue("Expected Rat, got: $sign", sign.contains("Rat"))
    }

    @Test
    fun `chinese zodiac cycle repeats every 12 years`() {
        val date1 = LocalDate.of(2000, 6, 1)
        val date2 = LocalDate.of(2012, 6, 1)
        assertEquals(
            calculator.getChineseZodiac(date1),
            calculator.getChineseZodiac(date2),
        )
    }

    // -------------------------------------------------------------------------
    // Rashi cusp detection (BUG-005)
    // -------------------------------------------------------------------------

    @Test
    fun `getRashi returns a non-empty string`() {
        val rashi = calculator.getRashi(LocalDate.of(1990, 6, 15))
        assertTrue(rashi.isNotBlank())
    }

    @Test
    fun `getRashi cusp marker is present or absent — no crash`() {
        // Exercise many dates to ensure no crash; cusp detection must not throw
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            val d = start.plusDays(i.toLong())
            calculator.getRashi(d) // must not throw
        }
    }
}
