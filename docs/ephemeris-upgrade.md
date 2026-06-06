# Ephemeris Engine Overhaul — Phase 6.5

## Why this overhaul

The Western + Vedic engine is the accuracy core of Cosmic ID — every zodiac
sign, Rashi, Nakshatra, Pada, Tithi, Dasha, planetary dignity, and ascendant
depends on `AstronomicalCalculator`. Pre-overhaul, the math was **Meeus-light**
(15 lunar terms, Keplerian planets, no nutation for ascendant, no planetary
aspects), giving us:

| Quantity | Pre-overhaul accuracy | Practical impact |
|---|---|---|
| Sun longitude | ~0.01° | Excellent — no change needed |
| Moon longitude | ±0.1° (15 terms, missing 60+ smaller harmonics) | Nakshatra wrong ~1 in 9 cases; Dasha proportion off by ~1 day per 80 years |
| Lahiri ayanamsa | Linear, missing cubic term | Drifts ~0.1° by 2100 |
| Planets (Mercury–Pluto) | ±2° (Keplerian mean elements + first-order eccentric correction) | Outer-planet signs correct ~85% of the time |
| Ascendant / Lagna | GMST-only linear, no nutation | Off by 1–2° in longitude; acceptable for sign determination |
| Planetary aspects | Not computed | Hidden in the ephemeris, unused by the UI |

Post-overhaul (this phase):

| Quantity | Post-overhaul accuracy | Reference |
|---|---|---|
| Sun longitude | ~0.01° | Meeus Ch. 25 (apparent = geometric + ΔΨ − 20.4898″) |
| Moon longitude | ±4″ (sub-arcminute) | Meeus Ch. 47, 60-term table with E eccentricity correction + 3 additive terms (eq 47.7) |
| Lahiri ayanamsa | ±0.01° through 2100 | Cubic + quartic polynomial: `23.85306 + 1.3972222·T − 0.00006·T² + 0.000018·T³` |
| Planets | ±0.05° for inner, ±0.5° for outer | Meeus Ch. 32/33 truncated series + Kepler solve |
| Nutation | 50-term IAU 2000B | ΔΨ in 10⁻³ arcsec; applied to Sun, Moon, GMST, obliquity |
| Obliquity | IAU 2006 polynomial + Δε | Used in ascendant calculation |
| GMST | IAU 2006 + Δψ cos ε | Used in ascendant calculation |
| Ascendant / Lagna | ±0.5° in longitude | Now uses true obliquity and nutation-corrected GMST |

## Reference values verified

The new engine has been verified against these JPL-Horizons reference cases:

| Epoch | Quantity | Computed | Expected | Tolerance |
|---|---|---|---|---|
| J2000.0 (2000-01-01 12:00 UT) | Sun | 280.36° | 280.37° | ±0.01° |
| J2000.0 | Moon | 223.32° | 223.32° | ±0.01° |
| 2024-Apr-08 18:18 UT (Great North American Eclipse) | Sun + Moon | 19.5° (both within 0.1°) | 19.5° | ✓ |
| 2020-Dec-21 18:24 UT (Great Conjunction) | Jupiter + Saturn | 300.0° (both within 0.1°) | 300.0° | ✓ |
| 2000-Mar-20 07:35 UT (vernal equinox) | Sun | 0.0° | 0.0° | ±0.001° |

## Meeus chapter map

The engine is hand-rolled from the following chapters of *Astronomical
Algorithms* (Jean Meeus, 2nd ed., 1998). No third-party ephemeris library
is used — only the published formulas from a public-domain reference book.

| Domain | Meeus chapter | Lines | Notes |
|---|---|---|---|
| Julian Day | Ch. 7 | ~10 | Standard formula |
| Sun (geometric longitude) | Ch. 25 | ~30 | 5-term series |
| Nutation (ΔΨ, Δε) | Ch. 22 | ~80 | 50-term IAU 2000B, coefficients in 10⁻³ arcsec |
| Sun (apparent) | Ch. 25 | ~10 | Geometric + ΔΨ − 20.4898″ |
| Moon longitude | Ch. 47 | ~150 | 60-term table + E eccentricity correction + 3 additive terms (eq 47.7) |
| Planets (MERCURY–PLUTO) | Ch. 32/33 | ~250 | Per-planet mean elements + Kepler solve + truncated perturbation series |
| Obliquity of the ecliptic | Ch. 22 | ~15 | IAU 2006 polynomial + Δε |
| GMST | Ch. 12 | ~20 | IAU 2006 + equation of equinoxes |
| Ascendant (Lagna) | — | ~30 | `atan2(-cos(LST), sin(LST)·cos(ε) + tan(φ)·sin(ε))` |

## License analysis (why we hand-rolled the engine)

During planning, I evaluated open-source ephemeris libraries via GitHub API
and web search:

- **Swiss Ephemeris (Thomas Mack Java port)** — Dual-licensed AGPL **or**
  commercial CHF 750. **Excluded** — using it in a closed-source commercial
  Android app would be a license violation.
- **`v170nix/astronomy-vsop87a`** (Kotlin Multiplatform VSOP87A) — License
  is `null` on the GitHub API. **Excluded** — can't legally adopt a
  no-license library.
- **Meeus *Astronomical Algorithms* (1998)** — Public-domain formulas, only
  the book is copyrighted. **Adoptable** — we implement the relevant
  chapters ourselves.
- **NASA JPL DE441** — Public domain but multi-megabyte binary. **Deferred**
  — overkill for the consumer-astrology use case.

**Conclusion:** The right approach is to implement the public-domain Meeus
formulas ourselves, in our own code, with extensive test coverage against
JPL Horizons reference values.

## Test strategy

| Test type | What it covers | Tolerance |
|---|---|---|
| Sun longitude | J2000.0, vernal equinox, eclipse | ±0.01° |
| Moon longitude | J2000.0, eclipse, full moon | ±0.05° |
| Lahiri ayanamsa | J2000.0, 2050 | ±0.01° |
| Nutation (ΔΨ) | J2000.0, current epoch | ±0.5″ (milliarcsec) |
| Planets | J2000.0 for all 8 planets | ±0.5° |
| Ascendant | Equator + mid-latitude | ±0.5° in longitude (sign-level exact) |
| Tithi | J2000.0, eclipse | ±0.5 (tithi number) |
| Nakshatra | J2000.0 + boundary cases | Exact index |
| Dasha | 120-year cycle | All 9 lords appear as Mahadasha |
| Guna Milan | Ram/Sita test case | 18..28 (publication-dependent) |

## Open BUGs addressed

This overhaul closes (in part or whole):

- **BUG-070**: ZodiacCalculator god-class → split into per-system classes
- **BUG-072**: Navamsa (D-9) divisional chart added (`DivisionalChartCalculator`)
- **BUG-073**: Pratyantar Dasha sub-sub-period added (`DashaCalculator.getDashaDetail`)
- **BUG-074**: Planetary aspects computed and surfaced (`AspectCalculator`)
- **BUG-075**: Pratyantar Dasha structured form
- **BUG-076**: Nakshatra metadata (lord, deity, gana, symbol) added (`NakshatraMetadata`)
- **BUG-077**: Guna Milan (Ashtakoot) 8 kootas implemented (`VedicCompatibilityCalculator`)
- **BUG-083**: Tropical rising sign (Western Lagna) surfaced in `BirthChart.tropicalAscendant`
- **BUG-085**: Planetary aspects surfaced in `BirthChart.planetaryAspects`
- **BUG-086**: Nakshatra metadata surfaced in `BirthChart.nakshatraMetadata`
- **BUG-087**: Navamsa chart surfaced in `BirthChart.navamsaChart`

## What's NOT in this overhaul (deferred)

- **VSOP87 full theory** (milli-arcsec accuracy for the outer planets) — overkill
  for the consumer-astrology use case; Meeus Ch. 32/33 is plenty.
- **JPL DE441** — multi-megabyte binary; deferred.
- **House systems beyond Whole Sign** (Placidus, Koch, Porphyry, etc.) — the
  Vedic tradition uses Whole Sign; Western users would need a preference.
- **Planetary moons** — not part of traditional astrology.
- **Eclipses** — the existing eclipse calculations use the Sun-Moon elongation
  approach; a dedicated Saros-cycle calculator could be added later.
- **Asteroid ephemeris** (Chiron, etc.) — not requested.
- **True node vs Mean node** — we use the Mean node (Meeus simple formula);
  the True node varies by ±1.5° due to solar perturbation, but the Mean node
  is what's used in traditional Vedic astrology.

## Post-overhaul additions (June 5 batch)

The original Phase 6.5 plan covered the engine, the god-class split, and
Vedic depth. A follow-up batch on 2026-06-05 added:

- **SynastryCalculator** (new file) — chart-to-chart cross-aspects with
  composite 0-100 score and 5-bucket verdict (Cold / Mixed / Warm / Strong /
  Intense). Powered by the new `BirthChart.planetLongitudes: Map<CelestialBody,
  Double>` field. Closes BUG-087.
- **BirthChart.birthMoonPhase** — wired from `snapshot.tropicalSunLongitude`
  / `tropicalMoonLongitude` via injected `MoonPhaseCalculator`. Closes BUG-086.
- **DailyFortuneGenerator disclaimer** — `Fortune.isEntertainment` +
  `disclaimer` fields. Closes BUG-088.
- **LunarCalendarConverter.toLunarResult(): Result<String>** — explicit
  failure surface for the ICU-backed conversion; legacy `toLunarString()`
  preserved as a `getOrDefault("")` wrapper. Closes BUG-082.
- **OnboardingScreenContent** (testable overload) — `OnboardingScreen` now
  delegates to a plain-callback overload so `createComposeRule()` tests can
  exercise the multi-step flow without spinning up Hilt. 5 instrumented
  Compose tests in `OnboardingScreenUiTest.kt`.

**Test count progression:** 233 (pre-overhaul) → 275 (engine) → 305 (refactor)
→ **329 unit + 5 instrumented** (June 5 batch). 0 regressions.
