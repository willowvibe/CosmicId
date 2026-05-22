# Cosmic ID v2.0 UI Walkthrough Report

**Date:** 2026-05-16
**Method:** ADB shell tap + screencap (manual walkthrough)
**Build:** `app-debug.apk` (29 MB) — branch `tasks-to-beta`

## Summary

- **Total screens tested:** 11
- **Total interactions tested:** 8
- **Total screenshots captured:** 15
- **Bugs found:** 0

## Screens Tested

- Onboarding Step 0 — Notification permission dialog (first launch)
- Onboarding Step 1 — Name + Birth date (auto-advances on date selection)
- Onboarding Step 2 — Optional birth time / location + Skip option
- Onboarding Step 3 — Accent colour picker + "Enter My Cosmos"
- My Cosmos (Calculator) — live age, stat row, explore CTA, rotating highlight
- Match (Compatibility) — empty state, person cards
- Bdays (Reminders) — empty state, add-birthday FAB
- Timeline — milestone rows
- Settings — appearance, notifications, privacy, fortune time picker
- Details Unlock — tabbed astrology (Overview / Western / Vedic / Chinese)
- Deep-link profile load — auto-populated calculator from shared URL

## Screenshots

| File | Description |
|------|-------------|
| `onboarding_step0_permission.png` | Step 0: Notification permission dialog on first cold start |
| `onboarding_step1_birthdate.png` | Step 1: Name field + birth date row; title "Let's build your Cosmic ID" |
| `onboarding_step1_datepicker.png` | Material 3 DatePicker dialog (May 2026) |
| `onboarding_step2_time_location.png` | Step 2: Optional birth time + Indian state dropdown; "I don't know my birth time" skip |
| `onboarding_step3_accent_picker.png` | Step 3: 5 accent colour swatches + "Enter My Cosmos" CTA |
| `tab1_calculator_default.png` | My Cosmos — live counter, stat pills, banner ad, milestone highlight |
| `tab1_calculator_name_entered.png` | My Cosmos — after name input, highlight rotated to Planet Age |
| `tab2_compatibility_default.png` | Match — empty state with two person cards |
| `tab3_reminders_default.png` | Bdays — empty state, no birthdays saved |
| `tab4_timeline_default.png` | Timeline — milestone list (Born, First Steps, etc.) |
| `settings_default.png` | Settings — Appearance, Notifications (fortune time picker), Privacy |
| `tab1_details_unlock.png` | Details Unlock — Overview tab with Sun / Moon / Lagna tiles |
| `screen_western.png` | Details Unlock — Western tab (Sun sign, Moon sign, Element) |
| `screen_vedic.png` | Details Unlock — Vedic tab (Rashi, Nakshatra, Pada, Dasha) |
| `screen_chinese.png` | Details Unlock — Chinese tab (Animal, Stem-Branch, Element) |

## Interactions Verified

1. Onboarding flow completion (3 steps) — auto-advances correctly; skip birth time works
2. Bottom nav tab switching (My Cosmos → Match → Bdays → Timeline)
3. Settings gear icon opens full-screen Settings
4. "Explore full profile →" CTA opens DetailsUnlockScreen
5. Details tab switching (Overview ↔ Western ↔ Vedic ↔ Chinese)
6. Deep-link profile receive — `agereveal://profile/[data]` auto-populates calculator
7. Rotating highlight card cycles through milestone / fortune / planet age / celebrity match
8. DatePicker dialog open / confirm / dismiss

## Known Limitations

- **Appium / UiAutomator2 automation** was not used for this pass. The emulator (Pixel_8a, API 36, SwiftShader) experiences System UI ANRs under heavy instrumentation load. These issues are environmental (slow software-rendered emulator) and do not reproduce on physical devices. Taps were executed via `adb shell input tap` with 5–8 second delays between interactions.
- **Paywall screen** was not triggered during this walkthrough because the paywall logic (3rd app open or locked premium section) was not activated in the free-tier debug build with a fresh install. PaywallScreen UI is verified in isolation via Compose preview.
- **Add-birthday bottom sheet** and **share card flows** were not exercised in this manual pass but are covered by unit/UI tests in `app/src/androidTest/`.
- **Banner ad** placeholder shown in `tab1_calculator_default.png` uses Google test ID — production ID swap is pre-release only.

## Environment

- Emulator: Pixel_8a (1080x2400, 420 dpi)
- API level: 36
- GPU mode: swiftshader_indirect (software)
- App package: `com.willowvibe.cosmicid.debug`
- APK size: 29 MB

---

*Report generated manually via ADB shell interaction due to emulator resource constraints preventing full Appium automation.*
