# Cosmic ID — Bugs & Edge Case Issues

_Last updated: 2026-05-23 — v2.0.0; engine architecture audit complete_

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

## Revamp v2.0 — Open Issues

### 🟢 BUG-044 — Rewarded/Interstitial Ad Code Still Present After Removal Plan
**Status:** 🟢 Fixed in v2.0
**Severity:** Medium (code debt, confusing for new devs)
**Files:** `ads/AdManager.kt`, `ui/screen/DetailsUnlockScreen.kt`, `ui/viewmodel/CalculatorViewModel.kt`
**Description:** The revamp plan removes rewarded and interstitial ads in favor of a freemium subscription. The old ad code paths are still present and referenced. Leaving them in place creates confusion and increases APK size.
**Fix applied:** `AdManager.kt` now banner-only. `DetailsUnlockScreen` uses `isPremium` flag check instead of "Watch & Reveal" UI. All interstitial call sites removed.

### 🟢 BUG-045 — No `isPremium` Flag in UserPreferencesRepository
**Status:** 🟢 Fixed in v2.0
**Severity:** High (feature blocker)
**File:** `data/preferences/UserPreferencesRepository.kt`
**Description:** The subscription paywall requires a persistent `isPremium` boolean. There was no DataStore key for this yet.
**Fix applied:** Added `IS_PREMIUM_KEY` (boolean, default false) to `UserPreferencesRepository`. Exposed as `Flow<Boolean>`. Synced from `BillingManager.handlePurchases()`.

### 🟡 BUG-046 — Hindi Locale Toggle Removed But strings-hi Still Maintained
**Status:** 🟡 Open — design gap
**Severity:** Low
**File:** `res/values-hi/strings.xml`
**Description:** The custom in-app Hindi toggle is being removed (Android system locale handles this natively since API 33). The `values-hi` strings must still be maintained for users whose system language is Hindi.
**Planned fix:** Keep `values-hi` resources. Remove `LocaleManager` and `AppCompatDelegate.setApplicationLocales` call from Settings. Document that Hindi is system-locale only.

### 🟢 BUG-047 — BillingManager Uses `GlobalScope` and Lacks Error Handling
**Status:** 🟢 Fixed in v2.0
**Severity:** High (memory leak + silent failures)
**File:** `billing/BillingManager.kt`
**Description:** `BillingManager` used `GlobalScope` for coroutines, which never cancels and leaks after `Activity`/`Fragment` destruction. Billing errors were silently swallowed — users saw infinite loading spinners with no feedback.
**Fix applied:** Replaced `GlobalScope` with `CoroutineScope(SupervisorJob())`. Added `scope.cancel()` in `endConnection()`. Added `_error: MutableStateFlow<String?>` with human-readable `billingErrorMessage()` mapping for all `BillingResponseCode` values. `PaywallScreen` now shows error banner with retry CTA.

### 🟢 BUG-048 — RotatingHighlightCard Timer Restarts Every Second
**Status:** 🟢 Fixed in v2.0
**Severity:** Medium (UX — highlight never advances)
**File:** `ui/screen/CalculatorScreen.kt`
**Description:** The 4-second rotating highlight used `remember(result, fortune)` to build the highlight list. Since `result` is recomputed every second (totalSeconds changes), `LaunchedEffect(highlights)` restarted the 4-second delay coroutine every second, effectively freezing the rotation.
**Fix applied:** Changed to `remember(fortune != null)` so the highlight list is stable. Added index clamping `if (index >= highlights.size) index = 0` for safety.

### 🟢 BUG-049 — Stale UI References and Dead Code in AppNavGraph
**Status:** 🟢 Fixed in v2.0
**Severity:** Low (compiler warnings, minor confusion)
**File:** `ui/navigation/AppNavGraph.kt`
**Description:** `Screen.Badges` route, `BadgeScreen` composable, `BadgeViewModel` import, and `onOpenBadges` callback were removed from the nav graph but stale references remained in some comments. `AppNavGraph` also had an unused `scope` variable.
**Fix applied:** Removed all dead references. `snackbarHostState` and `rememberCoroutineScope` also removed from `AppNavGraph` (handled per-screen). `LifeTimelineScreen` now uses reactive `collectAsState()` instead of direct `.value` read.

### 🟢 BUG-050 — AdView Leak in CalculatorScreen Banner
**Status:** 🟢 Fixed in v2.0
**Severity:** Medium (memory leak)
**File:** `ui/screen/CalculatorScreen.kt`
**Description:** The `BannerAdView` composable used `AndroidView(factory = { ... })` without an `onRelease` callback. When the composable leaves composition, the underlying `AdView` is never destroyed, leaking WebView resources and ad listeners.
**Fix applied:** Added `onRelease = { it.destroy() }` to the `AndroidView` factory.

### 🟢 BUG-051 — No Free Trial UX Indicator
**Status:** 🟢 Fixed in v2.0
**Severity:** Low (conversion optimization)
**File:** `billing/BillingManager.kt`, `ui/screen/CalculatorScreen.kt`
**Description:** Users in a free trial had no visible indication of how many days remained, making it easy to forget and lapse.
**Fix applied:** `BillingManager` parses free-trial pricing phase duration and computes `trialDaysRemaining`. CalculatorScreen header shows a teal "N days left" chip when active.

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

## Astrological Calculations

### BUG-003 — Nakshatra and Vedic Rashi Are Approximate Without Birth Time
**Status:** 🟢 Fixed in v0.8
**Severity:** Medium (accuracy / misleading output)
**Files:** `domain/AstronomicalCalculator.kt`, `domain/NakshatraCalculator.kt`, `domain/ZodiacCalculator.kt`
**Description:** The Moon moves approximately 13° per day (roughly one Nakshatra per day). When birth time is unknown, the calculator defaults to solar noon, which can place the Moon in the wrong Nakshatra or even the wrong Rashi. The UI did not warn users that the result is approximate.
**Fix applied:** Added "Approximate" label next to Nakshatra and Rashi when birthTime is null; displayed in WarmAmber color.

### BUG-004 — Chinese Zodiac Ignores Lunar New Year Cutoff
**Status:** 🟢 Fixed in v0.9
**Severity:** Low (accuracy)
**File:** `domain/ZodiacCalculator.kt`
**Description:** The Chinese Zodiac was calculated using the Gregorian year alone (`year % 12`). People born in January or early February before the lunar new year were incorrectly assigned the current Gregorian year's animal instead of the previous year's.
**Example:** Someone born on January 25, 2000 (before lunar new year on February 5, 2000) should be a Rabbit, not a Dragon.
**Fix applied:** Added a 201-entry lookup table (`CNY_DATES`) covering 1900–2100 with exact lunar new year dates. `getChineseYear()` shifts back by one Gregorian year when the date falls before that year's CNY. Regression tests added in `ZodiacCalculatorTest`.

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

### BUG-007 — Milestone Notification Scheduler Not Connected to UI
**Status:** 🟢 Fixed in v0.5
**Severity:** Medium (feature incomplete)
**File:** `notification/MilestoneNotificationScheduler.kt`
**Description:** `MilestoneNotificationScheduler.kt` was implemented but never called from any screen or ViewModel. Milestone push notifications were not functional.
**Fix applied:** Milestone notifications are now scheduled when birth date is first entered in `CalculatorViewModel.onBirthDateSelected()`.

### BUG-008 — Widget May Display Stale Birthday Data
**Status:** 🟢 Fixed in v0.9
**Severity:** Low
**File:** `widget/BirthdayGlanceWidget.kt`, `data/repository/BirthdayRepository.kt`
**Description:** If a user added or deleted a saved birthday while the widget was on the home screen, the widget would not reflect the change until the next system-scheduled update.
**Fix applied:** `BirthdayRepository` calls `notifyWidget()` after every `save()`, `update()`, `delete()`, and `deleteAll()` operation. `notifyWidget()` iterates all active `GlanceId` instances via `GlanceAppWidgetManager` and calls `BirthdayGlanceWidget().update()` for each, triggering an immediate re-render.

### BUG-020 — Milestone Notification Targets Out of Sync with Calculator
**Status:** 🟢 Fixed in v0.9
**Severity:** High (missing notifications)
**Files:** `domain/AgeCalculator.kt`, `notification/MilestoneNotificationScheduler.kt`
**Description:** `MilestoneNotificationScheduler` hardcoded only 6 milestone targets (1K, 5K, 10K, 15K, 20K, 25K) while `AgeCalculator` shows 12 targets (500, 1K, 2K, 3K, 5K, 7K, 10K, 12.5K, 15K, 20K, 25K, 30K) in the UI. Notifications for 500, 2K, 3K, 7K, 12.5K, and 30K day milestones never fired.
**Fix applied:** `MILESTONE_TARGETS` in `MilestoneNotificationScheduler` updated to match `AgeCalculator` exactly.

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

### BUG-010 — Share Card Bitmap Generation May Block the Main Thread
**Status:** 🟢 Fixed in v0.9
**Severity:** Low (performance)
**File:** `domain/ShareCardGenerator.kt`, `ui/viewmodel/CalculatorViewModel.kt`, `ui/viewmodel/CompatibilityViewModel.kt`
**Description:** `ShareCardGenerator` performs Canvas drawing and file I/O synchronously. If called on the main thread this could cause a brief UI freeze.
**Fix applied:** All call sites (`CalculatorViewModel.shareCard()`, `CalculatorViewModel.shareMilestoneCard()`, `CompatibilityViewModel.shareCard()`) dispatch to `Dispatchers.IO` via `viewModelScope.launch(Dispatchers.IO)`. The `startActivity` call inside `ShareCardGenerator.share()` is posted back to the main thread via `Handler(Looper.getMainLooper())`.

### BUG-011 — Share Card Cropped on Some Social Platforms
**Status:** 🟢 Fixed in v0.9
**Severity:** Low (cosmetic)
**File:** `domain/ShareCardGenerator.kt`
**Description:** Share cards were rendered at 900 × 600 (3:2 ratio), causing crops on WhatsApp (16:9 preview) and Instagram (9:16 Stories).
**Fix applied:** All share cards are now output as 900 × 900 square bitmaps. The 900 × 600 content area is centred vertically with 150 px top/bottom margins filled by the theme's background gradient, ensuring no cropping on any major platform.

---

## Ads (Legacy — Being Removed in v2.0)

### BUG-012 — Rewarded Ad Unlock Button Not Disabled When Ad Permanently Fails to Load
**Status:** 🟢 Fixed in v0.5
**Severity:** Low (UX)
**File:** `ads/AdManager.kt`
**Description:** If the rewarded ad fails to load, the "Unlock Details" button remained tappable but produced no result.
**Fix applied:** Added `isRewardedAdAvailable()` method; UI conditionally shows/hides the unlock button based on ad availability.
**Note:** This entire flow is scheduled for removal in v2.0 (replaced by subscription paywall).

### BUG-013 — `AdManager` Uses `WeakReference<Activity>` Which May Be Prematurely Collected
**Status:** 🟢 Fixed in v0.7
**Severity:** Low (intermittent ad failure)
**File:** `ads/AdManager.kt`
**Description:** In low-memory situations the GC could collect the weak Activity reference between ad show and callback, causing the ad not to display.
**Fix applied:** Activity reference is passed directly to `ad.show()` which keeps it strongly referenced during display. The `WeakReference` is cleared immediately after ad dismissal.
**Note:** Interstitial ad code scheduled for removal in v2.0.

### BUG-002 — Interstitial Ad Impression Counter Resets on App Kill
**Status:** 🟢 Fixed in v0.7
**Severity:** Low (monetisation impact)
**File:** `ads/AdManager.kt`
**Description:** The interstitial impression count and last-shown timestamp are held in memory. If the user force-stops or the OS kills the app, the counter resets and the interstitial can fire again immediately on next app open rather than respecting the 5-minute cooldown.
**Fix applied:** Interstitial counter is now persisted in `SharedPreferences` using key `"last_interstitial_shown_ms"`.
**Note:** Interstitial scheduled for complete removal in v2.0.

---

## UI & UX

### BUG-014 — No Accessibility Labels on Icon-Only Buttons and Share Cards
**Status:** 🟢 Fixed in v0.7
**Severity:** Medium (accessibility)
**Files:** `ui/screen/RemindersScreen.kt`, `ui/screen/CompatibilityScreen.kt`, `ui/screen/SettingsScreen.kt`
**Description:** Several icon-only `IconButton` composables had no `contentDescription`, making them inaccessible to TalkBack users.
**Fix applied:** Added `contentDescription` to all icon-only buttons.

### BUG-015 — Settings Are Split Across Two Locations (UX Fragmentation)
**Status:** 🟢 Fixed in v0.9
**Severity:** Low
**Files:** `ui/screen/SettingsScreen.kt`, `ui/screen/RemindersScreen.kt`, `ui/navigation/AppNavGraph.kt`
**Description:** Notification time preferences were accessed via a gear icon in the Birthdays tab (opening a local `NotificationSettingsSheet`), while theme and other settings lived in a separate Settings tab. Users expecting all settings in one place had no indication of the split.
**Fix applied:** Removed `NotificationSettingsSheet` and its supporting code from `RemindersScreen`. The gear icon in the Birthdays header now navigates directly to the Settings screen via `onNavigateToSettings`. All settings — notification time, appearance, data management, and about — are now consolidated in the single Settings tab.

### BUG-016 — Date Picker Has No Minimum Year Guard Below API 26
**Status:** 🟢 Fixed in v0.7
**Severity:** Very Low
**File:** `ui/screen/CalculatorScreen.kt`
**Description:** The date picker did not enforce a minimum year, allowing input before 1900 where the Meeus ephemeris is uncalibrated.
**Fix applied:** Added validation in date picker confirm button to ensure selected date year is >= 1900.

### BUG-017 — Compare Screen Interstitial Counter Not Shared With `AdManager` Cooldown
**Status:** 🟢 Fixed in v0.7
**Severity:** Low
**Files:** `ui/viewmodel/CompareViewModel.kt`, `ads/AdManager.kt`
**Description:** The interstitial show logic was duplicated between `CompareViewModel` and `AdManager`, causing unsynchronised cooldown state.
**Fix applied:** `CompareViewModel` now delegates to `adManager.maybeShowInterstitial()`.
**Note:** Interstitial scheduled for complete removal in v2.0.

### BUG-019 — Dead and Duplicate Imports in `AppNavGraph.kt`
**Status:** 🟢 Fixed in v0.9
**Severity:** Very Low (compiler warning)
**File:** `ui/navigation/AppNavGraph.kt`
**Description:** Two import problems existed: (1) `Icons.Filled.CompareArrows` was imported but only the `AutoMirrored` variant was used; (2) `androidx.compose.runtime.remember` was imported twice on lines 17 and 27.
**Fix applied:** Removed the unused `filled.CompareArrows` import and the duplicate `remember` import.

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

### BUG-029 — Settings Screen Used Wrong ViewModel Type
**Status:** 🟢 Fixed in v0.9.1
**Severity:** High (feature broken)
**Files:** `ui/screen/SettingsScreen.kt`, `ui/viewmodel/SettingsViewModel.kt`, `ui/navigation/AppNavGraph.kt`
**Description:** The Settings screen was incorrectly using `RemindersViewModel` instead of `SettingsViewModel`. This caused a compile error because `RemindersViewModel` lacked the required methods (`milestoneEnabled`, `setMilestoneEnabled`, `clearAllBirthdays`, `notificationHour`, `setNotificationHour`).
**Fix applied:** Refactored `SettingsScreen.kt` to use `SettingsViewModel`; added missing methods; updated `AppNavGraph.kt` to inject `SettingsViewModel` for the Settings route.

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
**Fix applied:** Added JUnit 4 unit tests covering all four domain calculators.

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
| BUG-032 | Western Zodiac used static date table instead of Sun longitude | v0.9.2 |
| BUG-033 | AstronomicalCalculator recomputed JD/trig for every field | v0.9.2 |
| BUG-037 | Name Field Corrupted by Scroll Gestures | v1.0.1 |
| BUG-038 | Custom Clickable Elements Missing Button Role | v1.0.1 |
| BUG-039 | Zodiac Display Accessibility Confusion | v1.0.1 |
| BUG-040 | Share Compatibility Crash: Missing FLAG_ACTIVITY_NEW_TASK | v1.0.2 |
| BUG-041 | `RollingDigits` Composition Leak | v1.0.3 |
| BUG-042 | Missing Widget Preview Images | v1.0.3 |
| BUG-043 | AdManager Retry Hammering | v1.0.3 |

---

## Revamp v2.0 — New Testing Findings

### Appium Automated UI Testing — Status

**Updated 2026-05-16:** A manual ADB walkthrough was completed on branch `tasks-to-beta` covering 10 screens and 7 interactions. 16 screenshots were captured. The Appium automated suite (`screenshots/walkthrough.py`) still needs updating for v2.0 selectors.

### Expected Test Impact

| Feature | Test Status | Notes |
|---------|-------------|-------|
| 3-step onboarding | ⬜ Needs new test flow | First-launch gate blocks existing tests; use `pm clear` to re-trigger |
| Paywall screen | ⬜ Needs new test flow | Premium upsell may block astrology access; use test SKU `android.test.purchased` |
| "My Cosmos" tab rename | ✅ Selectors updated | Formerly "You"; tab label now "My Cosmos" |
| Badges removed from bottom nav | ✅ Selectors updated | Now inside "My Cosmos" section |
| Hindi locale toggle removed | ✅ Test step removed | System locale only (API 33+) |
| Deep-link profile receive | ⬜ Add new test | `agereveal://profile/*` intent handling |
| Daily fortune push | ✅ Worker verified | `DailyFortuneWorker` output verified manually; Settings toggle + hour picker exist |

---

## Revamp v2.0 — Bugs Fixed in 2026-05-22 Audit

### 🟢 BUG-052 — JSON Injection in Deep Link Payload
**Status:** 🟢 Fixed 2026-05-22
**Severity:** High (data integrity — unparseable deep links)
**File:** `domain/ProfileDeepLinkGenerator.kt`
**Description:** The `name` field was inserted directly into a JSON string with no escaping. A name containing double quotes (e.g., `John "Doe"`) would produce malformed JSON that could not be parsed on the receiving end.
**Fix applied:** Name is now escaped with `replace("\\", "\\\\").replace("\"", "\\\"")`. Parser regex updated to handle escaped characters.

### 🟢 BUG-053 — shareFortune() Used Wrong AtomicBoolean Gate
**Status:** 🟢 Fixed 2026-05-22
**Severity:** High (silent share failure)
**File:** `domain/ShareCardGenerator.kt`
**Description:** `shareFortune()` reused `sharingCard` as its concurrency gate instead of having a dedicated `sharingFortune` flag. Calling `share()` and `shareFortune()` in rapid succession silently dropped one of them.
**Fix applied:** Added `sharingFortune` AtomicBoolean; `shareFortune()` now uses it exclusively.

### 🟢 BUG-054 — shareCelebrity() Fired Wrong Error Callback
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Medium (incorrect error routing)
**File:** `domain/ShareCardGenerator.kt`
**Description:** `shareCelebrity()` invoked `onShareError` (generic card-share error handler) instead of a celebrity-specific handler. No `setCelebrityShareErrorHandler` existed.
**Fix applied:** Added `onCelebrityShareError` field and `setCelebrityShareErrorHandler()` setter. `shareCelebrity()` now uses `onCelebrityShareError` in both error paths. Also added `onFortuneShareError` and `setFortuneShareErrorHandler()` for parity.

### 🟢 BUG-055 — Negative Modulo Crash in LunarCalendarConverter
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Medium (silent empty-string fallback for BC dates)
**File:** `domain/LunarCalendarConverter.kt`
**Description:** `lunarYear % 12` on a negative `EXTENDED_YEAR` (BC dates) produced a negative array index. Caught by blanket `catch`, silently returning empty string.
**Fix applied:** Changed to `Math.floorMod(lunarYear.toLong(), 12).toInt()` for safe modulo.

### 🟢 BUG-056 — abs(Long.MIN_VALUE) Returns Negative in DailyFortuneGenerator
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Low (theoretically reachable only with extreme dates)
**File:** `domain/DailyFortuneGenerator.kt`
**Description:** `kotlin.math.abs(Long.MIN_VALUE)` returns `Long.MIN_VALUE` (still negative). Could produce negative array indices. Extremely unlikely with real-world dates but is a classic anti-pattern.
**Fix applied:** Added guard: `if (h == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(h)`.

### 🟢 BUG-057 — Integration Tests Reference Non-Existent WatchAdBanner Composable
**Status:** 🟢 Fixed 2026-05-22
**Severity:** High (compile failure — entire test suite broken)
**Files:** `DetailsUnlockScreenUiTest.kt`, `EndToEndFlowTest.kt`
**Description:** Both files imported and tested `WatchAdBanner` which was removed from the codebase when rewarded ads were removed in v2.0. Three tests in DetailsUnlockScreenUiTest and two in EndToEndFlowTest would not compile.
**Fix applied:** Removed `WatchAdBanner` import and all related tests. Remaining tests (MilestoneRow, HeartbeatRow, LifeProgressBar, AstroTile, compatibility flows) kept intact.

### 🟢 BUG-058 — AppDatabaseMigrationTest Missing MIGRATION_2_3
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Critical (both migration tests crashed at runtime)
**File:** `AppDatabaseMigrationTest.kt`
**Description:** AppDatabase is now at version 3 (with `MIGRATION_2_3` adding `unlocked_badges` table), but both migration tests only provided `MIGRATION_1_2`. Room threw `IllegalStateException: A migration from 2 to 3 was required but not found`.
**Fix applied:** Added `MIGRATION_2_3` to both existing tests. Added new test `migration_2_3_addsUnlockedBadgesTable` to verify the v2→v3 migration.

### 🟢 BUG-059 — AstroTileUiTest Wrong Text Assertions
**Status:** 🟢 Fixed 2026-05-22
**Severity:** High (two tests always failed)
**File:** `AstroTileUiTest.kt`
**Description:** `exactLocation_showsExactLabel` asserted `"(Exact)"` which does not appear in the composable. `noLocation_showsApproximateLabel` asserted `"Approximate — no location"` which also does not exist. Actual labels are `"Lagna"` (exact) and `"Lagna (approx)"` (approximate).
**Fix applied:** Updated assertions to match actual composable output.

### 🟢 BUG-060 — Dead Code in Domain Layer
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Low
**Files:** `RetirementCalculator.kt`, `TimeRemainingCalculator.kt`, `ParallelUniverseGenerator.kt`
**Description:** `RetirementCalculator` had a dead if/else with identical branches. `TimeRemainingCalculator` had a dead `val paychecks = fridays` assignment. `ParallelUniverseGenerator` used naive `year - year` age calculation ignoring birthday passage within the current year.
**Fix applied:** Removed dead conditional from RetirementCalculator. Removed dead assignment from TimeRemainingCalculator. Fixed ParallelUniverseGenerator to compute proper age accounting for month/day.

---

## Revamp v2.0 — Bugs Fixed in 2026-05-22 Audit (Round 2: Horoscope + Architecture)

### 🟢 BUG-061 — Western Zodiac Compatibility Used Hardcoded Date Ranges Instead of Ephemeris
**Status:** 🟢 Fixed 2026-05-22
**Severity:** High (inconsistent sign assignment between profile and compatibility)
**Files:** `domain/ZodiacCompatibilityCalculator.kt`, `domain/ZodiacCalculator.kt`
**Description:** `ZodiacCompatibilityCalculator.getSignIndex()` used hardcoded date ranges (e.g., "Mar 21 – Apr 19 = Aries") to determine Western zodiac signs. `ZodiacCalculator.getWesternZodiac()` used the actual tropical Sun longitude from the astronomical ephemeris. On cusp dates (±1 day of a sign change, which varies by year and leap-cycle), the two methods could assign different signs, causing users to see one sign on their profile and a different sign in compatibility results.
**Fix applied:** Added `getWesternSignIndex()` to `ZodiacCalculator` that returns the astronomical sign index (0–11) from the ephemeris. `ZodiacCompatibilityCalculator` now uses this index for element determination and Western compatibility scoring, eliminating the hardcoded date-range lookup.

### 🟢 BUG-062 — LunarCalendarConverter Caught Throwable Instead of Exception
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Medium (masked fatal errors)
**File:** `domain/LunarCalendarConverter.kt`
**Description:** The catch block used `catch (_: Throwable)` which catches `OutOfMemoryError`, `StackOverflowError`, and other fatal JVM errors. If such an error occurred in the Chinese calendar conversion, it would be silently swallowed and an empty string returned, making debugging impossible.
**Fix applied:** Changed to `catch (_: Exception)` — only catches recoverable exceptions.

### 🟢 BUG-063 — Dead Code: julianDayNoon() Never Used
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Low (dead code)
**Files:** `domain/AstronomicalCalculator.kt`, `domain/DailyFortuneGenerator.kt`
**Description:** `AstronomicalCalculator.julianDayNoon(date: LocalDate)` was a public method that was never called from any production code. `DailyFortuneGenerator` used it but now uses `julianDay(dateTime: LocalDateTime)` with noon time instead.
**Fix applied:** Removed `julianDayNoon()` method. Updated `DailyFortuneGenerator` to use `julianDay(today.atTime(12, 0))`.

### 🟢 BUG-064 — SharedPreferences Scattered Across 7+ Classes
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Medium (maintainability, data consistency risk)
**Files:** `CalculatorViewModel.kt`, `RemindersViewModel.kt`, `RemindersScreen.kt`, plus 4 widget/worker classes
**Description:** User profile data (birth_date, birth_time, birth_location, user_name, fortune cache) was written to `calculator_prefs` SharedPreferences by multiple ViewModels and screens independently, with no single source of truth. Accent color and target age were already mirrored to SharedPreferences from DataStore, but new keys were not centralized.
**Fix applied:** Extended `UserPreferencesRepository` with 7 new DataStore keys that all mirror to `calculator_prefs` SharedPreferences for widget/worker compatibility. `CalculatorViewModel` and `RemindersViewModel` no longer hold `Context` references or read SharedPreferences directly. `RemindersScreen` now reads the birth date via `viewModel.cachedUserBirthDate.collectAsState()` instead of `context.getSharedPreferences()`.

### 🟢 BUG-065 — CalculatorViewModel Held Direct Context Reference
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Medium (MVVM violation, testability)
**File:** `ui/viewmodel/CalculatorViewModel.kt`
**Description:** `CalculatorViewModel` injected `@ApplicationContext appContext: Context` and used it to create a `SharedPreferences` instance directly. ViewModels should not hold Context references — they should go through Repository classes.
**Fix applied:** Removed `appContext` parameter and `.getSharedPreferences()` call. All persistence now goes through `UserPreferencesRepository` suspend functions. `computeDailyFortune()` and `cacheFortune()` converted to suspend functions.

### 🟢 BUG-066 — RemindersViewModel Held Direct Context Reference
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Medium (MVVM violation)
**File:** `ui/viewmodel/RemindersViewModel.kt`
**Description:** `RemindersViewModel` injected `@ApplicationContext context: Context` solely to read `birth_date` from `calculator_prefs`. Also set `notification_hour` only in the scheduler's local prefs, not in the centralized store.
**Fix applied:** Replaced `context` with `UserPreferencesRepository` injection. `cachedUserBirthDate` now loaded asynchronously via `StateFlow`. `setNotificationHour()` now persists through `userPrefs.setNotificationHour()` as well as the scheduler.

### 🟢 BUG-067 — RemindersScreen Read SharedPreferences in Composition
**Status:** 🟢 Fixed 2026-05-22
**Severity:** Low (performance — disk I/O during recomposition)
**File:** `ui/screen/RemindersScreen.kt`
**Description:** Both "LATER" and single-birthday sections called `context.getSharedPreferences("calculator_prefs", ...).getString("birth_date", ...)` inside `remember` blocks during composition, causing repeated disk I/O on every recomposition.
**Fix applied:** Replaced with `viewModel.cachedUserBirthDate.collectAsState()` which reads from the in-memory `StateFlow`, eliminating disk I/O during composition.

---

## Phase 5.6 — Calculation Engine Improvements (Completed)

### ✅ BUG-068 — No Unified Birth Chart Model
**Status:** ✅ Fixed in Phase 5.6
**Severity:** Medium (maintainability, feature velocity)
**Files:** `domain/model/BirthChart.kt`, `AgeCalculator.kt`
**Description:** Every calculator independently derives its data from raw `LocalDate`/`LocalTime` parameters. There was no single `BirthChart` data class that captures all computed astrological data for a given birth moment.
**Fix applied:** Created `BirthChart` data class in `domain/model/` package. This comprehensive container holds the `EphemerisSnapshot` plus all computed astrological values (zodiac, rashi, ascendant, nakshatra, dasha, baZi, lunar birthday, retrograde status). Provides `BirthChart.compute()` factory method for easy instantiation. Decouples age math from astrology for cleaner architecture.

### ✅ BUG-069 — Planet Enum Duplication
**Status:** ✅ Fixed in Phase 5.6
**Severity:** Low (code debt)
**Files:** `domain/model/CelestialBody.kt`, `domain/AstronomicalCalculator.kt`, `domain/PlanetAgeCalculator.kt`
**Description:** Two separate `Planet` enums existed with different fields and no mapping between them. `AstronomicalCalculator.Planet` had orbital elements; `PlanetAgeCalculator.Planet` had display name and emoji.
**Fix applied:** Created consolidated `CelestialBody` enum in `domain/model/` package with orbital periods, aphelion/perihelion distances, display names, and emojis. Both calculators now reference the same type.

### Phase 5.6 Completed (Cross-Cutting Architecture Fixed)

#### ✅ BUG-068 — No Unified Birth Chart Model
**Status:** ✅ Fixed in Phase 5.6
**Severity:** Medium (maintainability, feature velocity)
**Files:** `domain/model/BirthChart.kt`, `AgeCalculator.kt`
**Description:** Every calculator independently derived data from raw `LocalDate`/`LocalTime` parameters. No single `BirthChart` container captured all computed astrological data.
**Fix applied:** Created `BirthChart` data class holding `EphemerisSnapshot` plus all computed values (zodiac, rashi, ascendant, nakshatra, dasha, baZi, lunar birthday, retrograde status). Provides `BirthChart.compute()` factory method. Decouples age math from astrology.

#### ✅ BUG-069 — Planet Enum Duplication
**Status:** ✅ Fixed in Phase 5.6
**Severity:** Low (code debt)
**Files:** `domain/model/CelestialBody.kt`, `domain/AstronomicalCalculator.kt`, `domain/PlanetAgeCalculator.kt`
**Description:** Two separate `Planet` enums existed with different fields and no mapping. `AstronomicalCalculator.Planet` had orbital elements; `PlanetAgeCalculator.Planet` had display name and emoji.
**Fix applied:** Created consolidated `CelestialBody` enum in `domain/model/` with orbital periods, aphelion/perihelion, display names, and emojis. Both calculators now reference the same type.

#### ✅ BUG-070 — ZodiacCalculator Has Too Many Responsibilities
**Status:** ✅ Fixed in Phase 6.5
**Severity:** Medium (maintainability)
**File:** `domain/ZodiacCalculator.kt` (split into 4 files)
**Description:** This single class handled Western zodiac (Sun/Moon signs + cusp), Vedic Rashi + Rashi Lord, Tithi, Lagna/Ascendant (both exact and approximate), planet positions, Chinese zodiac + stem-branch, AND a 201-entry CNY lookup table.
**Fix applied:** Split into four focused calculators — `WesternZodiacCalculator` (76 lines, Western signs + cusp), `VedicZodiacCalculator` (134 lines, Rashi + Tithi + Lagna), `ChineseZodiacCalculator` (143 lines, animal cycle + stem-branch + CNY table), `PlanetaryCalculator` (70 lines, planet longitudes + positions). `ZodiacCalculator` is now a 137-line thin facade that delegates to all four. Existing call sites (AgeCalculator, BirthChart, ZodiacCompatibilityCalculator, BaZiCalculator, VedicCompatibilityCalculator, DailyFortuneGenerator) require no changes — the public API surface is preserved. A back-compat secondary constructor accepts just an `AstronomicalCalculator` for tests that wire the facade manually. 28 new unit tests cover the four split classes (`WesternZodiacCalculatorTest`, `VedicZodiacCalculatorTest`, `ChineseZodiacCalculatorTest`, `PlanetaryCalculatorTest`); the original `ZodiacCalculatorTest` (34 tests) still passes as a delegation test.

### ✅ BUG-071 — No AstronomicalCalculator Unit Tests
**Status:** ✅ Fixed in Phase 5.6
**Severity:** High (untested foundational engine)
**Files:** `domain/AstronomicalCalculator.kt` (298 lines), `app/src/test/java/com/willowvibe/agereveal/domain/AstronomicalCalculatorTest.kt`
**Description:** The foundational ephemeris engine had zero unit tests. All other calculators depend on it. A regression in Sun/Moon longitude or ayanamsa would silently corrupt every astrological output.
**Fix applied:** Created `AstronomicalCalculatorTest` with 16 tests covering: J2000 epoch verification (Sun ~280.37°, Moon ~223.3°), Lahiri ayanamsa at J2000 = 23.85306°, Sidereal conversion, Tithi calculation (new moon=1, full moon=16), Planet longitudes (Jupiter, Saturn), Retrograde detection, and outer planets (Uranus, Neptune, Pluto).

---

### Vedic Engine Gaps

#### ✅ BUG-072 — No Navamsa (D-9) or Other Divisional Charts
**Severity:** Low (feature gap)
**Files:** `domain/DivisionalChartCalculator.kt` (new), `domain/model/BirthChart.kt` (navamsaChart field)
**Description:** Vedic astrology relies heavily on divisional charts (vargas). The Navamsa (D-9) is the most important — it's used for marriage compatibility, spiritual evolution, and planetary strength. The code computes Lagna and planet positions but doesn't derive any divisional charts from them.
**Fix applied (Phase 6.5):** Added `DivisionalChartCalculator.getNavamsa(siderealLongitude): SignPosition` and `getNavamsaChart(planetLongitudes): NavamsaChart`. Each rashi is divided into 9 parts of 3°20′ and mapped to signs `(rashi + k) mod 12` per Brihat Parashara Hora Shastra Ch. 6. 11 unit tests cover boundaries, wrap-around, and per-planet chart generation.

#### 🟡 BUG-073 — No Planetary Strength (Shadbala) or Dignity
**Severity:** Low (feature gap)
**Files:** `domain/ZodiacCalculator.kt`, `domain/NakshatraCalculator.kt`
**Description:** No calculation of planetary dignities (exaltation, debilitation, own sign, moolatrikona). No Shadbala (six-fold strength) computation. Users see "Mars in Taurus" but get no indication that Mars is debilitated there, which is fundamental to Vedic interpretation.
**Recommended fix:** Add `PlanetaryDignityCalculator` with exaltation/debilitation degrees and moolatrikona ranges. Add a `getDignity(planet, longitude)` function returning Dignity enum (Exalted, Own, Moolatrikona, Friendly, Neutral, Inimical, Debilitated).

#### ✅ BUG-074 — No Retrograde Detection
**Severity:** Medium (accuracy gap)
**Files:** `domain/AstronomicalCalculator.kt` (`isRetrograde`)
**Description:** The Keplerian planet longitude solver computes position but not apparent motion direction. Retrograde planets are critical in Vedic interpretation — a retrograde Jupiter behaves very differently from a direct one. The data is computable from the same ephemeris by checking daily longitude change rate.
**Fix applied (Phase 6.5):** `AstronomicalCalculator.isRetrograde(planet, jd): Boolean` was already in place from earlier work; the new Meeus Ch. 32/33 engine returns more accurate results because the underlying longitudes are now sign-level exact.

#### ✅ BUG-075 — Dasha Missing Pratyantar and Deeper Levels
**Severity:** Low (feature depth)
**File:** `domain/DashaCalculator.kt`
**Description:** Only Mahadasha (major period) and Antardasha (sub-period) are computed. Full Vimshottari Dasha includes Pratyantar Dasha (sub-sub-period), Sookshma Dasha, and Prana Dasha. Adding Pratyantar at minimum would significantly improve the feature's perceived depth.
**Fix applied (Phase 6.5):** Added `getDashaDetail(): DashaInfo` returning structured `DashaPeriod` records for Mahadasha, Antardasha, and Pratyantar. The original `getDashaInfo(): String` is preserved for backward compatibility. 4 new unit tests verify the structured form.

#### ✅ BUG-076 — No Nakshatra Lord or Deity Info
**Severity:** Low (feature depth)
**File:** `domain/NakshatraMetadata.kt` (new), `domain/NakshatraCalculator.kt` (`getNakshatraDetails`)
**Description:** `getNakshatra()` returns the name (e.g., "Rohini (रोहिणी)") but not the ruling planet (Moon) or presiding deity (Brahma/Prajapati). This rich metadata would improve the DetailsUnlockScreen Vedic tab.
**Fix applied (Phase 6.5):** Added `NakshatraMetadata` (lookup table for all 27 nakshatras — lord, deity, gana, symbol, emoji, start/end degree). `NakshatraCalculator.getNakshatraDetails()` returns a `NakshatraDetails` wrapper with the rich data + padas + position in mansion. 14 unit tests verify the Vimshottari Dasha lord sequence, Gana distribution, and degree-boundary continuity.

#### ✅ BUG-077 — No Vedic Compatibility (Guna Milan/Ashtakoot)
**Severity:** Low (feature gap)
**Files:** `domain/VedicCompatibilityCalculator.kt` (new)
**Description:** Compatibility only covers Western element matching and Chinese zodiac matrix. Vedic Kundali matching (Ashtakoot/Guna Milan — 8-fold matching scoring 36 points) is completely absent. This is the primary compatibility system used by ~1 billion people.
**Fix applied (Phase 6.5):** Added `VedicCompatibilityCalculator.calculate(male, female): GunaMilan` implementing all 8 kootas (Varna 1pt, Vashya 2pt, Tara 3pt, Yoni 4pt, Graha Maitri 5pt, Gana 6pt, Bhakoot 7pt, Nadi 8pt = 36pt max). The scoring tables follow Brihat Parashara Hora Shastra Ch. 95 and the Government of India Jyotish publication standards. 6 unit tests verify the Aries+Taurus "Ram/Sita" test case, Nadi dosha detection (0/8 for same nadi), and Bhakoot 6/8 dosha detection.

---

### Chinese Engine Gaps

#### 🟡 BUG-078 — Missing Day and Hour Pillars (Full BaZi)
**Severity:** Medium (feature incomplete)
**File:** `domain/BaZiCalculator.kt`
**Description:** Only Year and Month pillars are computed. A proper BaZi (八字 / Four Pillars) reading requires all four: Year, Month, Day, and Hour. The Day Pillar's Heavenly Stem (日主 / Day Master) is the single most important element — it represents the self and is the reference point for all Ten Gods analysis. The code comment acknowledges this gap: "Day and Hour pillars require a full Chinese calendar / solar-term ephemeris which is beyond the scope of this approximation."
**Recommended fix:** Compute Day Stem from Julian Day offset (day stem = (JD + 11) % 10, day branch = (JD + 1) % 12). Hour branch from birth hour in 2-hour blocks. This requires no new ephemeris — only the birth date/time.

#### 🟡 BUG-079 — No Day Master or Ten Gods Analysis
**Severity:** Medium (feature incomplete — depends on BUG-078)
**File:** `domain/BaZiCalculator.kt`
**Description:** Without the Day Master, Ten Gods (十神) relationships cannot be computed. Ten Gods describe how every other stem in the chart relates to the Day Master (e.g., 正官 Direct Officer, 正财 Direct Wealth, 食神 Eating God). This is the core interpretive framework of BaZi.
**Recommended fix:** After BUG-078 is resolved, add `TenGodsCalculator` that maps stem-to-stem relationships (Same, Producing, Controlling, Produced by, Controlled by) × yin/yang polarity → Ten God type.

#### 🟡 BUG-080 — Month Pillar Uses Hardcoded Solar Term Dates
**Severity:** Medium (accuracy)
**File:** `domain/BaZiCalculator.kt` (lines 115–131)
**Description:** `getMonthBranchIndex()` uses fixed Gregorian date boundaries (e.g., 立春 ≈ Feb 4) for solar term divisions. Actual solar terms can shift by ±1 day depending on the year and leap cycles. On boundary dates, the Month Pillar can be wrong.
**Recommended fix:** Replace hardcoded date ranges with astronomical solar term computation (the Sun's tropical longitude crossing multiples of 15°). This requires computing the exact moment the Sun enters each 15° segment, which can be done with the existing `sunLongitude()` function using bisection.

#### 🟡 BUG-081 — No Luck Pillars (大运 / Da Yun)
**Severity:** Low (feature gap)
**File:** `domain/BaZiCalculator.kt`
**Description:** BaZi timing uses 10-year luck pillars (大运) that determine which element/animal energy dominates each decade of life. The calculation depends on gender, year stem yin/yang, and birth month — all available data. This is fundamental to BaZi forecasting.
**Recommended fix:** Add `LuckPillarCalculator` with gender parameter. Compute starting age (3–8 years depending on birth date proximity to next/prev solar term), then generate 10-year pillar sequence using the same stem-branch cycling logic.

#### ✅ BUG-082 — LunarCalendarConverter Silent Empty-String Fallback
**Severity:** Low (UX)
**File:** `domain/LunarCalendarConverter.kt`
**Description:** On any `Exception`, `toLunarString()` returned `""` with no logging. The silent empty-string return masked legitimate failures and the UI showed nothing with no indication that an error occurred.
**Fix applied:** Added a new `toLunarResult(date): Result<String>` method that wraps the conversion in a `kotlin.Result` — failure carries the original `Throwable`. The legacy `toLunarString()` is preserved as a thin `getOrDefault("")` wrapper for back-compat (existing call sites in `BirthChart.compute()` and `AgeCalculator.calculate()` keep working unchanged). A `safeWarn()` helper swallows the JVM-unit-test `Log.w` `RuntimeException` so logging is no-op on the JVM but active in production. `ChineseCalendar.EXTENDED_YEAR` is used directly (was `Calendar.EXTENDED_YEAR`, which is correct but more specific to `ChineseCalendar`). 5 new unit tests in `LunarCalendarConverterTest.kt` cover: result wrapping, no-throw contract, back-compat delegation, leap-year path, and 4 extreme dates (pre-1970, 2050, leap day, baseline).

---

### Western Engine Gaps

#### ✅ BUG-083 — No Tropical Rising Sign
**Severity:** Low (feature gap)
**File:** `domain/model/BirthChart.kt` (`tropicalAscendant` field)
**Description:** `getApproximateAscendant()` computes the sidereal (Vedic) ascendant only. The tropical ascendant longitude is computed internally (`tropicalAsc` variable) but only the sidereal result is returned. Western astrology users expect a tropical rising sign — the data is already available but not exposed.
**Fix applied (Phase 6.5):** `BirthChart.tropicalAscendant: String?` is now surfaced alongside the sidereal ascendant. Computed only when a precise birth location is provided (location != null); otherwise returns null.

#### 🟡 BUG-084 — Missing Outer Planets (Uranus, Neptune, Pluto)
**Severity:** Low (feature gap)
**File:** `domain/AstronomicalCalculator.kt`
**Description:** `Planet` enum only includes Mercury through Saturn. Uranus, Neptune, and Pluto are missing. While traditional astrology uses only the visible planets, modern Western astrology considers the outer planets essential (Uranus = innovation, Neptune = spirituality, Pluto = transformation). The DailyFortuneGenerator references all three in its messages but they can't be computed.
**Recommended fix:** Add Uranus, Neptune, Pluto to `AstronomicalCalculator.Planet` enum with their Keplerian elements. The same `planetLongitude()` function works for them.

#### ✅ BUG-085 — No Planetary Aspects
**Severity:** Medium (feature depth)
**Files:** `domain/AspectCalculator.kt` (new), `domain/model/BirthChart.kt` (`planetaryAspects` field)
**Description:** No aspect computation between planets (conjunction 0°, sextile 60°, square 90°, trine 120°, opposition 180°). Aspects are fundamental to both Western and Vedic chart interpretation. The planet positions are already computed — aspects are just angular differences with orb tolerance.
**Fix applied (Phase 6.5):** Added `AspectCalculator.computeAspects(jd, longitudes): List<Aspect>` covering all 5 major Western aspects. Orbs: conjunction/opposition 8°, sextile 6°, square/trine 8°. The aspect data class includes the type, exact degree, orb, and applying/separating direction. 8 unit tests cover each aspect type and orb boundaries.

#### ✅ BUG-086 — Moon Phase Not Integrated Into Astrological Profile
**Severity:** Low (integration gap)
**Files:** `domain/MoonPhaseCalculator.kt`, `domain/model/BirthChart.kt` (new `birthMoonPhase` field)
**Description:** `MoonPhaseCalculator` existed but the moon phase at birth wasn't exposed in the model. The DailyFortuneGenerator uses today's moon phase but the birth moon phase was never shown.
**Fix applied (Phase 6.5):** `BirthChart.compute()` now wires `MoonPhaseCalculator` and populates the new `birthMoonPhase: MoonPhase?` field from `snapshot.tropicalSunLongitude` and `snapshot.tropicalMoonLongitude` — both already available in the snapshot, no extra astronomical work. 6 unit tests in `BirthChartTest.kt` cover field presence, standard 8-phase name set, illumination in [0, 1], age in [0, 29.53] days (one synodic month), summary content, and JD round-trip.

#### ✅ BUG-087 — Compatibility Uses No Synastry (Chart-to-Chart Aspects)
**Severity:** Low (feature depth)
**File:** `domain/SynastryCalculator.kt` (new), `domain/model/BirthChart.kt` (new `planetLongitudes` field)
**Description:** Western compatibility scoring was pure element-based (Fire/Earth/Air/Water) using trine/sextile/square/opposition of Sun signs only. True synastry overlays two full birth charts and computes inter-chart aspects (e.g., Person A's Venus conjunct Person B's Mars) — what professional astrology apps (Co-Star, The Pattern, Chani) offer.
**Fix applied:** Added `SynastryCalculator.calculate(chartA, chartB): Synastry` that emits a list of `SynastryAspect` rows (personA planet × personB planet × type × orb) plus a composite 0..100 score. The score is orb-tightness-weighted: trines (1.5×) and conjunctions (1.2×) are harmonious, sextiles (0.8×) mildly so, squares (-0.6×) and oppositions (-0.4×) tense. The `Synastry.verdict()` method returns a 5-bucket label (Cold/Mixed/Warm/Strong/Intense) for the UI headline. `grouped()` splits aspects into Harmonious vs Tense for an accordion UI. To support cross-chart math, `BirthChart` exposes a new `planetLongitudes: Map<CelestialBody, Double>` field (sidereal longitudes, degrees 0-360) computed once at chart-build time alongside the existing sign map. Rahu/Ketu are excluded (Vedic-specific). 8 unit tests in `SynastryCalculatorTest.kt` cover: self-comparison guard, identical-charts-everything-conjunction, score range, verdict buckets, grouped split, label rendering, planetLongitudes wiring, half-year pair.

---

### Daily Fortune & Life Stats Gaps

#### ✅ BUG-088 — DailyFortune Messages Reference Non-Existent Transits
**Severity:** Low (misleading UX)
**File:** `domain/DailyFortuneGenerator.kt`
**Description:** The 80+ fortune messages reference "Mars energy," "Venus transit," "Saturn testing patience," "Jupiter saying yes," "your 10th house buzzing," etc. None of these transits or house positions were actually computed against the user's birth chart. Users with astrological knowledge would notice the disconnect.
**Fix applied (Phase 6.5):** Took the (b) path — added a small "for entertainment only" disclaimer without diluting the curated message body. `DailyFortuneGenerator.Fortune` data class gains `isEntertainment: Boolean = true` + `disclaimer: String = DEFAULT_DISCLAIMER`. `DEFAULT_DISCLAIMER = "For entertainment only — not astrological advice."` is exposed as a public constant so the UI layer can render it in a card subtitle. The message body is unchanged. 5 unit tests in `DailyFortuneGeneratorTest.kt` lock in: required fields present, determinism (BUG-056 regression guard), entertainment flag surfaced, lucky number in 1..99 across a 30-day sweep, no exception thrown across a full year sweep (365 days). The "compute real transits" path is deferred to Phase 6+ as documented in `docs/ephemeris-upgrade.md`.

#### 🟡 BUG-089 — LifeStatsCalculator Not Injectable
**Severity:** Low (consistency)
**File:** `domain/LifeStatsCalculator.kt`
**Description:** Every other domain calculator is `@Singleton` with `@Inject constructor`. `LifeStatsCalculator` is a plain class with no annotation, making it inconsistent with the project's DI pattern. It cannot be injected via Hilt.
**Recommended fix:** Add `@Singleton` and `@Inject constructor()` annotations.

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
