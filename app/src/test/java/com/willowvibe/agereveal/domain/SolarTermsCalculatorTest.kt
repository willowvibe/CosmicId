package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SolarTermsCalculatorTest {

    private lateinit var calculator: SolarTermsCalculator

    @Before
    fun setUp() {
        calculator = SolarTermsCalculator()
    }

    // -------------------------------------------------------------------------
    // Catalog: 24 terms, 12 month-starts, correct Hanji/Hangul labels
    // -------------------------------------------------------------------------

    @Test
    fun `year 2024 has 24 solar terms in UTC`() {
        val terms = calculator.getTermsForYear(2024)
        assertEquals(24, terms.size)
    }

    @Test
    fun `year 2024 has exactly 12 month-starting 節`() {
        val jie = calculator.getTermsForYear(2024).filter { it.isMonthStart }
        assertEquals(12, jie.size)
    }

    @Test
    fun `year 2024 has 12 mid-month 中氣`() {
        val qi = calculator.getTermsForYear(2024).filter { it.isMidMonth }
        assertEquals(12, qi.size)
    }

    @Test
    fun `month starts include 입춘, 경칩, 청명, 입하 for 2024`() {
        val jie = calculator.getMonthStartsForYear(2024)
        val hanjas = jie.map { it.hanja }.toSet()
        assertTrue("Should include 입춘: $hanjas", "立春" in hanjas)
        assertTrue("Should include 경칩 (simplified Hanji 惊蛰): $hanjas", "惊蛰" in hanjas)
        assertTrue("Should include 청명: $hanjas", "清明" in hanjas)
        assertTrue("Should include 입하: $hanjas", "立夏" in hanjas)
    }

    @Test
    fun `입춘 2024 occurs on Feb 4`() {
        val term = calculator.getTermsForYear(2024).first { it.hanja == "立春" }
        assertEquals(LocalDate.of(2024, 2, 4), term.solarDate)
        assertEquals("입춘", term.hangul)
        assertTrue("입춘 is the year-pillar boundary, must be a month-start 節",
            term.isMonthStart)
    }

    @Test
    fun `冬至 2024 occurs on Dec 21`() {
        val term = calculator.getTermsForYear(2024).first { it.hanja == "冬至" }
        assertEquals(LocalDate.of(2024, 12, 21), term.solarDate)
        assertEquals("동지", term.hangul)
        assertTrue("동지 is a mid-month 氣, not a month start", term.isMidMonth)
    }

    // -------------------------------------------------------------------------
    // nextJieAfter / prevJieOnOrBefore / currentJie
    // -------------------------------------------------------------------------

    @Test
    fun `next jie after Jan 15 2024 is 대한 on Jan 20`() {
        // The 12 節 cycle runs 입춘 → … → 대한 (12th month). On Jan 15 the
        // current month hasn't yet started (next 節 is the upcoming one).
        val next = calculator.nextJieAfter(LocalDate.of(2024, 1, 15))
        assertNotNull(next)
        assertEquals("大寒", next!!.hanja)
        assertEquals(LocalDate.of(2024, 1, 20), next.solarDate)
    }

    @Test
    fun `prev jie on or before Mar 10 2024 is 경칩 on Mar 5`() {
        val prev = calculator.prevJieOnOrBefore(LocalDate.of(2024, 3, 10))
        assertNotNull(prev)
        assertEquals("惊蛰", prev!!.hanja)
        assertEquals(LocalDate.of(2024, 3, 5), prev.solarDate)
    }

    @Test
    fun `current jie for Jun 1 2024 is 망종 from May 5`() {
        // Jun 1 2024 is between 입하 (May 5) and 망종 (Jun 5) → current jie is 입하
        val current = calculator.currentJie(LocalDate.of(2024, 6, 1))
        assertNotNull(current)
        assertEquals("立夏", current!!.hanja)
    }

    @Test
    fun `current jie for Dec 25 2024 is 대설 from Dec 6`() {
        val current = calculator.currentJie(LocalDate.of(2024, 12, 25))
        assertNotNull(current)
        assertEquals("大雪", current!!.hanja)
    }

    // -------------------------------------------------------------------------
    // Korean-school Daeun rule: daysToNextJie / 3 ≈ start age
    // -------------------------------------------------------------------------

    @Test
    fun `days to next jie from Jan 1 2024 is 19 (Jan 20 minus Jan 1)`() {
        val days = calculator.daysToNextJie(LocalDate.of(2024, 1, 1))
        assertEquals(19L, days)
    }

    @Test
    fun `days to next jie from Sruthi birth date is positive and small`() {
        // Sruthi was born 1993-12-11 → next 節 is 大寒 1994 (~Jan 20)
        val days = calculator.daysToNextJie(LocalDate.of(1993, 12, 11))
        assertNotNull(days)
        assertTrue("Days to next 節 should be small (≤ 60), got: $days", days!! in 1L..60L)
    }

    // -------------------------------------------------------------------------
    // No-crash stress test
    // -------------------------------------------------------------------------

    @Test
    fun `no exception for terms lookup over a 5-year window`() {
        for (year in 2018..2024) {
            val terms = calculator.getTermsForYear(year)
            assertEquals("Year $year should have 24 terms", 24, terms.size)
        }
    }

    // -------------------------------------------------------------------------
    // Hangul / Hanji lookup
    // -------------------------------------------------------------------------

    @Test
    fun `lookupMeta returns canonical Hangul-Hanji mapping`() {
        assertEquals("입춘", SolarTermsCalculator.lookupMeta("立春")?.hangul)
        assertEquals("동지", SolarTermsCalculator.lookupMeta("冬至")?.hangul)
        assertEquals("소서", SolarTermsCalculator.lookupMeta("小暑")?.hangul)
    }
}
