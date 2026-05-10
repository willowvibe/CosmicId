# Cosmic ID — PRD

_Last updated: 2026-05-10 — v2.0 Revamp (in progress)_

## Original problem statement

> resolve all the bugs, validate all features with ui/ux working as expected. implement all
> remaining important features. ready for android play store

## Project

Native Android app (Kotlin + Jetpack Compose, Material3, Hilt, Room, WorkManager, Glance,
AdMob, Google Play Billing). Single-activity architecture. minSdk 26, targetSdk 35, compileSdk 36.

## User personas

* **Astrology curious** — wants to know Rashi / Nakshatra / Chinese zodiac precisely. Willing to pay for depth.
* **Milestone chaser** — enjoys "1 000 days alive!", "25 000 days", etc. Shares milestones.
* **Birthday organiser** — needs reminders for family & friends.
* **Casual user** — wants a live "how old am I" ticker with shareable cards.
* **Gen Z flexer** — wants TikTok/Reels content, celebrity matches, viral share formats.

## Core (static) requirements

* Live age to the second
* Western, Vedic, Chinese zodiac (basic free; depth premium)
* Milestone timeline (500 → 30 000 days)
* Saved birthdays with reminders (1 day before, hour-of-day configurable)
* Home screen widget (2×2 + 4×2)
* Zodiac compatibility (user ↔ friend + arbitrary two people)
* Share cards (3 themes, 900×900 square + 1080×1920 story + transparent + MP4 + stickers)
* Bilingual EN + HI (system locale)
* Dark / Light / System theme
* AdMob monetisation (Banner only on free tier)
* Google Play Billing subscription (Premium tier)

## v2.0 Revamp — What's being built

### Monetisation
* **Freemium subscription** — Premium tier (₹49/mo or ₹299/yr) replaces ad-gated astrology.
* **Remove Ads one-time** — ₹199 alternative for users who hate subscriptions.
* **Paywall screen** — Beautiful upsell with 7-day free trial.
* **Banner-only ads** — Interstitial and rewarded ads removed completely.

### Onboarding & Activation
* **3-Step Animated Onboarding** — Date picker → instant zodiac reveal → optional birth time.
* **Progressive Disclosure** — Main screen reduced to hero counter + rotating highlight + CTA.

### Social & Viral
* **Deep-Link Profile Sharing** — `agereveal://profile/[data]` for organic viral loops.
* **Celebrity Birthday Matching** — "You share a birthday with [Name]" from curated data.
* **Animated MP4 Export** — 5-second ticking-seconds video for Reels/TikTok.
* **WhatsApp Sticker Pack** — Direct import of cosmic stickers.
* **Cosmic Twins Discovery** — Offline match by Rashi + Nakshatra.

### Retention
* **Daily Cosmic Fortune Push** — Push notification at user-set time (default 8AM).
* **Cosmic Year Report Notification** — Rich birthday notification with Mahadasha + fortune.

### Brand
* **App rename evaluation** — Nakshatra, CosmAge, BornAt.
* **Icon redesign** — Cosmic/zodiac motif.

## What's implemented (historical)

* 2026-04-23 — **Play Store assets rendered** (`scripts/render_store_assets.py`):
  - `store_listing/icon_512.png` — 512×512 launcher icon (teal→amber gradient disc,
    serif "A" numeral, clock dial anchor ticks, rounded-squircle shape).
  - `store_listing/feature_graphic.png` — 1024×500 banner with headline, feature
    chips (EN / हिन्दी via Noto Sans Devanagari), and prominent clock dial.
  - `store_listing/screenshots/01–07_*.png` — 7 marketing phone mocks (1080×2400)
    covering Hero, Cosmic Profile, Life Timeline, Cosmic Match, Saved Birthdays
    with widget preview, Settings (Hindi + milestone grid), and Share card preview.
  - All validated via AI visual review (icon 9/10, feature 8/10, hero 8/10 — clean
    layout, legible text, correct Devanagari rendering, no overlapping elements).
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

## Next action items (v2.0)

* **Remove rewarded & interstitial ads** and replace with subscription paywall
* **Integrate Google Play Billing Library 6+** with SUBS product type
* **Build 3-step onboarding** (`OnboardingScreen.kt`, `OnboardingViewModel.kt`)
* **Implement deep-link profile sharing** (`ProfileDeepLinkGenerator.kt`, manifest intent-filter)
* **Add celebrity birthday matching** (`CelebrityMatchCalculator.kt`, `assets/celebrities.json`)
* **Wire daily fortune push notification** (`DailyFortuneWorker.kt`, fortune time setting)
* **Refactor Calculator main screen** to progressive disclosure (hero + highlight + CTA)
* **Build animated MP4 export** (`VideoExportWorker.kt`, MediaCodec + MediaMuxer)
* **Build WhatsApp sticker pack** (`WhatsAppStickerProvider.kt`, `stickerpack.json`)
* **Build cosmic year report notification** (`CosmicYearReportWorker.kt`)
* **Build cosmic twins discovery** (`CosmicTwinScreen.kt`, offline matching)
* **Evaluate app rename** and redesign icon
* **Swap AdMob test IDs** for production banner ID

## Dependencies / setup

* JDK 17, Gradle 8.13, Android Gradle Plugin 8.13.2, Kotlin 2.1.0
* Room 2.7.0 with KSP; schemas exported to `app/schemas/` for migration tests
* DataStore 1.1.1, AppCompat 1.7.0, Play Review 2.0.2
* **NEW (v2.0):** Google Play Billing 6+ (`billing-ktx`)

## Build commands (local)

```bash
./gradlew :app:testDebugUnitTest    # run unit tests
./gradlew :app:assembleDebug        # debug APK at app/build/outputs/apk/debug/
./gradlew :app:bundleRelease        # release AAB at app/build/outputs/bundle/release/
```
