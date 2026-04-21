# AgeReveal

AgeReveal is a native Android app (Kotlin + Jetpack Compose) that calculates your exact age in real-time and enriches it with astrological insights, shareable cards, milestone tracking, saved birthday reminders, and a home screen widget. Current version: **0.9**.

## Features

### Core
- **Live Age Calculation** — Exact age in years, months, days, hours, minutes, and ticking seconds; updates every second via a StateFlow ticker.
- **Milestone Days** — Automatically flags special days (500, 1 000, 2 000, 3 000, 5 000, 7 000, 10 000, 12 500, 15 000, 20 000, 25 000, 30 000) with dedicated share cards.
- **Age Compare** — Two date-picker comparison showing the exact age difference between two people.

### Astrological Insights (Rewarded Ad Unlock)
- **Western Zodiac** — Tropical sun-sign (date-based).
- **Vedic Rashi** — Sidereal sun sign via Lahiri ayanamsa; labelled *Approximate* until birth time is provided.
- **Nakshatra** — One of 27 lunar mansions based on the Moon's sidereal position; labelled *Approximate* without birth time.
- **Chinese Zodiac** — 12-year cycle.
- **Heartbeat Estimate** — Lifetime heartbeats at 72 BPM average.
- **Astrology Info Dialogs** — Educational overlays explaining each astrological system.

### Social & Sharing
- **Shareable Cards** — Canvas-rendered 900 × 600 PNG cards in three themes:
  - *Dark Cosmos* (default)
  - *Minimal Light*
  - *Festive India*
- **Google Calendar Export** — One-tap Intent to add any birthday to Google Calendar.
- **Zodiac Compatibility** — Western (angle-based) + Chinese (trine group) compatibility scoring with a shareable headline card; available on the Compatibility tab and from Saved Birthdays.

### Reminders & Notifications
- **Saved Birthdays** — Store family and friends' birthdays with name + emoji; backed by Room DB.
- **Birthday Notifications** — WorkManager job fires 1 day before each saved birthday at a user-selected hour (7 AM – 9 PM presets).
- **Milestone Notifications** — WorkManager scheduling for upcoming day-milestone reminders (e.g., "You turn 10 000 days old tomorrow 🎉").
- **Notification Time Customisation** — Settings gear on the Birthdays tab; change reminder hour and all active jobs reschedule automatically.

### Home Screen Widget
- **2 × 2 Glance Widget** — Jetpack Glance-powered countdown to the next upcoming birthday from saved birthdays.

### Monetisation (AdMob)
- Banner ad on Calculator screen.
- Rewarded ad to unlock full astrological details.
- **Interstitial ad after the 2nd comparison (5-minute cooldown).**
- **Ad Retry Logic** — Rewarded and Interstitial ads now retry up to 3 times on load failure, improving ad availability.
- **Calendar Export Feedback** — CalendarExport now checks for calendar app availability before launching the Intent.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture (domain / data / ui layers) |
| Navigation | Compose Navigation (tab-based, 5 tabs) |
| DI | Dagger Hilt |
| Database | Room (with core library desugaring for `java.time`) |
| Date / Time | `java.time` (native API 26+; desugared for API 21–25) |
| Background Work | WorkManager |
| Home Widget | Jetpack Glance |
| Ads | Google AdMob (Banner, Rewarded, Interstitial) |
| Astro Maths | Meeus low-precision ephemeris + Lahiri ayanamsa |

---

## Project Structure

```
app/src/main/java/com/willowvibe/agereveal/
├── AgeRevealApp.kt            # Application class (Hilt + AdMob init)
├── MainActivity.kt            # Single-activity host + notification permission
├── ads/
│   └── AdManager.kt          # Centralised AdMob lifecycle (Banner/Rewarded/Interstitial)
├── data/
│   ├── db/                   # Room database, DAO, type converters
│   ├── model/                # AgeResult, SavedBirthday, Milestone data classes
│   └── repository/           # BirthdayRepository (single source of truth)
├── di/
│   └── DatabaseModule.kt     # Hilt module for Room singleton
├── domain/
│   ├── AgeCalculator.kt      # Core age + milestone logic (java.time)
│   ├── AstronomicalCalculator.kt  # Ephemeris: sidereal Sun/Moon positions
│   ├── ZodiacCalculator.kt        # Western, Vedic Rashi, Chinese Zodiac
│   ├── NakshatraCalculator.kt     # 27 lunar mansions
│   ├── ZodiacCompatibilityCalculator.kt  # Compatibility scoring
│   ├── ShareCardGenerator.kt      # Bitmap card renderer + share Intent
│   └── CalendarExport.kt          # Google Calendar add-event Intent
├── notification/
│   ├── BirthdayNotificationScheduler.kt
│   ├── BirthdayReminderWorker.kt
│   └── MilestoneNotificationScheduler.kt
├── ui/
│   ├── navigation/AppNavGraph.kt
│   ├── screen/               # 8 Compose screens
│   ├── theme/                # Color, Theme, Type
│   └── viewmodel/            # CalculatorViewModel, CompareViewModel, CompatibilityViewModel, RemindersViewModel
└── widget/
    ├── BirthdayGlanceWidget.kt
    └── BirthdayGlanceWidgetReceiver.kt
```

---

## Building the App

1. Clone the repository.
2. Open in **Android Studio Hedgehog** or newer.
3. Sync Gradle.
4. Run on a device or emulator (API 26+ recommended; API 21+ supported via desugaring).

> **AdMob IDs:** All bundled IDs are Google's safe test values — they generate no real revenue. Replace all four before publishing to the Play Store. Exact file locations and line numbers are in [TASKS.md](TASKS.md).

> **Development Branch:** Phase 3 features are actively developed on `feature/phase-3-depth-retention`. The `main` branch tracks the latest stable release.

---

## Permissions

| Permission | Purpose |
|---|---|
| `POST_NOTIFICATIONS` | Birthday and milestone reminders (Android 13+, requested at runtime) |
| `INTERNET` | AdMob ad loading |
| `ACCESS_NETWORK_STATE` | Ad network availability check |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Precise birthday notification timing |

---

## Known Limitations

- Nakshatra and Vedic Rashi calculations are **approximate** (date-only) until birth time input is implemented.
- The ephemeris uses low-precision Meeus algorithms (±1° accuracy) — sufficient for sign/nakshatra identification but not for exact degrees.
- Room DB uses `fallbackToDestructiveMigration()` — a schema change **will wipe saved birthdays** until explicit `Migration` objects are added.
- The app has **no automated tests** yet; all domain logic is manually verified.

See [BUGS_AND_ISSUES.md](BUGS_AND_ISSUES.md) for the full list of known bugs and edge cases.

---

## Related Docs

- [TASKS.md](TASKS.md) — Pre-release checklist and upcoming implementation tasks
- [roadmap.md](roadmap.md) — Phase-by-phase development plan
- [CONTRIBUTING.md](CONTRIBUTING.md) — How to contribute
- [BUGS_AND_ISSUES.md](BUGS_AND_ISSUES.md) — Known bugs and edge cases
