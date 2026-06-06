package com.willowvibe.agereveal.domain

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 사주 궁합 (Korean Saju compatibility) scoring.
 *
 * Produces a 0–100 score for the compatibility of two people based on
 * their Four Pillars (사주). Designed for K-drama / K-pop / K-diaspora
 * audiences who want a Korean-cultural reading alongside the existing
 * Western + Chinese compatibility in
 * [ZodiacCompatibilityCalculator]. See `roadmap.md` Mission 7.
 *
 * Scoring breakdown (sums to 100):
 *   - 40 pts — Day Master element affinity (오행 상생/상극)
 *   - 30 pts — Day Master stem relation (합/충/破/刑)
 *   - 20 pts — Year branch relation (六合/三合/六沖/六害/三刑)
 *   - 10 pts — Birth chart balance complementarity (do their 五行
 *             imbalances complement each other?)
 *
 * The 0–100 score is bucketed into cultural verdict labels:
 *   - 90+ 천생연분 (destined pair)
 *   - 75+ 좋은 인연 (good match)
 *   - 55+ 무난 (fair / neutral)
 *   - 35+ 노력 필요 (needs effort)
 *   - <35  인연 약함 (weak connection)
 *
 * **Design note:** this is a rule-based starting point. The classical
 * 사주 궁합 school weighs 십신, 12운성, 납음, and 大運 in addition. We're
 * covering the four highest-signal axes here; future work can fold in
 * the rest.
 */
@Singleton
class SajuKoreanCompatibilityCalculator @Inject constructor(
    private val sajuCalculator: SajuKoreanCalculator,
) {

    /** Overall compatibility result, with breakdown + Korean label. */
    data class SajuCompatibility(
        val totalScore: Int,                  // 0–100
        val verdict: Verdict,
        val elementScore: Int,                // 0–40
        val stemRelationScore: Int,           // 0–30
        val branchRelationScore: Int,         // 0–20
        val balanceComplementarityScore: Int, // 0–10
        val dayMasterA: String,
        val dayMasterB: String,
        val dayMasterElementA: String,
        val dayMasterElementB: String,
        val stemRelationLabel: String,        // e.g. "갑(甲)·갑(甲) — 비견(比肩)"
        val branchRelationLabel: String,      // e.g. "자(子)·축(丑) — 六合"
        val headline: String,                 // Korean one-liner
        val description: String,              // Korean paragraph
    )

    enum class Verdict(
        val threshold: Int,
        val labelHangul: String,
        val labelHanja: String,
    ) {
        EXCELLENT(90, "천생연분", "天緣"),
        GOOD(75, "좋은 인연", "良緣"),
        FAIR(55, "무난한 인연", "平緣"),
        CHALLENGING(35, "노력이 필요한 인연", "勉緣"),
        WEAK(0, "인연이 약함", "薄緣");

        companion object {
            fun forScore(score: Int): Verdict = values()
                .sortedByDescending { it.threshold }
                .first { score >= it.threshold }
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Compute 사주 궁합 for two people.
     *
     * @param dateA / [dateB] Birth dates (Gregorian)
     * @param hourA / [hourB] Optional birth hours. Required for full
     *              accuracy (Day Master uses only the day-pillar, but the
     *              balance and stem-relation scoring use the full chart
     *              when both hours are given).
     * @param nameA / [nameB] Display names for the verdict string.
     */
    fun calculate(
        dateA: LocalDate,
        dateB: LocalDate,
        nameA: String = "",
        nameB: String = "",
        hourA: Int? = null,
        hourB: Int? = null,
    ): SajuCompatibility {
        val chartA = sajuCalculator.computeChart(dateA, hourA, gender = null)
        val chartB = sajuCalculator.computeChart(dateB, hourB, gender = null)

        val elementA = chartA.dayMasterElement
        val elementB = chartB.dayMasterElement
        val stemA = chartA.dayMaster
        val stemB = chartB.dayMaster
        val branchA = chartA.year.branch
        val branchB = chartB.year.branch

        val elementScore = elementAffinityScore(elementA, elementB)
        val stemRelation = stemRelation(stemA, stemB)
        val branchRelation = branchRelation(branchA, branchB)
        val balanceScore = balanceComplementarity(chartA, chartB)

        // Weighted sum
        val total = elementScore + stemRelation.score + branchRelation.score + balanceScore
        val totalScore = total.coerceIn(0, 100)
        val verdict = Verdict.forScore(totalScore)

        val displayA = nameA.ifBlank { "A" }
        val displayB = nameB.ifBlank { "B" }
        val headline = "${displayA}와(과) ${displayB}의 사주 궁합: ${verdict.labelHangul} (${totalScore}점)"
        val description = buildDescription(
            displayA, displayB, elementA, elementB, stemRelation, branchRelation, verdict,
        )

        return SajuCompatibility(
            totalScore = totalScore,
            verdict = verdict,
            elementScore = elementScore,
            stemRelationScore = stemRelation.score,
            branchRelationScore = branchRelation.score,
            balanceComplementarityScore = balanceScore,
            dayMasterA = stemA.hangul + "(" + stemA.hanja + ")",
            dayMasterB = stemB.hangul + "(" + stemB.hanja + ")",
            dayMasterElementA = elementA,
            dayMasterElementB = elementB,
            stemRelationLabel = stemRelation.label,
            branchRelationLabel = branchRelation.label,
            headline = headline,
            description = description,
        )
    }

    // -------------------------------------------------------------------------
    // 1. Element affinity (40 pts)
    // -------------------------------------------------------------------------

    /**
     * Generates (Wood→Fire, Fire→Earth, Earth→Metal, Metal→Water, Water→Wood)
     * is the strongest positive relationship — each element "feeds" the next.
     *
     * Overcomes (Wood→Earth, Earth→Water, Water→Fire, Fire→Metal, Metal→Wood)
     * is destructive.
     *
     * Same element is neutral; opposites in the cycle are weakest.
     */
    private fun elementAffinityScore(a: String, b: String): Int {
        if (a == b) return 25  // Same element: stable but no spark
        val generates = SajuKoreanCalculator.GENERATES
        if (generates[a] == b || generates[b] == a) return 40  // 상생 (mutual generation)
        val overcomes = SajuKoreanCalculator.OVERCOMES
        if (overcomes[a] == b || overcomes[b] == a) return 5   // 상극 (clash)
        return 20  // neutral / cycle-adjacent
    }

    // -------------------------------------------------------------------------
    // 2. Stem relation (30 pts) — 합/충/破/刑 between day stems
    // -------------------------------------------------------------------------

    private data class StemRelation(val score: Int, val label: String)

    /**
     * 천간 합 (heavenly stem combinations) — 5 pairs that produce a new
     * "combined" element. Highly auspicious in 궁합:
     *   갑(甲)·기(己) → 土   을(乙)·경(庚) → 金
     *   병(丙)·신(辛) → 水   정(丁)·임(壬) → 木
     *   무(戊)·계(癸) → 火
     *
     * 천간 충 (clash) — 6 pairs of opposite stems (positive-index-distance
     * 5 in the stem cycle). Inauspicious in 궁합:
     *   갑(甲)·경(庚)  을(乙)·신(辛)
     *   병(丙)·임(壬)  정(丁)·계(癸)
     *   무(戊)·병(丙)  기(己)·정(丁)  (same polarity-adjacent — debatable)
     */
    private fun stemRelation(a: SajuKoreanCalculator.StemLabels, b: SajuKoreanCalculator.StemLabels): StemRelation {
        val ha = a.hanja
        val hb = b.hanja
        // 합
        val hePairs = setOf(
            "甲" to "己", "乙" to "庚", "丙" to "辛",
            "丁" to "壬", "戊" to "癸",
        )
        val isHe = hePairs.any { (x, y) -> (ha == x && hb == y) || (ha == y && hb == x) }
        if (isHe) {
            val combined = combinedElementFor(ha, hb)
            return StemRelation(30, "${a.hangul}(${a.hanja})·${b.hangul}(${b.hanja}) — 천간합 → $combined")
        }
        // 충 (clash) — distance 5 in the stem cycle (mod 10)
        val idxA = a.hanjaIndex
        val idxB = b.hanjaIndex
        val dist = (idxB - idxA + 10) % 10
        if (dist == 5) {
            return StemRelation(0, "${a.hangul}(${a.hanja})·${b.hangul}(${b.hanja}) — 천간충 (clash)")
        }
        if (dist == 4 || dist == 6) {
            // 相害 / 害 — secondary harm
            return StemRelation(10, "${a.hangul}(${a.hanja})·${b.hangul}(${b.hanja}) — 상해")
        }
        // 비견 / 겁재 (same stem = same element)
        if (a.elementEn == b.elementEn) {
            val rel = if (idxA == idxB) "비견" else "겁재"
            return StemRelation(15, "${a.hangul}(${a.hanja})·${b.hangul}(${b.hanja}) — $rel")
        }
        // Neutral
        return StemRelation(20, "${a.hangul}(${a.hanja})·${b.hangul}(${b.hanja}) — 중립")
    }

    private fun combinedElementFor(a: String, b: String): String {
        // From the 5-he pairs above, the combined element is the element of
        // the stem that "receives" the combination. Reference: the saju
        // 합 produces the element of the second stem when listed first.
        val pairToElement = mapOf(
            ("甲" to "己") to "토(Earth)",
            ("乙" to "庚") to "금(Metal)",
            ("丙" to "辛") to "수(Water)",
            ("丁" to "壬") to "목(Wood)",
            ("戊" to "癸") to "화(Fire)",
        )
        return pairToElement[a to b] ?: pairToElement[b to a] ?: "?"
    }

    // -------------------------------------------------------------------------
    // 3. Branch relation (20 pts) — 六合/三合/六沖/六害/三刑 between year branches
    // -------------------------------------------------------------------------

    private data class BranchRelation(val score: Int, val label: String)

    /**
     * 六合 (6 harmonious pairs) — most auspicious for couple 궁합:
     *   子·丑  寅·亥  卯·戌  辰·酉  巳·申  午·未
     *
     * 三合 (3-harmony triangles) — also auspicious:
     *   申子辰(수국)  亥卯未(목국)  寅午戌(화국)  巳酉丑(금국)
     *
     * 六沖 (6 clashes) — most inauspicious:
     *   子·午  丑·未  寅·申  卯·酉  辰·戌  巳·亥
     *
     * 六害 (6 harms) — second-worst:
     *   子·未  丑·午  寅·巳  卯·辰  申·亥  酉·戌
     */
    private fun branchRelation(
        a: SajuKoreanCalculator.BranchLabels,
        b: SajuKoreanCalculator.BranchLabels,
    ): BranchRelation {
        val ba = a.hanja
        val bb = b.hanja
        val pair = setOf(ba, bb)

        // 六合
        val liuHe = setOf(
            setOf("子", "丑"), setOf("寅", "亥"), setOf("卯", "戌"),
            setOf("辰", "酉"), setOf("巳", "申"), setOf("午", "未"),
        )
        if (liuHe.any { it == pair }) {
            return BranchRelation(20, "${a.hangul}(${a.hanja})·${b.hangul}(${b.hanja}) — 六合")
        }
        // 三合
        val sanHe = listOf(
            setOf("申", "辰", "子"),
            setOf("亥", "未", "卯"),
            setOf("寅", "戌", "午"),
            setOf("巳", "丑", "酉"),
        )
        if (sanHe.any { it == pair }) {
            return BranchRelation(15, "${a.hangul}(${a.hanja})·${b.hangul}(${b.hanja}) — 삼합(三合)")
        }
        // 六沖
        val liuChong = setOf(
            setOf("子", "午"), setOf("丑", "未"), setOf("寅", "申"),
            setOf("卯", "酉"), setOf("辰", "戌"), setOf("巳", "亥"),
        )
        if (liuChong.any { it == pair }) {
            return BranchRelation(0, "${a.hangul}(${a.hanja})·${b.hangul}(${b.hanja}) — 六沖 (clash)")
        }
        // 六害
        val liuHai = setOf(
            setOf("子", "未"), setOf("丑", "午"), setOf("寅", "巳"),
            setOf("卯", "辰"), setOf("申", "亥"), setOf("酉", "戌"),
        )
        if (liuHai.any { it == pair }) {
            return BranchRelation(2, "${a.hangul}(${a.hanja})·${b.hangul}(${b.hanja}) — 六害")
        }
        return BranchRelation(10, "${a.hangul}(${a.hanja})·${b.hangul}(${b.hanja}) — 중립")
    }

    // -------------------------------------------------------------------------
    // 4. Balance complementarity (10 pts)
    // -------------------------------------------------------------------------

    /**
     * If A is heavy in Wood and B is heavy in Fire, they "balance" each
     * other (A feeds B). If both are heavy in the same element, the
     * relationship is imbalanced.
     *
     * We compare the 4 visible stems + 4 branches (8 elements total per
     * person) and award points when the dominant elements are mutually
     * generating.
     */
    private fun balanceComplementarity(
        a: SajuKoreanCalculator.SajuChart,
        b: SajuKoreanCalculator.SajuChart,
    ): Int {
        val aTotal = a.oHaengBalance.total
        val bTotal = b.oHaengBalance.total
        val aDom = aTotal.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.key ?: return 5
        val bDom = bTotal.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.key ?: return 5
        // If their dominant elements generate each other → bonus
        if (SajuKoreanCalculator.GENERATES[aDom] == bDom ||
            SajuKoreanCalculator.GENERATES[bDom] == aDom
        ) {
            return 10
        }
        if (aDom == bDom) return 2  // same dominant → mild redundancy
        return 5  // neutral
    }

    // -------------------------------------------------------------------------
    // Description builder
    // -------------------------------------------------------------------------

    private fun buildDescription(
        nameA: String, nameB: String,
        elementA: String, elementB: String,
        stemRelation: StemRelation,
        branchRelation: BranchRelation,
        verdict: Verdict,
    ): String = buildString {
        val elemA = SajuKoreanCalculator.ELEMENT_HANGUL[elementA] ?: elementA
        val elemB = SajuKoreanCalculator.ELEMENT_HANGUL[elementB] ?: elementB
        append("두 분의 일간은 각각 ")
        append(elemA).append("(").append(elementA).append("), ")
        append(elemB).append("(").append(elementB).append(") 입니다. ")
        when (SajuKoreanCalculator.GENERATES[elementA]) {
            elementB -> append("A의 $elemA 기운이 B의 $elemB 기운을 생해주어 서로에게 도움이 됩니다. ")
            else -> when (SajuKoreanCalculator.GENERATES[elementB]) {
                elementA -> append("B의 $elemB 기운이 A의 $elemA 기운을 생해주어 서로에게 도움이 됩니다. ")
                else -> append("두 오행의 관계가 중립적입니다. ")
            }
        }
        append(stemRelation.label).append(", ")
        append(branchRelation.label).append("의 관계입니다. ")
        append("종합적으로 ").append(verdict.labelHangul).append(" 사주로 봅니다.")
    }
}

/** Helper for [SajuKoreanCalculator.StemLabels] — find the index of the Hanji in the canonical stem table. */
private val SajuKoreanCalculator.StemLabels.hanjaIndex: Int
    get() = SajuKoreanCalculator.STEMS.indexOf(this)
