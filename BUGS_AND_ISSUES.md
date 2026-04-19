# AgeReveal — Bugs & Edge Case Issues

_Last updated: 2026-04-19 — v0.8_

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
**Note:** Consider persisting `lastInterstitialShownMs` in `SharedPreferences` for robustness across app kills.

---

## Astrological Calculations

### BUG-003 — Nakshatra and Vedic Rashi Are Approximate Without Birth Time
**Status:** 🟢 Fixed in v0.8
**Severity:** Medium (accuracy / misleading output)  
**Files:** `domain/AstronomicalCalculator.kt`, `domain/NakshatraCalculator.kt`, `domain/ZodiacCalculator.kt`  
**Description:** The Moon moves approximately 13° per day (roughly one Nakshatra per day). When birth time is unknown, the calculator defaults to solar noon, which can place the Moon in the wrong Nakshatra or even the wrong Rashi. The UI does not currently warn users that the result is approximate.  
**Fix applied:** Added "Approximate" label next to Nakshatra and Rashi when birthTime is null; displayed in WarmAmber color.

---

### BUG-004 — Chinese Zodiac Ignores Lunar New Year Cutoff
**Status:** 🟡 Known Limitation  
**Severity:** Low (accuracy)  
**File:** `domain/ZodiacCalculator.kt`  
**Description:** The Chinese Zodiac is calculated using the Gregorian year alone (`year % 12`). The Chinese New Year falls between January 21 and February 20 each year. A person born in January or early February before the lunar new year technically belongs to the previous animal year, but the app assigns them to the current Gregorian year's sign.  
**Example:** Someone born on January 25, 2000 (before the lunar new year on February 5, 2000) should be a Rabbit, not a Dragon. The app incorrectly shows Dragon.  
**Fix needed:** Use a precomputed lookup table of lunar new year dates (or a lightweight lunar calendar algorithm) to determine the correct animal year.

---

### BUG-005 — Low-Precision Ephemeris (±1° Error)
**Status:** 🟡 Known Limitation  
**Severity:** Low (edge case accuracy)  
**File:** `domain/AstronomicalCalculator.kt`  
**Description:** The Meeus low-precision algorithms used for Sun and Moon positions have an error of approximately ±1°. For users born very close to the cusp of a Zodiac sign or Nakshatra boundary, the app may display the wrong sign or nakshatra.  
**Example:** A user born on the day the Sun crosses from Aries to Taurus (around April 14 in sidereal terms) may get either sign depending on birth time and the ±1° rounding error.  
**Fix needed:** Replace with a higher-precision ephemeris library, or clearly label cusp-date results as *"Cusp — birth time required for exact sign"*.

---

## Notifications & Scheduling

### BUG-006 — `SCHEDULE_EXACT_ALARM` Permission Silently Fails on Android 12+
**Status:** 🟢 Fixed in v0.7  
**Severity:** Medium (feature breakage, silent)  
**File:** `notification/BirthdayNotificationScheduler.kt`  
**Description:** On Android 12 (API 31) and above, apps must hold `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM` permission to set exact alarms. The manifest declares both permissions, but if the user has revoked or not granted exact alarm permission via the system settings, `WorkManager`'s exact scheduling silently falls back to inexact timing (or fails entirely on some OEMs), causing birthday notifications to fire at unpredictable times or not at all.  
**Fix applied:** Added `canScheduleExactAlarms()` check before scheduling; logs status for debugging.

---

### BUG-007 — Milestone Notification Scheduler Not Connected to UI
**Status:** 🟢 Fixed in v0.5  
**Severity:** Medium (feature incomplete)  
**File:** `notification/MilestoneNotificationScheduler.kt`  
**Description:** `MilestoneNotificationScheduler.kt` is implemented but never called from any screen or ViewModel. Milestone push notifications are not functional despite the file being present.  
**Fix applied:** Milestone notifications are now scheduled when birth date is first entered in `CalculatorViewModel.onBirthDateSelected()`.

---

### BUG-008 — Widget May Display Stale Birthday Data
**Status:** 🟡 Known Limitation  
**Severity:** Low  
**File:** `widget/BirthdayGlanceWidget.kt`  
**Description:** The Glance widget reads saved birthdays from the Room DB and updates when `AppWidgetManager.updateAppWidget` is called. If a user adds or deletes a saved birthday while the widget is on the home screen, the widget will not reflect the change until it is explicitly refreshed (e.g., on the next system-scheduled update or app reopen). There is no real-time listener connecting the Room DB to the Glance widget state.  
**Fix needed:** Emit a `GlanceStateDefinition` update or call `GlanceAppWidgetManager.updateIf` from `RemindersViewModel` after any CRUD operation on saved birthdays.

---

## Sharing & Export

### BUG-009 — `CalendarExport` Fails Silently If No Calendar App Is Installed
**Status:** 🟢 Fixed in v0.5  
**Severity:** Low  
**File:** `domain/CalendarExport.kt`  
**Description:** `CalendarExport` fires an implicit Intent (`CalendarContract.Events.CONTENT_URI`). If no calendar app handles this Intent (e.g., on a device without Google Calendar or any substitute), the app throws an `ActivityNotFoundException` and crashes, or silently does nothing depending on how the Intent is fired.  
**Fix applied:** Added `resolveActivity(packageManager)` check in `launchCalendarIntent()`. Added `isCalendarAppAvailable()` utility method.

---

### BUG-010 — Share Card Bitmap Generation May Run on Main Thread
**Status:** 🟡 Known Issue (performance)  
**Severity:** Low  
**File:** `domain/ShareCardGenerator.kt`  
**Description:** `ShareCardGenerator.generateCard(...)` performs Canvas drawing and file I/O synchronously. Depending on how it is called from the ViewModel, this may block the main thread briefly (noticeable as a UI freeze of 100–300 ms on mid-range devices).  
**Fix needed:** Ensure all callers in `CalculatorViewModel` and `CompatibilityViewModel` call `generateCard` on `Dispatchers.Default` via `withContext(Dispatchers.Default) { ... }` and switch back to `Dispatchers.Main` only to update UI state.

---

### BUG-011 — Share Card Cropped on Some Social Platforms
**Status:** 🟡 Known Limitation  
**Severity:** Low (cosmetic)  
**File:** `domain/ShareCardGenerator.kt`  
**Description:** Share cards are rendered at 900 × 600 pixels (3:2 aspect ratio). WhatsApp preview crops images to roughly 16:9 or square depending on the context; Instagram Stories requires 9:16. The current dimensions optimise for WhatsApp chat preview but may appear letterboxed or cropped on other platforms.  
**Fix needed (optional):** Add an alternative square (900 × 900) or portrait (600 × 1200) export option in the Share Theme Sheet.

---

## Ads

### BUG-012 — Rewarded Ad Unlock Button Not Disabled When Ad Permanently Fails to Load
**Status:** 🟢 Fixed in v0.5  
**Severity:** Low (UX)  
**File:** `ads/AdManager.kt`  
**Description:** If the rewarded ad fails to load (no network, AdMob fill rate issue) and does not recover, the "Unlock Details" button remains visible and tappable. Tapping it shows a generic error (or nothing) because there is no loaded ad to show.  
**Fix applied:** Added `isRewardedAdAvailable()` method to check ad readiness; UI can now conditionally show/hide the unlock button based on ad availability.

---

### BUG-013 — `AdManager` Uses `WeakReference<Activity>` Which May Be Prematurely Collected
**Status:** 🟢 Fixed in v0.7  
**Severity:** Low (intermittent ad failure)  
**File:** `ads/AdManager.kt`  
**Description:** `AdManager` stores the current `Activity` as a `WeakReference` to avoid memory leaks. In some low-memory situations, the GC may collect the weak reference between when the rewarded/interstitial ad is shown and the ad callback fires, causing the ad to not display.  
**Fix applied:** Activity reference is passed directly to `ad.show()` which keeps it strongly referenced during display. The `WeakReference` is cleared immediately after ad dismissal.

---

## UI & UX

### BUG-014 — No Accessibility Labels on Icon-Only Buttons and Share Cards
**Status:** 🟢 Fixed in v0.7  
**Severity:** Medium (accessibility)  
**Files:** `ui/screen/RemindersScreen.kt`, `ui/screen/CompatibilityScreen.kt`, `ui/screen/SettingsScreen.kt`  
**Description:** Several icon-only `IconButton` composables (e.g., info buttons, delete buttons on Reminders screen) and the rendered share card image do not have `contentDescription` set. TalkBack users cannot identify these elements.  
**Fix applied:** Added `contentDescription` to all icon-only buttons:
- Delete button: "Clear all saved birthdays"
- Toggle notification: "Toggle notification"
- Share icon: "Share match card"
- Calendar icon: "Select date"
- Warning icon: "Warning: This will permanently delete all birthdays"

---

### BUG-015 — Settings Are Split Across Two Locations (UX Fragmentation)
**Status:** 🟡 Known Design Issue  
**Severity:** Low  
**Files:** `ui/screen/SettingsScreen.kt`, `ui/screen/RemindersScreen.kt`  
**Description:** Notification time preferences are accessed via a gear icon in the Birthdays tab, while theme and other settings live in a separate Settings tab. This inconsistency confuses users who expect all settings in one place.  
**Fix needed:** Consolidate into a single Settings screen (TASKS.md §2d).

---

### BUG-016 — Date Picker Has No Minimum Year Guard Below API 26
**Status:** 🟢 Fixed in v0.7  
**Severity:** Very Low  
**File:** `ui/screen/CalculatorScreen.kt`  
**Description:** `java.time.LocalDate` supports dates back to year −999 999 999. The date picker does not enforce a minimum year. If a user manually inputs a year before 1900, the astronomical calculations may produce unreliable results because the Meeus ephemeris is calibrated for modern dates.  
**Fix applied:** Added validation in date picker confirm button to ensure selected date year is >= 1900.

---

### BUG-017 — Compare Screen Interstitial Counter Not Shared With `AdManager` Cooldown
**Status:** 🟢 Fixed in v0.7  
**Severity:** Low  
**Files:** `ui/viewmodel/CompareViewModel.kt`, `ads/AdManager.kt`  
**Description:** The logic that decides when to show the interstitial ad (after 2nd comparison, 5-min cooldown) is duplicated between `CompareViewModel` and `AdManager`. If the interstitial is triggered from another screen in the future, the cooldown state won't be synchronised.  
**Fix applied:** CompareViewModel now accepts AdManager dependency and calls `adManager.maybeShowInterstitial()`. Clear comparison count after showing interstitial.

---

## Testing Gaps

### BUG-018 — No Automated Test Coverage
**Status:** 🔴 Open  
**Severity:** High (long-term maintainability risk)  
**Description:** The project has zero unit tests and zero instrumented tests. All logic — including the age calculator, astrological sign lookups, notification scheduling, and Room queries — is tested only manually. Any refactor or schema change can introduce regressions that are not caught until a user reports them.  
**Fix needed:** Add unit tests for all domain calculators and the `BirthdayRepository`; add Compose UI tests for the Calculator screen happy path and validation errors. See [TASKS.md §5a](TASKS.md) for the full test plan.

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
| BUG-003 | Nakshatra and Rashi Approximation | v0.8 |
