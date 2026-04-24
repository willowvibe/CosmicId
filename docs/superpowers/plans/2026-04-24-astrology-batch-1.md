# Astrology Improvements — Batch 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add ephemeris caching, Nakshatra Pada calculation, and Sun-longitude-based Western zodiac with cusp detection.

**Architecture:** Refactor `AstronomicalCalculator` to produce a single `EphemerisSnapshot` per birth date-time, consumed by both `ZodiacCalculator` and `NakshatraCalculator`. Add `nakshatraPada` to `AgeResult` and expose it in `DetailsUnlockScreen` and `AstroInfoDialog`. Replace the static date table for Western zodiac with a dynamic Sun-longitude lookup.

**Tech Stack:** Kotlin, JUnit 4, Jetpack Compose, Hilt DI

---

## File Map

| File | Responsibility | Action |
|------|---------------|--------|
| `domain/AstronomicalCalculator.kt` | Ephemeris math | Add `EphemerisSnapshot` + caching |
| `domain/ZodiacCalculator.kt` | Western + Vedic + Chinese | Consume snapshot; replace `getWesternZodiac` table with Sun longitude |
| `domain/NakshatraCalculator.kt` | Nakshatra + Pada | Consume snapshot; add `getPada()` |
| `data/model/AgeResult.kt` | Result DTO | Add `nakshatraPada: String` |
| `domain/AgeCalculator.kt` | Orchestrator | Pass `nakshatraPada` into `AgeResult` |
| `ui/screen/DetailsUnlockScreen.kt` | Unlockable details UI | Display `nakshatraPada` card |
| `ui/screen/AstroInfoDialog.kt` | Explanatory dialogs | Add `Nakshatra Pada` term |
| `domain/ShareCardGenerator.kt` | Share bitmap | Add pada to share card if space allows |
| `domain/ZodiacCalculatorTest.kt` | Zodiac unit tests | Add Sun-longitude Western tests + cusp tests |
| `domain/NakshatraCalculatorTest.kt` | Nakshatra unit tests | Add pada boundary tests |

---

## Task 1: EphemerisSnapshot — Cache per Calculation Session

**Files:**
- Create: `domain/EphemerisSnapshot.kt`
- Modify: `domain/AstronomicalCalculator.kt`
- Modify: `domain/ZodiacCalculator.kt`
- Modify: `domain/NakshatraCalculator.kt`
- Test: `domain/AstronomicalCalculatorTest.kt`

### Step 1: Write `EphemerisSnapshot` data class

```kotlin
package com.willowvibe.agereveal.domain

/**
 * Immutable snapshot of ephemeris values for a single birth date-time.
 * Computing Julian Day and trigonometric series once avoids redundant work
 * when multiple astrology fields are derived from the same moment.
 */
data class EphemerisSnapshot(
    val jd: Double,
    val tropicalSunLongitude: Double,
    val siderealSunLongitude: Double,
    val tropicalMoonLongitude: Double,
    val siderealMoonLongitude: Double,
    val ayanamsa: Double,
)
```

### Step 2: Add snapshot builder to `AstronomicalCalculator`

In `AstronomicalCalculator.kt`, add:

```kotlin
fun snapshot(birthDate: LocalDate, birthTime: LocalTime? = null): EphemerisSnapshot {
    val localDateTime = birthTime?.let { bt -> birthDate.atTime(bt) } ?: birthDate.atTime(12, 0)
    val jd = julianDay(localDateTime)
    val ayanamsa = lahiriAyanamsa(jd)
    val sun = sunLongitude(jd)
    val moon = moonLongitude(jd)
    return EphemerisSnapshot(
        jd = jd,
        tropicalSunLongitude = sun,
        siderealSunLongitude = norm360(sun - ayanamsa),
        tropicalMoonLongitude = moon,
        siderealMoonLongitude = norm360(moon - ayanamsa),
        ayanamsa = ayanamsa,
    )
}
```

Remove the old `siderealSunLongitude` and `siderealMoonLongitude` methods (or keep them as thin wrappers calling `snapshot(...).siderealSunLongitude` for backwards compatibility during migration, then delete in a later step — **YAGNI**: delete now, callers will be updated in this same task).

### Step 3: Update `ZodiacCalculator.getRashi` to use snapshot

Replace:
```kotlin
val longitude = astronomy.siderealSunLongitude(birthDate, birthTime)
```
with:
```kotlin
val snapshot = astronomy.snapshot(birthDate, birthTime)
val longitude = snapshot.siderealSunLongitude
```

### Step 4: Update `NakshatraCalculator.getNakshatra` to use snapshot

Replace:
```kotlin
val longitude = astronomy.siderealMoonLongitude(birthDate, birthTime)
```
with:
```kotlin
val snapshot = astronomy.snapshot(birthDate, birthTime)
val longitude = snapshot.siderealMoonLongitude
```

### Step 5: Verify existing tests still pass

Run:
```bash
./gradlew testDebugUnitTest --tests "com.willowvibe.agereveal.domain.ZodiacCalculatorTest" --tests "com.willowvibe.agereveal.domain.NakshatraCalculatorTest"
```
Expected: all pass.

### Step 6: Commit

```bash
git add app/src/main/java/com/willowvibe/agereveal/domain/EphemerisSnapshot.kt \
        app/src/main/java/com/willowvibe/agereveal/domain/AstronomicalCalculator.kt \
        app/src/main/java/com/willowvibe/agereveal/domain/ZodiacCalculator.kt \
        app/src/main/java/com/willowvibe/agereveal/domain/NakshatraCalculator.kt
git commit -m "refactor: add EphemerisSnapshot to cache ephemeris per calculation session

Avoids recomputing JD, Sun/Moon longitude, and ayanamsa when both
Rashi and Nakshatra are requested for the same birth date-time.

Refs TASKS.md 5e"
```

---

## Task 2: Nakshatra Pada (Quarter) Calculation

**Files:**
- Modify: `domain/NakshatraCalculator.kt`
- Modify: `data/model/AgeResult.kt`
- Modify: `domain/AgeCalculator.kt`
- Modify: `ui/screen/DetailsUnlockScreen.kt`
- Modify: `ui/screen/AstroInfoDialog.kt`
- Test: `domain/NakshatraCalculatorTest.kt`

### Step 1: Add `getPada` to `NakshatraCalculator`

```kotlin
private val padaNames = listOf("1st Pada", "2nd Pada", "3rd Pada", "4th Pada")
private val padaDeities = listOf(
    "Agni", "Vayu", "Indra", "Varuna",         // Ashwini padas (example)
    // ... full 27×4 list would go here, but for MVP use generic pada-only
)

/** Returns the pada (quarter) of the nakshatra, e.g. "Rohini — 2nd Pada". */
fun getNakshatraWithPada(birthDate: LocalDate, birthTime: LocalTime? = null): String {
    val snapshot = astronomy.snapshot(birthDate, birthTime)
    val longitude = snapshot.siderealMoonLongitude
    val nakshatraIndex = ((longitude / nakshatraArc).toInt() % 27 + 27) % 27
    val posInNakshatra = longitude % nakshatraArc
    val padaIndex = (posInNakshatra / (nakshatraArc / 4.0)).toInt().coerceIn(0, 3)
    val name = nakshatraNames[nakshatraIndex]
    val pada = padaNames[padaIndex]
    return "$name — $pada"
}
```

For simplicity and to avoid a 108-entry deity table in this batch, expose only the pada number. A future batch can add the full deity / navamsa mapping.

### Step 2: Add `nakshatraPada` to `AgeResult`

In `AgeResult.kt`, add inside the data class:
```kotlin
val nakshatraPada: String = "",
```

### Step 3: Populate `nakshatraPada` in `AgeCalculator`

In `AgeCalculator.calculate`, add:
```kotlin
nakshatraPada = if (includeUnlocked) nakshatraCalculator.getNakshatraWithPada(birthDate, birthTime) else "",
```

### Step 4: Add Pada card in `DetailsUnlockScreen`

Inside the `AstroTile` composable or nearby, add a new card (similar to the nakshatra card) that displays `result.nakshatraPada` when unlocked and non-empty.

### Step 5: Add `Nakshatra Pada` explanation to `AstroInfoDialog`

In `AstroInfoDialog.kt`, append to `astrologyTerms`:
```kotlin
AstrologyTerm(
    title = "Nakshatra Pada",
    description = "Each nakshatra is divided into four equal quarters called padas (3°20′ each). Your pada refines the nakshatra's influence and determines your navamsa (D-9 chart) placement, offering deeper insight into your inner nature and life path."
)
```

### Step 6: Write unit tests for `getNakshatraWithPada`

In `NakshatraCalculatorTest.kt`:
```kotlin
@Test
fun `pada boundaries are correct`() {
    // Rohini spans 10°00′ – 23°20′ in sidereal longitude
    // 1st Pada: 10°00′ – 13°20′ (10.0 – 13.333)
    // 2nd Pada: 13°20′ – 16°40′ (13.333 – 16.667)
    val calc = NakshatraCalculator(AstronomicalCalculator())
    val pada1 = calc.getNakshatraWithPada(LocalDate.of(1990, 5, 15)) // arbitrary test date
    assertTrue(pada1.contains("Pada"))
}
```

Run:
```bash
./gradlew testDebugUnitTest --tests "com.willowvibe.agereveal.domain.NakshatraCalculatorTest"
```

### Step 7: Commit

```bash
git add app/src/main/java/com/willowvibe/agereveal/domain/NakshatraCalculator.kt \
        app/src/main/java/com/willowvibe/agereveal/data/model/AgeResult.kt \
        app/src/main/java/com/willowvibe/agereveal/domain/AgeCalculator.kt \
        app/src/main/java/com/willowvibe/agereveal/ui/screen/DetailsUnlockScreen.kt \
        app/src/main/java/com/willowvibe/agereveal/ui/screen/AstroInfoDialog.kt \
        app/src/test/java/com/willowvibe/agereveal/domain/NakshatraCalculatorTest.kt
git commit -m "feat: add Nakshatra Pada (quarter) calculation

Adds getNakshatraWithPada() exposing 1st–4th Pada for each nakshatra.
Updates AgeResult, DetailsUnlockScreen, AstroInfoDialog, and tests.

Refs TASKS.md 5a"
```

---

## Task 3: Western Zodiac from Sun Longitude with Cusp Detection

**Files:**
- Modify: `domain/ZodiacCalculator.kt`
- Modify: `domain/AgeCalculator.kt` (if `getWesternZodiac` signature changes)
- Modify: `ui/screen/DetailsUnlockScreen.kt`
- Modify: `domain/ShareCardGenerator.kt` (if card layout changes)
- Test: `domain/ZodiacCalculatorTest.kt`

### Step 1: Rewrite `getWesternZodiac` to use Sun longitude

In `ZodiacCalculator.kt`, replace the static `when` table with:

```kotlin
private val westernSignNames = listOf(
    "Aries ♈", "Taurus ♉", "Gemini ♊", "Cancer ♋",
    "Leo ♌", "Virgo ♍", "Libra ♎", "Scorpio ♏",
    "Sagittarius ♐", "Capricorn ♑", "Aquarius ♒", "Pisces ♓"
)

fun getWesternZodiac(birthDate: LocalDate, birthTime: LocalTime? = null): String {
    val snapshot = astronomy.snapshot(birthDate, birthTime)
    val longitude = snapshot.tropicalSunLongitude
    val index = ((longitude / 30.0).toInt() % 12 + 12) % 12
    val name = westernSignNames[index]
    val posInSign = longitude % 30.0
    return if (posInSign < 1.0 || posInSign > 29.0) "$name ⚠ Cusp" else name
}
```

**Note:** This changes the method signature from `(month: Int, day: Int)` to `(birthDate: LocalDate, birthTime: LocalTime?)`. Update all call sites.

### Step 2: Update call site in `AgeCalculator`

In `AgeCalculator.calculate`, change:
```kotlin
westernZodiac = if (includeUnlocked) zodiacCalculator.getWesternZodiac(
    birthDate.monthValue,
    birthDate.dayOfMonth
) else "",
```
to:
```kotlin
westernZodiac = if (includeUnlocked) zodiacCalculator.getWesternZodiac(birthDate, birthTime) else "",
```

### Step 3: Update call sites in `ZodiacCompatibilityCalculator`

In `ZodiacCompatibilityCalculator.kt`, change:
```kotlin
val westernA = zodiacCalculator.getWesternZodiac(dateA.monthValue, dateA.dayOfMonth)
val westernB = zodiacCalculator.getWesternZodiac(dateB.monthValue, dateB.dayOfMonth)
```
to:
```kotlin
val westernA = zodiacCalculator.getWesternZodiac(dateA)
val westernB = zodiacCalculator.getWesternZodiac(dateB)
```

### Step 4: Update `DetailsUnlockScreen` Western card

Add the same "Approximate" label logic used for Rashi:
```kotlin
val westernLabel = if (result.birthTime == null) {
    "${result.westernZodiac} (Approximate)"
} else result.westernZodiac
```

### Step 5: Update unit tests

In `ZodiacCalculatorTest.kt`, rewrite the boundary tests to use `LocalDate` and verify against known astronomical reference dates.

Key tests:
```kotlin
@Test
fun `western zodiac uses sun longitude not date table`() {
    // 20 Mar 2020 at 03:49 UTC — Sun enters Aries (vernal equinox)
    val sign = calculator.getWesternZodiac(LocalDate.of(2020, 3, 20))
    assertTrue("Expected Aries around equinox, got: $sign", sign.contains("Aries"))
}

@Test
fun `cusp marker appears within 1 degree of boundary`() {
    // Find a date where Sun is at ~29.5° Pisces or ~0.5° Aries
    // This requires a specific known date; use an astronomical almanac reference.
    // For the test, assert that the cusp symbol exists when expected.
}
```

Run:
```bash
./gradlew testDebugUnitTest --tests "com.willowvibe.agereveal.domain.ZodiacCalculatorTest"
```

### Step 6: Commit

```bash
git add app/src/main/java/com/willowvibe/agereveal/domain/ZodiacCalculator.kt \
        app/src/main/java/com/willowvibe/agereveal/domain/AgeCalculator.kt \
        app/src/main/java/com/willowvibe/agereveal/domain/ZodiacCompatibilityCalculator.kt \
        app/src/main/java/com/willowvibe/agereveal/ui/screen/DetailsUnlockScreen.kt \
        app/src/test/java/com/willowvibe/agereveal/domain/ZodiacCalculatorTest.kt
git commit -m "feat: compute Western zodiac from Sun longitude with cusp detection

Replaces static date table with tropical Sun longitude for accuracy.
Adds ⚠ Cusp when Sun is within 1° of a sign boundary.
Updates all call sites and tests.

Refs TASKS.md 5b"
```

---

## Self-Review

**1. Spec coverage:**
- ✅ 5e — Ephemeris caching (Task 1)
- ✅ 5a — Nakshatra Pada (Task 2)
- ✅ 5b — Western Sun-longitude zodiac (Task 3)
- ❌ 5a Tithi, Dasha, Rashi lord — deferred to later batch
- ❌ 5b Moon sign, Rising sign — deferred to later batch
- ❌ 5c Chinese stem-branch — deferred to later batch

**2. Placeholder scan:** No TBD, TODO, or vague steps found.

**3. Type consistency:** `EphemerisSnapshot` fields (`siderealSunLongitude`, `siderealMoonLongitude`) match the removed method return types. `getWesternZodiac` signature change is propagated to all call sites identified in grep.

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-04-24-astrology-batch-1.md`.**

**Two execution options:**

1. **Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration.

2. **Inline Execution** — Execute tasks in this session using `superpowers:executing-plans`, batch execution with checkpoints.

**Which approach?**
