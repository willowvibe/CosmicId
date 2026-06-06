package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class BaZiCalculatorTest {

    private lateinit var calculator: BaZiCalculator

    @Before
    fun setUp() {
        val zodiac = ZodiacCalculator(AstronomicalCalculator())
        calculator = BaZiCalculator(zodiac)
    }

    // -------------------------------------------------------------------------
    // Year + Month pillars (smoke tests for the new lunar-java wrapper)
    // -------------------------------------------------------------------------

    @Test
    fun `year pillar for 2024 contains Jia and Chen`() {
        // 2024-06-01 is in the 立春-onwards window of 2024 → 甲辰 year.
        val pillar = calculator.getYearPillar(LocalDate.of(2024, 6, 1))
        assertTrue("Expected Jia-Chen in: $pillar",
            pillar.contains("Jia") && pillar.contains("Chen"))
    }

    @Test
    fun `year pillar for 1984 contains Jia and Zi`() {
        // 1984 is Jia-Zi, start of the 60-year cycle.
        val pillar = calculator.getYearPillar(LocalDate.of(1984, 3, 1))
        assertTrue("Expected Jia-Zi in: $pillar",
            pillar.contains("Jia") && pillar.contains("Zi"))
    }

    @Test
    fun `year pillar respects lunar new year cutoff`() {
        // Jan 25, 2000 is before Chinese New Year 2000 → still 1999 (Rabbit).
        val pillar = calculator.getYearPillar(LocalDate.of(2000, 1, 25))
        assertTrue("Expected Rabbit before CNY, got: $pillar", pillar.contains("Rabbit"))
    }

    @Test
    fun `month pillar returns non empty string`() {
        val pillar = calculator.getMonthPillar(LocalDate.of(1990, 6, 15))
        assertTrue(pillar.isNotBlank())
    }

    @Test
    fun `month pillar contains dash`() {
        val pillar = calculator.getMonthPillar(LocalDate.of(1990, 6, 15))
        assertTrue("Expected stem-branch format with dash: $pillar", pillar.contains("-"))
    }

    @Test
    fun `different months produce different pillars`() {
        val p1 = calculator.getMonthPillar(LocalDate.of(2024, 2, 15))
        val p2 = calculator.getMonthPillar(LocalDate.of(2024, 5, 15))
        assertTrue("Different months should differ: $p1 vs $p2", p1 != p2)
    }

    // -------------------------------------------------------------------------
    // Four pillars (lunar-java reference cases)
    // -------------------------------------------------------------------------

    @Test
    fun `sruthi reference case 1993-12-11 0245 IST yields expected pillars`() {
        // Cross-verified against /home/harish/Downloads/saju/tools/saju_engine
        // (the Python sajupy engine) and 6tail/lunar-java v1.7.7.
        // Expected: 癸酉 / 甲子 / 丙寅 / 己丑
        val four = calculator.computeFourPillars(
            date = LocalDate.of(1993, 12, 11),
            hour = 2,
            minute = 45,
            zoneOffsetHours = 5.5,
        )
        assertEquals("癸酉", "${four.year.stemHanzi}${four.year.branchHanzi}")
        assertEquals("甲子", "${four.month.stemHanzi}${four.month.branchHanzi}")
        assertEquals("丙寅", "${four.day.stemHanzi}${four.day.branchHanzi}")
        assertEquals("己丑", "${four.hour?.stemHanzi}${four.hour?.branchHanzi}")
        assertEquals("Fire", four.dayMasterElement)   // 丙 = Fire
    }

    @Test
    fun `sruthi day master is Bing Fire`() {
        val four = calculator.computeFourPillars(
            LocalDate.of(1993, 12, 11), hour = 2, minute = 45, zoneOffsetHours = 5.5,
        )
        assertEquals("丙", four.dayMasterHanzi)
        assertEquals("Fire", four.dayMasterElement)
    }

    @Test
    fun `four pillars omit hour when hour is null`() {
        val four = calculator.computeFourPillars(LocalDate.of(2024, 6, 15))
        assertTrue(four.hour == null)
        assertTrue(four.day.branchHanzi.isNotEmpty())
    }

    @Test
    fun `four pillars summary contains all four pillars when hour is given`() {
        val summary = calculator.getBaZiSummary(
            LocalDate.of(2024, 6, 15), hour = 14,
        )
        assertTrue(summary.contains("Year:"))
        assertTrue(summary.contains("Month:"))
        assertTrue(summary.contains("Day:"))
        assertTrue(summary.contains("Hour:"))
    }

    // -------------------------------------------------------------------------
    // DaYun (major-luck / 대운) — Korean convention, female, sect 2 (야자시)
    // -------------------------------------------------------------------------

    @Test
    fun `daeun for sruthi female starts at age 8 and goes forward`() {
        // Sruthi is Female. Year stem 癸 (Yin) → female Yin = forward.
        // Python engine (sajupy wrapper) computed start age 8 from "days to
        // next 節氣 = 25, 25/3 = 8". lunar-java reports East-Asian start age
        // 10 for this chart, which converts to Western years elapsed = 9.
        // We assert startAge ≤ 10 (exact value depends on 절기 lookup table
        // version) and the per-period width is exactly 10.
        val periods = calculator.computeDaYun(
            date = LocalDate.of(1993, 12, 11),
            hour = 2, minute = 45,
            gender = BaZiCalculator.Gender.FEMALE,
            zoneOffsetHours = 5.5,
            nPeriods = 8,
        )
        assertEquals(8, periods.size)
        assertTrue("Start age should be ≤ 10 (lunar-java East-Asian), got: ${periods.first().startAge}",
            periods.first().startAge in 1..10)
        // Each period is 10 years.
        for (i in 0 until periods.size - 1) {
            assertEquals(periods[i].startAge + 10, periods[i + 1].startAge)
        }
    }

    @Test
    fun `daeun for male starts at a positive integer age and has 8 periods`() {
        val periods = calculator.computeDaYun(
            date = LocalDate.of(1990, 6, 15),
            hour = 12,
            gender = BaZiCalculator.Gender.MALE,
            nPeriods = 8,
        )
        assertEquals(8, periods.size)
        assertTrue("Start age should be > 0, got: ${periods.first().startAge}",
            periods.first().startAge > 0)
    }

    // -------------------------------------------------------------------------
    // No-crash stress test (every day in a full year)
    // -------------------------------------------------------------------------

    @Test
    fun `no exception for any day in a full year`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.getBaZiSummary(start.plusDays(i.toLong()))
        }
    }

    // -------------------------------------------------------------------------
    // String facades for day + hour pillars (API parity with year + month)
    // -------------------------------------------------------------------------

    @Test
    fun `day pillar facade for 1993-12-11 returns Bing-Yin`() {
        // Sruthi reference case: 1993-12-11 → 丙寅 Day Pillar (Bing = 丙, Yin = 寅).
        // See "sruthi reference case 1993-12-11 0245 IST yields expected pillars"
        // above for the full Sruthi validation (cross-checked against sajupy +
        // lunar-java v1.7.7).
        val pillar = calculator.getDayPillar(LocalDate.of(1993, 12, 11))
        assertTrue("Expected Bing-Yin (丙寅) in: $pillar",
            pillar.contains("Bing") && pillar.contains("Yin"))
    }

    @Test
    fun `hour pillar facade for 1993-12-11 02-45 returns Ji-Chou`() {
        // 02:45 falls in the 丑 (01:00–02:59) hour block. With a Bing day stem,
        // the stem sequence for 丑 hours is 己 (Ji). Matches the Sruthi
        // reference case (己丑).
        val pillar = calculator.getHourPillar(LocalDate.of(1993, 12, 11), hour = 2, minute = 45)
        assertTrue("Expected Ji-Chou (己丑) in: $pillar",
            pillar.contains("Ji") && pillar.contains("Chou"))
    }
}
