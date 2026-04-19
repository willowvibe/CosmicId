# AgeReveal Roadmap

## Phase 1: Core MVP (Days 1-4) 

### Day 1 ✅
- [x] Project setup: Kotlin + Jetpack Compose, AdMob SDK, Room DB, build.gradle java.time desugaring
- [x] Main screen UI: date picker, age result display, live ticking seconds
- [x] Core date logic: years/months/days/hours/total days calculation
- [x] Banner ad unit integrated and showing test ads

### Day 2 ✅
- [x] Zodiac, Rashi, nakshatra, Chinese zodiac lookup functions
- [x] Milestone days calculator (10,000th day etc.)
- [x] Rewarded ad integration — preload on startup, unlock flow
- [x] Compare screen — two date pickers, age difference output
- [x] Interstitial ad with frequency cap logic

### Day 3 ✅
- [x] Share card: Canvas bitmap rendering, dark cosmos theme, watermark
- [x] Share intent → WhatsApp + general share sheet
- [x] Saved birthdays screen — Room DB, add/delete, notification scheduling
- [x] 2×2 home screen widget with AppWidgetProvider

### Day 4 (Pending Polish & Launch) 🚧
- [ ] Polish: animations on result reveal, smooth date picker UX (scroll snap, haptic feedback)
- [ ] Edge cases: leap year birthdays (Feb 29), future date validation, today's date zero-duration display
- [ ] Clean up stale TODO comment in `BirthdayReminderWorker.kt:44` (`ic_cake` drawable already exists)
- [ ] Add Inter font files to `res/font/` and enable custom typography in `Type.kt`
- [ ] Switch all 4 AdMob IDs from test → live (App ID in `build.gradle.kts`, Banner/Rewarded/Interstitial in `AdManager.kt`)
- [ ] Play Store listing: app icon (512×512), feature graphic (1024×500), screenshots (min 2), short + full description
- [ ] Submit for review

> See [TASKS.md](TASKS.md) for exact file locations and replacement instructions.

## Phase 2: Post-Launch Additions 🚀
- [x] **Zodiac Compatibility Screen** — 5th tab; enter two birthdays and get Western + Chinese compatibility score with element analysis and shareable headline
- [x] **Notification Time Customization** — Settings gear on Birthdays tab; choose reminder hour (7 AM – 9 PM presets); stored in SharedPreferences, reschedules all active WorkManager jobs
- [ ] Light and festive card themes (second + third rewarded ad unlock)
- [ ] Hindi language UI toggle
- [ ] 4×2 wide widget with 3 upcoming birthdays
- [ ] In-app review prompt after user shares their card
- [ ] Remove ads IAP at ₹99 one-time (optional paywall)

## Phase 3: Depth & Retention 🔭 (In Progress)

**Branch:** [`feature/phase-3-depth-retention`](https://github.com/willowvibe/AgeReveal/tree/feature/phase-3-depth-retention)

### Completed ✅ (v0.3)
- **Zodiac Compatibility Screen** — 5th tab; enter two birthdays and get Western + Chinese compatibility score with element analysis and shareable headline
- **Notification Time Customization** — Settings gear on Birthdays tab; choose reminder hour (7 AM – 9 PM presets); stored in SharedPreferences, reschedules all active WorkManager jobs
- **Google Calendar Export** — `CalendarExport.kt` utility for one-tap Intent to add birthdays to Google Calendar
- **Astrology Explanations** — `AstroInfoDialog.kt` with educational dialogs for Western Zodiac, Rashi, Nakshatra, Chinese Zodiac, and Moon Phase

### High Priority (In Progress)
- [ ] **Birth Time Support** — Optional time picker alongside date; pass exact time to AstronomicalCalculator for precise Nakshatra + Rashi (Moon moves ~13°/day so time matters); show "Approximate" vs "Exact" label
- [ ] **Milestone Push Notifications** — Schedule WorkManager jobs for upcoming day-milestones (1,000th, 5,000th, 10,000th…); "You turn 10,000 days old tomorrow 🎉" — toggle per-milestone in Details screen
- [ ] **Zodiac Compatibility Share Card** — Generate a shareable bitmap for the compatibility result (reuse ShareCardGenerator with a new COMPATIBILITY theme)
- [ ] **Life Timeline Visual** — Horizontal/vertical scrollable timeline of past + future milestones with achievement badges; gamification to drive return visits
- [ ] **Zodiac Compatibility for Saved Birthdays** — On the Birthdays tab, tap any saved birthday to see compatibility with the user's own birth date (requires storing user's own date persistently)
- [ ] **Settings Screen** — Dedicated settings tab or overflow menu: theme (light/dark/auto), language, notification defaults, clear data

### Build Status
- **APK:** `AgeReveal-0.3-debug.apk` (27.5 MB)
- **Version:** 0.3 (updated from 0.2)

## Ideas Backlog 💡
- Sync saved birthdays across devices via Firebase Firestore (cloud backup)
- WhatsApp-optimised share card with sticker-ready transparent background
- Age quiz / trivia: "Guess who was born closest to you?"
- Yearly re-engagement notification: "You've now lived X days!" on the user's own birthday
- "Days until retirement" calculator with configurable target age
- Widgetkit-style lock screen widget (API 33+)
