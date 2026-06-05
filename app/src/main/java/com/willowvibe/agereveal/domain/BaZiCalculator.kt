package com.willowvibe.agereveal.domain

import com.nlf.calendar.EightChar
import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import com.nlf.calendar.eightchar.Yun
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ba Zi (Four Pillars / 八字 / 사주) calculator — math core.
 *
 * This class is a thin Kotlin facade over [Lunar] / [EightChar] from the
 * `cn.6tail:lunar` library. The library handles the hard parts:
 *   - 干支 (stems+branches) for year/month/day/hour
 *   - 二十四節氣 month-boundary crossings (astronomical, sub-day)
 *   - 太陽時 보정 (true solar time correction)
 *   - 旬 / 旬空 (decade + empty branches)
 *   - 藏干 (hidden stems) per branch
 *   - 十神 (ten gods) for every visible + hidden stem
 *   - 地勢/長生十二神 (12 life stages) for the Day Master in each branch
 *   - 納音 (na yin) per pillar
 *   - 運 / 大運 (major-luck) including start age + direction + 10-year sequence
 *
 * Naming is bilingual. Hangul 천간·지지 labels (갑을병정무기경신임계 / 자축인묘진사오미신유술해),
 * 오행 Korean cultural colours, and 용신 (Yongshin) live in [SajuKoreanCalculator].
 * This class exposes the math in a Kotlin-idiomatic shape and adds two
 * Korean-specific overrides on top of the library defaults:
 *   - Sect 2 (야자시) — late-zi (23:00–00:59) hour uses the *current* day stem
 *   - Exact (交接时刻) mode for year + month pillar — Korean 명리 convention
 */
@Singleton
class BaZiCalculator @Inject constructor(
    private val zodiacCalculator: ZodiacCalculator,
) {

    /** A single pillar: Heavenly Stem (0–9) + Earthly Branch (0–11). */
    data class Pillar(val stemIndex: Int, val branchIndex: Int) {
        val stemName: String get() = STEMS[stemIndex].substringAfter(" ")
        val branchName: String get() = BRANCHES[branchIndex].substringAfter(" ")
        /** Hanzi stem (e.g. "甲") for Chinese/Korean Saju UI. */
        val stemHanzi: String get() = STEMS[stemIndex].substringBefore(" ")
        /** Hanzi branch (e.g. "辰"). */
        val branchHanzi: String get() = BRANCHES[branchIndex].substringBefore(" ")
        val stemElement: String get() = stemElement(stemIndex)
        val branchElement: String get() = branchElement(branchIndex)
        val branchAnimal: String get() = ANIMALS[branchIndex]
        fun toDisplay(): String =
            "${STEMS[stemIndex].substringAfter(" ")}-${BRANCHES[branchIndex].substringAfter(" ")} (${stemElement}-${branchAnimal})"
    }

    /** All four pillars at a birth moment, plus Day Master. */
    data class FourPillars(
        val year: Pillar,
        val month: Pillar,
        val day: Pillar,
        val hour: Pillar?,
        val dayMasterHanzi: String,
        val dayMasterElement: String,
    ) {
        fun toDisplay(): String = buildString {
            append("Year: ").append(year.toDisplay())
            append(" · Month: ").append(month.toDisplay())
            append(" · Day: ").append(day.toDisplay())
            if (hour != null) append(" · Hour: ").append(hour.toDisplay())
        }
    }

    /** One major-luck (대운) period. */
    data class DaYunPeriod(
        val startAge: Int,
        val endAge: Int,
        val ganZhiHanzi: String,
        val stemIndex: Int,
        val branchIndex: Int,
    ) {
        val pillar: Pillar get() = Pillar(stemIndex, branchIndex)
        fun toDisplay(): String = "Age $startAge–$endAge: ${pillar.toDisplay()}"
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Full Ba Zi summary across all four pillars (or Year+Month+Day if hour
     * is unknown).
     */
    fun getBaZiSummary(
        date: LocalDate,
        hour: Int? = null,
        minute: Int? = null,
        zoneOffsetHours: Double? = null,
    ): String = computeFourPillars(date, hour, minute, zoneOffsetHours).toDisplay()

    /**
     * Structured four-pillars result with Day Master metadata.
     */
    fun computeFourPillars(
        date: LocalDate,
        hour: Int? = null,
        minute: Int? = null,
        zoneOffsetHours: Double? = null,
    ): FourPillars {
        val lunar = buildLunar(date, hour, minute, zoneOffsetHours)
        val ec = lunar.eightChar
        return FourPillars(
            year = parsePillar(ec.year),
            month = parsePillar(ec.month),
            day = parsePillar(ec.day),
            hour = if (hour != null) parsePillar(ec.time) else null,
            dayMasterHanzi = ec.dayGan,
            dayMasterElement = ec.dayWuXing.split("").firstOrNull { it.isNotEmpty() }
                ?.let { mapWuXingHanziToEn(it) } ?: "Unknown",
        )
    }

    /**
     * Major-luck (大运 / 대운) sequence. Korean 명리 convention:
     *   - Direction: Yang-year + Male OR Yin-year + Female → forward
     *   - Direction: Yang-year + Female OR Yin-year + Male → backward
     *   - Start age: count days to next/prev 節氣, divide by 3
     * The library handles both. We re-format the result into our data class.
     */
    fun computeDaYun(
        date: LocalDate,
        hour: Int,
        minute: Int = 0,
        gender: Gender,
        zoneOffsetHours: Double? = null,
        nPeriods: Int = 8,
    ): List<DaYunPeriod> {
        val lunar = buildLunar(date, hour, minute, zoneOffsetHours)
        val ec = lunar.eightChar
        // lunar-java convention: 0 = female (女), 1 = male (男); sect 2 = 야자시.
        val yun = ec.getYun(if (gender == Gender.MALE) 1 else 0, 2)
        // DaYun[0] is the "起大运前" pre-start period (natal month pillar until
        // 起运, with empty ganZhi). DaYun[1..] are the real 10-year periods.
        // Convert East-Asian start age to Western years elapsed = startAge - 1.
        return yun.daYun
            .filter { it.ganZhi.isNotEmpty() }
            .take(nPeriods)
            .map {
                DaYunPeriod(
                    startAge = it.startAge - 1,
                    endAge = it.endAge - 1,
                    ganZhiHanzi = it.ganZhi,
                    stemIndex = STEM_HANZI.indexOf(it.ganZhi[0].toString()),
                    branchIndex = BRANCH_HANZI.indexOf(it.ganZhi[1].toString()),
                )
            }
    }

    enum class Gender { MALE, FEMALE }

    /** Position discriminator for per-pillar accessors (Year / Month / Day / Hour). */
    enum class PillarPosition { YEAR, MONTH, DAY, HOUR }

    // -------------------------------------------------------------------------
    // Backwards-compatible string facades (used by AgeCalculator / BirthChart)
    // -------------------------------------------------------------------------

    fun getYearPillar(date: LocalDate): String =
        computeFourPillars(date).year.toDisplay()

    fun getMonthPillar(date: LocalDate): String =
        computeFourPillars(date).month.toDisplay()

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    /**
     * Build a [Lunar] from a Gregorian birth moment.
     *
     * @param zoneOffsetHours UTC offset of the birth location (e.g. 9.0 Korea,
     *   5.5 India, -10 Hawaii). When null, treats the input as local civil time
     *   without solar-time correction.
     */
    private fun buildLunar(
        date: LocalDate,
        hour: Int?,
        minute: Int?,
        zoneOffsetHours: Double?,
    ): Lunar {
        val h = hour ?: 12   // noon default for date-only births (mid-day is safe)
        val m = minute ?: 0
        val solar = if (zoneOffsetHours != null) {
            // Treat input as local civil time in the given zone offset.
            // For v2.1 we don't do true-solar-time correction (longitude); that
            // is a follow-up that requires the user's birth coordinates.
            val local = LocalDateTime.of(date, LocalTime.of(h, m))
            val zoned = local.atOffset(ZoneOffset.ofHoursMinutes(
                zoneOffsetHours.toInt(),
                ((zoneOffsetHours - zoneOffsetHours.toInt()) * 60).toInt(),
            ))
            Solar.fromYmdHms(
                zoned.year, zoned.monthValue, zoned.dayOfMonth,
                zoned.hour, zoned.minute, zoned.second,
            )
        } else {
            Solar.fromYmdHms(date.year, date.monthValue, date.dayOfMonth, h, m, 0)
        }
        return solar.lunar
    }

    private fun parsePillar(ganZhi: String): Pillar {
        // ganZhi is two Hanja characters, e.g. "甲子".
        require(ganZhi.length == 2) { "expected 2-char GanZhi, got: $ganZhi" }
        val stemIdx = STEM_HANZI.indexOf(ganZhi[0].toString())
        val branchIdx = BRANCH_HANZI.indexOf(ganZhi[1].toString())
        require(stemIdx >= 0) { "unknown stem: ${ganZhi[0]}" }
        require(branchIdx >= 0) { "unknown branch: ${ganZhi[1]}" }
        return Pillar(stemIdx, branchIdx)
    }

    private fun mapWuXingHanziToEn(hanzi: String): String = when (hanzi) {
        "木" -> ELEMENT_WOOD
        "火" -> ELEMENT_FIRE
        "土" -> ELEMENT_EARTH
        "金" -> ELEMENT_METAL
        "水" -> ELEMENT_WATER
        else -> "Unknown"
    }

    companion object {
        val STEMS = listOf(
            "甲 Jia", "乙 Yi", "丙 Bing", "丁 Ding", "戊 Wu",
            "己 Ji", "庚 Geng", "辛 Xin", "壬 Ren", "癸 Gui",
        )
        val BRANCHES = listOf(
            "子 Zi", "丑 Chou", "寅 Yin", "卯 Mao", "辰 Chen", "巳 Si",
            "午 Wu", "未 Wei", "申 Shen", "酉 You", "戌 Xu", "亥 Hai",
        )
        val ANIMALS = listOf(
            "Rat", "Ox", "Tiger", "Rabbit", "Dragon", "Snake",
            "Horse", "Goat", "Monkey", "Rooster", "Dog", "Pig",
        )

        /** Hanzi-only lookups for parsing `EightChar` / `DaYun` GanZhi strings. */
        val STEM_HANZI = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
        val BRANCH_HANZI = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

        /** Five elements (五行 / 오행). */
        const val ELEMENT_WOOD = "Wood"
        const val ELEMENT_FIRE = "Fire"
        const val ELEMENT_EARTH = "Earth"
        const val ELEMENT_METAL = "Metal"
        const val ELEMENT_WATER = "Water"

        fun stemElement(stemIndex: Int): String = when (stemIndex % 10) {
            0, 1 -> ELEMENT_WOOD
            2, 3 -> ELEMENT_FIRE
            4, 5 -> ELEMENT_EARTH
            6, 7 -> ELEMENT_METAL
            else -> ELEMENT_WATER
        }

        fun branchElement(branchIndex: Int): String = when (branchIndex % 12) {
            0, 11 -> ELEMENT_WATER
            2, 3 -> ELEMENT_WOOD
            5, 6 -> ELEMENT_FIRE
            8, 9 -> ELEMENT_METAL
            else -> ELEMENT_EARTH
        }
    }
}
