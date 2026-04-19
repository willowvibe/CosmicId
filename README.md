# AgeReveal

AgeReveal is a native Android app that calculates your exact age in years, months, days, hours, and seconds. It also provides astrological insights (Zodiac, Rashi, Nakshatra) and lets you share your age as a beautifully generated card.

## Features
- **Live Age Calculation:** Exact age updated in real-time.
- **Astrological Insights:** Western Zodiac, Vedic Rashi, Nakshatra, and Chinese Zodiac.
- **Milestones Calculator:** Discover special days like your 10,000th or 25,000th day alive.
- **Shareable Cards:** High-quality image share cards for WhatsApp with multiple themes.
- **Compare:** Compare ages with friends to see who is older.
- **Reminders & Widget:** Save birthdays, get notifications, and see a countdown on your home screen.

## Phase 3 Features (In Progress)
- **Google Calendar Export:** One-tap intent to add birthdays to Google Calendar.
- **Astrology Explanations:** Info dialogs providing educational content on astrology terms.
- **Life Timeline Visual:** Scrollable timeline of milestones with achievement badges (coming soon).
- **Birth Time Support:** Optional time picker for precise Nakshatra + Rashi calculations (coming soon).
- **Milestone Push Notifications:** Schedule WorkManager jobs for upcoming day-milestones (built-in).
- **Zodiac Compatibility for Saved Birthdays:** Display compatibility scores when viewing saved birthdays.

## Tech Stack
- **Kotlin & Jetpack Compose**
- **Architecture:** MVVM, Clean Architecture
- **Dependency Injection:** Dagger Hilt
- **Local Storage:** Room Database
- **Date/Time:** `java.time` (with API desugaring for older Android versions)
- **Background Work:** WorkManager
- **Monetization:** Google AdMob (Banner, Interstitial, Rewarded)
- **Widgets:** Built using Compose / RemoteViews

## Building the App
1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle and run on an Android device or emulator (API 26+ recommended, though API 21+ supported via desugaring).

> **Note:** The AdMob App ID and Ad Unit IDs in the codebase are currently set to Google's safe test IDs. Replace them with real IDs from your AdMob account before publishing. See [TASKS.md](TASKS.md) for the full pre-release checklist including file locations and line numbers.
>
> **Development Status:** Phase 3 features are actively being developed in the `feature/phase-3-depth-retention` branch. For the latest additions including Calendar Export, Astrology Explanations, and Compatibility for Saved Birthdays, check out that branch.
