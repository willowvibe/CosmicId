package com.willowvibe.agereveal.domain

import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Solar terms (二十四節氣 / 이십사절기) — astronomical boundaries of the
 * Korean Saju month system.
 *
 * Each of the 12 Saju months starts at a 節 (Jie) — not at the Gregorian
 * month boundary. For example, 입춘 (立春, ~Feb 4) is when the year-pillar
 * switches over, and 경칩 (驚蟄, ~Mar 5) is when the month-pillar switches
 * from 寅 to 卯.
 *
 * 12 節 (months) + 12 氣 (mid-months) = 24 terms, each ~15 days apart, with
 * the sun on a multiple of 15° ecliptic longitude.
 *
 * The math is astronomical (sub-day accuracy) and is delegated to the
 * `cn.6tail:lunar:1.7.7` library via [Lunar.getJieQiTable]. This class
 * just reformats the results into a Kotlin-idiomatic shape with both
 * Hanzi and Hangul names.
 */
@Singleton
class SolarTermsCalculator @Inject constructor() {

    /** The 12 divisions of the tropical year. */
    enum class Kind { JIE, QI }

    /** A single solar term with its solar-occurrence instant (UTC) and name. */
    data class SolarTerm(
        /** Hanji name (e.g. "立春"). */
        val hanja: String,
        /** Hangul reading (e.g. "입춘"). */
        val hangul: String,
        /** The 12 divisions of the tropical year. */
        val kind: Kind,
        /** Sun's ecliptic longitude in degrees at the moment of the term. */
        val sunLongitudeDeg: Int,
        /** Solar date (Gregorian) when the term occurs. */
        val solarDate: LocalDate,
        /** UTC hour-of-day when the term occurs (0–23). */
        val utcHour: Int,
        /** UTC minute-of-hour. */
        val utcMinute: Int,
    ) {
        val isMonthStart: Boolean get() = kind == Kind.JIE   // 12 of these per year
        val isMidMonth: Boolean get() = kind == Kind.QI
    }

    data class TermMeta(
        val hanja: String,
        val hangul: String,
        val kind: Kind,
        val sunLongitudeDeg: Int,
        /** Optional override of the key used to look up this term in the
         *  lunar-java [Lunar.getJieQiTable]. Defaults to [hanja]. The library
         *  uses pinyin aliases for 冬至 (`DONG_ZHI`); other aliases may exist
         *  in future library versions. */
        val tableKeyOverride: String? = null,
    ) {
        /** The actual key to use when looking up in `jieQiTable`. */
        val tableKey: String get() = tableKeyOverride ?: hanja
    }

    companion object {
        /**
         * Canonical 24-term table. Order matters — index = (sun longitude / 15) % 24.
         *
         * Hanji names match the `cn.6tail:lunar` library's [Lunar.getJieQiTable]
         * keys exactly (simplified Hanji for those that have simplified
         * variants: 惊蛰 vs 驚蟄, 谷雨 vs 穀雨, 处暑 vs 處暑).
         *
         * 12 JIE (節 — month starts) marked with `(month)` annotation:
         *   立春 입춘 雨水 우수 惊蛰 경칩 春分 춘분 清明 청명 谷雨 곡우
         *   立夏 입하 小满 소만 芒种 망종 夏至 하지 小暑 소서 大暑 대서
         *   立秋 입추 处暑 처서 白露 백로 秋分 추분 寒露 한로 霜降 상강
         *   立冬 입동 小雪 소설 大雪 대설 冬至 동지
         */
        val TERMS: List<TermMeta> = listOf(
            TermMeta("小寒", "소한", Kind.QI,    285),
            TermMeta("大寒", "대한", Kind.JIE,   300),  // month start
            TermMeta("立春", "입춘", Kind.JIE,   315),  // year start ←
            TermMeta("雨水", "우수", Kind.QI,    330),
            TermMeta("惊蛰", "경칩", Kind.JIE,   345),  // month start ← (simplified Hanji)
            TermMeta("春分", "춘분", Kind.QI,    0),
            TermMeta("清明", "청명", Kind.JIE,   15),   // month start ←
            TermMeta("谷雨", "곡우", Kind.QI,    30),   // simplified Hanji
            TermMeta("立夏", "입하", Kind.JIE,   45),   // month start ←
            TermMeta("小满", "소만", Kind.QI,    60),   // simplified Hanji
            TermMeta("芒种", "망종", Kind.JIE,   75),   // month start ← simplified
            TermMeta("夏至", "하지", Kind.QI,    90),
            TermMeta("小暑", "소서", Kind.JIE,   105),  // month start ←
            TermMeta("大暑", "대서", Kind.QI,    120),
            TermMeta("立秋", "입추", Kind.JIE,   135),  // month start ←
            TermMeta("处暑", "처서", Kind.QI,    150),  // simplified Hanji
            TermMeta("白露", "백로", Kind.JIE,   165),  // month start ←
            TermMeta("秋分", "추분", Kind.QI,    180),
            TermMeta("寒露", "한로", Kind.JIE,   195),  // month start ←
            TermMeta("霜降", "상강", Kind.QI,    210),
            TermMeta("立冬", "입동", Kind.JIE,   225),  // month start ←
            TermMeta("小雪", "소설", Kind.QI,    240),
            TermMeta("大雪", "대설", Kind.JIE,   255),  // month start ←
            // Note: the lunar-java library uses the pinyin alias `DONG_ZHI` for
            // 冬至 in its jieQiTable. We canonicalise the lookup below.
            TermMeta("冬至", "동지", Kind.QI,    270, tableKeyOverride = "DONG_ZHI"),
        )

        /** Look up metadata for a Hanji name, case-sensitive. */
        fun lookupMeta(hanja: String): TermMeta? = TERMS.firstOrNull { it.hanja == hanja }

        /** The 12 month-starting terms in order, used by [SajuKoreanCalculator] and the UI. */
        val MONTH_STARTS: List<TermMeta> = TERMS.filter { it.kind == Kind.JIE }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Return all 24 solar terms that occur within the calendar [year].
     * We query the table from mid-year (Jul 1) so the year-window slices
     * cleanly: any term whose UTC Solar year matches [year] is included.
     */
    fun getTermsForYear(year: Int): List<SolarTerm> {
        val table = Solar.fromYmd(year, 7, 1).lunar.getJieQiTable()
        return TERMS.mapNotNull { meta ->
            val solar = table[meta.tableKey] ?: return@mapNotNull null
            // Only include terms that actually occur in [year] (UTC).
            if (solar.year != year) return@mapNotNull null
            SolarTerm(
                hanja = meta.hanja,
                hangul = meta.hangul,
                kind = meta.kind,
                sunLongitudeDeg = meta.sunLongitudeDeg,
                solarDate = LocalDate.of(solar.year, solar.month, solar.day),
                utcHour = solar.hour,
                utcMinute = solar.minute,
            )
        }
    }

    /** Convenience: the 12 month-starting 節 only. */
    fun getMonthStartsForYear(year: Int): List<SolarTerm> =
        getTermsForYear(year).filter { it.isMonthStart }

    /**
     * The current/next 節 (month start) for [date]. Used by the 대운 (Daeun)
     * calculator — start age = days to next 節 / 3 (Korean-school rule).
     */
    fun nextJieAfter(date: LocalDate): SolarTerm? {
        val yearTerms = getMonthStartsForYear(date.year)
        // Find the first 節 whose solar date is ≥ today
        val upcoming = yearTerms.firstOrNull { !it.solarDate.isBefore(date) }
        // If none this year, the next 節 is the first one of next year (which
        // is also the start of the next month in the solar-term system).
        if (upcoming != null) return upcoming
        val nextYear = getMonthStartsForYear(date.year + 1)
        return nextYear.firstOrNull()
    }

    /** The most recent 節 (month start) on or before [date]. */
    fun prevJieOnOrBefore(date: LocalDate): SolarTerm? {
        val yearTerms = getMonthStartsForYear(date.year)
        val past = yearTerms.lastOrNull { !it.solarDate.isAfter(date) }
        if (past != null) return past
        val prevYear = getMonthStartsForYear(date.year - 1)
        return prevYear.lastOrNull()
    }

    /** The current month-starting 節 that [date] falls in. */
    fun currentJie(date: LocalDate): SolarTerm? {
        val prev = prevJieOnOrBefore(date) ?: return null
        val next = nextJieAfter(date) ?: return null
        // Current is the one whose interval contains the date.
        return if (!date.isBefore(prev.solarDate) && date.isBefore(next.solarDate)) prev else null
    }

    /**
     * Distance in days from [date] to the next 節 (used to compute 대운
     * start age per the Korean-school rule: days / 3).
     */
    fun daysToNextJie(date: LocalDate): Long? {
        val next = nextJieAfter(date) ?: return null
        return java.time.temporal.ChronoUnit.DAYS.between(date, next.solarDate)
    }
}
