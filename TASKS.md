# AgeReveal — Tasks & Implementation Checklist

_Last updated: 2026-05-01 — v1.0.0-rc1_

---

## 1. Pre-Release Blockers (Must Fix Before Play Store)

### 1a. AdMob IDs — Replace All Four Test Values

All four IDs below generate **no real revenue** and must be swapped for production values from your AdMob account before submitting to the Play Store.

| # | File | Line | Current test value | Replace with |
|---|------|------|--------------------|--------------|
| 1 | `app/build.gradle.kts` | 23 | `ca-app-pub-3940256099942544~3347511713` | Real AdMob **App ID** |
| 2 | `ads/AdManager.kt` | 34 | `ca-app-pub-3940256099942544/6300978111` | Real **Banner** ad unit ID |
| 3 | `ads/AdManager.kt` | 35 | `ca-app-pub-3940256099942544/5224354917` | Real **Rewarded** ad unit ID |
| 4 | `ads/AdManager.kt` | 36 | `ca-app-pub-3940256099942544/1033173712` | Real **Interstitial** ad unit ID |

### 1b. Play Store Listing Assets

- [ ] App icon — 512 × 512 PNG, no transparency
- [ ] Feature graphic — 1024 × 500 PNG
- [ ] Phone screenshots — minimum 2, recommended 5–8 (portrait)
- [ ] Short description — max 80 characters
- [ ] Full description — max 4 000 characters
- [ ] Content rating questionnaire completed in Play Console

---

## 2. UI/UX & Design Polish (Clutter-Free Enhancements) ✨

### 2a. Typography & Hierarchy
- [ ] **Activate Inter Font:** Download Inter TTF files, place in `app/src/main/res/font/`, and uncomment `InterFamily` block in `Type.kt`. Replace all generic system fonts.
- [ ] **Data Contrast:** Ensure primary numbers (like total age) use thin/light weights, while labels use bold, smaller, all-caps styles to establish clear visual hierarchy.

### 2b. Progressive Disclosure & Layout
- [ ] **Tabbed Details Screen:** Refactor `DetailsUnlockScreen` from a single long scrolling list into a horizontal Pager/Tab layout (e.g., `Overview` | `Western` | `Vedic` | `Chinese`).
- [ ] **Expandable Cards:** Hide deep educational text (like Nakshatra meanings or Element descriptions) inside collapsible `AnimatedVisibility` sections on the cards to reduce initial clutter.
- [ ] **Consistent Card Padding:** Standardize all card elevations to 0dp with subtle borders, or pure elevated surfaces with 16dp margins and 16dp internal padding to let data breathe.

### 2c. Micro-Interactions & Animations
- [ ] **Number Roll Animation:** Wrap the live-ticking seconds in an `AnimatedContent` block using `slideInVertically` + `fadeIn` so the numbers smoothly roll like a digital clock.
- [ ] **Hero Stagger Entrance:** Add an enter/reveal stagger animation when the age result first appears on the Calculator screen.
- [ ] **Haptic Feedback Sweep:** Add `HapticFeedbackType.TextHandleMove` to date/time picker scrolls, and `LongPress` feedback to important button taps (Save, Share, Unlock).

### 2d. Empty States & Theming
- [ ] **Beautiful Empty States:** Design and implement aesthetic placeholder graphics + friendly text for the "Saved Birthdays" and "Compare" screens when no data is present.
- [ ] **Cosmos Glow Effects:** In the Dark Cosmos theme, replace flat backgrounds on primary buttons with subtle gradient glows or colored drop-shadows to emphasize interactivity.

---

## 3. Phase 4 — Scale & Ecosystem (Active)

- [ ] **Remove Ads IAP (₹99)** — One-time purchase via Google Play Billing Library 6+; on successful purchase set a flag in `SharedPreferences` that `AdManager` checks before loading any ads
- [ ] **Firebase Firestore sync** — Cloud backup for saved birthdays; login with Google account; conflict resolution by `updatedAt` timestamp
- [ ] **WhatsApp sticker cards** — 512 × 512 transparent-background PNG export following WhatsApp sticker pack format
- [ ] **Age trivia / quiz** — "Guess who was born closest to you?" game mode using saved birthdays
- [ ] **Yearly re-engagement notification** — "You've now lived X days!" notification sent on the user's own birthday each year
- [ ] **Days-until-retirement calculator** — Configurable target age; remaining working days + % of working life completed
- [ ] **Lock screen widget** — API 33+ `AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE`

---

## 4. Deep Astrology Enhancements (Backlog)

### 4a. Vedic & Western Astrology
- [x] **Location (latitude/longitude) support** — Lat/lon input in `CalculatorScreen` with `GeoLocation` data model. `AstronomicalCalculator.exactAscendantLongitude()` computes true ecliptic ascendant using LST + observer latitude. Stored in SharedPreferences. `DetailsUnlockScreen` shows "(Exact)" when location is set, "(Approximate — no location)" otherwise.
- [x] **Dasha (Vimshottari) approximation** — Compute the current Mahadasha / Antardasha based on Moon's nakshatra at birth. Displayed in `DetailsUnlockScreen` as "Lord Mahadasha · Lord Antardasha". Approximate when birth time is absent. 
- [x] **Tithi calculation** — Lunar day (1–30) derived from Moon–Sun elongation. Displayed as "[Name] ([Paksha] Paksha)" in DetailsUnlockScreen.
- [x] **Approximate Lagna (Ascendant)** — GMST-based equatorial ascendant with Lahiri ayanamsa displayed as "Approximate — no location". Full Campanus/Placidus calculation still requires location picker (see above).
- [x] **Planetary positions summary** — Show a "Planet | Sign" table (Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn). Computed via simplified Keplerian elements in `AstronomicalCalculator.planetLongitude()` with geocentric correction. Displayed in `DetailsUnlockScreen` as a compact table.

### 4b. Chinese Astrology
- [x] **Chinese zodiac compatibility matrix** — Expand `ZodiacCompatibilityCalculator` with a full 12×12 Chinese compatibility table: 六合 (Six Harmonies) 92, 三合 (Trine) 95, 相冲 (Clash) 35, 相害 (Harm) 45, 相刑 (Punishment) 40, self-punishment for Dragon/Horse/Rooster/Pig 42. Displayed in `CompatibilityScreen` as a relationship label.
- [x] **Ba Zi (Four Pillars) approximation** — `BaZiCalculator` computes Year pillar (delegates to `getChineseStemBranch`) and Month pillar via 五虎遁月 rule with approximate solar month boundaries. Displayed in `DetailsUnlockScreen` as "Year: X · Month: Y".
- [ ] **Lunar birthday display** — Show the user's birth date converted to Chinese lunar calendar.

---

## 5. Technical Debt

### 5a. Testing
- [ ] UI tests (Compose test) — Calculator screen happy path, date validation errors
- [ ] Instrumented test for Room DAO
- [ ] Write a migration test using `MigrationTestHelper` for explicit `Migration(1, 2)`

### 5b. Performance & Error Handling
- [ ] Profile widget update frequency — evaluate `GlanceStateDefinition` caching.
- [x] Hindi UI toggle — In-app language switch via Settings screen using `AppCompatDelegate.setApplicationLocales` (System / English / Hindi).
- [x] Remove unnecessary exact alarm permissions (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`) from manifest — WorkManager does not require them for `setInitialDelay`.
- [x] Replace noon-default with device time-zone-aware default for astronomical calculations when exact time is missing. `AstronomicalCalculator.snapshot()` now accepts optional `ZoneOffset` and converts local date-time to UT before computing JD. `CalculatorViewModel` passes `OffsetDateTime.now().offset` to ensure accurate ephemeris for users in any timezone.