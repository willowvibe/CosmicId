# AgeReveal — Bugs & Edge Case Issues

_Last updated: 2026-04-21 — v0.9 (BUG-015 fixed)_

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
**Description:** If the rewarded ad failed to load, the "Unlock Details" button remained tappable but produced no result.  
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
