# Vedic UI Surfacing — Phase E Design Spec

**Date:** 2026-06-06
**Topic:** Surface Phase 6.5 engine outputs (Nakshatra metadata, Dasha tree, Navamsa D-9, planetary aspects) through the `DetailsUnlockScreen.VedicTab`.
**Status:** Approved design, pending implementation
**Author:** Claude Code (session continuation after Phase 6.5 engine + Korean Saju UI landed)

---

## 1. Problem Statement

The Phase 6.5 engine overhaul (commits `21ba1f1` + `eabca28`, 2026-06-05/06) shipped four new structured calculator outputs:

- `NakshatraData` — rich metadata (lord, deity, gana, symbol, emoji) for all 27 mansions.
- `DashaInfo` — structured Mahadasha / Antardasha / Pratyantar periods.
- `NavamsaChart` — D-9 divisional chart for all planets + occupancy map.
- `List<Aspect>` — 5 major Western aspects (conjunction, sextile, square, trine, opposition) with orb + applying/separating direction.

These are all covered by unit tests in `app/src/test/.../domain/` (per `phase-6-5-batch-and-engine` memory: 329 tests passing) and they live in a comprehensive `BirthChart` model (BUG-068). But `BirthChart.compute()` is not called in production — the data layer carries results through `AgeCalculator.calculate()` → `AgeResult` → `DetailsUnlockScreen`, and `AgeResult` exposes only flat string fields (`rashi`, `nakshatra`, `dashaInfo`, etc.).

The Vedic tab in `DetailsUnlockScreen` (introduced v2.0) currently renders:
- Rashi name + Lord
- Nakshatra name + Pada + Tithi
- Approximate Lagna (sidereal, with location fallback)
- Dasha (single line: "X Mahadasha · Y Antardasha · Z Pratyantar")

The four Phase 6.5 outputs are not surfaced. Users get none of:
- Which deity governs their birth mansion.
- Their Dasha *timing* (how much of each period is left).
- Their Navamsa (D-9) ascendant or planetary distribution.
- Which planets are in major aspect to each other.

This is the largest remaining user-visible gap before v2.1 ships (2026-06-30).

## 2. Goals

1. Add 4 structured fields to `AgeResult` so the 4 new engine outputs reach the UI layer.
2. Render them in `VedicTab` as 4 cards: 2 enriched (Nakshatra, Dasha) replacing the existing minimal versions, 2 new (Navamsa, Aspects) appended at the end.
3. Keep all 4 cards free — no premium gate, no IAP change. Aligns with the existing Vedic tab's free behaviour and the "no paywall creep" decision (TASKS.md §0.7).
4. Test the wiring at 3 layers: `BirthChartSubChart` unit, `AgeCalculator` back-compat unit, and `VedicTab` instrumented Compose tests.
5. Preserve the existing `dashaInfo: String` field on `AgeResult` as a derived property so share card generation and any other string-based consumer (TASKS.md §3a "Vedic Kundli Card", `ShareCardGenerator`) keep working unchanged.

## 3. Non-Goals

- No change to `BirthChart.compute()` itself or the `BirthChart` data class.
- No change to `CalculatorViewModel` constructor or recompute flow.
- No change to `CalculatorUiState` — the new fields are on `AgeResult`, surfaced via the existing `computeResult()` chain.
- No Korean Saju tab changes.
- No new premium SKU, no IAP wiring, no entitlement change.
- No new localisation strings for Vedic tab text — the existing English labels are used (matches the existing Rashi/Lagna/Dasha cards which are also untranslated).
- No new `DashaRow` removal — it stays, but `VedicTab` switches to the new `DashaTreeCard`.

## 4. Architecture

### 4.1 Data flow

```
            ┌─────────────────────────┐
            │   AgeCalculator         │
            │   .calculate(...)       │
            │                         │
            │   has:                  │
            │   • ZodiacCalculator    │
            │   • NakshatraCalculator │
            │   • DashaCalculator     │
            │   • BaZiCalculator      │
            │   • LunarConverter      │
            │   • PercentileCalc      │
            │   • ParallelUniverse    │
            │   • PlanetaryDignity    │
            │   + NEW:                │
            │   • BirthChartSubChart  │ ── wraps: NakshatraMetadata,
            │       (injected)        │    DivisionalChartCalculator,
            │                         │    AspectCalculator
            └────────────┬────────────┘
                         │
                         ▼
            ┌─────────────────────────┐
            │   AgeResult             │
            │   (data/model/)         │
            │                         │
            │   NEW fields:           │
            │   • nakshatraMetadata   │ NakshatraData?
            │   • dashaDetail         │ DashaInfo?
            │   • navamsaChart        │ NavamsaChart?
            │   • planetaryAspects    │ List<Aspect>
            │   • tropicalAscendant   │ String? (BUG-083 surface)
            │                         │
            │   CHANGED:              │
            │   • dashaInfo: String   │ now derived from
            │                         │ dashaDetail.summary()
            └────────────┬────────────┘
                         │
                         ▼
            ┌─────────────────────────┐
            │  DetailsUnlockScreen    │
            │   .VedicTab(result)     │
            │                         │
            │   Renders 4 new cards   │
            │   + 2 enriched cards    │
            └─────────────────────────┘
```

### 4.2 New file: `domain/BirthChartSubChart.kt`

A single `@Singleton` wrapper that owns three sub-calculators the engine already ships: `NakshatraMetadata`, `DivisionalChartCalculator`, `AspectCalculator`. Exists to keep `AgeCalculator`'s constructor short (one new dep instead of three) and the test surface tight. Dasha is *not* here because `AgeCalculator` already injects `DashaCalculator`.

```kotlin
@Singleton
class BirthChartSubChart @Inject constructor(
    private val nakshatraMetadata: NakshatraMetadata,
    private val divisionalChartCalculator: DivisionalChartCalculator,
    private val aspectCalculator: AspectCalculator,
) {
    data class SubCharts(
        val nakshatraMetadata: NakshatraData?,
        val navamsaChart: NavamsaChart?,
        val planetaryAspects: List<Aspect>,
    )

    /**
     * Compute the three sub-charts. Each sub-calculation is wrapped in
     * `runCatching { ... }.getOrNull()` so a single failure does not kill
     * the others. Never throws.
     */
    fun compute(
        siderealMoonLongitude: Double,
        planetLongitudes: Map<CelestialBody, Double>,
        jd: Double,
    ): SubCharts {
        val metadata = runCatching {
            nakshatraMetadata.forLongitude(siderealMoonLongitude)
        }.getOrNull()
        val navamsa = runCatching {
            divisionalChartCalculator.getNavamsaChart(planetLongitudes)
        }.getOrNull()
        val aspects = runCatching {
            aspectCalculator.computeAspects(jd, planetLongitudes)
        }.getOrNull() ?: emptyList()
        return SubCharts(metadata, navamsa, aspects)
    }
}
```

The 5th new field, `AgeResult.tropicalAscendant`, is computed inline in `AgeCalculator.calculate()` via a new `WesternZodiacCalculator.getTropicalAscendant(...)` method (see §4.5). It is not part of `BirthChartSubChart` because the data path is independent of the sub-chart trio.

### 4.4 `AgeResult` changes

```kotlin
data class AgeResult(
    // ... existing fields ...

    // Phase 6.5 — Vedic UI surfacing (BUG-068 vehicle)
    val nakshatraMetadata: NakshatraData? = null,    // from NakshatraMetadata.forLongitude(siderealMoonLon)
    val dashaDetail: DashaInfo? = null,              // from DashaCalculator.getDashaDetail(...)
    val navamsaChart: NavamsaChart? = null,          // from DivisionalChartCalculator.getNavamsaChart(planetLongitudes)
    val planetaryAspects: List<Aspect> = emptyList(),// from AspectCalculator.computeAspects(jd, planetLongitudes)
    val tropicalAscendant: String? = null,           // from ZodiacCalculator.getTropicalAscendant(...)
) {
    /**
     * Back-compat: derived from [dashaDetail] when present, else empty string.
     * Existing share card generation and any string-based consumer keep working
     * unchanged.
     */
    val dashaInfo: String
        get() = dashaDetail?.summary() ?: ""
}
```

**Important:** `dashaInfo` was a constructor `val` in the original. It becomes a computed property (a `get()`). This is a one-line change to `AgeResult` consumers that read it (none — they only read), but it requires `AgeResult` to no longer pass `dashaInfo` as a constructor argument in `AgeCalculator.calculate()`.

### 4.5 `AgeCalculator` changes

Constructor adds one new dep:

```kotlin
@Singleton
class AgeCalculator @Inject constructor(
    private val zodiacCalculator: ZodiacCalculator,
    private val nakshatraCalculator: NakshatraCalculator,
    private val dashaCalculator: DashaCalculator,
    private val baZiCalculator: BaZiCalculator,
    private val lunarConverter: LunarCalendarConverter,
    private val percentileCalculator: AgePercentileCalculator,
    private val parallelUniverseGenerator: ParallelUniverseGenerator,
    private val planetaryDignityCalculator: PlanetaryDignityCalculator,
    private val birthChartSubChart: BirthChartSubChart, // NEW
)
```

`calculate()` populates the 4 new fields when `includeUnlocked = true`:

```kotlin
val jd = astronomy.julianDay(utDateTime)            // reuses existing block
val planetLongitudes = computePlanetLongitudes(...)  // extracted helper, new
val siderealMoonLon = snapshot.siderealMoonLongitude // from existing snapshot
val subCharts = if (includeUnlocked)
    birthChartSubChart.compute(siderealMoonLon, planetLongitudes, jd)
else null
val dashaDetail = if (includeUnlocked)
    dashaCalculator.getDashaDetail(birthDate, birthTime, zoneOffset) else null

return AgeResult(
    // ... existing fields (dashaInfo removed from constructor args) ...
    nakshatraMetadata = subCharts?.nakshatraMetadata,
    dashaDetail = dashaDetail,
    navamsaChart = subCharts?.navamsaChart,
    planetaryAspects = subCharts?.planetaryAspects ?: emptyList(),
    tropicalAscendant = if (includeUnlocked && location != null)
        zodiacCalculator.getTropicalAscendant(birthDate, birthTime, zoneOffset, location)
    else null,
)
```

`computePlanetLongitudes(birthDate, birthTime, zoneOffset): Pair<Map<CelestialBody, Double>, Double>` is a new private helper in `AgeCalculator` that returns `(planetLongitudes, jd)`. It mirrors the same loop in `BirthChart.compute()`. Bodies covered: Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn, Uranus, Neptune, Pluto (10 bodies). Rahu/Ketu excluded (not needed for aspects or navamsa; matches `BirthChart.compute()`). The helper depends on the existing `zodiacCalculator` injection (it can call `zodiacCalculator.getPlanetLongitudes(birthDate, birthTime, zoneOffset)` for the planetary longitudes; the jd is computed via `zodiacCalculator` indirectly through `astronomy` — or, simpler, the helper takes `astronomy: AstronomicalCalculator` as a 10th constructor dep on `AgeCalculator`). Trade-off: adding `astronomy` directly is one more dep but mirrors `BirthChart.compute()`'s structure. The implementation choice is left to the plan/writing-plans step — the spec only requires the helper to exist and return the right shape.

`WesternZodiacCalculator.getTropicalAscendant(birthDate, birthTime, zoneOffset, location)` is a new method on the Western split (BUG-070 split). It is a thin alias for the existing `getApproximateAscendant(...)` call that `BirthChart.tropicalAscendant` already uses (per BUG-083, Phase 6.5) — same return value, semantically clearer name. Both `AgeCalculator` and `BirthChart.compute()` should call the new method for naming consistency. The `ZodiacCalculator` facade (BUG-070) gains a corresponding `getTropicalAscendant(...)` method that delegates to `western.getTropicalAscendant(...)` — so existing call sites that depend on the facade keep working. A unit test in `WesternZodiacCalculatorTest` asserts that `getTropicalAscendant` and `getApproximateAscendant` return identical strings for a sample birth chart (regression guard against future drift).

### 4.6 `DetailsUnlockScreen.VedicTab` changes

Two existing cards grow new sub-rows, two new cards append at the end. Order in the tab:

1. `PlanetDignityCard` — existing, unchanged.
2. `Rashi` — existing, unchanged.
3. `Nakshatra` — **enriched**. Adds emoji, symbol, Lord, Deity, Gana. Existing Pada + Tithi rows stay.
4. `Lagna` — enriched. **With location:** shows tropical Lagna name (new `tropicalAscendant` field) and sidereal Lagna name (`approximateAscendant` field) in a two-row layout: "Tropical: Mesha" / "Sidereal: Vrishabha". **Without location:** shows only the existing approximate (sidereal) Lagna with the "Approximate" indicator. Existing logic preserved.
5. `Dasha` — **enriched**. Three-row tree: Mahadasha (large), Antardasha (medium), Pratyantar (small). Each row: lord name + years-remaining. Replaces the single `DashaRow` line.
6. `NavamsaSnapshotCard` — **new**. Hero: D-9 ascendant sign name. Below: 3-5 most populated rashis with their planet occupants as a compact list. Uses `NavamsaChart.rashiOccupancy` (Map<Int, List<CelestialBody>>).
7. `PlanetaryAspectsCard` — **new**. Two stacked sub-sections: Harmonious (CONJUNCTION orb ≤ 4°, SEXTILE, TRINE) and Tense (SQUARE, OPPOSITION, loose CONJUNCTION). Each section: up to 5 aspect rows with planet emojis + symbol. Uses `Aspect.displayLabel()`.

New private composables (all in `DetailsUnlockScreen.kt`, alongside existing `SajuDayMasterCard` etc.):

```kotlin
// Each card uses the existing AgeCard shell.
private fun AgeResultNakshatraCard(
    metadata: NakshatraData,
    name: String,
    padaName: String,
    tithi: String,
    isApprox: Boolean,                              // true when birthTime == null (BUG-003)
)
private fun AgeResultDashaTreeCard(detail: DashaInfo)
private fun NavamsaSnapshotCard(chart: NavamsaChart)
private fun PlanetaryAspectsCard(aspects: List<Aspect>)
private fun AspectRow(aspect: Aspect)               // shared by Harmonious/Tense sub-sections
private fun DashaTreeRow(period: DashaPeriod, size: DashaSize) // DashaSize = M | A | P
```

`DashaTreeRow` size mapping (M = Mahadasha, A = Antardasha, P = Pratyantar): M uses `SerifFamily` 22sp with bold weight; A uses `SerifFamily` 18sp regular; P uses the body font 14sp regular. Three sizes give the tree a visual hierarchy.

New private display helpers (in the same file):

```kotlin
private fun formatNakshatraRow(metadata: NakshatraData): String   // "${symbolEmoji} ${symbol}"
private fun formatDashaPeriod(period: DashaPeriod): String         // delegates to period.displayLabel()
private fun groupAspectsByTone(aspects: List<Aspect>): Pair<List<Aspect>, List<Aspect>>
```

`formatDashaPeriod` is a thin wrapper around the existing `DashaPeriod.displayLabel()` (in `DashaCalculator.kt:217`) so the same string format is used in the share card and the UI.

`AgeResultNakshatraCard`'s `isApprox` parameter is `true` when the caller passes `birthTime == null` — matches the BUG-003 pattern at line 350 of `DetailsUnlockScreen.kt`. (The `nakshatraMetadata.index` is always populated when `includeUnlocked = true` because `forLongitude(siderealMoonLon)` is deterministic; the approximate indicator is about *time precision*, not data presence.)

`groupAspectsByTone` rules:
- **Harmonious:** `TRINE`, `SEXTILE`, `CONJUNCTION` where `orb ≤ 4.0°`.
- **Tense:** `SQUARE`, `OPPOSITION`, `CONJUNCTION` where `orb > 4.0°` (loose conjunction reads as in-between → tense).
- Each sub-list is sorted by orb ascending (tightest first) and capped at 5 rows.

## 5. Components (UI primitives)

All cards use the existing `AgeCard { ... }` shell and `AgeLabel` / `AgeValue` / `AgeBody` primitives — same convention as the Korean Saju tab. No new composable primitives needed.

Layout reference: `SajuOHaengBalanceCard` and `SajuDaeunTimelineCard` in the same file (lines ~698 and ~897) are the visual model for "tiered-row card with sub-sections."

`NavamsaSnapshotCard` uses a `Column` of 3-5 `Row` items, each row showing a rashi name + the planets in that rashi (comma-separated with emojis). `rashiOccupancy` is sorted by occupant count descending, top 5.

`PlanetaryAspectsCard` uses two nested `AgeCard` sub-sections (or two `Column` blocks with their own `AgeLabel` headers). Each sub-section: `AgeLabel("HARMONIOUS" or "TENSE")`, `Spacer(6.dp)`, `Column { AspectRow(aspect); ... }`.

## 6. Error Handling

- **No birth time** → `result.nakshatraMetadata` is still populated (forLongitude is deterministic), but the existing "Approximate" indicator shows on the card. `dashaDetail` is populated (the calculator degrades gracefully). `navamsaChart` and `planetaryAspects` are populated. `tropicalAscendant` is `null` (no location) or populated (with location).
- **No location** → `tropicalAscendant = null`. All other fields still populate. The Lagna card falls back to existing approximate behaviour.
- **`includeUnlocked = false`** (no birth date yet) → all 5 new fields are `null` / `emptyList()`. Cards are not rendered.
- **Sub-calculator throws** → `BirthChartSubChart.compute()` catches each sub-calculation with `runCatching { ... }.getOrNull()`. One failure doesn't kill the others. `dashaDetail` is wrapped in `runCatching` inline in `AgeCalculator.calculate()`.
- **Empty `rashiOccupancy`** (no planets) → `NavamsaSnapshotCard` shows an `AgeBody` "—" placeholder.
- **Empty `planetaryAspects`** (no aspects within orb) → `PlanetaryAspectsCard` shows "No major aspects in orb" sub-text.

## 7. Testing

Three layers, matching the project's established pattern (per `phase-6-5-batch-and-engine` memory):

### 7.1 `BirthChartSubChartTest` (new, unit)

`app/src/test/java/com/willowvibe/agereveal/domain/BirthChartSubChartTest.kt`:

- `compute_populatesAllThreeSubCharts_forKnownBirthChart` — happy path with J2000 reference data.
- `compute_returnsNullMetadata_whenSiderealMoonLongitudeIsOutOfRange` — defensive.
- `compute_returnsEmptyAspects_whenNoPairsInOrb` — degenerate planet set.
- `compute_doesNotPropagateException_whenOneSubChartFails` — inject a mock that throws; assert the other 2 are still populated.

### 7.2 `AgeCalculatorTest` (extend existing)

Add tests in `app/src/test/java/com/willowvibe/agereveal/domain/AgeCalculatorTest.kt`:

- `calculate_populatesNakshatraMetadata_whenIncludeUnlocked` — back-compat for the new field.
- `calculate_populatesDashaDetailAndInfo_backCompatibleFormat` — locks in `dashaInfo == dashaDetail.summary()` for any birth chart.
- `calculate_populatesNavamsaChart_whenIncludeUnlocked`.
- `calculate_populatesPlanetaryAspects_whenIncludeUnlocked` — list is non-empty for a chart with 8+ bodies.
- `calculate_tropicalAscendant_isNullWithoutLocation`.
- `calculate_tropicalAscendant_populatedWithLocation`.
- `calculate_allNewFieldsNull_whenIncludeUnlockedFalse` — locks the gating behaviour.

### 7.3 `VedicTabUiTest` (new, instrumented)

`app/src/androidTest/java/com/willowvibe/agereveal/ui/screen/VedicTabUiTest.kt`:

- `vedicTab_rendersNakshatraCardWithMetadata_whenNakshatraMetadataPresent` — assert "Lord: Moon" / "Deity: Brahma" / "Gana: Manushya" text appears.
- `vedicTab_rendersDashaTreeCard_whenDashaDetailPresent` — assert "MAHADASHA" / "ANTARDASHA" / "PRATYANTAR" headers.
- `vedicTab_rendersNavamsaCard_whenNavamsaChartPresent` — assert "D-9 ASCENDANT" + at least one rashi row.
- `vedicTab_rendersAspectsCard_withHarmoniousAndTenseSections`.
- `vedicTab_omitsAllEnrichmentCards_whenAllFieldsNull`.

These tests use `createComposeRule()` directly with a synthetic `AgeResult` — no Hilt, no ViewModel. Pattern matches `OnboardingScreenUiTest` (Phase 6.5).

**Test count target:** 329 unit + 5 instrumented → **~340 unit + ~9 instrumented** total after this work lands (+11 unit, +4 instrumented).

## 8. Files Touched

| File | Change |
|---|---|
| `data/model/AgeResult.kt` | +5 nullable fields; `dashaInfo` becomes computed `get()` |
| `domain/BirthChartSubChart.kt` | **NEW** — wraps 3 sub-calculators |
| `domain/WesternZodiacCalculator.kt` | +1 thin method `getTropicalAscendant(...)` (alias for `getApproximateAscendant(...)`) |
| `domain/ZodiacCalculator.kt` | +1 facade delegating `getTropicalAscendant(...)` → `western.getTropicalAscendant(...)` |
| `domain/AgeCalculator.kt` | +1 dep; +1 private helper `computePlanetLongitudes`; populate 5 new fields |
| `ui/screen/DetailsUnlockScreen.kt` | `VedicTab` reworked; +4 new private composables; +3 new display helpers; +2 new fields wired into `result` access |
| `app/src/test/.../domain/BirthChartSubChartTest.kt` | **NEW** — 4 tests |
| `app/src/test/.../domain/AgeCalculatorTest.kt` | +6 tests |
| `app/src/androidTest/.../ui/screen/VedicTabUiTest.kt` | **NEW** — 5 tests |

**9 files** (6 modified, 3 new). Lowest-touch approach for a Phase 6.5 engine-vs-UI closure.

## 9. Out-of-Scope (Deferred)

- **CompatibilityScreen Synastry surfacing** — separate work, not part of this spec. `SynastryCalculator` is in `app/src/main/.../domain/SynastryCalculator.kt` (BUG-087), but no `AgeResult` field carries the synastry. Phase F, not E.
- **Mangal Dosha detection** — `ManglikCalculator` not yet created. TASKS.md §2b. Separate work.
- **Daily Fortune shareable card** — TASKS.md §3a. Separate work.
- **One-time lifetime SKU** — TASKS.md §4a. Separate work.
- **BirthChart.compute() production wiring** — Approach B in the brainstorm. Deferred until the Vedic tab is fully surfaced; revisit after Phase E merges.

## 10. Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| `dashaInfo` becoming a computed `get()` breaks a string-based consumer I haven't read | Low | Medium | Grep all call sites in `app/src/main`; run `./gradlew testDebugUnitTest` + `connectedAndroidTest` after the change. If a test fails, address the consumer in this same PR. |
| `AspectCalculator.computeAspects` is O(n²) and triggers per-body `astronomy.snapshot()` calls | Known | Low | n=10 bodies → ~50 calls, sub-millisecond. Acceptable. Not changed. |
| `tropicalAscendant` method alias duplicates `getApproximateAscendant` | Low | Low | Add a unit test that both return the same string. Document the alias in KDoc. |
| New `BirthChartSubChart` adds an extra layer that obscures the data path | Low | Low | One file, one method, three sub-calculators. The alternative (injecting 3 sub-calcs directly into `AgeCalculator`) bloats the constructor. Documented in KDoc. |
| Breaking the share card generation | Low | High | `dashaInfo` stays a `String` (now derived). Run the share card tests after the change. |

## 11. Success Criteria

1. All 4 new cards render in the Vedic tab for a user with a saved birth date + birth time + location.
2. All existing tests pass (329 unit + 5 instrumented = 334 baseline; 0 regressions).
3. New test count: ~340 unit + ~9 instrumented (after this work lands).
4. `dashaInfo: String` reads the same way for every existing call site (no share card change, no `VedicTab` re-test).
5. The Vedic tab is the most informative tab in the app for users who care about Vedic depth — closes the engine-vs-UI gap.
6. Lint clean: `./gradlew lint` passes.
7. `AgeCalculator.calculate()` recompute cost stays under 1 ms per call on a mid-range device (negligible vs. the existing 10-planet dignity loop).

## 12. Implementation Sequence (for `writing-plans`)

Suggested build order, smallest blast radius first:

1. Add `getTropicalAscendant` method to `WesternZodiacCalculator` (thin alias for `getApproximateAscendant`) — standalone, easy to test.
2. Create `BirthChartSubChart` + tests — pure addition, no consumer changes.
3. Add 5 nullable fields to `AgeResult`; convert `dashaInfo` to computed `get()` — no consumer change.
4. Wire `AgeCalculator` (inject, compute, populate) + extend `AgeCalculatorTest` — touches the most code; do this with `dashaInfo` derivation locked in.
5. Update `DetailsUnlockScreen.VedicTab` — pure UI change, isolated to one function + new private composables.
6. Add `VedicTabUiTest` — last, validates the full pipeline end-to-end.

Steps 1-4 can land in one PR; step 5+6 in a second. Or all in one PR if the test count is comfortable (~340 unit + ~9 instrumented all in `git diff`).
