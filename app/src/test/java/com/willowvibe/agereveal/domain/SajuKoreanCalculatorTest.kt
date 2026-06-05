package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SajuKoreanCalculatorTest {

    private lateinit var calculator: SajuKoreanCalculator
    private lateinit var baZi: BaZiCalculator

    @Before
    fun setUp() {
        baZi = BaZiCalculator(ZodiacCalculator(AstronomicalCalculator()))
        calculator = SajuKoreanCalculator(baZi)
    }

    // -------------------------------------------------------------------------
    // Hangul + Hanja display for the four pillars
    // -------------------------------------------------------------------------

    @Test
    fun `chart pillars for 2024-06-15 include Hangul stem and branch`() {
        val (pillars, _) = calculator.chartPillars(LocalDate.of(2024, 6, 15), hour = 14)
        assertEquals(4, pillars.size) // year, month, day, hour
        for (p in pillars) {
            assertTrue("Hangul stem should be one of 갑을병정무기경신임계 in ${p.stem.hangul}",
                SajuKoreanCalculator.STEMS.any { it.hangul == p.stem.hangul })
            assertTrue("Hangul branch should be one of 자축인묘진사오미신유술해 in ${p.branch.hangul}",
                SajuKoreanCalculator.BRANCHES.any { it.hangul == p.branch.hangul })
        }
    }

    @Test
    fun `displayHangul pairs hanja in parens with hangul prefix`() {
        // 2024-06-15 is 甲辰 year → 갑(甲)진(辰) year pillar
        val (pillars, _) = calculator.chartPillars(LocalDate.of(2024, 6, 15), hour = 14)
        val yearPillar = pillars[0]
        assertEquals("갑(甲)", yearPillar.stem.hangul + "(" + yearPillar.stem.hanja + ")")
        assertTrue("Year pillar hangul display should pair hanja in parens: ${yearPillar.displayHangul}",
            yearPillar.displayHangul.contains("갑(甲)") && yearPillar.displayHangul.contains("진(辰)"))
    }

    // -------------------------------------------------------------------------
    // Sruthi reference case (validated against sajupy + 6tail/lunar-java)
    // -------------------------------------------------------------------------

    @Test
    fun `sruthi 1993-12-11 0245 IST yields Bing fire day master with Hangul labels`() {
        val chart = calculator.computeChart(
            date = LocalDate.of(1993, 12, 11),
            hour = 2, minute = 45,
            zoneOffsetHours = 5.5,
            gender = BaZiCalculator.Gender.FEMALE,
        )
        // Day master = 丙 (한자) / 병 (Hangul) = Fire
        assertEquals("丙", chart.dayMaster.hanja)
        assertEquals("병", chart.dayMaster.hangul)
        assertEquals("Fire", chart.dayMasterElement)
        // Day pillar = 丙寅 (병인)
        assertEquals("丙寅", chart.day.displayHanja)
        assertEquals("병(丙)인(寅)", chart.day.displayHangul)
    }

    @Test
    fun `sruthi chart has all four pillars populated when hour given`() {
        val chart = calculator.computeChart(
            LocalDate.of(1993, 12, 11), hour = 2, minute = 45,
            zoneOffsetHours = 5.5, gender = BaZiCalculator.Gender.FEMALE,
        )
        assertNotNull(chart.hour)
        assertEquals("己丑", chart.hour!!.displayHanja) // hour pillar from BaZiCalculator
    }

    // -------------------------------------------------------------------------
    // 오행 (OHaeng / Five Element) balance
    // -------------------------------------------------------------------------

    @Test
    fun `oHaeng balance sums to total count of all elements including hidden stems`() {
        val chart = calculator.computeChart(
            LocalDate.of(1990, 6, 15), hour = 12,
        )
        val total = chart.oHaengBalance.total
        // 4 pillars × (1 visible stem + 1 branch + ≥1 hidden stems) = ≥ 8
        val totalCount = total.values.sum()
        assertTrue("Total element count should be at least 8 (4 stems + 4 branches), got: $totalCount",
            totalCount >= 8)
        // All five elements should be present in the lookup keys
        for (e in listOf("Wood", "Fire", "Earth", "Metal", "Water")) {
            assertTrue("Balance should include $e", total.containsKey(e))
        }
    }

    @Test
    fun `visible stem counts add up to 4 for a full birth chart`() {
        val chart = calculator.computeChart(LocalDate.of(2024, 6, 15), hour = 14)
        assertEquals(4, chart.oHaengBalance.visibleStemCounts.values.sum())
    }

    @Test
    fun `branch counts add up to 4 for a full birth chart`() {
        val chart = calculator.computeChart(LocalDate.of(2024, 6, 15), hour = 14)
        assertEquals(4, chart.oHaengBalance.branchCounts.values.sum())
    }

    // -------------------------------------------------------------------------
    // 용신 (Yongshin) suggestion
    // -------------------------------------------------------------------------

    @Test
    fun `yongshin picks a valid element in the five-element cycle`() {
        val chart = calculator.computeChart(LocalDate.of(1990, 6, 15), hour = 12)
        val valid = setOf("Wood", "Fire", "Earth", "Metal", "Water")
        assertTrue("Favourable element should be one of 5 elements, got: ${chart.yongshin.favourable}",
            chart.yongshin.favourable in valid)
        assertTrue("Unfavourable element should be one of 5 elements, got: ${chart.yongshin.unfavourable}",
            chart.yongshin.unfavourable in valid)
    }

    @Test
    fun `yongshin reason mentions day master element in Korean`() {
        val chart = calculator.computeChart(LocalDate.of(1990, 6, 15), hour = 12)
        val dayMasterHangul = SajuKoreanCalculator.ELEMENT_HANGUL[chart.dayMasterElement]
        assertTrue("Reason should reference the day master element in Korean ($dayMasterHangul), got: ${chart.yongshin.reasoning}",
            chart.yongshin.reasoning.contains(dayMasterHangul!!))
    }

    // -------------------------------------------------------------------------
    // 용신 (Yongshin) suggestion card
    // -------------------------------------------------------------------------

    @Test
    fun `yongshin card short summary includes day master, status, and favourable element`() {
        val chart = calculator.computeChart(LocalDate.of(1990, 6, 15), hour = 12)
        val card = calculator.buildYongshinCard(chart)
        val dayMasterHangul = chart.dayMaster.hangul
        val favHangul = SajuKoreanCalculator.ELEMENT_HANGUL[card.favourableElementEn]
        assertTrue("Card summary should mention day master ($dayMasterHangul): ${card.shortSummary}",
            card.shortSummary.contains(dayMasterHangul!!))
        assertTrue("Card summary should mention status (신강/신약): ${card.shortSummary}",
            card.shortSummary.contains("신강") || card.shortSummary.contains("신약"))
        assertTrue("Card summary should mention favourable element ($favHangul): ${card.shortSummary}",
            card.shortSummary.contains(favHangul!!))
    }

    @Test
    fun `yongshin card has Korean colour, direction, and season hints`() {
        val chart = calculator.computeChart(LocalDate.of(2024, 6, 15), hour = 14)
        val card = calculator.buildYongshinCard(chart)
        val validColors = setOf("초록", "빨강", "노랑", "흰색", "파랑")
        val validDirections = setOf("동", "남", "중앙", "서", "북")
        val validSeasons = setOf("봄", "여름", "가을", "겨울", "환절기 (토의 계절)")
        assertTrue("Color name should be a valid Korean cultural color: ${card.favourableColorName}",
            card.favourableColorName in validColors)
        assertTrue("Direction should be a valid Korean cardinal: ${card.favourableDirection}",
            card.favourableDirection in validDirections)
        assertTrue("Season should be a valid Korean season: ${card.favourableSeason}",
            card.favourableSeason in validSeasons)
    }

    @Test
    fun `yongshin card provides 3+ lifestyle actions in Korean`() {
        val chart = calculator.computeChart(LocalDate.of(1990, 6, 15), hour = 12)
        val card = calculator.buildYongshinCard(chart)
        assertTrue("Card should have at least 3 action suggestions, got: ${card.actionSuggestions.size}",
            card.actionSuggestions.size >= 3)
        for (action in card.actionSuggestions) {
            assertTrue("Each action should end with a Korean sentence-ender (./요/니다), got: $action",
                action.endsWith(".") || action.endsWith("요") || action.endsWith("습니다") || action.endsWith("세요"))
        }
    }

    @Test
    fun `yongshin card isStrong matches chart yongshin isStrong`() {
        val chart = calculator.computeChart(LocalDate.of(1990, 6, 15), hour = 12)
        val card = calculator.buildYongshinCard(chart)
        assertEquals(chart.yongshin.isStrong, card.isStrong)
        assertEquals(if (chart.yongshin.isStrong) "신강" else "신약", card.status)
    }

    // -------------------------------------------------------------------------
    // 대운 (Daeun) — Korean display of 10-year luck periods
    // -------------------------------------------------------------------------

    @Test
    fun `daeun produces 8 periods for a female sruthi chart`() {
        val chart = calculator.computeChart(
            LocalDate.of(1993, 12, 11), hour = 2, minute = 45,
            zoneOffsetHours = 5.5, gender = BaZiCalculator.Gender.FEMALE,
        )
        assertEquals(8, chart.daeun.size)
        // Each 대운 is 10 years wide (BaZiCalculator returns endAge inclusive
        // of the last year, e.g. 0..9, 10..19, … so endAge - startAge = 9).
        for (period in chart.daeun) {
            assertEquals("Each 대운 period should be 10 years wide, got: $period",
                9, period.endAge - period.startAge)
        }
    }

    @Test
    fun `daeun is empty when gender is null`() {
        val chart = calculator.computeChart(LocalDate.of(2024, 6, 15), hour = 14, gender = null)
        assertTrue("Daeun should be empty when gender is not provided", chart.daeun.isEmpty())
    }

    @Test
    fun `daeun pillars use Hangul-formatted KoreanPillar`() {
        val chart = calculator.computeChart(
            LocalDate.of(1990, 6, 15), hour = 12, gender = BaZiCalculator.Gender.MALE,
        )
        for (period in chart.daeun) {
            assertTrue("Daeun period should have a non-empty Hangul display: ${period.displayHangul}",
                period.displayHangul.isNotBlank())
        }
    }

    // -------------------------------------------------------------------------
    // No-crash stress test
    // -------------------------------------------------------------------------

    @Test
    fun `no exception for any day in a full year`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.computeChart(start.plusDays(i.toLong()), hour = 12)
        }
    }
}
