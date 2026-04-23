# AgeReveal Roadmap

---

## Phase 1: Core MVP (Days 1–4)

### Day 1 ✅
- [x] Project setup: Kotlin + Jetpack Compose, AdMob SDK, Room DB, `build.gradle` `java.time` desugaring
- [x] Main screen UI: date picker, age result display, live ticking seconds
- [x] Core date logic: years / months / days / hours / total days calculation
- [x] Banner ad unit integrated with test ads

### Day 2 ✅
- [x] Western Zodiac, Vedic Rashi, Nakshatra, Chinese Zodiac lookup functions
- [x] Milestone days calculator (500, 1 000, 5 000, 10 000, 25 000… days)
- [x] Rewarded ad integration — preload on startup, unlock flow for astrological details
- [x] Compare screen — two date pickers, exact age-difference output
- [x] Interstitial ad with frequency cap (5-minute cooldown, fires after 2nd comparison)

### Day 3 ✅
- [x] Share card: Canvas bitmap rendering, Dark Cosmos theme, watermark
- [x] Share Intent → WhatsApp + general share sheet
- [x] Saved birthdays screen — Room DB, add / delete / edit, WorkManager notification scheduling
- [x] 2 × 2 home screen widget (Jetpack Glance) with countdown to next birthday

### Day 4 — Polish & Launch 🚧
- [ ] Enter/reveal animation when age result appears on Calculator screen
- [ ] Date picker UX polish: scroll snap, haptic feedback on value change
- [ ] Play Store listing assets (icon 512×512, feature graphic 1024×500, screenshots, descriptions)
- [ ] Replace all 4 AdMob test IDs with production IDs
- [ ] Submit for Play Store review

> See [TASKS.md](TASKS.md) for exact file locations and replacement instructions.

---

## Phase 2: Post-Launch Additions 🚀

### Completed ✅
- [x] **Zodiac Compatibility Screen** — 5th tab; enter two birthdays, get Western + Chinese compatibility score with element analysis and shareable headline card
- [x] **Notification Time Customisation** — Settings gear on Birthdays tab; choose reminder hour (7 AM–9 PM presets); stored in `SharedPreferences`, reschedules all active WorkManager jobs

### Pending
- [ ] **Hindi UI toggle** — In-app language switch using `AppCompatDelegate.setApplicationLocales`; requires `values-hi/strings.xml` translation file
- [ ] **4 × 2 home screen widget** — Wider Glance widget showing 3 upcoming birthdays with days-remaining
- [ ] **In-app review prompt** — Trigger `ReviewManager.requestReviewFlow()` after user shares their first card
- [ ] **Remove Ads IAP (₹99)** — One-time purchase via Google Play Billing Library 6+; `AdManager` checks a `SharedPreferences` flag before loading any ad

---

## Phase 3: Depth & Retention 🔭 (In Progress)

**Branch:** `feature/phase-3-depth-retention`
**Version:** 0.4 (pending 0.9.1)

### Completed ✅
- [x] **Google Calendar Export** — `CalendarExport.kt` one-tap Intent to add any birthday to Google Calendar
- [x] **Astrology Explanations** — `AstroInfoDialog.kt` educational dialogs for Western Zodiac, Rashi, Nakshatra, Chinese Zodiac, and Moon Phase
- [x] **Zodiac Compatibility for Saved Birthdays** — Compatibility scores accessible from the Saved Birthdays list; Western + Chinese scoring between any saved birthday and the user's own birth date
- [x] **Dedicated Settings Screen** — Consolidated tab with Appearance (theme), Notifications (default hour, global toggle), Data (clear saved birthdays, CSV export), and About sections

### High Priority (In Progress)
- [ ] **Birth Time Support** — Optional time picker alongside the date picker; pass exact `LocalDateTime` to `AstronomicalCalculator` for precise Nakshatra + Rashi; display *Exact* vs *Approximate* label
- [ ] **Milestone Push Notifications UI** — Wire `MilestoneNotificationScheduler` into `DetailsUnlockScreen`; per-milestone enable/disable toggle; "next milestone in X days" banner on Calculator screen
- [ ] **Life Timeline Visual** — Scrollable `LazyRow`/`LazyColumn` of past (achieved) and future (upcoming) milestones with badges; tappable to share milestone card

### Completed in v0.9.1
- [x] **Settings Screen ViewModel Fix** — Corrected `SettingsScreen` to use `SettingsViewModel` instead of `RemindersViewModel` (BUG-029)
- [x] **Automated Test Coverage** — Added JUnit 4 unit tests for all domain calculators

---

## Phase 4: Scale & Ecosystem 🌐 (Backlog)

- [ ] **Firebase Firestore sync** — Cloud backup for saved birthdays; Google sign-in; conflict resolution by `updatedAt` timestamp
- [ ] **WhatsApp sticker cards** — 512 × 512 transparent-background PNG following WhatsApp sticker pack format
- [ ] **Age trivia / quiz** — "Guess who was born closest to you?" game using saved birthdays
- [ ] **Yearly re-engagement notification** — "You've now lived X days!" notification on the user's own birthday each year
- [ ] **Days-until-retirement calculator** — Configurable target age; remaining working days + % of working life completed
- [ ] **Lock screen widget** — API 33+ `AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE`

---

## Technical Debt Backlog ⚙️

- [ ] **No automated tests** — Add unit tests for all domain calculators, Room DAO, and key ViewModels (see [TASKS.md §5a](TASKS.md))
- [ ] **Room `fallbackToDestructiveMigration`** — Must add explicit `Migration` objects before any schema change to avoid wiping saved birthdays
- [ ] **Interstitial counter is in-memory** — Impression count and cooldown timestamp reset on app kill; persist in `SharedPreferences`
- [ ] **Inter font disabled** — `Type.kt` falls back to system sans-serif; font files not yet included in the repo
- [ ] **Bitmap rendering on main thread** — `ShareCardGenerator` should run entirely on `Dispatchers.Default`
- [ ] **No accessibility labels** — Icon-only buttons and share cards lack `contentDescription` for TalkBack users
- [ ] **CalendarExport no fallback** — If no calendar app is installed the Intent fails silently; need a `resolveActivity` guard

---

## Build Info

| Item | Value |
|---|---|
| Version | 0.9.1 |
| minSdk | 26 (desugaring enables API 21+) |
| targetSdk | 35 |
| compileSdk | 36 |
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Active branch | `feature/phase-3-depth-retention` |
| Build Status | ✅ Passing tests, all known bugs resolved |
