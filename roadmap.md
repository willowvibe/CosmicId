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
- [ ] Light and festive card themes (second + third rewarded ad unlock)
- [ ] Hindi language UI toggle
- [ ] 4×2 wide widget with 3 upcoming birthdays
- [ ] In-app review prompt after user shares their card
- [ ] Remove ads IAP at ₹99 one-time (optional paywall)
