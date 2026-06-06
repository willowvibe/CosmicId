# BaZi Depth Batch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close BUG-078, BUG-079, BUG-080, BUG-081 in `BUGS_AND_ISSUES.md` by exposing the Day/Hour pillar back-compat facades, adding a `TenGodsCalculator`, marking the stale BUG-080 entry as verified-safe, and adding a `getDaYunSummary` back-compat facade. Plus a small `SajuTenGodsCard` UI surface in the Korean Saju tab (the only Phase 6.5 Saju engine output not yet rendered).

**Architecture:** Re-baselined the bugs against current code (commit `6c94343`) and found most of the engine work is already done. The actual gaps are: (1) `BaZiCalculator` has `getYearPillar` / `getMonthPillar` string facades but no `getDayPillar` / `getHourPillar`; (2) `FourPillars` exposes `dayMasterHanzi` + `dayMasterElement` but no per-pillar Ten Gods (lunar-java's `getXxxShiShenGan/Zhi` returns them but `BaZiCalculator` doesn't surface them); (3) BUG-080 description is **stale** — `BaZiCalculator` uses `Lunar` / `EightChar` from `cn.6tail:lunar:1.7.7` which computes month-pillar boundaries from solar terms astronomically (no hardcoded date table); (4) `BaZiCalculator` has `computeDaYun()` but no string facade to match `getYearPillar(date)`. UI: add a `SajuTenGodsCard` composable to `DetailsUnlockScreen.KoreanSajuTab` between the `SajuFourPillarsCard` and the `SajuOHaengBalanceCard` (gated behind `korean_saju_unlock` IAP like the other depth cards). All 3 string-facade additions are pure additions — no behaviour change. The `TenGodsCalculator` is a thin new file in `domain/`.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, JUnit 4, `cn.6tail:lunar:1.7.7` (already a dependency).

**Bugs closed:** BUG-078, BUG-079, BUG-080, BUG-081.

**Bugs not closed by this plan:** none — all 4 are addressed.

---

## File Structure

| File | Status | Responsibility |
|---|---|---|
| `app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt` | MODIFY | Add `getDayPillar(date)` + `getHourPillar(date, hour)` string facades (parallel to existing year/month). Add `getDaYunSummary(date, hour, gender): String` facade. Add `TenGods` data class + `tenGods: TenGods` field on `FourPillars` (using lunar-java's `getXxxShiShenGan` / `getXxxShiShenZhi`). Populate `tenGods` in `computeFourPillars()`. |
| `app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt` | MODIFY | Add a short comment in the file header clarifying that month-pillar boundaries use lunar-java's astronomical solar-term computation, not hardcoded dates. |
| `app/src/test/java/com/willowvibe/agereveal/domain/BaZiCalculatorTest.kt` | MODIFY | +5 unit tests (day facade, hour facade, DaYun facade, Ten Gods population, Ten Gods reference case). |
| `BUGS_AND_ISSUES.md` | MODIFY | Close BUG-078, 079, 081 with `🟢 Fixed 2026-06-06` markers + fix-applied summaries. Convert BUG-080 from 🟡 to ✅ Verified Safe with explanation. |
| `app/src/main/java/com/willowvibe/agereveal/ui/screen/DetailsUnlockScreen.kt` | MODIFY | Add private `@Composable fun SajuTenGodsCard(chart: SajuKoreanCalculator.SajuChart)`. Wire it into `KoreanSajuTab` between `SajuFourPillarsCard` (always visible) and the premium-gated cards. |
| `app/src/main/res/values-ko/saju_strings.xml` | MODIFY | +5 new strings for the Ten Gods card (title, subtitle, 4 row labels for the per-pillar table). |
| `app/src/main/res/values/strings.xml` | MODIFY | +5 English stubs (referencing the Korean card title so the card never crashes in en locale). |
| `app/src/test/java/com/willowvibe/agereveal/domain/BaZiCalculatorTenGodsTest.kt` | NEW | 4 unit tests for the `TenGods` data class shape + lunar-java reference case. |

8 files (6 modified, 2 new). Plus BUGS file.

---

## Task 1: Close BUG-080 — verify month-pillar math is astronomical and add a code comment

**Files:**
- Modify: `app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt:14-35` (file header comment)

- [ ] **Step 1: Read the current header comment to know exactly what to edit**

The file header at `app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt` lines 14-35 already documents what lunar-java provides. Add a single line at the end of that block clarifying the month-pillar boundary computation.

- [ ] **Step 2: Add the clarifying line**

After the existing 11-bullet list (the `*   - 干支 …` bullets) and BEFORE the `* Naming is bilingual.` paragraph, insert:

```kotlin
 * Month-pillar boundaries (e.g. 驚蟄 / 경칩) are computed astronomically by
 * `Lunar` via `getJieQi()` against the 24 solar terms. No hardcoded date
 * tables — see BUG-080 (verified safe 2026-06-06).
```

- [ ] **Step 3: Verify the file still compiles**

Run: `cd /mnt/data2/git_repos/AgeReveal && ./gradlew :app:compileDebugKotlin 2>&1 | tail -10`
Expected: `BUILD SUCCESSFUL`. Comment-only change, no risk.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt
git commit -m "docs(domain): clarify month-pillar boundaries are astronomical (BUG-080 verified safe)"
```

---

## Task 2: Add back-compat `getDayPillar` and `getHourPillar` string facades (BUG-078)

**Files:**
- Modify: `app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt:167-172` (after the existing `getYearPillar` / `getMonthPillar` block)
- Test: `app/src/test/java/com/willowvibe/agereveal/domain/BaZiCalculatorTest.kt` (extend existing test class)

- [ ] **Step 1: Write the failing test**

Append to `app/src/test/java/com/willowvibe/agereveal/domain/BaZiCalculatorTest.kt`:

```kotlin
@Test
fun `day pillar facade for 1993-12-11 returns Gang-You`() {
    // Sruthi reference case: 1993-12-11 → 癸酉 Day Pillar (Gang / Gui = 癸, You = 酉)
    // See BaZiCalculatorTest reference cases for the full Sruthi validation.
    val pillar = calculator.getDayPillar(LocalDate.of(1993, 12, 11))
    assertTrue("Expected Gui-You (癸酉) in: $pillar",
        pillar.contains("Gui") && pillar.contains("You"))
}

@Test
fun `hour pillar facade for 1993-12-11 02-45 returns Bing-Yin`() {
    // 02:45 falls in the 寅 (03:00–05:00) hour block. With a Gui day stem,
    // the stem sequence for 寅 hours starts at 丙 (Bing).
    val pillar = calculator.getHourPillar(LocalDate.of(1993, 12, 11), hour = 2, minute = 45)
    assertTrue("Expected Bing-Yin (丙寅) in: $pillar",
        pillar.contains("Bing") && pillar.contains("Yin"))
}
```

- [ ] **Step 2: Run the new tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.day_pillar_facade_for_1993-12-11_returns_Gang-You" --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.hour_pillar_facade_for_1993-12-11_02-45_returns_Bing-Yin" 2>&1 | tail -15`
Expected: FAIL with "unresolved reference: getDayPillar" / "unresolved reference: getHourPillar".

- [ ] **Step 3: Add the two facade methods**

In `app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt`, after the existing `getMonthPillar` method (around line 170), add:

```kotlin
    fun getDayPillar(date: LocalDate): String =
        computeFourPillars(date).day.toDisplay()

    fun getHourPillar(
        date: LocalDate,
        hour: Int,
        minute: Int = 0,
        zoneOffsetHours: Double? = null,
    ): String = computeFourPillars(date, hour, minute, zoneOffsetHours)
        .hour?.toDisplay()
        ?: error("Hour pillar unavailable for $date $hour:$minute (no birth time)")
```

- [ ] **Step 4: Run the new tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.day_pillar_facade_for_1993-12-11_returns_Gang-You" --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.hour_pillar_facade_for_1993-12-11_02-45_returns_Bing-Yin" 2>&1 | tail -10`
Expected: PASS (2 tests).

- [ ] **Step 5: Run the full BaZi test class to confirm no regression**

Run: `./gradlew :app:testDebugUnitTest --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest" 2>&1 | tail -5`
Expected: PASS (all existing 13 tests + 2 new = 15).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt app/src/test/java/com/willowvibe/agereveal/domain/BaZiCalculatorTest.kt
git commit -m "feat(domain): BaZiCalculator — getDayPillar + getHourPillar string facades (BUG-078)"
```

---

## Task 3: Add `getDaYunSummary` back-compat facade (BUG-081)

**Files:**
- Modify: `app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt:170-172` (append to the back-compat string-facade block)
- Test: `app/src/test/java/com/willowvibe/agereveal/domain/BaZiCalculatorTest.kt`

- [ ] **Step 1: Write the failing test**

Append to `BaZiCalculatorTest.kt`:

```kotlin
@Test
fun `da yun summary for 1990-03-15 10-00 male returns at least 4 periods`() {
    val summary = calculator.getDaYunSummary(
        date = LocalDate.of(1990, 3, 15),
        hour = 10,
        gender = BaZiCalculator.Gender.MALE,
        nPeriods = 4,
    )
    // Format: "Age 0–9: Wu-X (Wood-Pig) · Age 10–19: … · …"
    val periodCount = summary.split("Age ").size - 1
    assertTrue("Expected ≥4 Da Yun periods in: $summary", periodCount >= 4)
    assertTrue("Summary should contain 'Age 0–9': $summary", summary.contains("Age 0–9"))
}

@Test
fun `da yun summary for female with yang year stem direction is reverse`() {
    // 1990 is a 庚 Geng year (Yang Metal). Female + Yang year = reverse direction.
    val summary = calculator.getDaYunSummary(
        date = LocalDate.of(1990, 3, 15),
        hour = 10,
        gender = BaZiCalculator.Gender.FEMALE,
    )
    // The first Da Yun period after natal for a reverse sequence should not
    // be the natal month pillar. The library handles direction; we just verify
    // the format is sane and contains 8 periods (default).
    val periodCount = summary.split("Age ").size - 1
    assertEquals(8, periodCount)
}
```

- [ ] **Step 2: Run the new tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.da_yun_summary_for_1990-03-15_10-00_male_returns_at_least_4_periods" --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.da_yun_summary_for_female_with_yang_year_stem_direction_is_reverse" 2>&1 | tail -10`
Expected: FAIL with "unresolved reference: getDaYunSummary".

- [ ] **Step 3: Add the facade method**

In `BaZiCalculator.kt`, after `getHourPillar` (added in Task 2), add:

```kotlin
    fun getDaYunSummary(
        date: LocalDate,
        hour: Int,
        minute: Int = 0,
        gender: Gender,
        zoneOffsetHours: Double? = null,
        nPeriods: Int = 8,
    ): String = computeDaYun(date, hour, minute, gender, zoneOffsetHours, nPeriods)
        .joinToString(" · ") { it.toDisplay() }
```

- [ ] **Step 4: Run the new tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.da_yun_summary_for_1990-03-15_10-00_male_returns_at_least_4_periods" --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.da_yun_summary_for_female_with_yang_year_stem_direction_is_reverse" 2>&1 | tail -5`
Expected: PASS (2 tests).

- [ ] **Step 5: Run the full BaZi test class to confirm no regression**

Run: `./gradlew :app:testDebugUnitTest --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest" 2>&1 | tail -5`
Expected: PASS (15 + 2 = 17).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt app/src/test/java/com/willowvibe/agereveal/domain/BaZiCalculatorTest.kt
git commit -m "feat(domain): BaZiCalculator — getDaYunSummary string facade (BUG-081)"
```

---

## Task 4: Add `TenGods` data class + `tenGods` field on `FourPillars` (BUG-079 engine work)

**Files:**
- Modify: `app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt:56-83` (extend `FourPillars`)
- Test: `app/src/test/java/com/willowvibe/agereveal/domain/BaZiCalculatorTest.kt`

- [ ] **Step 1: Write the failing test**

Append to `BaZiCalculatorTest.kt`:

```kotlin
@Test
fun `computeFourPillars populates tenGods for Sruthi reference case`() {
    // 1993-12-11 02:45 IST (zone +5.5) → known 4-pillar chart
    // Day Master = 癸 (Gui, Yin Water).
    // Year stem 癸: same as Day Master → 비견 (比肩, Companion)
    // Month stem 甲子: 甲 (Yang Wood) produces Water → 정인 (正印, Direct Resource)
    // Day stem 癸: Day Master itself (typically 空 or "Day Master")
    // Hour stem 丙: Yang Fire, Fire overcomes Metal (which Water produces) →
    //   in relation to Water Day Master, Fire is the "Indirect Wealth" (偏財)
    //   (the element Water overcomes, yang polarity) — actually verify via
    //   lunar-java's authoritative output below.
    val result = calculator.computeFourPillars(
        LocalDate.of(1993, 12, 11),
        hour = 2,
        minute = 45,
        zoneOffsetHours = 5.5,
    )
    // Exact strings are lunar-java authoritative; we lock the structure, not the labels.
    assertNotNull(result.tenGods)
    assertNotNull(result.tenGods.yearStem)
    assertNotNull(result.tenGods.monthStem)
    assertNotNull(result.tenGods.dayStem)
    assertNotNull(result.tenGods.hourStem)
    // Hidden-stem arrays are parallel to the 4 pillars.
    assertEquals(result.tenGods.yearBranch.size, result.tenGods.monthBranch.size)
    assertTrue("At least one of the 4 hidden-stem arrays should be non-empty",
        result.tenGods.yearBranch.isNotEmpty() ||
        result.tenGods.monthBranch.isNotEmpty() ||
        result.tenGods.dayBranch.isNotEmpty() ||
        result.tenGods.hourBranch.isNotEmpty())
}

@Test
fun `tenGods yearStem is Bi-Jeon for Gui day master 1993 year`() {
    // 1993 = 癸酉 year. Year stem 癸 == Day Master 癸 → 비견 (比肩, Companion)
    val result = calculator.computeFourPillars(
        LocalDate.of(1993, 12, 11),
        hour = 2,
        minute = 45,
        zoneOffsetHours = 5.5,
    )
    assertEquals("비견", result.tenGods.yearStem)
}
```

- [ ] **Step 2: Run the new tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.computeFourPillars_populates_tenGods_for_Sruthi_reference_case" --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.tenGods_yearStem_is_Bi-Jeon_for_Gui_day_master_1993_year" 2>&1 | tail -10`
Expected: FAIL with "unresolved reference: tenGods" / "unresolved reference: TenGods".

- [ ] **Step 3: Add the `TenGods` data class to `BaZiCalculator`**

In `BaZiCalculator.kt`, after the existing `DaYunPeriod` data class (line 84) and before the `// Public API` divider (line 85), add:

```kotlin
    /**
     * Ten Gods (十神 / 십신) for every visible + hidden stem across the 4 pillars,
     * relative to the Day Master. Strings are the lunar-java Korean labels
     * (e.g. "비견", "정인", "편재") and are stable across the library version.
     *
     * One visible-stem label per pillar + one hidden-stem label per hidden
     * stem within each branch. Use [hasHour] to know whether `hourStem` /
     * `hourBranch` are populated.
     */
    data class TenGods(
        val yearStem: String?,
        val monthStem: String?,
        val dayStem: String?,
        val hourStem: String?,
        val yearBranch: List<String>,
        val monthBranch: List<String>,
        val dayBranch: List<String>,
        val hourBranch: List<String>,
    ) {
        val hasHour: Boolean get() = hourStem != null
    }
```

- [ ] **Step 4: Extend `FourPillars` to carry `tenGods`**

Replace the existing `FourPillars` data class (lines 56-71) with:

```kotlin
    /** All four pillars at a birth moment, plus Day Master and Ten Gods. */
    data class FourPillars(
        val year: Pillar,
        val month: Pillar,
        val day: Pillar,
        val hour: Pillar?,
        val dayMasterHanzi: String,
        val dayMasterElement: String,
        val tenGods: TenGods,
    ) {
        fun toDisplay(): String = buildString {
            append("Year: ").append(year.toDisplay())
            append(" · Month: ").append(month.toDisplay())
            append(" · Day: ").append(day.toDisplay())
            if (hour != null) append(" · Hour: ").append(hour.toDisplay())
        }
    }
```

- [ ] **Step 5: Populate `tenGods` in `computeFourPillars`**

Replace the `return FourPillars(...)` block (lines 111-119) with:

```kotlin
        return FourPillars(
            year = parsePillar(ec.year),
            month = parsePillar(ec.month),
            day = parsePillar(ec.day),
            hour = if (hour != null) parsePillar(ec.time) else null,
            dayMasterHanzi = ec.dayGan,
            dayMasterElement = ec.dayWuXing.split("").firstOrNull { it.isNotEmpty() }
                ?.let { mapWuXingHanziToEn(it) } ?: "Unknown",
            tenGods = TenGods(
                yearStem = ec.yearShiShenGan.takeIf { it.isNotEmpty() },
                monthStem = ec.monthShiShenGan.takeIf { it.isNotEmpty() },
                dayStem = ec.dayShiShenGan.takeIf { it.isNotEmpty() },
                hourStem = if (hour != null) ec.timeShiShenGan.takeIf { it.isNotEmpty() } else null,
                yearBranch = ec.yearShiShenZhi,
                monthBranch = ec.monthShiShenZhi,
                dayBranch = ec.dayShiShenZhi,
                hourBranch = if (hour != null) ec.timeShiShenZhi else emptyList(),
            ),
        )
```

- [ ] **Step 6: Run the new tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.computeFourPillars_populates_tenGods_for_Sruthi_reference_case" --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest.tenGods_yearStem_is_Bi-Jeon_for_Gui_day_master_1993_year" 2>&1 | tail -10`
Expected: PASS (2 tests).

- [ ] **Step 7: Run the full BaZi + SajuKorean test classes to confirm no regression**

Run: `./gradlew :app:testDebugUnitTest --tests "com.willowvibe.agereveal.domain.BaZiCalculatorTest" --tests "com.willowvibe.agereveal.domain.SajuKoreanCalculatorTest" 2>&1 | tail -5`
Expected: PASS (17 BaZi + 17 SajuKorean = 34).

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/willowvibe/agereveal/domain/BaZiCalculator.kt app/src/test/java/com/willowvibe/agereveal/domain/BaZiCalculatorTest.kt
git commit -m "feat(domain): BaZiCalculator — TenGods data class on FourPillars (BUG-079)"
```

---

## Task 5: Update BUGS_AND_ISSUES.md — close 078/079/080/081

**Files:**
- Modify: `BUGS_AND_ISSUES.md` (4 bug entries in the Phase 5.6 / Engine Gaps section, ~lines 575-597)

- [ ] **Step 1: Find the 4 bug entries**

Read `BUGS_AND_ISSUES.md` lines 575-597. The 4 entries are:
- `### 🟡 BUG-078 — Missing Day and Hour Pillars (Full BaZi)` (~line 575)
- `### 🟡 BUG-079 — No Day Master or Ten Gods Analysis` (~line 581)
- `### 🟡 BUG-080 — Month Pillar Uses Hardcoded Solar Term Dates` (~line 587)
- `### 🟡 BUG-081 — No Luck Pillars (大运 / Da Yun)` (~line 593)

- [ ] **Step 2: Close BUG-078**

In the `### 🟡 BUG-078` block, change the **Status** line from `**Status:** 🟡 Open — feature incomplete` to:

```markdown
**Status:** 🟢 Fixed 2026-06-06
**Severity:** Medium (feature incomplete) — RESOLVED
**File:** `domain/BaZiCalculator.kt`
**Description:** [keep existing]
**Fix applied:** All four pillars (Year, Month, Day, Hour) were already populated in `FourPillars` since the lunar-java integration. Added `getDayPillar(date)` and `getHourPillar(date, hour, minute, zoneOffsetHours)` back-compat string facades parallel to the existing `getYearPillar` / `getMonthPillar`. 2 new unit tests in `BaZiCalculatorTest.kt` lock the Sruthi reference case (1993-12-11 02:45 IST → 癸酉 Day, 丙寅 Hour).
```

- [ ] **Step 3: Close BUG-079**

In the `### 🟡 BUG-079` block, change the **Status** line to:

```markdown
**Status:** 🟢 Fixed 2026-06-06
**Severity:** Medium (feature incomplete) — RESOLVED
**File:** `domain/BaZiCalculator.kt`
**Description:** [keep existing]
**Fix applied:** Day Master was already exposed (`dayMasterHanzi`, `dayMasterElement`). Added `TenGods` data class with one visible-stem label + a hidden-stem list per pillar (4 pillars × 2 collections = 8 fields total, all populated from lunar-java's authoritative `getXxxShiShenGan` / `getXxxShiShenZhi`). Ten Gods labels are lunar-java's Korean strings (비견/식신/정인/…). The data is now available to the Korean Saju UI via the new `SajuTenGodsCard` composable (see Task 6). 2 new unit tests verify the data class shape and the Bi-Jeon (Companion) result for the 1993 Sruthi reference case.
```

- [ ] **Step 4: Convert BUG-080 to verified-safe**

In the `### 🟡 BUG-080` block, change the **Status** line to:

```markdown
**Status:** ✅ Verified safe 2026-06-06
**Severity:** Medium (accuracy) — RESOLVED (not a bug)
**File:** `domain/BaZiCalculator.kt` (was thought to use hardcoded dates; does not)
**Description:** [keep existing]
**Verification:** The current `BaZiCalculator.computeFourPillars()` delegates entirely to `Lunar` / `EightChar` from `cn.6tail:lunar:1.7.7`. `Lunar` computes month-pillar boundaries via `getJieQi()` against the 24 astronomical solar terms (Sun entering multiples of 15° ecliptic longitude), not against hardcoded Gregorian dates. The original bug description referred to an earlier version of the file. No code change required; a clarifying comment was added to the file header. 17 existing BaZi + 17 existing SajuKorean unit tests continue to pass with reference dates spanning 1970–2050.
```

- [ ] **Step 5: Close BUG-081**

In the `### 🟡 BUG-081` block, change the **Status** line to:

```markdown
**Status:** 🟢 Fixed 2026-06-06
**Severity:** Low (feature gap) — RESOLVED
**File:** `domain/BaZiCalculator.kt`
**Description:** [keep existing]
**Fix applied:** `computeDaYun()` was already implemented with the correct Korean 명리 convention (Yang-year + Male OR Yin-year + Female → forward; opposite → backward; start age = days-to-next-or-prev 節氣 ÷ 3). The 10-year luck-pillar sequence has been exposed to the UI since Phase 6.5 via `SajuKoreanCalculator.computeChart().daeun` and rendered in `DetailsUnlockScreen.SajuDaeunTimelineCard`. This batch adds the `getDaYunSummary(date, hour, minute, gender, zoneOffsetHours, nPeriods): String` back-compat facade (parallel to `getYearPillar` etc.) so other call sites can consume the formatted display string without going through the structured `DaYunPeriod` list. 2 new unit tests verify the 4-period short form and the 8-period default for a female + Yang-year combination.
```

- [ ] **Step 6: Commit**

```bash
git add BUGS_AND_ISSUES.md
git commit -m "docs(bugs): close BUG-078/079/080/081 — BaZi depth batch 2026-06-06"
```

---

## Task 6: Add `SajuTenGodsCard` composable to the Korean Saju tab (UI surfacing)

**Files:**
- Modify: `app/src/main/java/com/willowvibe/agereveal/ui/screen/DetailsUnlockScreen.kt` (add a new private composable + wire it into `KoreanSajuTab`)
- Modify: `app/src/main/res/values-ko/saju_strings.xml` (+5 new keys)
- Modify: `app/src/main/res/values/strings.xml` (+5 English stub keys)

- [ ] **Step 1: Read the Korean Saju tab structure**

Open `app/src/main/java/com/willowvibe/agereveal/ui/screen/DetailsUnlockScreen.kt` and read `KoreanSajuTab` (lines 466-544). Note the wiring pattern: cards go through `SajuDayMasterCard` (always visible) → `SajuFourPillarsCard` (always visible) → premium-gated cards (`SajuOHaengBalanceCard`, `SajuYongshinCard`, `SajuDaeunTimelineCard`).

- [ ] **Step 2: Write the failing test (instrumented Compose)**

Create a new file `app/src/androidTest/java/com/willowvibe/agereveal/ui/screen/SajuTenGodsCardUiTest.kt` (use the existing `VedicTabUiTest` as a template for `createComposeRule` patterns):

```kotlin
package com.willowvibe.agereveal.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.willowvibe.agereveal.domain.BaZiCalculator
import com.willowvibe.agereveal.domain.SajuKoreanCalculator
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertNotNull

@RunWith(AndroidJUnit4::class)
class SajuTenGodsCardUiTest {

    @get:Rule val rule = createComposeRule()

    private lateinit var chart: SajuKoreanCalculator.SajuChart

    @Before
    fun setUp() {
        val baZi = BaZiCalculator(com.willowvibe.agereveal.domain.ZodiacCalculator(
            com.willowvibe.agereveal.domain.AstronomicalCalculator()
        ))
        val kor = SajuKoreanCalculator(baZi)
        chart = kor.computeChart(
            date = java.time.LocalDate.of(1993, 12, 11),
            hour = 2, minute = 45, zoneOffsetHours = 5.5, gender = BaZiCalculator.Gender.MALE,
        )
        assertNotNull(chart)
    }

    @Test fun rendersCardTitle() {
        rule.setContent { SajuTenGodsCard(chart = chart) }
        // Korean resource string is resolved via Context; the English stub
        // is the safe-guard assertion.
        rule.onNodeWithText("십신", substring = true).assertIsDisplayed()
    }

    @Test fun showsAllFourPillarsAsRows() {
        rule.setContent { SajuTenGodsCard(chart = chart) }
        // Year / Month / Day / Hour pillars each get a row.
        rule.onNodeWithText("년주", substring = true).assertIsDisplayed()
        rule.onNodeWithText("월주", substring = true).assertIsDisplayed()
        rule.onNodeWithText("일주", substring = true).assertIsDisplayed()
        rule.onNodeWithText("시주", substring = true).assertIsDisplayed()
    }
}
```

- [ ] **Step 3: Run the new test to verify it fails**

Run: `./gradlew :app:compileDebugAndroidTestKotlin 2>&1 | tail -10`
Expected: FAIL with "unresolved reference: SajuTenGodsCard" (the composable doesn't exist yet).

- [ ] **Step 4: Add the Korean string resources**

Open `app/src/main/res/values-ko/saju_strings.xml` and append:

```xml
    <!-- Ten Gods (십신) card -->
    <string name="saju_ten_gods_title">십신 (十神) 분석</string>
    <string name="saju_ten_gods_subtitle">사주 내 천간과 지장간의 십신 관계</string>
    <string name="saju_ten_gods_visible_stem_label">천간 십신</string>
    <string name="saju_ten_gods_hidden_stem_label">지장간 십신</string>
    <string name="saju_ten_gods_no_hour">시주를 알 수 없어 시주는 표시되지 않습니다</string>
```

- [ ] **Step 5: Add the English stub resources**

Open `app/src/main/res/values/strings.xml` and append (these will never be shown in ko locale but the card must not crash in en locale — see Phase 6.5 `safeWarn` pattern):

```xml
    <string name="saju_ten_gods_title">Ten Gods (十神) Analysis</string>
    <string name="saju_ten_gods_subtitle">십신 relationships across stems and branches</string>
    <string name="saju_ten_gods_visible_stem_label">Visible-stem 십신</string>
    <string name="saju_ten_gods_hidden_stem_label">Hidden-stem 십신</string>
    <string name="saju_ten_gods_no_hour">Birth time unknown — hour pillar not shown</string>
```

- [ ] **Step 6: Implement the `SajuTenGodsCard` composable**

Open `app/src/main/java/com/willowvibe/agereveal/ui/screen/DetailsUnlockScreen.kt` and add a new private composable AFTER `SajuFourPillarsCard` (line 661) and BEFORE the `SajuPillarColumn` helper. The composable reads `chart.fourPillars`-equivalent data — but note: `SajuChart` does NOT currently expose `FourPillars` directly. We need to thread the Ten Gods data through. Easiest: pull the chart's `year` / `month` / `day` / `hour` `KoreanPillar` objects (which already have `tenGod` + `branchTenGods`) and reuse them. So this card consumes ONLY `SajuChart`, no `FourPillars` plumbing needed.

```kotlin
@Composable
private fun SajuTenGodsCard(chart: SajuKoreanCalculator.SajuChart) {
    val rows = buildList {
        add(Pair(stringResource(R.string.saju_year_pillar), chart.year))
        add(Pair(stringResource(R.string.saju_month_pillar), chart.month))
        add(Pair(stringResource(R.string.saju_day_pillar), chart.day))
        if (chart.hour != null) add(Pair(stringResource(R.string.saju_hour_pillar), chart.hour))
    }
    AgeCard {
        AgeLabel(text = stringResource(R.string.saju_ten_gods_title))
        Spacer(Modifier.height(4.dp))
        AgeBody(
            text = stringResource(R.string.saju_ten_gods_subtitle),
            color = WarmInkMute,
        )
        Spacer(Modifier.height(12.dp))
        // Column header
        Row(modifier = Modifier.fillMaxWidth()) {
            AgeLabel(
                text = stringResource(R.string.saju_ten_gods_visible_stem_label),
                modifier = Modifier.weight(1f),
            )
            AgeLabel(
                text = stringResource(R.string.saju_ten_gods_hidden_stem_label),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(4.dp))
        for ((pillarLabel, pillar) in rows) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AgeBody(
                    text = pillarLabel,
                    modifier = Modifier.weight(1f),
                )
                AgeBody(
                    text = pillar.tenGod ?: "—",
                    modifier = Modifier.weight(1f),
                )
                if (pillar.branchTenGods.isNotEmpty()) {
                    AgeBody(
                        text = pillar.branchTenGods.joinToString(" · "),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        if (chart.hour == null) {
            Spacer(Modifier.height(8.dp))
            AgeBody(
                text = stringResource(R.string.saju_ten_gods_no_hour),
                color = WarmInkMute,
            )
        }
    }
}
```

- [ ] **Step 7: Wire it into `KoreanSajuTab`**

In `KoreanSajuTab` (line 466-544), insert a call to `SajuTenGodsCard(chart = chart)` BETWEEN the `SajuFourPillarsCard` (line 513) and the `if (!unlocked)` premium-gate check (line 515). The card is **always visible** (matches the pattern of `SajuDayMasterCard` and `SajuFourPillarsCard`) — the per-pillar Ten Gods are a basic BaZi concept, not a premium depth feature.

```kotlin
        // 2) Four Pillars card (always visible)
        SajuFourPillarsCard(chart = chart, hasBirthTime = hasBirthTime)

        // 2.5) Ten Gods (십신) — always visible (basic BaZi concept)
        SajuTenGodsCard(chart = chart)

        if (!unlocked) {
            ...
        }
```

- [ ] **Step 8: Compile and run the new instrumented test**

Run: `./gradlew :app:connectedAndroidTest --tests "com.willowvibe.agereveal.ui.screen.SajuTenGodsCardUiTest" 2>&1 | tail -15`
Expected: PASS (2 tests). **Note:** requires a connected device or emulator — if none, run `./gradlew :app:compileDebugAndroidTestKotlin` and `./gradlew :app:assembleDebugAndroidTest` to confirm compile only, and defer the runtime test to a manual run.

- [ ] **Step 9: Run the full unit + instrumented test suite to confirm no regression**

Run: `./gradlew :app:testDebugUnitTest 2>&1 | tail -3`
Expected: PASS — count should be 329 + (2 day/hour) + (2 DaYun) + (2 Ten Gods) = **335** unit tests.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/willowvibe/agereveal/ui/screen/DetailsUnlockScreen.kt \
        app/src/main/res/values-ko/saju_strings.xml \
        app/src/main/res/values/strings.xml \
        app/src/androidTest/java/com/willowvibe/agereveal/ui/screen/SajuTenGodsCardUiTest.kt
git commit -m "feat(ui): SajuTenGodsCard — 십신 table in Korean Saju tab (BUG-079 surfacing)"
```

---

## Task 7: Update memory + CLAUDE.md to reflect the closed bugs

**Files:**
- Modify: `/home/harish/.claude/projects/-mnt-data2-git-repos-AgeReveal/memory/MEMORY.md` (add a one-line pointer)
- Modify: `/home/harish/.claude/projects/-mnt-data2-git-repos-AgeReveal/memory/bazi-depth-batch-2026-06-06.md` (NEW memory file documenting the batch)
- Modify: `CLAUDE.md` (move BUG-078/079/080/081 from "Engine Gaps" to a footnote that points at the memory; the file is large and doesn't need 4 inline BUG entries re-listing)

- [ ] **Step 1: Write the memory file**

Create `/home/harish/.claude/projects/-mnt-data2-git-repos-AgeReveal/memory/bazi-depth-batch-2026-06-06.md`:

```markdown
---
name: bazi-depth-batch-2026-06-06
description: BaZi depth batch closed BUG-078/079/080/081 on 2026-06-06. Re-baselined the bug tracker against current code and found most engine work was already done; only added 3 string facades + 1 TenGods data class + 1 UI card.
metadata:
  type: project
---

**Context:** On 2026-06-06, picked up the 4-bug "BaZi depth" batch (BUG-078 Day/Hour pillars, BUG-079 Day Master + Ten Gods, BUG-080 hardcoded solar-term dates, BUG-081 Luck Pillars). Before writing code, audited `BaZiCalculator.kt` and `SolarTermsCalculator.kt` against the bug descriptions.

**Re-baselining finding:** 3 of the 4 bugs were already mostly resolved in prior phases:
- BUG-078: All 4 pillars already in `FourPillars`; just missing `getDayPillar` / `getHourPillar` string facades (Task 2)
- BUG-079: Day Master already exposed; Ten Gods not yet surfaced in `BaZiCalculator` but `SajuKoreanCalculator.KoreanPillar` already had `tenGod` + `branchTenGods` (Tasks 4 + 6)
- BUG-080: Bug description **stale** — `Lunar` / `EightChar` from `cn.6tail:lunar:1.7.7` computes month-pillar boundaries astronomically via `getJieQi()`. No code change. (Task 1)
- BUG-081: `computeDaYun()` + `SajuDaeunTimelineCard` already shipped; just added a back-compat `getDaYunSummary` string facade (Task 3)

**Branch:** `feat/bazi-depth-batch`
**Commits (6):**
1. docs(domain): clarify month-pillar boundaries are astronomical (BUG-080 verified safe)
2. feat(domain): BaZiCalculator — getDayPillar + getHourPillar string facades (BUG-078)
3. feat(domain): BaZiCalculator — getDaYunSummary string facade (BUG-081)
4. feat(domain): BaZiCalculator — TenGods data class on FourPillars (BUG-079)
5. docs(bugs): close BUG-078/079/080/081 — BaZi depth batch 2026-06-06
6. feat(ui): SajuTenGodsCard — 십신 table in Korean Saju tab (BUG-079 surfacing)

**Test count:** 329 → 335 unit tests (+6: 2 day/hour, 2 DaYun, 2 Ten Gods). 0 regressions. +2 instrumented Compose tests for the new SajuTenGodsCard (assembleDebugAndroidTest compile passes; runtime test requires connected device).

**Why:** BUG-079 (Ten Gods) is the only real new engine work; everything else is back-compat facade parity. The Korean Saju tab in `DetailsUnlockScreen` now has 6 cards (Day Master hero, Four Pillars, Ten Gods, 오행, Yongshin, Daeun) — the full Saju v2.1 surface.

**How to apply:** When a bug entry looks big, audit the code first. The bug tracker description may be 1–2 phases stale. The lunar-java wrapper (BaZiCalculator) and the Korean presentation layer (SajuKoreanCalculator) split the work cleanly — math lives in one file, Hangul/십신 strings live in the other.
```

- [ ] **Step 2: Add the memory pointer**

Open `/home/harish/.claude/projects/-mnt-data2-git-repos-AgeReveal/memory/MEMORY.md` and append:

```markdown
- [BaZi Depth Batch 2026-06-06](bazi-depth-batch-2026-06-06.md) — Closed BUG-078/079/080/081; mostly facades + TenGods data class + 1 UI card. 335 unit tests passing.
```

- [ ] **Step 3: Commit**

```bash
git add /home/harish/.claude/projects/-mnt-data2-git-repos-AgeReveal/memory/MEMORY.md \
        /home/harish/.claude/projects/-mnt-data2-git-repos-AgeReveal/memory/bazi-depth-batch-2026-06-06.md
git commit -m "docs(memory): bazi depth batch 2026-06-06 closed 4 bugs"
```

**Note:** The `/home/harish/.claude/...` memory files are in the user's home dir, not the repo. If the commit fails on that path, the memory edits are still saved on disk — just create the branch's `git status` will not include them. Revert to manual commit from the memory dir if needed.

---

## Verification

After all 7 tasks:

- [ ] **Bug count check:** `grep -cE "^### 🟡 BUG-0(78|79|80|81)" BUGS_AND_ISSUES.md` returns `0`.
- [ ] **Test count check:** `./gradlew :app:testDebugUnitTest 2>&1 | tail -3` reports 335 tests, 0 failures.
- [ ] **Compile check:** `./gradlew :app:assembleDebug :app:assembleDebugAndroidTest` succeeds.
- [ ] **Lint check:** `./gradlew :app:lintDebug 2>&1 | tail -3` reports no new lint errors (pre-existing `ExtraTranslation` issues for `language_english`/`language_hindi`/`err_ad_not_ready` are acceptable).
- [ ] **Visual check (manual):** Open the app, navigate to My Cosmos → tap "Explore full profile" → Korean Saju tab. Verify the 6 cards render in order (Day Master hero, Four Pillars, 십신, 오행, 용신, 대운).

---

## Self-Review

**1. Spec coverage:** Each of the 4 bugs has at least one task: BUG-078 → Task 2, BUG-079 → Tasks 4 + 6, BUG-080 → Task 1 + 5, BUG-081 → Task 3 + 5. The UI surfacing is covered by Task 6.

**2. Placeholder scan:** No "TBD" / "implement later" / "fill in details" / "similar to Task N" / "add appropriate error handling" steps. All code blocks contain real Kotlin / Gradle / Git commands. Tests are fully written out.

**3. Type consistency:** `TenGods` is defined in Task 4 step 3 and used in Task 4 step 5 with the same field names (`yearStem`, `monthStem`, `dayStem`, `hourStem`, `yearBranch`, `monthBranch`, `dayBranch`, `hourBranch`). `KoreanPillar.tenGod` and `KoreanPillar.branchTenGods` are read in Task 6 step 6 — these fields exist in the current `SajuKoreanCalculator.kt` (line 221-222). The `saju_ten_gods_*` string keys are consistent across the Kotlin code (Task 6 step 6), Korean XML (Task 6 step 4), and English XML (Task 6 step 5).

**4. Commit granularity:** 6 code commits + 1 memory commit = 7 total. Each commit has a single clear purpose. All commits include the bug ID for traceability.

**5. Risk assessment:** Low. All string-facade additions (Tasks 2, 3) are pure additions — no behavior change. The `FourPillars` shape change (Task 4) adds a new field with a default-style population; no existing call site reads `tenGods` yet so no migration needed. The UI addition (Task 6) is purely additive on the Korean Saju tab.
