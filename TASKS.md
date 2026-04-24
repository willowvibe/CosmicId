# AgeReveal — Tasks & Implementation Checklist

_Last updated: 2026-04-24 — v0.9.2 (Astrology calculation improvements branch created)_

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

Steps:
1. Log in to admob.google.com
2. Add app → copy the App ID (`ca-app-pub-XXXXXXXX~XXXXXXXXXX`)
3. Create three ad units (Banner, Rewarded, Interstitial) → copy each ad unit ID
4. Replace the four values in the table above

### 1b. Play Store Listing Assets

- [ ] App icon — 512 × 512 PNG, no transparency
- [ ] Feature graphic — 1024 × 500 PNG
- [ ] Phone screenshots — minimum 2, recommended 5–8 (portrait)
- [ ] Short description — max 80 characters
- [ ] Full description — max 4 000 characters
- [ ] Content rating questionnaire completed in Play Console

### 1c. UI Polish (Day 4 from roadmap)

- [ ] Enter/reveal animation when age result first appears on Calculator screen
- [ ] Date picker UX — scroll snap and haptic feedback on value change

---

## 2. Phase 3 — High Priority (Complete)

### 2a. Birth Time Support
**Why:** Nakshatra and Rashi calculations use the Moon's position which moves ~13°/day; without birth time, the result can be off by one nakshatra or rashi.

- [x] Add optional time picker below the date picker on the Calculator screen
- [x] Store birth time alongside birth date in `AgeResult` (already immutable, needs new field)
- [x] Pass exact `LocalDateTime` to `AstronomicalCalculator` instead of noon default
- [x] Display *"Exact"* vs *"Approximate"* label on Rashi and Nakshatra cards in `DetailsUnlockScreen`
- [x] Propagate time to `SavedBirthday` entity (requires Room migration — see Section 5b)
- [x] Update `CompareScreen` and `CompatibilityScreen` if birth time is relevant there

### 2b. Milestone Push Notifications — UI Integration
**Why:** `MilestoneNotificationScheduler.kt` is already built; it just needs to be wired into the UI.

- [x] Add a milestones section in `DetailsUnlockScreen` listing upcoming day-milestones
- [x] Per-milestone toggle (enabled/disabled) stored in `SharedPreferences`
- [x] On toggle enable, call `MilestoneNotificationScheduler.schedule(...)` for that milestone
- [x] On toggle disable, cancel the WorkManager job by its unique tag
- [x] Show "next milestone in X days" countdown on the Calculator screen when a milestone is within 30 days

### 2c. Life Timeline Visual
**Why:** Gamification / re-engagement; users can see past achievements and anticipate future ones.

- [x] New `TimelineScreen` or expandable section in `CalculatorScreen`
- [x] Horizontal or vertical scrollable `LazyRow`/`LazyColumn` of milestone cards
- [x] Past milestones shown as *achieved* (filled badge + date)
- [x] Future milestones shown as *upcoming* (outlined badge + countdown)
- [x] Tappable milestone card → opens share sheet for that milestone card
- [x] Entry animation when the screen loads

### 2d. Dedicated Settings Screen
**Why:** Settings were split across `SettingsScreen.kt` and a local sheet in the Birthdays tab gear icon; consolidated into one place (BUG-015).

- [x] Single Settings tab with sections: Appearance, Notifications, Data, About
- [x] Appearance: theme selector (Light / Dark / System default)
- [x] Notifications: default reminder hour picker
- [x] Data: "Clear all birthdays" with confirmation dialog
- [x] About: app version and name
- [x] Birthdays tab gear icon now navigates to Settings instead of opening a duplicate local sheet
- [x] SettingsScreen.kt uses correct `SettingsViewModel` instead of `RemindersViewModel` (BUG-029)
- [x] Milestone notification targets match AgeCalculator exactly (BUG-020)
- [x] Milestone notification IDs won't collide with birthday IDs (BUG-025)
- [x] Notifications: global enable/disable toggle for all birthday reminders
- [x] Data: export birthdays as CSV
- [x] About: open-source licences and privacy policy link

---

## 3. Phase 2 — Post-Launch Additions

Completed:
- [x] Zodiac Compatibility Screen (5th tab)
- [x] Notification Time Customisation (Birthdays tab gear icon)
- [x] In-app review prompt — Trigger `ReviewManager.requestReviewFlow()` after first share (SHARE_THRESHOLD = 1)
- [x] CSV Export — BirthdayCsvExporter.kt exports via share sheet with FileProvider
- [x] Google Calendar Export — CalendarExport.kt one-tap Intent to add birthdays to Google Calendar
- [x] Calendar app availability check — `isCalendarAppAvailable()` guard added
- [x] Hindi translations — `values-hi/strings.xml` added for locale switching

Pending:
- [ ] **4 × 2 home screen widget** — Wider Glance widget showing 3 upcoming birthdays with days-remaining; update `widget_info.xml` with new `minResizeWidth`
- [ ] **Remove Ads IAP (₹99)** — One-time purchase via Google Play Billing Library 6+; on successful purchase set a flag in `SharedPreferences` that `AdManager` checks before loading any ads
- [ ] **Hindi UI toggle** — In-app language switch using Android `LocaleList` / `AppCompatDelegate.setApplicationLocales`; strings already in `values/strings.xml` (translation file `values-hi/strings.xml` exists but toggle UI may be incomplete)

---

## 4. Phase 4 — Future Ideas (Backlog)

- [ ] **Firebase Firestore sync** — Cloud backup for saved birthdays; login with Google account; conflict resolution by `updatedAt` timestamp
- [ ] **WhatsApp sticker cards** — 512 × 512 transparent-background PNG export following WhatsApp sticker pack format
- [ ] **Age trivia / quiz** — "Guess who was born closest to you?" game mode using saved birthdays
- [ ] **Yearly re-engagement notification** — "You've now lived X days!" notification sent on the user's own birthday each year
- [ ] **Days-until-retirement calculator** — Configurable target age; remaining working days + % of working life completed
- [ ] **Lock screen widget** — API 33+ `AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE`
- [ ] **Widgets for iOS** — Evaluate React Native or Flutter port with WidgetKit

---

## 5. Astrology Calculation Improvements (Active — feature/astrology-improvements-20260424)

**Goal:** Improve accuracy, completeness, and user trust in all three astrology systems (Western, Vedic, Chinese). Address known approximation gaps and add deeper interpretive data.

### 5a. Vedic Astrology Enhancements
**Status:** Partially implemented in v0.9; birth-time support added, but several approximation gaps remain.

- [ ] **Nakshatra Pada (quarter) calculation** — Each nakshatra has 4 padas (3°20′ each). Add `NakshatraCalculator.getPada()` and expose pada name + deity in `DetailsUnlockScreen`.
- [ ] **Rashi lord (graha) display** — Each rashi has a ruling planet (e.g., Mesha → Mars). Add a lord field to the rashi card and include it in share cards.
- [ ] **Dasha (Vimshottari) approximation** — Compute the current Mahadasha / Antardasha based on Moon's nakshatra at birth. This is a major engagement feature; start with a simplified lookup table and a "Current Dasha" card in `DetailsUnlockScreen`.
- [ ] **Tithi calculation** — Lunar day (1–30) derived from Moon–Sun elongation. Add `TithiCalculator` and a tithi card (useful for users who follow lunar calendars).
- [ ] **Ayanamsa verification** — The current Lahiri formula is linear; verify against the standard 23.85306° + 5028.84″/cy − 1.397″/cy² formula used by NASA/JPL. Add unit tests against known ephemeris reference dates (e.g., 1 Jan 2000, 1 Jan 1950).
- [ ] **Location (latitude/longitude) support** — Birth time alone is not enough for exact sidereal calculations; the ascendant (Lagna) changes every ~4 minutes. Add optional location picker (lat/lon or city search) and store it in `AgeResult` / `SavedBirthday`. **Blocked by:** UI design for location input.

### 5b. Western Astrology Enhancements
**Status:** Currently uses simple tropical date cutoffs; no planetary positions computed.

- [ ] **Sun longitude-based Western zodiac** — Replace the static date-table in `ZodiacCalculator.getWesternZodiac()` with a call to `AstronomicalCalculator.sunLongitude()` so that users born on cusp dates (e.g., 20 Mar) get the astronomically correct sign. Add cusp warning (⚠) when within 1° of boundary.
- [ ] **Moon sign (Western)** — Compute Moon's tropical longitude and display the Western moon sign alongside the sun sign. This is distinct from Vedic Rashi and appeals to Western-astrology users.
- [ ] **Rising sign (Ascendant) approximation** — With birth time + location, compute the ascendant using the standard Campanus or Placidus house formula. Start with a simplified algorithm (sidereal time → ascendant longitude) and a disclaimer about accuracy.
- [ ] **Planetary positions summary** — Show a "Planet | Sign" table (Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn) in a new "Planetary Positions" card. Re-use the existing Meeus formulas where possible.

### 5c. Chinese Astrology Enhancements
**Status:** Animal + element are computed; no deeper pillars or compatibility logic.

- [ ] **Five-element (Wu Xing) stem-branch calculation** — The current code returns only the animal. Add the full Heavenly Stem + Earthly Branch (e.g., "Jia-Chen / Wood-Dragon" for 2024) and expose the associated element (Wood, Fire, Earth, Metal, Water).
- [ ] **Chinese zodiac compatibility matrix** — Expand `ZodiacCompatibilityCalculator` with a proper 12×12 Chinese compatibility table (trine / clash / harm / punishment relationships) and blend it into the overall score.
- [ ] **Ba Zi (Four Pillars) approximation** — Compute Year, Month, Day, and Hour pillars. This is the most requested Chinese-astrology feature. Start with Year and Month pillars only (Day pillar requires a lookup table for solar terms / 節氣).
- [ ] **Lunar birthday display** — Show the user's birth date converted to Chinese lunar calendar (month + day). Use a simplified astronomical new-moon table or an existing library like `cn.lilytwins.lib:chinese-lunar-calendar`.

### 5d. Cross-System Polish
- [ ] **Unified astrology data model** — Create `AstrologyProfile` data class that holds all computed values (Western sun/moon/rising, Vedic rashi/nakshatra/pada/dasha lord, Chinese animal/stem-branch/element) so that `DetailsUnlockScreen`, `CompatibilityScreen`, and share cards draw from a single source of truth.
- [ ] **Astrology deep-link / share card redesign** — The current share card shows only Western + Chinese zodiac. Redesign to show a 3-column layout (Western | Vedic | Chinese) or a tabbed view.
- [ ] **Add explanatory tooltips** — Each card in `DetailsUnlockScreen` should have a small ℹ️ icon that opens `AstrologyExplanationDialog` with context-specific text (e.g., "Your Moon sign represents your emotional nature...").
- [ ] **Unit-test coverage for all new calculators** — Target 90%+ coverage for `NakshatraCalculator`, `ZodiacCalculator`, `AstronomicalCalculator`, and any new calculators. Verify against known reference data (e.g., JPL Horizons for Sun/Moon, Swiss Ephemeris test cases for ayanamsa).

### 5e. Performance & Accuracy
- [ ] **Cache ephemeris results per calculation session** — `AstronomicalCalculator` recomputes Julian Day and trigonometric series for every field. Cache `jd`, `sunLongitude`, and `moonLongitude` in a single `EphemerisSnapshot` object per birth date-time.
- [ ] **Replace noon-default with time-zone-aware default** — When the user does not provide a birth time, the current code uses 12:00 local time (via `atTime(12, 0)`). For users in time zones far from UT, this can shift the Moon by several degrees. Use the device's current time zone offset as a better approximation, or at least document the limitation.

---

## 5. Technical Debt & Improvements

### 5a. Testing
**Status:** Unit tests added for domain calculators and repositories in v0.9. Still need instrumented tests.

- [x] Unit tests for `AgeCalculator` — edge cases: Feb 29, Jan 1, today's date, year 0, very old dates (>100 years)
- [x] Unit tests for `AstronomicalCalculator` — verify Sun/Moon positions against known reference dates
- [x] Unit tests for `ZodiacCalculator` and `NakshatraCalculator` — boundary dates between signs/nakshatras
- [x] Unit tests for `ZodiacCompatibilityCalculator` — scoring for all sign combinations
- [x] Unit tests for `BirthdayRepository` — CRUD + `nextBirthdayEpochDay` auto-computation
- [x] Unit tests for `BirthdayNotificationScheduler` — Feb 29 edge case, past birthday handling
- [ ] UI tests (Compose test) — Calculator screen happy path, date validation errors
- [ ] Instrumented test for Room DAO

### 5b. Room DB Migration Strategy
**Status:** `AppDatabase` uses `addMigrations(*Migrations.ALL)` with explicit `Migration(1, 2)` object — no more `fallbackToDestructiveMigration()`.

- [x] Add explicit `Migration(1, 2)` object in `AppDatabase` (v0.9)
- [x] `Migrations.kt` contains migration logic for adding `birthTime` column
- [ ] Write a migration test using `MigrationTestHelper`
- [ ] After a stable migration is verified, ensure `fallbackToDestructiveMigration()` is not used

### 5c. Custom Typography
**Status:** `Type.kt` has `InterFamily` commented out; falls back to system sans-serif.

- [ ] Download Inter TTF files from rsms.me/inter:
  - `inter_regular.ttf`, `inter_medium.ttf`, `inter_semibold.ttf`, `inter_bold.ttf`
- [ ] Place in `app/src/main/res/font/`
- [ ] Uncomment `InterFamily` block in `Type.kt` (lines 20–25)
- [ ] Replace `FontFamily.Default` → `InterFamily` in every `TextStyle` in that file

### 5d. Ad Lifecycle & Resilience
**Status:** v0.7 fixes implemented.

- [x] Persist interstitial ad impression count and last-shown timestamp in `SharedPreferences` (v0.7)
- [x] Add retry logic in `AdManager` when an ad fails to load (exponential back-off, max 3 retries) (v0.9)
- [x] Gracefully disable the rewarded unlock button if the rewarded ad load has permanently failed after retries (v0.5)

### 5e. Accessibility
**Status:** v0.7 fixes implemented.

- [x] Add `contentDescription` to all icon-only buttons and the share card image view (v0.7)
- [x] Ensure tappable targets meet the 48 dp minimum touch target size (v0.7)
- [ ] Test with TalkBack enabled on Calculator, Reminders, and Compatibility screens

### 5f. Performance
- [ ] Profile widget update frequency — Glance widget may trigger unnecessary recompositions on every `AppWidgetManager.updateAppWidget` call; consider `GlanceStateDefinition` caching
- [ ] Bitmap generation in `ShareCardGenerator` runs on the main thread in some paths — move entirely to a coroutine dispatcher (`Dispatchers.Default`)
- [ ] Review `AstronomicalCalculator` for repeated trigonometric calls that can be cached per calculation session

### 5g. Error Handling
**Status:** Partially implemented in v0.5 and v0.9; ShareCardGenerator improved in v0.9.1.

- [x] `CalendarExport` fires an Intent without checking if a Calendar app exists; added `resolveActivity` check in v0.5
- [x] `ShareCardGenerator` now propagates errors back to the ViewModel with error messages (v0.9.1)
- [ ] Handle `SecurityException` when `SCHEDULE_EXACT_ALARM` permission is not granted (Android 12+); show a settings-deep-link prompt

---

## 6. Edge Cases — Verified

| Scenario | Status | Notes |
|---|---|---|
| Feb 29 birthday in non-leap year | ✅ Fixed | `yearSafeBirthday()` maps to Mar 1 |
| Feb 29 in notification scheduler | ✅ Fixed | Same helper used in `BirthdayNotificationScheduler` |
| Future date input | ✅ Fixed | Validated in `CalculatorViewModel`, `CompareViewModel`, `RemindersViewModel` |
| No date selected on Add Birthday sheet | ✅ Fixed | Treats missing date as validation error (`dateError = true`) |
| Equal-age comparison | ✅ Fixed | Shows "Same birthday!" instead of mislabelling Person B as older |
| Today's date (age = 0) | ✅ Verified | `Period.between(today, today)` returns 0; displays correctly |

For open/unresolved edge cases see [BUGS_AND_ISSUES.md](BUGS_AND_ISSUES.md).
