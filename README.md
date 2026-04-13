# AgeReveal

AgeReveal is a native Android app that calculates your exact age in years, months, days, hours, and seconds. It also provides astrological insights (Zodiac, Rashi, Nakshatra) and lets you share your age as a beautifully generated card.

## Features
- **Live Age Calculation:** Exact age updated in real-time.
- **Astrological Insights:** Western Zodiac, Vedic Rashi, Nakshatra, and Chinese Zodiac.
- **Milestones Calculator:** Discover special days like your 10,000th or 25,000th day alive.
- **Shareable Cards:** High-quality image share cards for WhatsApp with multiple themes.
- **Compare:** Compare ages with friends to see who is older.
- **Reminders & Widget:** Save birthdays, get notifications, and see a countdown on your home screen.

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

> **Note:** The AdMob App ID and Ad Unit IDs in the codebase are currently set to test IDs.
