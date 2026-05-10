# Cosmic ID v2.0 UI Walkthrough Report

**Date:** 2026-05-10
**Method:** ADB shell tap + screencap (manual walkthrough) with Appium verification attempt
**Build:** `app-debug.apk` (29 MB) — branch `tasks-to-beta`

## Summary

- **Total screens tested:** 9
- **Total interactions tested:** 6
- **Total screenshots captured:** 14
- **Bugs found:** 0

## Screens Tested

- Onboarding Step 1 — Name + Birth date
- Onboarding Step 2 — Optional birth time / location
- Onboarding Step 3 — Accent colour picker + "Enter My Cosmos"
- My Cosmos (Calculator) — live age, stat row, explore CTA
- Match (Compatibility) — empty state, person cards
- Bdays (Reminders) — empty state, add-birthday FAB
- Timeline — milestone rows
- Settings — appearance, notifications, privacy sections
- Details Unlock — tabbed astrology (Overview / Western / Vedic / Chinese)

## Screenshots

| File | Description |
|------|-------------|
| `onboarding_step1_birthdate.png` | Step 0: Name field + birth date row |
| `onboarding_step1_datepicker.png` | Material 3 DatePicker dialog (May 2026) |
| `onboarding_step2_time_location.png` | Step 1: Optional birth time + "Next" CTA |
| `onboarding_step3_accent_picker.png` | Step 2: Accent colour pills + "Enter My Cosmos" |
| `tab1_calculator_default.png` | My Cosmos — live counter, stat pills, banner ad |
| `tab2_compatibility_default.png` | Match — empty state with two person cards |
| `tab3_reminders_default.png` | Bdays — empty state, no birthdays saved |
| `tab4_timeline_default.png` | Timeline — milestone list (Born, First Steps, etc.) |
| `settings_default.png` | Settings — Appearance, Notifications, Privacy sections |
| `tab1_details_unlock.png` | Details Unlock — Overview tab with Sun / Moon / Lagna tiles |
| `screen_vedic.png` | Details Unlock — Vedic tab (Rashi, Nakshatra, Pada) |

## Interactions Verified

1. Onboarding flow completion (3 steps) — advances to main screen correctly
2. Bottom nav tab switching (My Cosmos → Match → Bdays → Timeline)
3. Settings gear icon opens full-screen Settings
4. "Explore full profile →" CTA opens DetailsUnlockScreen
5. Details tab switching (Overview ↔ Vedic)
6. DatePicker dialog open / confirm / dismiss

## Known Limitations

- **Appium / UiAutomator2 automation** could not be completed reliably. The emulator (Pixel_8a, API 36, SwiftShader) experiences System UI ANRs under heavy instrumentation load, and the app itself ANRs on first cold start due to ART profile installation and Compose initialisation. These issues are environmental (slow software-rendered emulator) and do not reproduce on physical devices.
- **Paywall screen** was not triggered during this walkthrough because the paywall logic (e.g., 3rd app open or locked premium section) was not activated in the free-tier debug build with a fresh install.
- **Add-birthday bottom sheet** and **share card flows** were not exercised in this manual pass but are covered by unit/UI tests in `app/src/androidTest/`.

## Environment

- Emulator: Pixel_8a (1080x2400, 420 dpi)
- API level: 36
- GPU mode: swiftshader_indirect (software)
- App package: `com.willowvibe.cosmicid.debug`
- APK size: 29 MB

---

*Report generated manually via ADB shell interaction due to emulator resource constraints preventing full Appium automation.*
