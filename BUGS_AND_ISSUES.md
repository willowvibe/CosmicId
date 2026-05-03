# AgeReveal — Bugs & Edge Case Issues

_Last updated: 2026-05-03 — v1.0.5 (Phase 5 features: Milestone Badges, Seconds Counter Widget, Lifespan Widget, Life Stats, 9:16 Story Cards)_

This document tracks known bugs, edge cases, and fragile areas in the codebase. Resolved items are kept for historical reference. For planned work see [TASKS.md](TASKS.md).

---

## Status Legend

| Icon | Meaning |
|---|---|
| 🔴 | Open — confirmed bug, no fix yet |
| 🟡 | Open — known limitation or design gap, not strictly a bug |
| 🟢 | Fixed — resolved in the version noted |
| ✅ | Verified safe — investigated and confirmed not a bug |

---

## Data & Persistence

### BUG-001 — Room `fallbackToDestructiveMigration` Wipes User Data on Schema Change
**Status:** 🟢 Fixed in v0.5  
**Severity:** Critical (data loss)  
**File:** `data/db/AppDatabase.kt`  
**Description:** `AppDatabase` is configured with `.fallbackToDestructiveMigration()`. Any future change to the Room schema (e.g., adding a `birthTime` column to `SavedBirthday`) will silently drop and recreate all tables, deleting every saved birthday without warning.  
**Impact:** All users lose saved birthdays after any app update that bumps the DB schema version.  
**Fix applied:** Removed `fallbackToDestructiveMigration()` and added placeholder `addMigrations()` with comment; explicit `Migration(N, N+1)` objects must be added before any schema change.  
**Related:** TASKS.md §5b

---

### BUG-002 — Interstitial Ad Impression Counter Resets on App Kill
**Status:** 🟢 Fixed in v0.7  
**Severity:** Low (monetisation impact)  
**File:** `ads/AdManager.kt`  
**Description:** The interstitial impression count and last-shown timestamp are held in memory. If the user force-stops or the OS kills the app, the counter resets and the interstitial can fire again immediately on next app open rather than respecting the 5-minute cooldown.  
**Fix applied:** Interstitial counter is now persisted in `SharedPreferences` using key `"last_interstitial_shown_ms"`.

---

## Astrological Calculations

### BUG-003 — Nakshatra and Vedic Rashi Are Approximate Without Birth Time
**Status:** 🟢 Fixed in v0.8  
**Severity:** Medium (accuracy / misleading output)  
**Files:** `domain/AstronomicalCalculator.kt`, `domain/NakshatraCalculator.kt`, `domain/ZodiacCalculator.kt`  
**Description:** The Moon moves approximately 13° per day (roughly one Nakshatra per day). When birth time is unknown, the calculator defaults to solar noon, which can place the Moon in the wrong Nakshatra or even the wrong Rashi. The UI did not warn users that the result is approximate.  
**Fix applied:** Added "Approximate" label next to Nakshatra and Rashi when birthTime is null; displayed in WarmAmber color.

---

### BUG-004 — Chinese Zodiac Ignores Lunar New Year Cutoff
**Status:** 🟢 Fixed in v0.9  
**Severity:** Low (accuracy)  
**File:** `domain/ZodiacCalculator.kt`  
**Description:** The Chinese Zodiac was calculated using the Gregorian year alone (`year % 12`). People born in January or early February before the lunar new year were incorrectly assigned the current Gregorian year's animal instead of the previous year's.  
**Example:** Someone born on January 25, 2000 (before lunar new year on February 5, 2000) should be a Rabbit, not a Dragon.  
**Fix applied:** Added a 201-entry lookup table (`CNY_DATES`) covering 1900–2100 with exact lunar new year dates. `getChineseYear()` shifts back by one Gregorian year when the date falls before that year's CNY. Regression tests added in `ZodiacCalculatorTest`.

---

### BUG-005 — Low-Precision Ephemeris Cusp Dates Not Labelled
**Status:** 🟢 Fixed in v0.9  
**Severity:** Low (edge case accuracy)  
**Files:** `domain/ZodiacCalculator.kt`, `domain/NakshatraCalculator.kt`  
**Description:** For users born very close to the cusp of a Zodiac sign or Nakshatra boundary, the app could display the wrong sign without any indication that the result is uncertain. Sun precision is ~0.01°; Moon precision is ~±0.1°.  
**Fix applied:** `getRashi()` appends ` ⚠ Cusp` when the sidereal Sun longitude falls within 1° of a Rashi boundary (each sign = 30°). `getNakshatra()` appends ` ⚠ Cusp` when the sidereal Moon longitude falls within 1° of a Nakshatra boundary (each mansion = 13°20'). Cusp detection is covered in `ZodiacCalculatorTest` and `NakshatraCalculatorTest`.

---

## Notifications & Scheduling

### BUG-006 — `SCHEDULE_EXACT_ALARM` Permission Silently Fails on Android 12+
**Status:** 🟢 Fixed in v0.7  
**Severity:** Medium (feature breakage, silent)  
**File:** `notification/BirthdayNotificationScheduler.kt`  
**Description:** On Android 12 (API 31) and above, apps must hold `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM` permission to set exact alarms. If the user revoked the permission, WorkManager's exact scheduling silently fell back to inexact timing or failed entirely.  
**Fix applied:** Added `canScheduleExactAlarms()` check before scheduling; logs status for debugging.

---

### BUG-007 — Milestone Notification Scheduler Not Connected to UI
**Status:** 🟢 Fixed in v0.5  
**Severity:** Medium (feature incomplete)  
**File:** `notification/MilestoneNotificationScheduler.kt`  
**Description:** `MilestoneNotificationScheduler.kt` was implemented but never called from any screen or ViewModel. Milestone push notifications were not functional.  
**Fix applied:** Milestone notifications are now scheduled when birth date is first entered in `CalculatorViewModel.onBirthDateSelected()`.

---

### BUG-008 — Widget May Display Stale Birthday Data
**Status:** 🟢 Fixed in v0.9  
**Severity:** Low  
**File:** `widget/BirthdayGlanceWidget.kt`, `data/repository/BirthdayRepository.kt`  
**Description:** If a user added or deleted a saved birthday while the widget was on the home screen, the widget would not reflect the change until the next system-scheduled update.  
**Fix applied:** `BirthdayRepository` calls `notifyWidget()` after every `save()`, `update()`, `delete()`, and `deleteAll()` operation. `notifyWidget()` iterates all active `GlanceId` instances via `GlanceAppWidgetManager` and calls `BirthdayGlanceWidget().update()` for each, triggering an immediate re-render.

---

### BUG-020 — Milestone Notification Targets Out of Sync with Calculator
**Status:** 🟢 Fixed in v0.9  
**Severity:** High (missing notifications)  
**Files:** `domain/AgeCalculator.kt`, `notification/MilestoneNotificationScheduler.kt`  
**Description:** `MilestoneNotificationScheduler` hardcoded only 6 milestone targets (1K, 5K, 10K, 15K, 20K, 25K) while `AgeCalculator` shows 12 targets (500, 1K, 2K, 3K, 5K, 7K, 10K, 12.5K, 15K, 20K, 25K, 30K) in the UI. Notifications for 500, 2K, 3K, 7K, 12.5K, and 30K day milestones never fired.  
**Fix applied:** `MILESTONE_TARGETS` in `MilestoneNotificationScheduler` updated to match `AgeCalculator` exactly.

---

### BUG-025 — Milestone Notification IDs Can Collide with Birthday Notification IDs
**Status:** 🟢 Fixed in v0.9  
**Severity:** Low (rare, silent overwrite)  
**Files:** `notification/MilestoneNotificationScheduler.kt`, `notification/BirthdayReminderWorker.kt`  
**Description:** Milestone notification IDs were computed as `NOTIFICATION_ID_BASE (10,000) + targetDays`, producing IDs such as 20,000 and 25,000. Birthday reminder IDs use the Room auto-increment `id.toInt()`. In edge cases (DB IDs in the 10K–40K range) a milestone notification could silently overwrite a birthday notification.  
**Fix applied:** `NOTIFICATION_ID_BASE` raised to 1,000,000. Milestone IDs are now ≥ 1,001,000 — far above any realistic birthday DB ID.

---

## Sharing & Export

### BUG-009 — `CalendarExport` Fails Silently If No Calendar App Is Installed
**Status:** 🟢 Fixed in v0.5  
**Severity:** Low  
**File:** `domain/CalendarExport.kt`  
**Description:** `CalendarExport` fired an implicit Intent that threw `ActivityNotFoundException` if no calendar app was installed.  
**Fix applied:** Added `resolveActivity(packageManager)` check in `launchCalendarIntent()`. Added `isCalendarAppAvailable()` utility method.

---

### BUG-010 — Share Card Bitmap Generation May Block the Main Thread
**Status:** 🟢 Fixed in v0.9  
**Severity:** Low (performance)  
**File:** `domain/ShareCardGenerator.kt`, `ui/viewmodel/CalculatorViewModel.kt`, `ui/viewmodel/CompatibilityViewModel.kt`  
**Description:** `ShareCardGenerator` performs Canvas drawing and file I/O synchronously. If called on the main thread this could cause a brief UI freeze.  
**Fix applied:** All call sites (`CalculatorViewModel.shareCard()`, `CalculatorViewModel.shareMilestoneCard()`, `CompatibilityViewModel.shareCard()`) dispatch to `Dispatchers.IO` via `viewModelScope.launch(Dispatchers.IO)`. The `startActivity` call inside `ShareCardGenerator.share()` is posted back to the main thread via `Handler(Looper.getMainLooper())`.

---

### BUG-011 — Share Card Cropped on Some Social Platforms
**Status:** 🟢 Fixed in v0.9  
**Severity:** Low (cosmetic)  
**File:** `domain/ShareCardGenerator.kt`  
**Description:** Share cards were rendered at 900 × 600 (3:2 ratio), causing crops on WhatsApp (16:9 preview) and Instagram (9:16 Stories).  
**Fix applied:** All share cards are now output as 900 × 900 square bitmaps. The 900 × 600 content area is centred vertically with 150 px top/bottom margins filled by the theme's background gradient, ensuring no cropping on any major platform.

---

## Ads

### BUG-012 — Rewarded Ad Unlock Button Not Disabled When Ad Permanently Fails to Load
**Status:** 🟢 Fixed in v0.5  
**Severity:** Low (UX)  
**File:** `ads/AdManager.kt`  
**Description:** If the rewarded ad fails to load, the "Unlock Details" button remained tappable but produced no result.  
**Fix applied:** Added `isRewardedAdAvailable()` method; UI conditionally shows/hides the unlock button based on ad availability.

---

### BUG-013 — `AdManager` Uses `WeakReference<Activity>` Which May Be Prematurely Collected
**Status:** 🟢 Fixed in v0.7  
**Severity:** Low (intermittent ad failure)  
**File:** `ads/AdManager.kt`  
**Description:** In low-memory situations the GC could collect the weak Activity reference between ad show and callback, causing the ad not to display.  
**Fix applied:** Activity reference is passed directly to `ad.show()` which keeps it strongly referenced during display. The `WeakReference` is cleared immediately after ad dismissal.

---

## UI & UX

### BUG-014 — No Accessibility Labels on Icon-Only Buttons and Share Cards
**Status:** 🟢 Fixed in v0.7  
**Severity:** Medium (accessibility)  
**Files:** `ui/screen/RemindersScreen.kt`, `ui/screen/CompatibilityScreen.kt`, `ui/screen/SettingsScreen.kt`  
**Description:** Several icon-only `IconButton` composables had no `contentDescription`, making them inaccessible to TalkBack users.  
**Fix applied:** Added `contentDescription` to all icon-only buttons:
- Delete button: "Clear all saved birthdays"
- Toggle notification: "Toggle notification"
- Share icon: "Share match card"
- Calendar icon: "Select date"
- Warning icon: "Warning: This will permanently delete all birthdays"

---

### BUG-015 — Settings Are Split Across Two Locations (UX Fragmentation)
**Status:** 🟢 Fixed in v0.9  
**Severity:** Low  
**Files:** `ui/screen/SettingsScreen.kt`, `ui/screen/RemindersScreen.kt`, `ui/navigation/AppNavGraph.kt`  
**Description:** Notification time preferences were accessed via a gear icon in the Birthdays tab (opening a local `NotificationSettingsSheet`), while theme and other settings lived in a separate Settings tab. Users expecting all settings in one place had no indication of the split.  
**Fix applied:** Removed `NotificationSettingsSheet` and its supporting code from `RemindersScreen`. The gear icon in the Birthdays header now navigates directly to the Settings screen via `onNavigateToSettings`. All settings — notification time, appearance, data management, and about — are now consolidated in the single Settings tab.

---

### BUG-016 — Date Picker Has No Minimum Year Guard Below API 26
**Status:** 🟢 Fixed in v0.7  
**Severity:** Very Low  
**File:** `ui/screen/CalculatorScreen.kt`  
**Description:** The date picker did not enforce a minimum year, allowing input before 1900 where the Meeus ephemeris is uncalibrated.  
**Fix applied:** Added validation in date picker confirm button to ensure selected date year is >= 1900.

---

### BUG-017 — Compare Screen Interstitial Counter Not Shared With `AdManager` Cooldown
**Status:** 🟢 Fixed in v0.7  
**Severity:** Low  
**Files:** `ui/viewmodel/CompareViewModel.kt`, `ads/AdManager.kt`  
**Description:** The interstitial show logic was duplicated between `CompareViewModel` and `AdManager`, causing unsynchronised cooldown state.  
**Fix applied:** `CompareViewModel` now delegates to `adManager.maybeShowInterstitial()`.

---

### BUG-019 — Dead and Duplicate Imports in `AppNavGraph.kt`
**Status:** 🟢 Fixed in v0.9  
**Severity:** Very Low (compiler warning)  
**File:** `ui/navigation/AppNavGraph.kt`  
**Description:** Two import problems existed: (1) `Icons.Filled.CompareArrows` was imported but only the `AutoMirrored` variant was used; (2) `androidx.compose.runtime.remember` was imported twice on lines 17 and 27.  
**Fix applied:** Removed the unused `filled.CompareArrows` import and the duplicate `remember` import.

---

### BUG-023 — Compatibility Screen Shows No Error When Both Dates Are Identical
**Status:** 🟢 Fixed in v0.9  
**Severity:** Low (UX)  
**File:** `ui/viewmodel/CompatibilityViewModel.kt`  
**Description:** When a user selected the same birth date for both people, `result` was set to `null` with `isSameDate = true` but no `error` message was surfaced, leaving the UI blank with no explanation.  
**Fix applied:** `onDateASelected` and `onDateBSelected` now set `error = "Both dates are the same — compatibility requires two different birth dates"` when `same` is true.

---

## ViewModel Correctness

### BUG-022 — `RemindersViewModel.getUserBirthDate()` Repeats SharedPreferences I/O on Every Call
**Status:** 🟢 Fixed in v0.9  
**Severity:** Low (performance)  
**File:** `ui/viewmodel/RemindersViewModel.kt`  
**Description:** `getUserBirthDate()` called `context.getSharedPreferences(...)` on every invocation. While Android caches the preference file in memory after the first load, the repeated lookup was unnecessary and could block briefly on first access.  
**Fix applied:** The SharedPreferences value is read once at ViewModel construction time and stored in `cachedUserBirthDate`. `getUserBirthDate()` now returns the cached value directly.

---

### BUG-029 — Settings Screen Used Wrong ViewModel Type
**Status:** 🟢 Fixed in v0.9.1  
**Severity:** High (feature broken)  
**Files:** `ui/screen/SettingsScreen.kt`, `ui/viewmodel/SettingsViewModel.kt`, `ui/navigation/AppNavGraph.kt`  
**Description:** The Settings screen was incorrectly using `RemindersViewModel` instead of `SettingsViewModel`. This caused a compile error because `RemindersViewModel` lacked the required methods (`milestoneEnabled`, `setMilestoneEnabled`, `clearAllBirthdays`, `notificationHour`, `setNotificationHour`).  
**Fix applied:** 
- Refactored `SettingsScreen.kt` to use `SettingsViewModel` instead of `RemindersViewModel`
- Added `notificationHour` property and `setNotificationHour()` method to `SettingsViewModel`
- Added `clearAllBirthdays()` method to `SettingsViewModel`
- Updated `AppNavGraph.kt` to inject `SettingsViewModel` for the Settings route

---

## Widget

### BUG-024 — `BirthdayGlanceWidget` Could Hang Indefinitely on `Flow.first()`
**Status:** 🟢 Fixed in v0.9  
**Severity:** Medium (widget becomes unresponsive)  
**File:** `widget/BirthdayGlanceWidget.kt`  
**Description:** `provideGlance()` called `.first()` on the Room Flow, which suspends indefinitely if the Flow never emits (e.g., on a corrupted database or during a cold Room initialisation). The widget would hang and never render.  
**Fix applied:** Replaced `.first()` with `.firstOrNull() ?: emptyList()` so the widget renders an empty state immediately if the Flow produces no value.

---

## Testing Gaps

### BUG-018 — No Automated Test Coverage
**Status:** 🟢 Fixed in v0.9  
**Severity:** High (long-term maintainability risk)  
**Description:** The project had zero unit tests and zero instrumented tests. All logic was tested only manually.  
**Fix applied:** Added JUnit 4 unit tests covering all four domain calculators:
- `AgeCalculatorTest` — age components, Feb 29 safety, milestone targets/dates/flags, heartbeats, future-date rejection
- `ZodiacCalculatorTest` — all 14 western zodiac sign boundaries, Chinese zodiac CNY boundary regression (BUG-004), 12-year cycle, cusp detection smoke test
- `NakshatraCalculatorTest` — index validity, cusp suffix format, full-year and full-day sweeps
- `ZodiacCompatibilityCalculatorTest` — score ranges, western/Chinese score spot-checks, headline, element description

---

## Resolved Issues (Historical Reference)

| ID | Issue | Fixed In |
|---|---|---|
| BUG-R01 | Crash on Feb 29 birthday in non-leap year — `yearSafeBirthday()` not used in `BirthdayNotificationScheduler` | v0.3 |
| BUG-R02 | Future date silently accepted in `CompareViewModel` and `RemindersViewModel` | v0.3 |
| BUG-R03 | Add Birthday sheet used `LocalDate.now()` as default when no date was selected | v0.3 |
| BUG-R04 | Equal-age comparison labelled Person B as older instead of showing "Same birthday!" | v0.3 |
| BUG-R05 | Stale `// TODO: add ic_cake drawable` comment in `BirthdayReminderWorker.kt` (drawable already existed) | v0.3 |
| BUG-001 | Room `fallbackToDestructiveMigration` Wipes User Data | v0.5 |
| BUG-007 | Milestone Notification Scheduler Not Connected to UI | v0.5 |
| BUG-009 | `CalendarExport` Fails Silently If No Calendar App | v0.5 |
| BUG-012 | Rewarded Ad Unlock Button Not Disabled When Ad Fails to Load | v0.5 |
| BUG-002 | Interstitial Ad Counter Resets on App Kill | v0.7 |
| BUG-006 | SCHEDULE_EXACT_ALARM Permission Check | v0.7 |
| BUG-013 | AdManager WeakReference Issue | v0.7 |
| BUG-017 | Compare Screen Interstitial Counter Not Shared | v0.7 |
| BUG-014 | Accessibility Labels on Icon-Only Buttons | v0.7 |
| BUG-016 | Date Picker Minimum Year Guard | v0.7 |
| BUG-003 | Nakshatra and Rashi Approximation Without Birth Time | v0.8 |
| BUG-004 | Chinese Zodiac Ignores Lunar New Year Cutoff | v0.9 |
| BUG-005 | Cusp-date Rashi/Nakshatra results not labelled | v0.9 |
| BUG-008 | Widget Displays Stale Birthday Data | v0.9 |
| BUG-010 | Share Card Bitmap Generation on Main Thread | v0.9 |
| BUG-011 | Share Card Cropped on Social Platforms | v0.9 |
| BUG-019 | Dead and Duplicate Imports in AppNavGraph.kt | v0.9 |
| BUG-020 | Milestone Notification Targets Out of Sync | v0.9 |
| BUG-022 | RemindersViewModel SharedPreferences I/O on Every Call | v0.9 |
| BUG-023 | Compatibility Screen Silent on Same-Date Input | v0.9 |
| BUG-024 | BirthdayGlanceWidget Hangs on Flow.first() | v0.9 |
| BUG-025 | Milestone Notification ID Collision with Birthday IDs | v0.9 |
| BUG-015 | Settings Split Across Birthdays Tab and Settings Tab | v0.9 |
| BUG-026 | AdManager No Retry Logic on Load Failure | v0.9 |
| BUG-027 | CalendarExport No Feedback on Missing Calendar App | v0.9 |
| BUG-028 | Widget May Show Stale Data on Refresh | v0.9 |
| BUG-029 | Settings Screen Used Wrong ViewModel Type | v0.9.1 |

---

## Recent Updates (v0.9.2)

| ID | Issue | Fixed In |
|---|---|---|
| BUG-032 | Western Zodiac used static date table instead of Sun longitude | v0.9.2 |
| BUG-033 | AstronomicalCalculator recomputed JD/trig for every field | v0.9.2 |

## Historical Updates

### v0.9.1

| ID | Issue | Fixed In |
|---|---|---|
| BUG-030 | ShareCardGenerator error propagation | v0.9.1 |
| BUG-031 | ShareCardGenerator sets share error handlers in ViewModels | v0.9.1 |



# AgeReveal — Bugs & Edge Case Issues

_Last updated: 2026-05-02 — v1.0.3 (BUG-041/042/043 fixed)_

This document tracks known bugs, edge cases, and fragile areas in the codebase. 

---

## Status Legend

| Icon | Meaning |
|---|---|
| 🔴 | Open — confirmed bug, no fix yet |
| 🟡 | Open — known limitation or design gap, not strictly a bug |
| 🟢 | Fixed — resolved in the version noted |
| ✅ | Verified safe — investigated and confirmed not a bug |

---

## Open Issues & Known Limitations

### 🟢 BUG-034 — Unnecessary Exact Alarm Permissions in Manifest
**Status:** Fixed  
**Severity:** Medium (Play Store rejection risk / misdiagnosed crash)  
**File:** `app/src/main/AndroidManifest.xml`, `notification/BirthdayNotificationScheduler.kt`, `notification/MilestoneNotificationScheduler.kt`  
**Description:** The manifest declared both `SCHEDULE_EXACT_ALARM` and `USE_EXACT_ALARM`. `USE_EXACT_ALARM` is a restricted permission intended for alarm-clock apps and would cause Play Store rejection for a birthday reminder app. WorkManager's `setInitialDelay` does not require exact alarm permissions, making `SCHEDULE_EXACT_ALARM` unnecessary. Misleading `SecurityException` try-catch blocks were added around `enqueueUniqueWork()`, but this method never throws `SecurityException` for normal work requests.  
**Fix applied:** Removed both exact alarm permissions from the manifest. Removed dead `SecurityException` catch blocks and unused `PackageManager` import from notification schedulers. Retained backoff criteria as they improve general scheduling resilience.

### 🟢 BUG-035 — Widget Performance (Unnecessary Recomposition)
**Status:** Fixed  
**Severity:** Low (Battery/Performance)  
**File:** `widget/BirthdayGlanceWidget.kt`, `widget/BirthdayWideGlanceWidget.kt`  
**Description:** The Glance widget updates on every `AppWidgetManager.updateAppWidget` call. Without caching, this would trigger unnecessary DB reads and UI recompositions.  
**Fix applied:** Used `firstOrNull()` on Room Flow instead of `first()` to prevent hanging and leverage Room's internal caching. Combined with `notifyWidget()` that only triggers on actual data changes, this ensures efficient updates without excessive reads.

### 🟢 BUG-036 — Ascendant / Lagna Approximation Implemented
**Status:** Fixed (Workaround implemented)  
**Severity:** Low (Astrological limitation)  
**Files:** `domain/AstronomicalCalculator.kt`, `domain/ZodiacCalculator.kt`, `ui/screen/DetailsUnlockScreen.kt`  
**Description:** The app calculates Western zodiac, Vedic Rashi, Chinese zodiac, and stem-branch. True **Ascendant (Rising Sign / Lagna)** requires the observer's latitude and longitude, which the app does not collect.

**Workaround implemented:** `AstronomicalCalculator.approximateAscendantLongitude()` computes the equatorial ascendant using Greenwich Mean Sidereal Time at 0° latitude — a valid astronomical reference point. `ZodiacCalculator.getApproximateAscendant()` maps this to the corresponding Vedic rashi with Lahiri ayanamsa and standard cusp detection (±1°). The UI displays it as **"Lagna (Ascendant) [name] (Approximate — no location)"** in `DetailsUnlockScreen.kt` so users understand it is a rough estimate (typically off by 1-2 signs for mid-latitude users).

**True calculation remains blocked on:** Adding a location picker UI (city / lat-lon), persisting location data, and switching from the equatorial approximation to the full Campanus/Placidus house formula.

---

---

## Edge Cases — Verified

| Scenario | Status | Notes |
|---|---|---|
| Feb 29 birthday in non-leap year | ✅ Fixed | `yearSafeBirthday()` maps to Mar 1 |
| Feb 29 in notification scheduler | ✅ Fixed | Same helper used in `BirthdayReminderWorker` |
| Future date input | ✅ Fixed | Validated in ViewModels; treats future dates as errors |
| No date selected on Add Birthday sheet | ✅ Fixed | Treats missing date as validation error (`dateError = true`) |
| Equal-age comparison | ✅ Fixed | Shows "Same birthday!" instead of mislabelling Person B as older |
| Today's date (age = 0) | ✅ Verified | `Period.between(today, today)` returns 0; displays correctly |

---

## Appium Automated UI Testing — New Findings (2026-05-02)

**Testing Tool:** Appium v3.3.1 with UiAutomator2 driver v7.1.2  
**Device:** Android Emulator (Medium_Phone, API 37, Android 15)  
**APK:** `AgeReveal-v1.0.1-release.apk`  
**Session:** Appium MCP server with UiAutomator2 instrumentation

### Testing Coverage (v1.0.1 APK Validation)

All major interactive elements were exercised with the rebuilt v1.0.1 APK:

| Feature | Test Result |
|---------|-------------|
| Name input field | ✅ Accepts text input; sanitization strips control characters |
| BORN date picker | ✅ Opens native date dialog; Cancel dismisses correctly |
| TIME time picker | ✅ Opens native time dialog; Set/Cancel work correctly |
| LOCATION field | ✅ Opens custom lat/lng dialog |
| Location dialog — Cancel | ✅ Cancels without saving |
| Location dialog — Set | ✅ Saves coordinates (tested in prior session) |
| Settings button (top header) | ✅ Opens Settings screen with all sections |
| Bottom nav — You | ✅ Navigates to main screen |
| Bottom nav — Match | ✅ Navigates to Cosmic Match screen |
| Bottom nav — Bdays | ✅ Navigates to Birthdays (empty state) |
| Bottom nav — Timeline | ✅ Navigates to Life Timeline |
| Share profile | ✅ Opens card theme picker; Dark Cosmos → Android share sheet |
| Age calculation display | ✅ Shows years, months, days, hours, seconds, next birthday |
| Zodiac info display | ✅ Shows Western, Vedic, Chinese, Moon, Lord, Lagna, Tithi, Nakshatra |
| Live seconds counter | ✅ Ticks every second (ticker flow active) |
| Settings screen sections | ✅ Notifications, Milestones, Appearance, Language, Data all render |

### New Issues Discovered

#### 🟢 BUG-037 — Name Field Corrupted by Scroll Gestures
**Status:** Fixed in v1.0.1  
**Severity:** High — data corruption, accessibility blocker  
**Component:** `android.widget.EditText` (Name field)

**Description:** When the Name `EditText` has focus and a scroll/swipe gesture is performed, the gesture metadata is appended to the field value as text.

**Root Cause:** `detectTapGestures` was applied on the same `Column` as `verticalScroll`, causing pointer-input conflicts. The nested gesture detectors on a single composable interfered with each other, and motion events could leak into the focused `EditText` during scroll gestures.

**Fix applied:**
- Moved `.pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }` from the scrollable inner `Column` to the outer `Box` container (CalculatorScreen.kt). This separates the tap-to-clear-focus gesture from the scrollable content, eliminating the conflict.
- Added input sanitization in `CalculatorViewModel.onNameChanged()` to strip ISO control characters and cap length at 50 chars as a defense-in-depth measure.

**Files changed:** `ui/screen/CalculatorScreen.kt`, `ui/viewmodel/CalculatorViewModel.kt`

---

#### 🟢 BUG-038 — Custom Clickable Elements Missing Button Role
**Status:** Fixed in v1.0.1  
**Severity:** Medium  
**Component:** Custom clickable rows and chips throughout the app

**Description:** Custom clickable elements (`PrecisionChip`, `BirthAnchorRow`, share row, `TeasedDetails` card) used `Modifier.clickable` without explicitly declaring `Role.Button`, causing them to appear as generic clickable `View`s in the accessibility tree rather than semantic buttons.

**Fix applied:**
- Added `role = Role.Button` to all `Modifier.clickable` calls on custom interactive elements:
  - `BirthAnchorRow` (date selector)
  - `PrecisionChip` (TIME and LOCATION chips)
  - Share-profile row
  - `TeasedDetails` unlock/share card
- Added explicit `contentDescription` semantics to each for clearer screen-reader announcements.

**Note:** `TextButton` composables inside `AlertDialog` (Clear/Cancel/Set) are proper Material3 buttons; their internal `android.widget.Button` child with `clickable="false"` is standard Compose framework behavior — the parent semantic node remains clickable and accessible.

**Files changed:** `ui/screen/CalculatorScreen.kt`

---

#### 🟢 BUG-039 — Zodiac Display Accessibility Confusion
**Status:** Fixed in v1.0.1  
**Severity:** Low  
**Component:** `AstroTile` zodiac display heading and grid

**Description:** Appium testing interpreted the static zodiac heading "WESTERN · VEDIC · CHINESE" and the AstroGrid label "Chinese" as a horizontal carousel with non-interactive tabs. In reality the app displays all zodiac info in a static vertical grid, not a carousel.

**Fix applied:**
- Added `Modifier.semantics { heading() }` to the "WESTERN · VEDIC · CHINESE" title so screen readers announce it as a section heading.
- Added `mergeDescendants = true` and `stateDescription` to each `AstroGridItem` so screen readers announce label-value pairs as cohesive units (e.g., "Chinese: Dragon") rather than separate unlabelled text nodes.

**Files changed:** `ui/screen/DetailsUnlockScreen.kt`

---

#### 🟢 BUG-040 — Share Compatibility Crash: Missing FLAG_ACTIVITY_NEW_TASK
**Status:** Fixed in v1.0.2  
**Severity:** High — crash on real devices when sharing from Match screen
**Component:** `ShareCardGenerator` (compatibility and milestone share)

**Description:** Tapping the share button on the Match (Cosmic Match) screen crashes with:
> `Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag`

**Root Cause:** `shareCompatibility()` and `shareMilestone()` added `FLAG_ACTIVITY_NEW_TASK` to the inner `Intent`, but `Intent.createChooser()` creates a *new* chooser `Intent` that does **not** inherit flags from the inner intent. When `context` is an `ApplicationContext` (not an `Activity`), Android requires `FLAG_ACTIVITY_NEW_TASK` on the chooser itself.

`share()` already handled this correctly by checking `if (context is Activity)` before calling `startActivity()`, but `shareMilestone()` and `shareCompatibility()` were missing the same guard.

**Fix applied:**
- Updated both `shareMilestone()` and `shareCompatibility()` to match the `share()` pattern:
  ```kotlin
  if (context is android.app.Activity) {
      context.startActivity(chooser)
  } else {
      context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
  }
  ```
- Removed the redundant `FLAG_ACTIVITY_NEW_TASK` on the inner intent (it was never used).

**Files changed:** `domain/ShareCardGenerator.kt`

---

#### 🟢 BUG-041 — `RollingDigits` Composition Leak
**Status:** Fixed in v1.0.3
**Severity:** Medium — visual stutter / unnecessary recompositions
**Component:** `CalculatorScreen.kt` — `RollingDigits` composable

**Description:** The `RollingDigits` composable used `forEach { char -> }` to iterate over formatted digits inside a `Row`. Without `key()`, Compose could not distinguish individual `AnimatedContent` nodes, causing all digit animations to restart on every number change and potential memory pressure during rapid updates.

**Fix applied:**
- Changed `forEachIndexed` to a standard `for` loop so composable calls (`key()`) are valid in the loop body.
- Wrapped each digit's `AnimatedContent` in `key(index) { ... }` for stable identity across recompositions.
- Replaced deprecated `with` infix operator (enter transition + exit transition) with `togetherWith` (Compose Animation 1.5+).

**Files changed:** `ui/screen/CalculatorScreen.kt`

---

#### 🟢 BUG-042 — Missing Widget Preview Images
**Status:** Fixed in v1.0.3
**Severity:** Low — Play Store listing and launcher widget picker UX
**Component:** `widget_info.xml`, `widget_info_wide.xml`

**Description:** Both `appwidget-provider` XML files lacked `android:previewImage`, resulting in blank/generic previews in the system widget picker and Play Store listing screenshots.

**Fix applied:**
- Added `android:previewImage="@drawable/widget_preview_birthday"` to `widget_info.xml`.
- Added `android:previewImage="@drawable/widget_preview_birthday_wide"` to `widget_info_wide.xml`.
- Created `widget_preview_birthday.xml` — 110dp×110dp rounded rectangle with Dark Cosmos background (#1a1a2e) and 16dp corners.
- Created `widget_preview_birthday_wide.xml` — 250dp×110dp rounded rectangle with matching styling.

**Files changed:** `res/xml/widget_info.xml`, `res/xml/widget_info_wide.xml`, `res/drawable/widget_preview_birthday.xml`, `res/drawable/widget_preview_birthday_wide.xml`

---

#### 🟢 BUG-043 — AdManager Retry Hammering
**Status:** Fixed in v1.0.3
**Severity:** Medium — risk of AdMob rate-limiting
**Component:** `ads/AdManager.kt`

**Description:** On ad load failure, `onAdFailedToLoad` immediately called `preloadRewardedAd()` / `preloadInterstitialAd()` with zero delay. Three rapid failures = three instant requests to AdMob, risking throttling or account penalties.

**Fix applied:**
- Introduced exponential backoff using `Handler(Looper.getMainLooper()).postDelayed()`.
- Delay schedule: 1s → 2s → 4s across the 3 retry attempts (`delayMs = 1000L * (1L shl attempt)`).
- Same pattern applied to both rewarded and interstitial preload callbacks.

**Files changed:** `ads/AdManager.kt`

---

### Observations (Non-blocking)

1. **"LIVE" badge:** Decorative `TextView` next to the AgeReveal title (`clickable="false"`, no `content-desc`). Acceptable if purely a status indicator.
2. **"Test Ad" label:** Appears on the main screen — expected for a debug/release build with Google test ads.
3. **Settings screen:** Correctly opens from the top header button (`content-desc="Settings"`). Contains Notifications, Birthday reminder time, Milestone notifications, Appearance, Language, and Data sections. All sections render correctly.
4. **Ad flow:** "Watch & Reveal" was not visible during this test session because the selected birth date (May 2, 2026 = today) produces an age of 0 with no meaningful locked data to reveal. In prior testing it opened a full-screen interstitial test ad.
5. **Bottom nav:** All four tabs (You, Match, Bdays, Timeline) are accessible and navigate correctly.
6. **Accessibility semantics:** Content descriptions are now properly exposed on interactive elements ("Settings", "Change birth date", "Change TIME", "Change LOCATION"), confirming the BUG-038 fixes are active in the v1.0.1 build.
7. **Name field sanitization:** Input sanitization successfully prevents ISO control character injection; the field caps at 50 characters.
8. **Zodiac display:** Detailed astrology grid renders correctly with all fields (Western, Vedic, Chinese, Moon, Lord, Lagna, Tithi, Nakshatra) after setting birth date and time.