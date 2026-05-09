# AgeReveal — Tasks & Implementation Checklist

_Last updated: 2026-05-03 — v1.0.7 (Phase 5 features: Transparent Overlay Cards, Daily Cosmic Fortune, Retro ASCII Art Share, plus all v1.0.6 features)_

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
- [x] **Activate Inter Font:** Download Inter TTF files, place in `app/src/main/res/font/`, and uncomment `InterFamily` block in `Type.kt`. Replace all generic system fonts.
- [x] **Data Contrast:** Ensure primary numbers (like total age) use thin/light weights, while labels use bold, smaller, all-caps styles to establish clear visual hierarchy.

### 2b. Progressive Disclosure & Layout
- [ ] **Tabbed Details Screen:** Refactor `DetailsUnlockScreen` from a single long scrolling list into a horizontal Pager/Tab layout (e.g., `Overview` | `Western` | `Vedic` | `Chinese`).
- [ ] **Expandable Cards:** Hide deep educational text (like Nakshatra meanings or Element descriptions) inside collapsible `AnimatedVisibility` sections on the cards to reduce initial clutter.
- [ ] **Consistent Card Padding:** Standardize all card elevations to 0dp with subtle borders, or pure elevated surfaces with 16dp margins and 16dp internal padding to let data breathe.

### 2c. Micro-Interactions & Animations
- [x] **Number Roll Animation:** Wrap the live-ticking seconds in an `AnimatedContent` block using `slideInVertically` + `fadeIn` so the numbers smoothly roll like a digital clock.
- [x] **Hero Stagger Entrance:** Add an enter/reveal stagger animation when the age result first appears on the Calculator screen.
- [x] **Haptic Feedback Sweep:** Add `HapticFeedbackType.TextHandleMove` to date/time picker scrolls, and `LongPress` feedback to important button taps (Save, Share, Unlock).

### 2d. Empty States & Theming
- [x] **Beautiful Empty States:** Design and implement aesthetic placeholder graphics + friendly text for the "Saved Birthdays" and "Compare" screens when no data is present.
- [x] **Cosmos Glow Effects:** In the Dark Cosmos theme, replace flat backgrounds on primary buttons with subtle gradient glows or colored drop-shadows to emphasize interactivity.

---

## 3. Phase 4 — Scale & Ecosystem (Active)

- [ ] **Remove Ads IAP (₹99)** — One-time purchase via Google Play Billing Library 6+; on successful purchase set a flag in `SharedPreferences` that `AdManager` checks before loading any ads
- [ ] **Firebase Firestore sync** — Cloud backup for saved birthdays; login with Google account; conflict resolution by `updatedAt` timestamp
- [ ] **WhatsApp sticker cards** — 512 × 512 transparent-background PNG export following WhatsApp sticker pack format
- [ ] **Age trivia / quiz** — "Guess who was born closest to you?" game mode using saved birthdays
- [x] **Yearly re-engagement notification** — "You've now lived X days!" notification sent on the user's own birthday each year ✅ Implemented in v1.0.7
- [x] **Days-until-retirement calculator** — Configurable target age; remaining working days + % of working life completed ✅ Implemented in v1.0.7
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
- [x] **Lunar birthday display** — Show the user's birth date converted to Chinese lunar calendar.

---

## 5. Technical Debt

### 5a. Testing
- [x] UI tests (Compose test) — Calculator screen happy path, date validation errors
- [x] Instrumented test for Room DAO
- [x] Write a migration test using `MigrationTestHelper` for explicit `Migration(1, 2)`

### 5b. Performance & Error Handling
- [x] Profile widget update frequency — evaluate `GlanceStateDefinition` caching. **Evaluation:** `GlanceStateDefinition` caching is unnecessary. `BirthdayRepository.notifyWidget()` triggers immediate updates on every DB change, and Room's in-memory Flow cache makes `firstOrNull()` reads cheap. The 1-hour `updatePeriodMillis` in widget XML is sufficient for the daily countdown refresh. Adding `GlanceStateDefinition` would add complexity without measurable benefit.
- [x] Hindi UI toggle — In-app language switch via Settings screen using `AppCompatDelegate.setApplicationLocales` (System / English / Hindi).
- [x] Remove unnecessary exact alarm permissions (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`) from manifest — WorkManager does not require them for `setInitialDelay`.
- [x] Replace noon-default with device time-zone-aware default for astronomical calculations when exact time is missing. `AstronomicalCalculator.snapshot()` now accepts optional `ZoneOffset` and converts local date-time to UT before computing JD. `CalculatorViewModel` passes `OffsetDateTime.now().offset` to ensure accurate ephemeris for users in any timezone.

---

## 6. Phase 5 — Gen Z Flexing & Viral Features 🔥

This section is the "main character energy" roadmap — features built to screenshot, share, and flex. Each subsection has estimated effort (S/M/L) and viral potential (⭐ to ⭐⭐⭐⭐⭐).

---

### 6.1 Live Widgets (Home Screen & Lock Screen) 🏠

**Goal:** Turn the home screen into a personal flex board. Gen Z curates their home screen like an aesthetic mood board.

#### 6.1.1 Live Seconds Counter Widget (S / ⭐⭐⭐⭐⭐) — ✅ Implemented in v1.0.5
- [x] Create `SecondsCounterGlanceWidget.kt` — 2×1 or 1×1 size.
- [x] Display total seconds alive in large monospace/monospace-alt digits.
- [x] Refresh on screen unlock / home swipe / periodic WorkManager (minute-level due to Android widget restrictions).
- [x] Brutalist/minimal typography — large number with "SECONDS ALIVE" label.
- [x] Dark background with accent mint text.
- [x] Tap opens the app directly to the Calculator screen.
- **Files to create:** `widget/SecondsCounterGlanceWidget.kt`, `widget/SecondsCounterGlanceWidgetReceiver.kt`, XML in `res/xml/seconds_counter_widget_info.xml`
- **Files to modify:** `AndroidManifest.xml` (add receiver), `ui/theme/Color.kt` (widget accent tokens)

#### 6.1.2 Lifespan Progress Bar Widget (S / ⭐⭐⭐⭐) — ✅ Implemented in v1.0.5
- [x] Create `LifespanProgressGlanceWidget.kt` — 4×1 horizontal bar widget.
- [x] Configurable target lifespan (default 80 years) via Settings chip picker.
- [x] Color-coded text showing % completed in teal → amber → rose → red progression.
- [x] Shows "XX%" and total days overlaid.
- [x] Updates once per day (`updatePeriodMillis="86400000"`).
- **Files to create:** `widget/LifespanProgressGlanceWidget.kt`, `widget/LifespanProgressGlanceWidgetReceiver.kt`, `res/xml/lifespan_progress_widget_info.xml`
- **Note:** Target age stored in `UserPreferencesRepository` and mirrored to SharedPreferences for widget sync.

#### 6.1.3 Cosmic Clock Widget (M / ⭐⭐⭐⭐)
- [ ] Create `CosmicClockGlanceWidget.kt` — 2×2 digital-clock style.
- [ ] Display age as `YY:MM:DD` in a digital-clock 7-seg style (or modern sans).
- [ ] Subtle zodiac icon in the corner that changes with current moon phase.
- [ ] "Next milestone: X days" footer text.
- [ ] Lock-screen widget variant for API 33+ (`WIDGET_FEATURE_RECONFIGURABLE`).

#### 6.1.4 Milestone Countdown Ring Widget (S / ⭐⭐⭐) — ✅ Implemented in v1.0.6
- [x] Create `MilestoneRingGlanceWidget.kt` — 2×1 compact widget.
- [x] Shows next upcoming milestone (e.g., "10,000 days") with days remaining and progress percentage.
- [x] Tap opens the app directly to the Calculator screen.
- [x] Updates every 30 minutes via `updatePeriodMillis`.

---

### 6.2 Shareable Formats (Stories, Reels, TikTok) 📱

**Goal:** The app generates content, users distribute it. Every share is organic marketing.

#### 6.2.1 9:16 Story Cards (M / ⭐⭐⭐⭐⭐) — ✅ Implemented in v1.0.5
- [x] Add `STORY_WIDTH = 1080`, `STORY_HEIGHT = 1920` constants to `ShareCardGenerator.kt`.
- [x] Create `generateStoryBitmap()` and `drawStoryDarkCosmos()` / `drawStoryMinimalLight()` / `drawStoryFestiveIndia()` methods.
- [x] Portrait layout: large stats centered, watermark at bottom, safe-zone margins for Instagram UI overlays.
- [x] Add "Story" option to `ShareThemeSheet.kt` alongside existing square cards.
- [x] 250px top and 350px bottom content-safe margins (Instagram Stories UI safe zones).

#### 6.2.2 Transparent Overlay Cards for Green Screen (M / ⭐⭐⭐⭐) — ✅ Implemented in v1.0.7
- [x] Add `generateTransparentOverlayBitmap()` — 1080×1920 with transparent background, only text and icons rendered.
- [x] Export as PNG with alpha channel via `FileProvider`.
- [x] Users can import into TikTok/Reels/YouTube Shorts as green-screen overlay.
- [x] Label in share sheet: "Green Screen Overlay (Transparent BG)".

#### 6.2.3 Animated Shareables — MP4/GIF Export (L / ⭐⭐⭐⭐⭐)
- [ ] Add `MediaEncoder.kt` utility class using `MediaCodec` + `MediaMuxer` to encode Canvas frames to MP4.
- [ ] **Milestone Celebration:** 3-second clip of the seconds counter rolling to the milestone number with confetti particle effects drawn on Canvas.
- [ ] **Compatibility Reveal:** 2-second clip of the compatibility score animating from 0 to final value with a glow pulse.
- [ ] **Age Counter:** 5-second loop of the live seconds counter ticking.
- [ ] Add to `ShareThemeSheet.kt` as "Video (MP4)" option.
- **Library alternative:** Evaluate `androidx.media3:media3-transformer` or `Lottie` export.

#### 6.2.4 Daily Cosmic Fortune Card (M / ⭐⭐⭐⭐) — ✅ Implemented in v1.0.7
- [x] Create `DailyFortuneGenerator.kt` — generates a daily "vibe check" based on:
  - Current moon phase + sign
  - User's sun sign
  - Chinese day stem/branch for the current date
  - Random "fortune cookie" message from a curated pool (80+ messages)
- [x] Show as a card on the Calculator screen (always visible after birth date is set).
- [x] One-tap share as a dedicated fortune card.
- [x] Cache in `SharedPreferences` with daily TTL (regenerates at midnight).
- **Sample messages:** "The moon is in your communication sector today — text that person.", "Your Mars energy is high. Start something bold.", "A Wood day favors growth. Plant a seed (metaphorically or literally)."

---

### 6.3 Social Proof & Comparison 👥

**Goal:** Give users stats that make them feel unique or part of something bigger.


#### 6.3.2 Global Age Percentile (M / ⭐⭐⭐⭐) — ✅ Implemented
- [x] Create `AgePercentileCalculator.kt` using UN World Population Prospects 2024 data.
- [ ] Store age-distribution quintiles for each year (male/female combined for simplicity).
- [ ] Calculate: "At age X, you're older than Y% of the global population."
- [ ] Add to DetailsUnlockScreen as a shareable stat.
- [ ] Also show: "There are ~Z people alive who share your exact birth date."
- **Data:** 20 rows (0–100 years) with cumulative percentages. ~2KB JSON asset.

#### 6.3.3 Generational Badge (S / ⭐⭐⭐) — ✅ Implemented in v1.0.4
- [x] Auto-detect generation from birth year:
  - Silent Generation: 1928–1945
  - Baby Boomers: 1946–1964
  - Gen X: 1965–1980
  - Millennials: 1981–1996
  - Gen Z: 1997–2012
  - Gen Alpha: 2013+
- [x] Generate a shareable badge card: "Certified Gen Z · 1.2 billion seconds survived · 1997–2012 cohort".
- [x] Add generation-typical emoji/icon (Gen Z = 👾, Millennial = 📠, Gen Alpha = 🤖).
- **Files:** `domain/GenerationCalculator.kt` — `GenerationBadgeChip()` in `DetailsUnlockScreen.kt`

---

### 6.4 Gamification & Achievements 🎮

**Goal:** Dopamine loops. Users open the app to see what they unlocked.

#### 6.4.1 Milestone Badges System (M / ⭐⭐⭐⭐⭐) — ✅ Implemented in v1.0.5
- [x] Create `data/model/Badge.kt` with `BadgeDefinition`, `BadgeRarity`, and `UnlockedBadge` Room entity.
- [x] Badge definitions (13 badges):
  | ID | Title | Unlock At | Rarity |
  |---|---|---|---|
  | `1M_SEC` | 1 Million Seconds | 1,000,000 sec (~11.5 days) | Common |
  | `10M_SEC` | 10 Million Seconds | 10,000,000 sec | Common |
  | `100M_SEC` | 100 Million Seconds | 100,000,000 sec (~3.1 years) | Uncommon |
  | `1B_SEC` | Billion Seconds Club | 1,000,000,000 sec (~31.7 years) | Rare |
  | `5K_DAYS` | 5K Days Society | 5,000 days | Common |
  | `10K_DAYS` | 10K Days Society | 10,000 days | Uncommon |
  | `25K_DAYS` | Silver Jubilee (Days) | 25,000 days | Rare |
  | `30K_DAYS` | 30K Legend | 30,000 days | Epic |
  | `QUARTER_CENT` | Quarter-Century Captain | 25 years | Uncommon |
  | `HALF_CENT` | Half-Century Hero | 50 years | Rare |
  | `FULL_CENT` | Century Survivor | 100 years | Legendary |
  | `LEAP_BABY` | Leap Baby | Born Feb 29 | Special |
  | `MILLENNIUM` | Millennium Baby | Born in 2000 | Special |
- [x] Create `BadgeRepository.kt` backed by Room table `unlocked_badges` (DB v3 + migration).
- [x] Check for newly unlocked badges on birth date change and show confetti celebration overlay.
- [x] Each badge is shareable as an individual unlock card via `generateBadgeBitmap()`.
- [x] Add "Badges" tab to bottom nav with `BadgeScreen.kt` (3-column grid + detail bottom sheet).

#### 6.4.2 Life Stats Dashboard (M / ⭐⭐⭐⭐) — ✅ Implemented in v1.0.5
- [x] Create `LifeStatsCalculator.kt` — compute fun aggregated stats:
  - Total full moons witnessed = `daysAlive / 29.53`
  - Total Fridays the 13th survived = count from birth date to today
  - Total leap years lived through
  - Total heartbeats (~72 BPM average) = `secondsAlive × 1.2`
  - Total breaths (~16/min average) = `secondsAlive × 0.267`
  - Total meals eaten (assuming 3/day) = `daysAlive × 3`
  - Total words spoken (avg 7,000/day) = `daysAlive × 7000`
  - Total steps walked (avg 5,000/day) = `daysAlive × 5000`
- [x] Add "Life Stats" section card to `DetailsUnlockScreen`.
- [x] Each stat is individually shareable via `generateLifeStatBitmap()` / `shareLifeStat()`.
- [x] Show as a 2×4 grid of cards with emoji, large number, and label.

#### 6.4.3 Time Remaining Visuals (S / ⭐⭐⭐) — ✅ Implemented in v1.0.6
- [x] Add configurable "Target Age" in Settings (default 30, 40, 50, 60, 65, 70, 80, 90, 100).
- [x] Show on Calculator screen: "You have X weekends left until you're [target age]."
- [x] Also show: "That's X Fridays, X paychecks, X full moons."
- [x] Darkly motivational. Toggle in settings for users who find it depressing.

---

### 6.5 Aesthetic & Vibes 🎨

**Goal:** Let users make the app *theirs*. Aesthetic is identity for Gen Z.

#### 6.5.1 Custom Accent Color Picker (S / ⭐⭐⭐⭐) — ✅ Implemented in v1.0.6
- [x] Add "Accent Color" section in Settings → Appearance.
- [x] Pre-set color swatches: Mint, Amber, Pink, Blue, Purple, Emerald.
- [x] Store selected accent in `UserPreferencesRepository` as DataStore `int` (ARGB), mirrored to SharedPreferences.
- [x] ShareCardGenerator reads accent color from SharedPreferences and applies it to DARK_COSMOS and MINIMAL_LIGHT themes. FESTIVE_INDIA retains gold accent.

#### 6.5.2 Aesthetic Theme Packs (L / ⭐⭐⭐⭐⭐)
- [ ] Create `ThemePack` enum / sealed class with custom color tokens:
  | Theme | Background | Surface | Accent | Text | Vibe |
  |---|---|---|---|---|---|
  | Vaporwave | `#1a0b2e` | `#2d1b4e` | `#ff00ff` | `#00ffff` | Sunset gradients + chrome |
  | Cottagecore | `#f5f0e8` | `#e8e0d0` | `#8b7355` | `#4a4032` | Floral + serif |
  | Y2K | `#ffe4f2` | `#ffb7e6` | `#ff0099` | `#330066` | Bling + bubblegum |
  | Dark Academia | `#2c241b` | `#3d3226` | `#c9a96e` | `#e8dcc5` | Sepia + old maps |
  | Cyberpunk | `#0a0a0f` | `#1a1a2e` | `#00ff41` | `#ff003c` | Neon + grid lines |
- [ ] Each theme pack needs its own `drawThemeBackground()` and `draw*Content()` methods in `ShareCardGenerator.kt`.
- [ ] Store active theme pack in DataStore.
- [ ] Apply theme to all screens, not just share cards.

#### 6.5.3 Retro ASCII Art Share (S / ⭐⭐⭐) — ✅ Implemented in v1.0.7
- [x] Add "ASCII Art" share format to `ShareThemeSheet.kt`.
- [x] Generate monospace text art of the user's age in large block digits.
- [x] Copy to clipboard as plain text for Discord, Reddit, terminal screenshots.
- [x] Simple algorithm: map each digit 0–9 to a 5×5 block-art pattern.

#### 6.5.4 Widget Transparency & Shape (S / ⭐⭐⭐)
- [ ] Support rounded corners, squircle, and pill shapes in all Glance widgets.
- [ ] Support transparent background (show wallpaper behind widget).
- [ ] Support semi-transparent blur background (`android:background="@drawable/widget_bg_rounded_blur"`).

---

### 6.6 "What If" & Interactive Fun 🪐

**Goal:** Give users conversation starters. "Did you know on Mars you'd only be 14?"

#### 6.6.1 Planet Age Converter (M / ⭐⭐⭐⭐⭐) — ✅ Implemented in v1.0.4
- [x] Create `PlanetAgeCalculator.kt` using orbital periods:
  | Planet | Orbital Period (Earth years) |
  |---|---|
  | Mercury | 0.2408 |
  | Venus | 0.6152 |
  | Mars | 1.8809 |
  | Jupiter | 11.8626 |
  | Saturn | 29.4571 |
  | Uranus | 84.0205 |
  | Neptune | 164.8 |
  | Pluto | 248.0 |
- [x] Add "Planet Ages" card to DetailsUnlockScreen (when unlocked).
- [x] Show as a scrollable horizontal row of planet icons with ages.
- [x] Highlight the "youngest" and "oldest" planets.
- [x] Shareable card: "On Mars, John is only 14 years old. 🚀"
- **Files:** `domain/PlanetAgeCalculator.kt`, `PlanetAgesRow()` in `DetailsUnlockScreen.kt`

#### 6.6.2 Birth Moon Phase Visual (S / ⭐⭐⭐⭐) — ✅ Implemented in v1.0.4
- [x] Add moon-phase renderer using Canvas arcs to `DetailsUnlockScreen`.
- [x] Show a visual representation of what the moon looked like on the user's birth date.
- [x] Label: "The moon on your birthday was a waxing gibbous."
- [x] Also show current moon phase with "Moon tonight: [phase]".
- **Files:** `domain/MoonPhaseCalculator.kt`, `MoonPhaseCard()` + `MoonPhaseVisual()` in `DetailsUnlockScreen.kt`

#### 6.6.3 Parallel Universe Birth (S / ⭐⭐⭐) — ✅ Implemented in v1.0.7
- [x] Add "Parallel Universe" card to DetailsUnlockScreen.
- [x] Show 3 random historical/cultural contexts:
  - "If you were born in 1920s India, you'd be [X] years old in the Independence era."
  - "If you were born in 1980s Tokyo, you'd be [X] years old during the bubble economy."
  - "If you were born in ancient Rome, you'd be [X] years old in [consulship year]."
- [x] Curated in-code list with 8 historical contexts (deterministic selection by birth year seed).
- [x] Shareable as a fun fact card via `ShareCardGenerator.shareParallelUniverse()`.

#### 6.6.4 Voice Narration (M / ⭐⭐⭐)
- [ ] Add "🔊 Read My Profile" floating action button to DetailsUnlockScreen.
- [ ] Use Android `TextToSpeech` with pitch/speed adjustments.
- [ ] Pre-scripted dramatic narration: "John Doe... born under the sign of Taurus... [astrology details]..."
- [ ] Option to export as audio file (cache as MP3 via `MediaRecorder` or `TTS` synthesis).

---

## 7. Bug Fixes & Maintenance

### Recently Fixed
- [x] **BUG-040** — `shareCompatibility()` and `shareMilestone()` crash with `FLAG_ACTIVITY_NEW_TASK` error when context is `ApplicationContext`. Fixed by adding Activity-context check and applying flag to the chooser Intent instead of the inner intent.

### Fixed in v1.0.3
- [x] **BUG-041** — `RollingDigits` composition leak: `forEach { char -> }` without `key()` caused Compose to recreate all `AnimatedContent` nodes on every tick. Fixed with `for` loop + `key(index)` and replaced deprecated `with` infix operator with `togetherWith`.
- [x] **BUG-042** — Widget preview images (`previewImage`) missing from both widget XML definitions. Added `android:previewImage` attributes and created `widget_preview_birthday.xml` / `widget_preview_birthday_wide.xml` shape drawables.
- [x] **BUG-043** — `AdManager` retry logic called `preloadRewardedAd()` / `preloadInterstitialAd()` immediately on failure with no delay. Added exponential backoff via `Handler(Looper.getMainLooper()).postDelayed()` with delays of 1s, 2s, 4s across the 3 retry attempts.

---

## Appendix: File Naming Conventions for Phase 5

When implementing Phase 5 features, follow these patterns:

| Feature Area | File Prefix | Example |
|---|---|---|
| Widgets | `*GlanceWidget.kt` | `SecondsCounterGlanceWidget.kt` |
| Share generators | `draw*()` methods in `ShareCardGenerator.kt` | `drawStoryDarkCosmos()` |
| Calculators | `*Calculator.kt` | `PlanetAgeCalculator.kt`, `LifeStatsCalculator.kt` |
| Data models | `data/model/*.kt` | `Celebrity.kt`, `AchievementBadge.kt` |
| Repository | `data/repository/*.kt` | `BadgeRepository.kt` |
| UI screens | `ui/screen/*.kt` | `BadgeCollectionScreen.kt` |
| ViewModels | `ui/viewmodel/*ViewModel.kt` | `BadgeViewModel.kt` |
| JSON assets | `res/raw/*.json` | `celebrities.json`, `historical_contexts.json` |
