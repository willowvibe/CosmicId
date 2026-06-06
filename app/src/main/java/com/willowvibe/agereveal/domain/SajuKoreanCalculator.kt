package com.willowvibe.agereveal.domain

import com.nlf.calendar.EightChar
import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Korean Saju (사주 / 四柱) — the Korean naming + interpretation layer
 * that sits on top of [BaZiCalculator].
 *
 * Same Four Pillars math, but presented Hangul-first (천간·지지 갑을병정무기경신임계
 * / 자축인묘진사오미신유술해) with Korean cultural element colours, 대운
 * (Daeun 10-year luck periods) timeline, 오행 (Five Element) balance chart,
 * and 용신 (Yongshin) suggestion.
 *
 * Audience target: K-drama, K-pop, K-diaspora — see `roadmap.md` Mission 7.
 *
 * **Design note on 용신:** the classical Korean-school 용신 rule weighs the
 * Day Master's element against support (인성 / 식상 of the month branch)
 * and attack (관성 / 재성) across the 4 pillars. This calculator uses a
 * simplified "count by element, compare Day Master to its
 * supporting element" rule — see [suggestYongshin]. We document the
 * simplification because full 신강/신약 needs 신살 (special stars) and
 * support-weighting tables that the underlying library doesn't expose.
 */
@Singleton
class SajuKoreanCalculator @Inject constructor(
    private val baZiCalculator: BaZiCalculator,
) {

    // -------------------------------------------------------------------------
    // Hangul naming tables (천간 / 지지 in Korean reading)
    // Sourced from /home/harish/Downloads/saju/tools/saju_engine/lookup.py
    //   STEM_INFO[].korean  +  BRANCH_INFO[].korean
    // -------------------------------------------------------------------------

    data class StemLabels(
        val hanja: String,        // 漢字
        val hangul: String,       // 한글
        val elementEn: String,    // Wood / Fire / Earth / Metal / Water
        val elementHangul: String,// 목 / 화 / 토 / 금 / 수
        val elementHanja: String, // 木 / 火 / 土 / 金 / 水
        val polarity: String,     // Yang / Yin
    )

    data class BranchLabels(
        val hanja: String,
        val hangul: String,
        val elementEn: String,
        val elementHangul: String,
        val elementHanja: String,
        val animalEn: String,
        val animalHangul: String,
        val polarity: String,
        val hourRange: String,    // "23-01" etc — 12 double-hours
    )

    companion object {
        val STEMS: List<StemLabels> = listOf(
            StemLabels("甲", "갑", "Wood",  "목", "木", "Yang"),
            StemLabels("乙", "을", "Wood",  "목", "木", "Yin"),
            StemLabels("丙", "병", "Fire",  "화", "火", "Yang"),
            StemLabels("丁", "정", "Fire",  "화", "火", "Yin"),
            StemLabels("戊", "무", "Earth", "토", "土", "Yang"),
            StemLabels("己", "기", "Earth", "토", "土", "Yin"),
            StemLabels("庚", "경", "Metal", "금", "金", "Yang"),
            StemLabels("辛", "신", "Metal", "금", "金", "Yin"),
            StemLabels("壬", "임", "Water", "수", "水", "Yang"),
            StemLabels("癸", "계", "Water", "수", "水", "Yin"),
        )
        val BRANCHES: List<BranchLabels> = listOf(
            BranchLabels("子", "자", "Water", "수", "水", "Rat",    "쥐",  "Yang", "23-01"),
            BranchLabels("丑", "축", "Earth", "토", "土", "Ox",     "소",  "Yin",  "01-03"),
            BranchLabels("寅", "인", "Wood",  "목", "木", "Tiger",  "호랑이", "Yang", "03-05"),
            BranchLabels("卯", "묘", "Wood",  "목", "木", "Rabbit", "토끼", "Yin",  "05-07"),
            BranchLabels("辰", "진", "Earth", "토", "土", "Dragon", "용",  "Yang", "07-09"),
            BranchLabels("巳", "사", "Fire",  "화", "火", "Snake",  "뱀",  "Yin",  "09-11"),
            BranchLabels("午", "오", "Fire",  "화", "火", "Horse",  "말",  "Yang", "11-13"),
            BranchLabels("未", "미", "Earth", "토", "土", "Goat",   "양",  "Yin",  "13-15"),
            BranchLabels("申", "신", "Metal", "금", "金", "Monkey", "원숭이", "Yang", "15-17"),
            BranchLabels("酉", "유", "Metal", "금", "金", "Rooster","닭",  "Yin",  "17-19"),
            BranchLabels("戌", "술", "Earth", "토", "土", "Dog",    "개",  "Yang", "19-21"),
            BranchLabels("亥", "해", "Water", "수", "水", "Pig",    "돼지","Yin",  "21-23"),
        )

        /** Korean cultural element colours (RGB hex) for 오행 balance chart. */
        val ELEMENT_COLOURS: Map<String, Long> = mapOf(
            "Wood"  to 0xFF4CAF50,  // 초록 (green) — growth, spring
            "Fire"  to 0xFFE53935,  // 빨강 (red)   — passion, summer
            "Earth" to 0xFFFDD835,  // 노랑 (yellow) — stability, late-summer
            "Metal" to 0xFFECEFF1,  // 흰색 (white)  — clarity, autumn
            "Water" to 0xFF1976D2,  // 파랑 (blue)  — wisdom, winter
        )

        val ELEMENT_HANGUL: Map<String, String> = mapOf(
            "Wood" to "목", "Fire" to "화", "Earth" to "토",
            "Metal" to "금", "Water" to "수",
        )
        val ELEMENT_EMOJI: Map<String, String> = mapOf(
            "Wood"  to "🌳",
            "Fire"  to "🔥",
            "Earth" to "🏔️",
            "Metal" to "⚪",
            "Water" to "💧",
        )

        // -------------------------------------------------------------------------
        // Ten Gods (십신) names
        // -------------------------------------------------------------------------

        /** 십신 Korean name + English subtitle. */
        val TEN_GOD_NAMES: Map<String, String> = mapOf(
            "비견" to "Companion (比肩)",
            "겁재" to "Robber (劫財)",
            "식신" to "Eating God (食神)",
            "상관" to "Hurting Officer (傷官)",
            "편재" to "Indirect Wealth (偏財)",
            "정재" to "Direct Wealth (正財)",
            "편관" to "Seven Killings (偏官)",
            "정관" to "Direct Officer (正官)",
            "편인" to "Indirect Resource (偏印)",
            "정인" to "Direct Resource (正印)",
        )

        /**
         * Chinese 십신 label → `saju_ten_god_*` string-resource key.
         *
         * lunar-java v1.7.7 returns the *Chinese* 십신 labels (e.g. "正官",
         * "偏印", "伤官", "日主") on `ec.{year,month,day,time}ShiShenGan` —
         * NOT the Korean Hangul names. The UI layer remaps to the existing
         * `values-ko/saju_strings.xml` keys via this map.
         *
         * Special case: "日主" means "Day Master" (the day stem compared to
         * itself has no 십신 relationship) and is rendered as a special
         * "일간" / day-master label by the UI rather than via this map.
         *
         * Note: lunar-java uses simplified Chinese (偏财, 劫财, 正财) for
         * these labels; the resource keys are the same regardless.
         */
        val SHI_SHEN_ZH_TO_RES_KEY: Map<String, String> = mapOf(
            "比肩" to "saju_ten_god_bijeon",
            "劫财" to "saju_ten_god_geopjae",
            "食神" to "saju_ten_god_siksin",
            "伤官" to "saju_ten_god_sanggwan",
            "偏财" to "saju_ten_god_pyeonjae",
            "正财" to "saju_ten_god_jeongjae",
            "偏官" to "saju_ten_god_pyeonggwan",
            "正官" to "saju_ten_god_jeonggwan",
            "偏印" to "saju_ten_god_pyeonin",
            "正印" to "saju_ten_god_jeongin",
        )

        // -------------------------------------------------------------------------
        // 12 life stages (장생12신)
        // -------------------------------------------------------------------------

        val TWELVE_STAGES: List<String> = listOf(
            "장생", "목욕", "관대", "건록", "제왕", "쇠",
            "병", "사", "묘", "절", "태", "양",
        )

        // -------------------------------------------------------------------------
        // 오행 generating + overcoming cycles
        // -------------------------------------------------------------------------

        val GENERATES: Map<String, String> = mapOf(
            "Wood" to "Fire", "Fire" to "Earth", "Earth" to "Metal",
            "Metal" to "Water", "Water" to "Wood",
        )
        val OVERCOMES: Map<String, String> = mapOf(
            "Wood" to "Earth", "Earth" to "Water", "Water" to "Fire",
            "Fire" to "Metal", "Metal" to "Wood",
        )

        // -------------------------------------------------------------------------
        // Cultural lifestyle suggestions per element (for YongshinCard)
        // -------------------------------------------------------------------------

        val ELEMENT_COLOR_NAME: Map<String, String> = mapOf(
            "Wood" to "초록",
            "Fire" to "빨강",
            "Earth" to "노랑",
            "Metal" to "흰색",
            "Water" to "파랑",
        )
        val ELEMENT_DIRECTION: Map<String, String> = mapOf(
            "Wood"  to "동",
            "Fire"  to "남",
            "Earth" to "중앙",
            "Metal" to "서",
            "Water" to "북",
        )
        val ELEMENT_SEASON: Map<String, String> = mapOf(
            "Wood"  to "봄",
            "Fire"  to "여름",
            "Earth" to "환절기 (토의 계절)",
            "Metal" to "가을",
            "Water" to "겨울",
        )
        val ELEMENT_HANJA: Map<String, String> = mapOf(
            "Wood" to "木", "Fire" to "火", "Earth" to "土",
            "Metal" to "金", "Water" to "水",
        )
        val ELEMENT_ACTIONS: Map<String, List<String>> = mapOf(
            "Wood" to listOf(
                "초록 계열의 옷이나 소품을 가까이 두세요.",
                "동쪽으로 짧은 산책을 자주 다니면 좋아요.",
                "나무·식물 인테리어로 기운을 보충합니다.",
            ),
            "Fire" to listOf(
                "따뜻한 조명·촛불 등 밝은 환경을 선호합니다.",
                "남쪽 방향의 자리를 활용하면 좋습니다.",
                "활동적인 운동으로 에너지 순환을 도모하세요.",
            ),
            "Earth" to listOf(
                "노란색·베이지 계열 소품을 가까이 두세요.",
                "규칙적인 식사·수면으로 중심을 잡습니다.",
                "실내 안정적인 환경이 도움이 됩니다.",
            ),
            "Metal" to listOf(
                "흰색·은색 액세서리로 결단력을 보충합니다.",
                "서쪽 방향이 유리한 기운을 줍니다.",
                "정리·정돈·명상 등 명료한 활동을 추천합니다.",
            ),
            "Water" to listOf(
                "파란색·검정 계열이 안정감을 줍니다.",
                "북쪽 방향의 공간 활용을 권합니다.",
                "물가 산책·수영 등 물과 관련된 활동이 좋습니다.",
            ),
        )
    }

    // -------------------------------------------------------------------------
    // Domain model
    // -------------------------------------------------------------------------

    /**
     * A Korean-Saju-formatted pillar: 한자 + Hangul reading, element, animal.
     *
     * Distinct from [BaZiCalculator.Pillar] which is the math-layer object.
     * Callers consume this for display.
     */
    data class KoreanPillar(
        val stem: StemLabels,
        val branch: BranchLabels,
        val tenGod: String?,         // 십신 of the visible stem (e.g. "正官" from lunar-java — Chinese, not Korean)
        val branchTenGods: List<String>, // 십신 of hidden stems (1–3 entries)
        val twelveStage: String?,    // 12운성 of Day Master in this branch
        val element: String,         // primary element of this pillar (stem's element)
        val naYin: String,           // 納音 (e.g. "楊柳木")
    ) {
        /** Hangul-first display, e.g. "갑(甲)자(子)". */
        val displayHangul: String get() = "${stem.hangul}(${stem.hanja})${branch.hangul}(${branch.hanja})"
        /** Hanja-only display, e.g. "甲子". */
        val displayHanja: String get() = "${stem.hanja}${branch.hanja}"
    }

    /** A single major-luck (대운) period with Korean labels. */
    data class KoreanDaYunPeriod(
        val startAge: Int,
        val endAge: Int,
        val pillar: KoreanPillar,
    ) {
        val displayHangul: String get() = "${startAge}–${endAge}세: ${pillar.displayHangul}"
    }

    /**
     * 오행 (Five Element) balance across all four pillars.
     *
     * `visibleStemCounts` is element → count from the 4 visible stems (Year,
     * Month, Day, Hour). `branchCounts` is element → count from the 4 branches
     * (primary element of each branch). `hiddenStemCounts` is element → count
     * from all hidden stems across the 4 branches.
     *
     * `total` is `visibleStems + branches + hiddenStems` — the canonical
     * balance to render in the radar / bar chart.
     */
    data class OHaengBalance(
        val visibleStemCounts: Map<String, Int>,
        val branchCounts: Map<String, Int>,
        val hiddenStemCounts: Map<String, Int>,
    ) {
        val total: Map<String, Int> = run {
            val all = mutableMapOf<String, Int>()
            for (e in listOf("Wood", "Fire", "Earth", "Metal", "Water")) {
                all[e] = (visibleStemCounts[e] ?: 0) +
                        (branchCounts[e] ?: 0) +
                        (hiddenStemCounts[e] ?: 0)
            }
            all.toMap()
        }
        /** The element with the highest count, or null if tie. */
        val dominant: String? get() = total.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.key
        /** The element with the lowest count (may be 0 = absent). */
        val weakest: String? get() = total.minByOrNull { it.value }?.key
    }

    /** Result of the 용신 (favourable element) suggestion rule. */
    data class YongshinSuggestion(
        val dayMasterElement: String,
        val isStrong: Boolean,         // 신강 = true, 신약 = false (heuristic)
        val favourable: String,        // the suggested element
        val unfavourable: String,      // the element to avoid
        val reasoning: String,         // Korean-language explanation
    )

    /**
     * Rich card view of a 용신 recommendation, suitable for a Compose
     * "suggestion card" UI. Adds colour / direction / lifestyle hints per
     * Korean 사주 cultural convention (see `docs/saju/08-luck-pillars.md`).
     */
    data class YongshinCard(
        val dayMaster: String,         // e.g. "병화(丙火)"
        val dayMasterHangul: String,   // e.g. "병(丙) 화(火)"
        val status: String,            // "신강" or "신약" (Hangul label)
        val isStrong: Boolean,
        val favourableElementEn: String,
        val unfavourableElementEn: String,
        val favourableElementHangul: String,
        val favourableElementHanja: String,
        val favourableColorHex: Long,  // Korean cultural colour for the element
        val favourableColorName: String, // "초록", "빨강", "노랑", "흰색", "파랑"
        val favourableDirection: String, // "동", "남", "중앙", "서", "북"
        val favourableSeason: String,  // "봄", "여름", "환절기", "가을", "겨울"
        val actionSuggestions: List<String>, // Korean lifestyle suggestions
        val shortSummary: String,      // one-line summary
    )

    /** Full Korean Saju chart for a birth moment. */
    data class SajuChart(
        val year: KoreanPillar,
        val month: KoreanPillar,
        val day: KoreanPillar,
        val hour: KoreanPillar?,
        val dayMaster: StemLabels,
        val dayMasterElement: String,
        val dayMasterTwelveStage: String,  // e.g. "제왕" — Day Master in the month branch
        val oHaengBalance: OHaengBalance,
        val yongshin: YongshinSuggestion,
        val daeun: List<KoreanDaYunPeriod>,
        val birthDate: LocalDate,
        val birthHour: Int?,
    ) {
        fun displaySummary(): String = buildString {
            append("년주: ").append(year.displayHangul)
            append(" · 월주: ").append(month.displayHangul)
            append(" · 일주: ").append(day.displayHangul)
            if (hour != null) append(" · 시주: ").append(hour.displayHangul)
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Compute a complete Korean Saju chart for a birth moment.
     *
     * @param date Birth date (Gregorian)
     * @param hour Optional hour 0–23. If null, the day master strength and
     *   대운 are not computed (the hour pillar is omitted from the chart).
     * @param minute Optional minute
     * @param zoneOffsetHours UTC offset of birth location (e.g. 9.0 Korea)
     * @param gender Optional gender for 대운 computation; if null, Daeun is empty
     */
    fun computeChart(
        date: LocalDate,
        hour: Int? = null,
        minute: Int? = null,
        zoneOffsetHours: Double? = null,
        gender: BaZiCalculator.Gender? = null,
    ): SajuChart {
        val fourPillars = baZiCalculator.computeFourPillars(date, hour, minute, zoneOffsetHours)
        val eightChar = buildEightChar(date, hour, minute, zoneOffsetHours)

        val yearP = buildKoreanPillar(eightChar, BaZiCalculator.PillarPosition.YEAR)
        val monthP = buildKoreanPillar(eightChar, BaZiCalculator.PillarPosition.MONTH)
        val dayP = buildKoreanPillar(eightChar, BaZiCalculator.PillarPosition.DAY)
        val hourP = hour?.let { buildKoreanPillar(eightChar, BaZiCalculator.PillarPosition.HOUR) }

        val dayMaster = STEMS[fourPillars.day.stemIndex]
        val dayMasterElement = fourPillars.dayMasterElement

        val oHaeng = computeOHaengBalance(yearP, monthP, dayP, hourP)
        val yongshin = suggestYongshin(dayMasterElement, oHaeng)
        val daeun = if (hour != null && gender != null) {
            computeDaeun(date, hour, minute ?: 0, gender, zoneOffsetHours, eightChar, yearP, monthP, dayP, hourP)
        } else emptyList()

        return SajuChart(
            year = yearP, month = monthP, day = dayP, hour = hourP,
            dayMaster = dayMaster,
            dayMasterElement = dayMasterElement,
            dayMasterTwelveStage = dayP.twelveStage ?: "",
            oHaengBalance = oHaeng,
            yongshin = yongshin,
            daeun = daeun,
            birthDate = date,
            birthHour = hour,
        )
    }

    /** Just the four Hangul-formatted pillars + Day Master — lightweight view. */
    fun chartPillars(
        date: LocalDate,
        hour: Int? = null,
        minute: Int? = null,
        zoneOffsetHours: Double? = null,
    ): Pair<List<KoreanPillar>, StemLabels> {
        val fourPillars = baZiCalculator.computeFourPillars(date, hour, minute, zoneOffsetHours)
        val eightChar = buildEightChar(date, hour, minute, zoneOffsetHours)
        val list = buildList {
            add(buildKoreanPillar(eightChar, BaZiCalculator.PillarPosition.YEAR))
            add(buildKoreanPillar(eightChar, BaZiCalculator.PillarPosition.MONTH))
            add(buildKoreanPillar(eightChar, BaZiCalculator.PillarPosition.DAY))
            if (hour != null) add(buildKoreanPillar(eightChar, BaZiCalculator.PillarPosition.HOUR))
        }
        return list to STEMS[fourPillars.day.stemIndex]
    }

    /**
     * Build a rich [YongshinCard] for a [SajuChart]. Use this for a Compose
     * "추천 카드" — short summary line + colour swatch + Korean action
     * suggestions in one place.
     */
    fun buildYongshinCard(chart: SajuChart): YongshinCard {
        val sug = chart.yongshin
        val fav = sug.favourable
        val elementHangul = ELEMENT_HANGUL[fav] ?: fav
        val elementHanja = ELEMENT_HANJA[fav] ?: fav
        val colorName = ELEMENT_COLOR_NAME[fav] ?: ""
        val colorHex = ELEMENT_COLOURS[fav] ?: 0xFF000000
        val direction = ELEMENT_DIRECTION[fav] ?: ""
        val season = ELEMENT_SEASON[fav] ?: ""
        val actions = ELEMENT_ACTIONS[fav] ?: emptyList()
        val dayMasterHangul = "${chart.dayMaster.hangul}(${chart.dayMaster.hanja}) ${ELEMENT_HANGUL[chart.dayMasterElement]}(${ELEMENT_HANJA[chart.dayMasterElement]})"
        val statusHangul = if (sug.isStrong) "신강" else "신약"
        val shortSummary = "${chart.dayMaster.hangul}(${chart.dayMaster.hanja}) ${ELEMENT_HANGUL[chart.dayMasterElement]} 일간은 ${statusHangul}합니다. 용신은 ${elementHangul}(${elementHanja})이에요."
        return YongshinCard(
            dayMaster = chart.dayMaster.hanja + ELEMENT_HANJA[chart.dayMasterElement].orEmpty(),
            dayMasterHangul = dayMasterHangul,
            status = statusHangul,
            isStrong = sug.isStrong,
            favourableElementEn = fav,
            unfavourableElementEn = sug.unfavourable,
            favourableElementHangul = elementHangul,
            favourableElementHanja = elementHanja,
            favourableColorHex = colorHex,
            favourableColorName = colorName,
            favourableDirection = direction,
            favourableSeason = season,
            actionSuggestions = actions,
            shortSummary = shortSummary,
        )
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private fun buildEightChar(
        date: LocalDate, hour: Int?, minute: Int?, zoneOffsetHours: Double?,
    ): EightChar {
        val solar = if (zoneOffsetHours != null) {
            val local = java.time.LocalDateTime.of(date, java.time.LocalTime.of(hour ?: 12, minute ?: 0))
            val off = java.time.ZoneOffset.ofHoursMinutes(
                zoneOffsetHours.toInt(),
                ((zoneOffsetHours - zoneOffsetHours.toInt()) * 60).toInt(),
            )
            val zoned = local.atOffset(off)
            Solar.fromYmdHms(zoned.year, zoned.monthValue, zoned.dayOfMonth,
                zoned.hour, zoned.minute, zoned.second)
        } else {
            Solar.fromYmdHms(date.year, date.monthValue, date.dayOfMonth,
                hour ?: 12, minute ?: 0, 0)
        }
        return solar.lunar.eightChar
    }

    private fun buildKoreanPillar(ec: EightChar, pos: BaZiCalculator.PillarPosition): KoreanPillar {
        val (gan, zhi, hideGan, wuXing, naYin, shiShenGan, shiShenZhi, diShi) = when (pos) {
            BaZiCalculator.PillarPosition.YEAR -> PillarData(
                ec.yearGan, ec.yearZhi, ec.yearHideGan, ec.yearWuXing,
                ec.yearNaYin, ec.yearShiShenGan, ec.yearShiShenZhi, ec.yearDiShi,
            )
            BaZiCalculator.PillarPosition.MONTH -> PillarData(
                ec.monthGan, ec.monthZhi, ec.monthHideGan, ec.monthWuXing,
                ec.monthNaYin, ec.monthShiShenGan, ec.monthShiShenZhi, ec.monthDiShi,
            )
            BaZiCalculator.PillarPosition.DAY -> PillarData(
                ec.dayGan, ec.dayZhi, ec.dayHideGan, ec.dayWuXing,
                ec.dayNaYin, ec.dayShiShenGan, ec.dayShiShenZhi, ec.dayDiShi,
            )
            BaZiCalculator.PillarPosition.HOUR -> PillarData(
                ec.timeGan, ec.timeZhi, ec.timeHideGan, ec.timeWuXing,
                ec.timeNaYin, ec.timeShiShenGan, ec.timeShiShenZhi, ec.timeDiShi,
            )
        }
        val stemIdx = STEMS.indexOfFirst { it.hanja == gan }
        val branchIdx = BRANCHES.indexOfFirst { it.hanja == zhi }
        require(stemIdx >= 0) { "unknown stem from lunar-java: $gan" }
        require(branchIdx >= 0) { "unknown branch from lunar-java: $zhi" }
        val stem = STEMS[stemIdx]
        val branch = BRANCHES[branchIdx]
        // 십신 values are lunar-java's *Chinese* labels (正官, 偏印, 伤官, …).
        // The UI remaps to Korean via [SHI_SHEN_ZH_TO_RES_KEY] when rendering
        // in the Korean locale. See bazi-depth-batch-2026-06-06 memory.
        val branchTenGods = hideGan.zip(shiShenZhi).map { it.second }
        return KoreanPillar(
            stem = stem,
            branch = branch,
            tenGod = shiShenGan.takeIf { it.isNotEmpty() },
            branchTenGods = branchTenGods,
            twelveStage = diShi.takeIf { it.isNotEmpty() },
            element = wuXingToEn(wuXing),
            naYin = naYin,
        )
    }

    private fun computeOHaengBalance(
        year: KoreanPillar, month: KoreanPillar, day: KoreanPillar, hour: KoreanPillar?,
    ): OHaengBalance {
        val stems = mutableMapOf<String, Int>()
        val branches = mutableMapOf<String, Int>()
        val hidden = mutableMapOf<String, Int>()
        for (p in listOfNotNull(year, month, day, hour)) {
            stems.merge(p.element, 1, Int::plus)
            branches.merge(p.branch.elementEn, 1, Int::plus)
            // Hidden stems aren't directly returned in our KoreanPillar model;
            // re-derive from the lookup tables for accurate counts.
            val branchHidden = HIDDEN_STEMS[p.branch.hanja] ?: emptyMap()
            for ((_, stemHanja) in branchHidden) {
                val stemElement = STEMS.first { it.hanja == stemHanja }.elementEn
                hidden.merge(stemElement, 1, Int::plus)
            }
        }
        return OHaengBalance(
            visibleStemCounts = stems.toMap(),
            branchCounts = branches.toMap(),
            hiddenStemCounts = hidden.toMap(),
        )
    }

    /**
     * 용신 (Yongshin) — rule-based favourable element suggestion.
     *
     * Simplified Korean-school rule:
     *   - Count the Day Master's element across visible stems + branches +
     *     hidden stems (from [OHaengBalance.total]).
     *   - Count its "support" element (the one that GENERATES it per the
     *     오행 cycle) the same way.
     *   - If Day Master count + 0.5*support count > 2 (≈ majority), it's
     *     신강 (strong) → suggest the element it OVERCOMES (drains it).
     *   - Otherwise 신약 (weak) → suggest the element that GENERATES it
     *     (supports it).
     *   - Avoid the element that OVERCOMES the Day Master (attack).
     *
     * This is a starting point, not classical 신살-aware analysis. A full
     * reading weights the month branch (令) more heavily and accounts for
     * seasonal support, which the underlying library exposes via
     * `Lunar.getJieQi()` but not in a tabular form. Documented in the
     * class header and in the [SajuChart.yongshin] field.
     */
    private fun suggestYongshin(dayMasterElement: String, balance: OHaengBalance): YongshinSuggestion {
        val total = balance.total
        val dayCount = total[dayMasterElement] ?: 0
        val support = GENERATES.entries.first { it.value == dayMasterElement }.key
        val supportCount = total[support] ?: 0
        val overcomes = OVERCOMES[dayMasterElement] ?: error("unreachable")
        val overcomesCount = total[overcomes] ?: 0

        // Heuristic: strong if Day Master alone is dominant, or Day Master +
        // its support (scaled down) outweighs the rest.
        val totalAll = total.values.sum().coerceAtLeast(1)
        val dayMasterWeighted = dayCount + (supportCount * 0.5f)
        val isStrong = dayMasterWeighted > totalAll * 0.40f && overcomesCount <= 1

        val favourable = if (isStrong) overcomes else support
        val unfavourable = OVERCOMES[favourable] ?: error("unreachable")

        val reasoning = buildString {
            if (isStrong) {
                append("일간(").append(ELEMENT_HANGUL[dayMasterElement]).append(")이(가) 사주에 강하게 자리잡고 있어, ")
                append("약화시키는 ").append(ELEMENT_HANGUL[favourable])
                append(" 기운이 용신으로 적합합니다.")
            } else {
                append("일간(").append(ELEMENT_HANGUL[dayMasterElement]).append(")이(가) 사주에 약하게 자리잡고 있어, ")
                append("생해주는 ").append(ELEMENT_HANGUL[support])
                append(" 기운이 용신으로 적합합니다.")
            }
        }
        return YongshinSuggestion(
            dayMasterElement = dayMasterElement,
            isStrong = isStrong,
            favourable = favourable,
            unfavourable = unfavourable,
            reasoning = reasoning,
        )
    }

    private fun computeDaeun(
        date: LocalDate, hour: Int, minute: Int,
        gender: BaZiCalculator.Gender,
        zoneOffsetHours: Double?,
        eightChar: EightChar,
        year: KoreanPillar, month: KoreanPillar, day: KoreanPillar, hourP: KoreanPillar?,
    ): List<KoreanDaYunPeriod> {
        val baZiPeriods = baZiCalculator.computeDaYun(
            date, hour, minute, gender, zoneOffsetHours, nPeriods = 8,
        )
        // Map each (startAge, endAge, stemIndex, branchIndex) to a fresh
        // KoreanPillar using the lookup tables (we don't have EightChar
        // for the future periods).
        return baZiPeriods.map { p ->
            val stem = STEMS[p.stemIndex]
            val branch = BRANCHES[p.branchIndex]
            KoreanDaYunPeriod(
                startAge = p.startAge,
                endAge = p.endAge,
                pillar = KoreanPillar(
                    stem = stem,
                    branch = branch,
                    tenGod = null,
                    branchTenGods = emptyList(),
                    twelveStage = null,
                    element = stem.elementEn,
                    naYin = "",
                ),
            )
        }
    }

    /** Parse lunar-java's "木火" style WuXing string into English element name. */
    private fun wuXingToEn(wuXing: String): String = when {
        wuXing.contains("木") -> "Wood"
        wuXing.contains("火") -> "Fire"
        wuXing.contains("土") -> "Earth"
        wuXing.contains("金") -> "Metal"
        wuXing.contains("水") -> "Water"
        else -> "Unknown"
    }

    /** Internal carrier for the per-pillar getter results. */
    private data class PillarData(
        val gan: String,
        val zhi: String,
        val hideGan: List<String>,
        val wuXing: String,
        val naYin: String,
        val shiShenGan: String,
        val shiShenZhi: List<String>,
        val diShi: String,
    )

    /** Hidden stems per branch (지장간). From `lookup.py:HIDDEN_STEMS`. */
    private val HIDDEN_STEMS: Map<String, Map<String, String>> = mapOf(
        "子" to mapOf("main" to "癸"),
        "丑" to mapOf("main" to "己", "middle" to "癸", "residual" to "辛"),
        "寅" to mapOf("main" to "甲", "middle" to "丙", "residual" to "戊"),
        "卯" to mapOf("main" to "乙"),
        "辰" to mapOf("main" to "戊", "middle" to "乙", "residual" to "癸"),
        "巳" to mapOf("main" to "丙", "middle" to "庚", "residual" to "戊"),
        "午" to mapOf("main" to "丁", "middle" to "己"),
        "未" to mapOf("main" to "己", "middle" to "丁", "residual" to "乙"),
        "申" to mapOf("main" to "庚", "middle" to "壬", "residual" to "戊"),
        "酉" to mapOf("main" to "辛"),
        "戌" to mapOf("main" to "戊", "middle" to "辛", "residual" to "丁"),
        "亥" to mapOf("main" to "壬", "middle" to "甲"),
    )
}
