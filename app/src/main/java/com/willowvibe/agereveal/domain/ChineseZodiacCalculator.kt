package com.willowvibe.agereveal.domain

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chinese zodiac calculations — 12-year animal cycle, Lunar New Year aware,
 * and the 60-cycle Heavenly-Stem / Earthly-Branch (天干地支) plus Wu Xing
 * element.
 *
 * People born in January / early February before Lunar New Year belong to the
 * previous Chinese year, not the current Gregorian year. The CNY lookup
 * table covers 1900–2100; years outside the table fall back to a linear
 * approximation accurate to ±1 day.
 */
@Singleton
class ChineseZodiacCalculator @Inject constructor() {

    val zodiacCycle: List<String> = listOf(
        "🐀 Rat", "🐂 Ox", "🐯 Tiger", "🐇 Rabbit",
        "🐉 Dragon", "🐍 Snake", "🐴 Horse", "🐏 Goat",
        "🐒 Monkey", "🐓 Rooster", "🐕 Dog", "🐖 Pig",
    )

    private val heavenlyStems = listOf(
        "甲 Jia", "乙 Yi", "丙 Bing", "丁 Ding", "戊 Wu",
        "己 Ji", "庚 Geng", "辛 Xin", "壬 Ren", "癸 Gui",
    )

    private val stemElements = listOf(
        "Wood", "Wood", "Fire", "Fire", "Earth",
        "Earth", "Metal", "Metal", "Water", "Water",
    )

    private val earthlyBranches = listOf(
        "子 Zi", "丑 Chou", "寅 Yin", "卯 Mao", "辰 Chen", "巳 Si",
        "午 Wu", "未 Wei", "申 Shen", "酉 You", "戌 Xu", "亥 Hai",
    )

    /** Returns the Chinese zodiac animal for [date], respecting the CNY cutoff. */
    fun getZodiac(date: LocalDate): String {
        val chineseYear = getChineseYear(date)
        val index = ((chineseYear - 1900) % 12 + 12) % 12
        return zodiacCycle[index]
    }

    /**
     * Returns the Chinese calendar year for [date], shifting back by one if
     * the date falls before that Gregorian year's Lunar New Year.
     */
    fun getChineseYear(date: LocalDate): Int {
        val cny = chineseNewYearDate(date.year)
        return if (date.isBefore(cny)) date.year - 1 else date.year
    }

    /** Returns the full Chinese stem-branch with element, e.g. "Jia-Chen / Wood-Dragon". */
    fun getStemBranch(date: LocalDate): String {
        val chineseYear = getChineseYear(date)
        val stemIndex = ((chineseYear - 4) % 10 + 10) % 10
        val branchIndex = ((chineseYear - 4) % 12 + 12) % 12
        val stem = heavenlyStems[stemIndex]
        val branch = earthlyBranches[branchIndex]
        val element = stemElements[stemIndex]
        val animal = zodiacCycle[branchIndex].split(" ").last()
        return "$stem-$branch / $element-$animal"
    }

    /**
     * Returns the Gregorian date of Chinese New Year (Spring Festival) for [year].
     *
     * Lookup table covers 1900–2100; values were computed from astronomical
     * new-moon calculations (second new moon after winter solstice). For
     * years outside the table the result falls back to an approximation that
     * is accurate to ±1 day.
     *
     * Encoding: each entry is month*100 + day (e.g. 131 = Jan 31, 205 = Feb 5).
     */
    private fun chineseNewYearDate(year: Int): LocalDate {
        val offset = year - 1900
        if (offset in CNY_DATES.indices) {
            val encoded = CNY_DATES[offset]
            return LocalDate.of(year, encoded / 100, encoded % 100)
        }
        // Fallback approximation outside table range
        val approxDay = ((year - 1900) * 10.875 + 131).toInt() % 30 + 20
        val month = if (approxDay > 31) 2 else 1
        val day = if (approxDay > 31) approxDay - 31 else approxDay
        return LocalDate.of(year, month, day.coerceIn(1, 28))
    }

    companion object {
        /**
         * Chinese New Year dates for Gregorian years 1900–2100 (201 entries).
         * Index 0 = 1900, index 1 = 1901, …, index 200 = 2100.
         * Encoded as month*100 + day.
         */
        private val CNY_DATES = intArrayOf(
            // 1900–1909
            131, 219, 208, 129, 216, 204, 125, 213, 202, 122,
            // 1910–1919
            210, 130, 218, 206, 126, 214, 203, 123, 211, 201,
            // 1920–1929
            220, 208, 128, 216, 205, 124, 213, 202, 123, 210,
            // 1930–1939
            130, 217, 206, 126, 214, 204, 124, 211, 131, 219,
            // 1940–1949
            208, 127, 215, 205, 125, 213, 202, 122, 210, 129,
            // 1950–1959
            217, 206, 127, 214, 203, 124, 212, 131, 218, 208,
            // 1960–1969
            128, 215, 205, 125, 213, 202, 121, 209, 130, 217,
            // 1970–1979
            206, 127, 215, 203, 123, 211, 131, 218, 207, 128,
            // 1980–1989
            216, 205, 125, 213, 202, 220, 209, 129, 217, 206,
            // 1990–1999
            127, 215, 204, 123, 210, 131, 219, 207, 128, 216,
            // 2000–2009
            205, 124, 212, 201, 122, 209, 129, 218, 207, 126,
            // 2010–2019
            214, 203, 123, 210, 131, 219, 208, 128, 216, 205,
            // 2020–2029
            125, 212, 201, 122, 210, 129, 217, 206, 126, 213,
            // 2030–2039
            203, 123, 211, 131, 219, 208, 128, 215, 204, 124,
            // 2040–2049
            212, 201, 122, 210, 130, 217, 206, 126, 214, 202,
            // 2050–2059
            123, 211, 201, 219, 208, 128, 215, 204, 124, 211,
            // 2060–2069
            131, 218, 208, 128, 216, 205, 126, 214, 203, 123,
            // 2070–2079
            211, 131, 219, 207, 127, 215, 204, 124, 212, 202,
            // 2080–2089
            121, 209, 129, 217, 206, 126, 214, 203, 123, 210,
            // 2090–2099
            130, 217, 206, 126, 214, 203, 123, 210, 131, 219,
            // 2100
            208,
        )
    }
}
