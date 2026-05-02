package com.willowvibe.agereveal.domain

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ba Zi (Four Pillars / 八字) approximation — Year and Month pillars.
 *
 * In a full Ba Zi reading there are four pillars: Year, Month, Day, and Hour.
 * This calculator provides the Year and Month pillars, which are the most
 * commonly used and can be computed from the birth date alone. Day and Hour
 * pillars require a full Chinese calendar / solar-term ephemeris which is
 * beyond the scope of this approximation.
 *
 * Year pillar: Heavenly Stem + Earthly Branch of the Chinese year.
 * Month pillar: determined by the solar month (approximated via standard
 * month boundaries) and the year stem via the 五虎遁月 rule.
 */
@Singleton
class BaZiCalculator @Inject constructor(
    private val zodiacCalculator: ZodiacCalculator,
) {

    private val heavenlyStems = listOf(
        "甲 Jia", "乙 Yi", "丙 Bing", "丁 Ding", "戊 Wu",
        "己 Ji", "庚 Geng", "辛 Xin", "壬 Ren", "癸 Gui",
    )

    private val earthlyBranches = listOf(
        "子 Zi", "丑 Chou", "寅 Yin", "卯 Mao", "辰 Chen", "巳 Si",
        "午 Wu", "未 Wei", "申 Shen", "酉 You", "戌 Xu", "亥 Hai",
    )

    private val branchAnimals = listOf(
        "Rat", "Ox", "Tiger", "Rabbit", "Dragon", "Snake",
        "Horse", "Goat", "Monkey", "Rooster", "Dog", "Pig",
    )

    /**
     * Returns the Year pillar for [date], e.g. "Jia-Chen (Wood-Dragon)".
     *
     * This delegates to [ZodiacCalculator.getChineseStemBranch] and re-formats
     * the output to match the Ba Zi pillar convention.
     */
    fun getYearPillar(date: LocalDate): String {
        val sb = zodiacCalculator.getChineseStemBranch(date)
        // Input: "甲 Jia-辰 Chen / Wood-Dragon"
        // Extract stem and branch names
        val parts = sb.split(" / ")
        val stemBranch = parts[0].split("-")
        val stem = stemBranch[0].trim().split(" ").last()
        val branch = stemBranch[1].trim().split(" ").last()
        val elementAnimal = parts[1].split("-")
        val element = elementAnimal[0].trim()
        val animal = elementAnimal[1].trim()
        return "$stem-$branch ($element-$animal)"
    }

    /**
     * Returns the Month pillar for [date], e.g. "Bing-Si (Fire-Snake)".
     *
     * The month branch is determined by the approximate solar month using
     * standard solar-term boundaries (立春 ≈ Feb 4, etc.).
     * The month stem follows the 五虎遁月 rule based on the year stem.
     */
    fun getMonthPillar(date: LocalDate): String {
        val yearStemIndex = getYearStemIndex(date)
        val monthBranchIndex = getMonthBranchIndex(date)
        val monthStemIndex = getMonthStemIndex(yearStemIndex, monthBranchIndex)

        val stem = heavenlyStems[monthStemIndex].split(" ").last()
        val branch = earthlyBranches[monthBranchIndex].split(" ").last()
        val animal = branchAnimals[monthBranchIndex]
        val element = getStemElement(monthStemIndex)

        return "$stem-$branch ($element-$animal)"
    }

    /** Full Ba Zi summary: Year pillar + Month pillar. */
    fun getBaZiSummary(date: LocalDate): String {
        val year = getYearPillar(date)
        val month = getMonthPillar(date)
        return "Year: $year · Month: $month"
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Year stem index (0–9) for the Chinese year of [date]. */
    private fun getYearStemIndex(date: LocalDate): Int {
        val chineseYear = zodiacCalculator.getChineseYear(date)
        return ((chineseYear - 4) % 10 + 10) % 10
    }

    /**
     * Month branch index (0–11) based on approximate solar month boundaries.
     *
     * The Chinese month branch cycle starts with 寅 (Tiger) at 立春 (~Feb 4).
     * Mapping uses the standard solar term month divisions:
     *   寅 Tiger:  ~Feb 4  – Mar 5
     *   卯 Rabbit: ~Mar 6  – Apr 4
     *   辰 Dragon: ~Apr 5  – May 5
     *   巳 Snake:  ~May 6  – Jun 5
     *   午 Horse:  ~Jun 6  – Jul 6
     *   未 Goat:   ~Jul 7  – Aug 6
     *   申 Monkey: ~Aug 7  – Sep 7
     *   酉 Rooster:~Sep 8  – Oct 7
     *   戌 Dog:    ~Oct 8  – Nov 6
     *   亥 Pig:    ~Nov 7  – Dec 6
     *   子 Rat:    ~Dec 7  – Jan 5
     *   丑 Ox:     ~Jan 6  – Feb 3
     */
    private fun getMonthBranchIndex(date: LocalDate): Int {
        val m = date.monthValue
        val d = date.dayOfMonth
        return when {
            (m == 2 && d >= 4) || (m == 3 && d <= 5) -> 2   // 寅 Tiger
            (m == 3 && d >= 6) || (m == 4 && d <= 4) -> 3   // 卯 Rabbit
            (m == 4 && d >= 5) || (m == 5 && d <= 5) -> 4   // 辰 Dragon
            (m == 5 && d >= 6) || (m == 6 && d <= 5) -> 5   // 巳 Snake
            (m == 6 && d >= 6) || (m == 7 && d <= 6) -> 6   // 午 Horse
            (m == 7 && d >= 7) || (m == 8 && d <= 6) -> 7   // 未 Goat
            (m == 8 && d >= 7) || (m == 9 && d <= 7) -> 8   // 申 Monkey
            (m == 9 && d >= 8) || (m == 10 && d <= 7) -> 9  // 酉 Rooster
            (m == 10 && d >= 8) || (m == 11 && d <= 6) -> 10 // 戌 Dog
            (m == 11 && d >= 7) || (m == 12 && d <= 6) -> 11 // 亥 Pig
            (m == 12 && d >= 7) || (m == 1 && d <= 5) -> 0  // 子 Rat
            else -> 1                                         // 丑 Ox (Jan 6 – Feb 3)
        }
    }

    /**
     * Month stem index (0–9) via 五虎遁月 (Wu Hu Dun Yue).
     *
     * The starting stem for the first month (寅) depends on the year stem:
     *   甲/Jia (0), 己/Ji (5) → starts with 丙/Bing (2)
     *   乙/Yi (1), 庚/Geng (6) → starts with 戊/Wu (4)
     *   丙/Bing (2), 辛/Xin (7) → starts with 庚/Geng (6)
     *   丁/Ding (3), 壬/Ren (8) → starts with 壬/Ren (8)
     *   戊/Wu (4), 癸/Gui (9) → starts with 甲/Jia (0)
     *
     * The [monthBranchIndex] is the absolute earthly-branch index (寅=2, 卯=3, …).
     * The month position within the year cycle is (branchIndex - 2) mod 12, since
     * 寅 is the first month. Then: monthStem = (startingStem + monthPosition) % 10.
     */
    private fun getMonthStemIndex(yearStemIndex: Int, monthBranchIndex: Int): Int {
        val startingStem = when (yearStemIndex) {
            0, 5 -> 2   // 甲/Ji → 丙
            1, 6 -> 4   // 乙/Geng → 戊
            2, 7 -> 6   // 丙/Xin → 庚
            3, 8 -> 8   // 丁/Ren → 壬
            4, 9 -> 0   // 戊/Gui → 甲
            else -> 0
        }
        val monthPosition = (monthBranchIndex - 2 + 12) % 12
        return (startingStem + monthPosition) % 10
    }

    private fun getStemElement(stemIndex: Int): String = when (stemIndex % 10) {
        0, 1 -> "Wood"
        2, 3 -> "Fire"
        4, 5 -> "Earth"
        6, 7 -> "Metal"
        else -> "Water"
    }
}
