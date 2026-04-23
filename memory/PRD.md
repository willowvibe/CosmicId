# AgeReveal — PRD

_Last updated: 2026-04-23 — v1.0 Play Store candidate_

## Original problem statement

> resolve all the bugs, validate all features with ui/ux working as expected. implement all
> remaining important features. ready for android play store

## Project

Native Android app (Kotlin + Jetpack Compose, Material3, Hilt, Room, WorkManager, Glance,
AdMob). Single-activity architecture. minSdk 26, targetSdk 35, compileSdk 36.

## User personas

* **Astrology curious** — wants to know Rashi / Nakshatra / Chinese zodiac precisely.
* **Milestone chaser** — enjoys "1 000 days alive!", "25 000 days", etc.
* **Birthday organiser** — needs reminders for family & friends.
* **Casual user** — wants a live "how old am I" ticker with shareable cards.

## Core (static) requirements

* Live age to the second
* Western, Vedic, Chinese zodiac (opt-in unlock via rewarded ad)
* Milestone timeline (500 → 30 000 days)
* Saved birthdays with reminders (1 day before, hour-of-day configurable)
* Home screen widget (2×2 + 4×2)
* Zodiac compatibility (user ↔ friend + arbitrary two people)
* Share cards (3 themes, 900×900 square)
* Bilingual EN + HI
* Dark / Light / System theme
* AdMob monetisation (Banner, Rewarded, Interstitial)

## What's implemented (this run)

* 2026-04-23 — **Features a-e from the Play Store roadmap**:
  - [a] Birth Time Support: TimePicker in Calculator, `SavedBirthday.birthTime` + Room
    Migration 1→2, threaded through `AgeCalculator` / `ZodiacCalculator` /
    `NakshatraCalculator`.
  - [b] Milestone Push UI: per-target toggles in DetailsUnlockScreen + Settings,
    "Next milestone in Nd" chip on Calculator (shown when ≤30 days away),
    `MilestoneNotificationScheduler.scheduleSingle/cancelSingle`.
  - [c] Hindi UI: `values/strings.xml` + `values-hi/strings.xml`, `xml/locales_config.xml`,
    `LocaleManager` (AppCompatDelegate), Language picker in Settings.
  - [d] 4×2 wide widget: `BirthdayWideGlanceWidget` showing 3 upcoming birthdays, receiver
    registered in manifest, repository refreshes both widgets.
  - [e] In-app review: `ReviewHelper` using Google Play `review-ktx`, triggered after first share.
* **Settings overhaul**: theme selector now persists via DataStore; language picker;
  global notifications toggle (off cancels all WorkManager jobs); CSV export of
  birthdays via share sheet; privacy policy link.
* **Release signing scaffold**: `keystore.properties` loaded at config time,
  release build falls back to debug keystore for local testing.
* **Play Store listing**: `store_listing/` has short/full descriptions, privacy
  policy, release notes, submission checklist, screenshot guide.
* **Bug fixes**:
  - `yearSafeBirthday` now correctly returns Mar 1 in non-leap years (was silently
    returning Feb 28 — affected `AgeCalculator`, `BirthdayRepository`,
    `BirthdayNotificationScheduler`).
  - "Days until next birthday" now returns 0 on the birthday itself (previously 365).
  - Theme selector now actually switches theme (was no-op; missing appcompat dep and
    pref persistence).
  - `SettingsScreen` consolidated (was using a stale `AppCompatDelegate` call).
  - Room DB: added explicit Migration 1→2 (kept `fallbackToDestructiveMigration`
    removed as of v0.5; added birthTime column without data loss).
  - Lint `FullBackupContent` error in `data_extraction_rules.xml` fixed.
* **Unit tests**: 64 tests pass (`AgeCalculatorTest` × 16, `AgeCalculatorBirthTimeTest` × 5,
  `ZodiacCalculatorTest` × 24, `NakshatraCalculatorTest` × 6,
  `ZodiacCompatibilityCalculatorTest` × 13). Release AAB (11 MB) builds successfully.

## Next action items

* **Replace 4 AdMob test IDs** with real production IDs:
  - `app/build.gradle.kts` → `admobAppId` placeholder
  - `ads/AdManager.kt` lines 37-39 → BANNER / REWARDED / INTERSTITIAL unit IDs
* **Generate real keystore** and fill in `keystore.properties`.
* **Produce Play Store assets**: 512×512 icon PNG, 1024×500 feature graphic, 2-8 phone
  screenshots (see `store_listing/screenshots/README.md`).
* **Host privacy policy** at `https://willowvibe.com/agereveal/privacy` (or edit the URL
  in `SettingsScreen.kt`) using `store_listing/privacy_policy.md` as the source.
* **Sanity-check** on a real device: Hindi locale switch, milestone notifications firing
  with the per-target toggle, share-then-review prompt, 4×2 widget resize.

## Prioritised backlog (P2, future versions)

* Remove Ads IAP (₹99) via Google Play Billing Library
* Firebase Firestore sync for saved birthdays (opt-in Google sign-in)
* WhatsApp sticker pack (512×512 transparent PNGs)
* Yearly "you've now lived X days!" re-engagement notification
* Lock screen widget (API 33+)
* iOS port (Flutter or React Native with WidgetKit)

## Dependencies / setup

* JDK 17, Gradle 8.13, Android Gradle Plugin 8.13.2, Kotlin 2.1.0
* Room 2.7.0 with KSP; schemas exported to `app/schemas/` for migration tests
* DataStore 1.1.1, AppCompat 1.7.0, Play Review 2.0.2

## Build commands (local)

```bash
./gradlew :app:testDebugUnitTest    # run unit tests
./gradlew :app:assembleDebug        # debug APK at app/build/outputs/apk/debug/
./gradlew :app:bundleRelease        # release AAB at app/build/outputs/bundle/release/
```
