# Screenshots — Play Store requirements

Play Store requires a minimum of 2 phone screenshots (max 8). Portrait recommended.

## Current set (7 screenshots, 1080×2400)

| # | File | What's shown | Feature highlights |
|---|------|--------------|---------------------|
| 01 | `01_hero.png` | Calculator — hero shot | Live seconds, age in yrs/mo/days, next birthday |
| 02 | `02_profile.png` | Cosmic profile unlocked | Western + Vedic + Chinese + Nakshatra + Dasha + Ba Zi + Planets |
| 03 | `03_timeline.png` | Life Timeline | Past/future milestones, progress bar, share buttons |
| 04 | `04_match.png` | Cosmic Match | Two-person compatibility with Western + Chinese scores |
| 05 | `05_birthdays.png` | Saved birthdays + widgets | Birthday list + 4×2 widget preview |
| 06 | `06_settings.png` | Settings | Theme, notification time, accent color, milestone toggles |
| 07 | `07_share.png` | Share card preview | Dark Cosmos square card + story card + transparent overlay |

## Recommended replacements for v2.0

Replace or add screenshots that showcase the revamp features:

1. **Onboarding Step 2** — "You're a Leo" zodiac reveal with animated counter
2. **Progressive Disclosure main screen** — Hero counter + rotating highlight + "Explore full profile →"
3. **Premium Paywall** — "Unlock Your Full Cosmic Profile" with pricing and free trial CTA
4. **Celebrity Birthday Match** — "You share a birthday with Albert Einstein"
5. **Tabbed Details Screen** — Overview | Western | Vedic | Chinese tabs
6. **Planet Age Hero CTA** — "On Mars, you're only 14" prominent share card
7. **MP4 Export preview** — Video export option in share sheet
8. **WhatsApp Sticker** — Sticker pack import preview

## How to capture

1. Run on a Pixel emulator at 1080×2400 for crisp density.
2. Use Android Studio's built-in screenshot: Logcat → camera icon.
3. Save as PNG (no JPEG).
4. Name them `01_hero.png`, `02_profile.png`, etc. so Play Console displays them in order.
5. Overwrite existing files in this directory.

## v2.0-specific capture notes

- **Onboarding:** Clear app data (`pm clear com.willowvibe.agereveal`) to trigger first-launch flow.
- **Paywall:** Use a fresh install (3rd open) or tap locked astrology section.
- **Premium features:** Use Google Play test purchase to unlock premium-only content for screenshots.
- **Banner ad:** Only visible on free tier; ensure test account has no active subscription.
