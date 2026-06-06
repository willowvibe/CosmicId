# Cosmic ID

Cosmic ID is a native Android app (Kotlin + Jetpack Compose) that calculates your exact age in real-time and enriches it with astrological insights, shareable cards, milestone tracking, saved birthday reminders, and home screen widgets.

Current version: **2.0.0** (Revamp, beta complete 2026-05-22 — production rollout in progress; v2.1 in active development on `feat/smaller-features-v2`).

## Features

### Core
- **3-Step Animated Onboarding** — First-launch flow: date picker → instant zodiac + live counter reveal → optional birth time for exact Nakshatra. Sets tone and maximises activation.
- **Live Age Calculation** — Exact age in years, months, days, hours, minutes, and ticking seconds; updates every second via a StateFlow ticker.
- **Milestone Days** — Automatically flags special days (500, 1 000, 2 000, 3 000, 5 000, 7 000, 10 000, 12 500, 15 000, 20 000, 25 000, 30 000) with dedicated share cards.
- **Age Compare** — Two date-picker comparison showing the exact age difference between two people.
- **Celebrity Birthday Matching** — "You share a birthday with [Name]" card matched from a curated JSON asset (375 celebrities). Most retweetable stat in the app.

### Astrological Insights (Free + Premium)
- **Western Zodiac** — Tropical sun-sign computed from the Sun's ecliptic longitude with cusp detection (⚠) when near a boundary.
- **Western Moon Sign** — Moon's tropical longitude for emotional-nature analysis; distinct from Vedic Rashi.
- **Vedic Rashi** — Sidereal sun sign via Lahiri ayanamsa; labelled *Approximate* until birth time is provided.
- **Rashi Lord** — The ruling planet (graha) of your Vedic Rashi displayed alongside the sign.
- **Nakshatra** — One of 27 lunar mansions based on the Moon's sidereal position; labelled *Approximate* without birth time.
- **Nakshatra Pada** — Quarter (1st–4th) of your nakshatra, refining the lunar mansion influence.
- **Nakshatra Metadata** — Each nakshatra's ruling planet (Dasha lord), presiding deity, Gana (Deva/Manushya/Rakshasa) and symbol, surfaced in the Vedic tab.
- **Navamsa (D-9) Chart** — Vedic divisional chart used in marriage compatibility and spiritual evolution analysis; surfaced as a snapshot card.
- **Guna Milan (Ashtakoot)** — 36-point Indian matchmaking scoring across 8 Kootas (Varna, Vashya, Tara, Yoni, Graha Maitri, Gana, Bhakoot, Nadi); shown in the Compatibility screen when both users provide a birth time.
- **Planetary Aspects** — Western conjunction, sextile, square, trine, and opposition aspects between planets with orbs and applying/separating status.
- **Chinese Zodiac** — 12-year cycle with Lunar New Year awareness.
- **Chinese Stem-Branch** — Full Heavenly Stem + Earthly Branch with Wu Xing element (e.g., "Jia-Chen / Wood-Dragon").
- **Heartbeat Estimate** — Lifetime heartbeats at 72 BPM average.
- **Astrology Info Dialogs** — Educational overlays explaining each astrological system.
- **Premium-only depth:** Vimshottari Dasha (Mahadasha + Antardasha + Pratyantar sub-sub-period), Ba Zi (Four Pillars) / Korean Saju, exact Lagna (with location), full planetary positions + aspects + Navamsa, Nakshatra metadata.

### Social & Sharing
- **Deep-Link Profile Sharing** — Share your cosmic profile as a URL (`agereveal://profile/[data]`). Friends tap to view your profile or auto-populate compatibility.
- **Shareable Cards** — Canvas-rendered 900 × 900 PNG cards in three themes:
  - *Dark Cosmos* (default)
  - *Minimal Light*
  - *Festive India*
- **9:16 Story Cards** — Portrait 1080 × 1920 cards optimized for Instagram Stories, WhatsApp Status, and Snapchat with UI-safe margins.
- **Transparent Green-Screen Overlay** — 1080×1920 transparent-background PNG for TikTok/Reels/YouTube Shorts green-screen import.
- **Google Calendar Export** — One-tap Intent to add any birthday to Google Calendar with app availability check.
- **CSV Export** — Export all saved birthdays as a CSV file via share sheet.
- **Zodiac Compatibility** — Western (angle-based) + Chinese (trine group) compatibility scoring with a shareable headline card; available via deep-link invite or manual input.
- **Life Stats Dashboard** — Fun aggregated stats: full moons witnessed, Fridays the 13th survived, leap years, heartbeats, breaths, meals, words, steps. All individually shareable.
- **Custom Accent Color** — Six preset swatches (Mint, Amber, Pink, Blue, Purple, Emerald) in Settings → Appearance. Applied to share card highlights across themes.
- **Planet Age Hero CTA** — "On Mars, you're only 14" as a prominent shareable card near the top of the profile screen.
- **Daily Cosmic Fortune Push** — Deterministic daily "vibe check" delivered as a push notification at user-set time (default 8AM). Tap opens the app.
- **Cosmic Twins Discovery** — (Planned) Offline matching of users with the same Rashi + Nakshatra combo; generates a "We're Cosmic Twins" dual share card.

### Reminders & Notifications
- **Saved Birthdays** — Store family and friends' birthdays with name + emoji; backed by Room DB.
- **Birthday Notifications** — WorkManager job fires 1 day before each saved birthday at a user-selected hour (7 AM – 9 PM presets).
- **Milestone Notifications** — WorkManager scheduling for upcoming day-milestone reminders (e.g., "You turn 10 000 days old tomorrow 🎉").
- **Cosmic Year Report Notification** — (Planned) On the user's own birthday, a rich notification: "You've lived [X] days — here's your cosmic year ahead" with Mahadasha + fortune summary.
- **Notification Time Customisation** — Settings gear on the Birthdays tab; change reminder hour and all active jobs reschedule automatically.

### Home Screen Widgets
- **2 × 2 Birthday Countdown Widget** — Jetpack Glance-powered countdown to the next upcoming birthday from saved birthdays.
- **4 × 2 Wide Birthday Widget** — Shows the next 3 upcoming birthdays in a horizontal list.
- **2 × 1 Seconds Counter Widget** — Live total seconds alive in large monospace digits. Dark background with accent mint text.
- **4 × 1 Lifespan Progress Widget** — Shows lifespan % completed with color-coded text (teal → amber → rose → red). Configurable target age (30–100) in Settings.
- **2 × 1 Milestone Countdown Widget** — Shows next upcoming milestone (e.g., "10,000 days") with days remaining and progress percentage. Tap opens app.
- **Widget data refresh** — Immediate update on birthday add/update/delete via `notifyWidget()`.

### Monetisation (Freemium)
- **Free tier:** Banner ad on Calculator screen; all core age + basic astrology features.
- **Premium tier (₹49/mo or ₹299/yr):** Remove ads; unlock full astrology depth (Dasha, Ba Zi, exact Lagna, planetary table); priority support.
- **Subscription paywall** — Shown when tapping locked astrology sections, or on 3rd app open if no sub. 7-day free trial.
- **Real AdMob IDs** — Production banner ad unit required for any revenue-generating build.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture (domain / data / ui layers) |
| Navigation | Compose Navigation (tab-based, 4 tabs: My Cosmos, Match, Bdays, Timeline) |
| DI | Dagger Hilt |
| Database | Room (with core library desugaring for `java.time`) |
| Date / Time | `java.time` (native API 26+; desugared for API 21–25) |
| Background Work | WorkManager |
| Home Widget | Jetpack Glance |
| Ads | Google AdMob (Banner only on free tier) |
| Billing | Google Play Billing Library 7+ |
| Astro Maths | Meeus Ch. 25 (Sun) + Ch. 47 (Moon, 60-term) + Ch. 32/33 (planets) + Ch. 22 (IAU 2000B nutation, 50 terms) + Lahiri ayanamsa (cubic polynomial). See [docs/ephemeris-upgrade.md](docs/ephemeris-upgrade.md). |

---

## Project Structure

```
app/src/main/java/com/willowvibe/agereveal/
├── AgeRevealApp.kt            # Application class (Hilt + AdMob init)
├── MainActivity.kt            # Single-activity host + notification permission + deep-link receiver + BillingManager lifecycle
├── analytics/
│   └── AnalyticsManager.kt    # Firebase Analytics wrapper
├── billing/
│   └── BillingManager.kt      # Google Play Billing 7+ (SUBS only); trial parsing + error handling
├── ads/
│   └── AdManager.kt           # Centralised AdMob lifecycle (Banner only on free tier)
├── ai/
│   ├── AiModels.kt            # AI request/response data classes
│   ├── AiService.kt           # AI service abstraction interface
│   ├── AiDiModule.kt          # Hilt module for AI service binding
│   └── NoOpAiServiceImpl.kt   # No-op implementation (placeholder for future AI integration)
├── data/
│   ├── db/                    # Room database, DAO, type converters, migrations
│   ├── model/                 # AgeResult, SavedBirthday, Milestone, GeoLocation, CelebrityMatch data classes
│   ├── preferences/           # UserPreferencesRepository (DataStore)
│   └── repository/            # BirthdayRepository, BadgeRepository (single source of truth)
├── di/
│   └── DatabaseModule.kt      # Hilt module for Room singleton
├── domain/
│   ├── AgeCalculator.kt       # Core age + milestone logic (java.time)
│   ├── AgePercentileCalculator.kt  # Age percentile vs global population
│   ├── AstronomicalCalculator.kt  # Ephemeris: sidereal Sun/Moon positions
│   ├── AspectCalculator.kt      # Planetary aspects (conjunction/sextile/square/trine/opposition) with orbs
│   ├── BaZiCalculator.kt         # Four Pillars of Destiny
│   ├── BadgeDefinitions.kt       # Achievement badge definitions
│   ├── CalendarExport.kt         # Calendar event export intent
│   ├── CelebrityMatchCalculator.kt  # Birthday-to-celebrity matcher from JSON assets
│   ├── DailyFortuneGenerator.kt     # Deterministic daily fortune based on birth date
│   ├── DashaCalculator.kt        # Vimshottari Dasha: Mahadasha + Antardasha + Pratyantar
│   ├── DivisionalChartCalculator.kt  # Navamsa (D-9) and divisional chart framework
│   ├── EphemerisSnapshot.kt      # Current planetary snapshot (with nutation + obliquity)
│   ├── GenerationCalculator.kt   # Generational cohort classification
│   ├── LifeStatsCalculator.kt    # Life statistics dashboard
│   ├── LunarCalendarConverter.kt # Gregorian → Chinese lunar (android.icu)
│   ├── MoonPhaseCalculator.kt    # Moon phase + illumination
│   ├── NakshatraCalculator.kt     # 27 lunar mansions + Pada, with rich metadata (lord/deity/gana/symbol)
│   ├── NakshatraMetadata.kt      # 27-nakshatra lookup table (lord, deity, Gana, symbol, emoji)
│   ├── ParallelUniverseGenerator.kt  # "What if" age in different eras
│   ├── PlanetAgeCalculator.kt       # Age on Mercury, Venus, Mars, Jupiter, Saturn, etc.
│   ├── ProfileDeepLinkGenerator.kt  # Base64-encoded profile URL builder
│   ├── RelationshipType.kt          # Compatibility relationship types
│   ├── RetirementCalculator.kt      # Retirement stats
│   ├── ShareCardGenerator.kt      # Bitmap card renderer + share Intent
│   ├── TimeRemainingCalculator.kt  # Time until target age
│   ├── VedicCompatibilityCalculator.kt  # Guna Milan (Ashtakoot) 8-koota, 36-pt matchmaking
│   ├── ZodiacCalculator.kt        # Western, Vedic Rashi, Chinese Zodiac + Stem-Branch
│   └── ZodiacCompatibilityCalculator.kt  # Western + Chinese composite compatibility
├── notification/
│   ├── BirthdayNotificationScheduler.kt
│   ├── BirthdayReminderWorker.kt
│   ├── DailyFortuneScheduler.kt
│   ├── DailyFortuneWorker.kt
│   ├── MilestoneNotificationScheduler.kt
│   ├── MilestoneReminderWorker.kt
│   ├── YearlyReengagementScheduler.kt
│   └── YearlyReengagementWorker.kt
├── ui/
│   ├── components/                 # Reusable composables (AgeBody, AgeCard, AgeLabel, AgeValue)
│   ├── navigation/AppNavGraph.kt    # NavHost + bottom nav (4 tabs); onboarding gate; deep-link auto-populate
│   ├── screen/                      # Compose screens (12 screens)
│   ├── theme/                       # Color, Theme, Type (Inter font family)
│   └── viewmodel/                   # 8 ViewModels
├── util/
│   ├── BirthdayCsvExporter.kt       # CSV export utility
│   ├── LocaleManager.kt             # Locale helper (system-only since v2.0)
│   └── ReviewHelper.kt             # In-app review prompt
└── widget/
    ├── BirthdayGlanceWidget.kt
    ├── BirthdayWideGlanceWidget.kt
    ├── LifespanProgressGlanceWidget.kt
    ├── MilestoneRingGlanceWidget.kt
    ├── SecondsCounterGlanceWidget.kt
    └── SecondsCounterUpdateWorker.kt  # Periodic widget refresh (15 min)
```

---

## Building the App

1. Clone the repository.
2. Open in **Android Studio Hedgehog** or newer.
3. Sync Gradle.
4. Run on a device or emulator (API 26+ recommended; API 21+ supported via desugaring).

> **AdMob IDs:** All bundled IDs are Google's safe test values — they generate no real revenue. Replace the banner ID before publishing to the Play Store. See [TASKS.md](TASKS.md).

> **Billing:** Test purchases use Google Play's test SKU `android.test.purchased` during development. Switch to real product IDs before release.

> **Development branches:** v2.0 beta features are on `beta-release-v2` (merged 2026-05-22). Active development for v2.1 (Korean Saju + Vedic depth) is on `feat/smaller-features-v2`. The `main` branch tracks the latest stable release. Branch names follow `<type>/<short-description>` (e.g. `feat/`, `fix/`).

---

## Permissions

| Permission | Purpose |
|---|---|
| `POST_NOTIFICATIONS` | Birthday, milestone, daily fortune, and cosmic year reminders (Android 13+, requested at runtime) |
| `INTERNET` | AdMob ad loading, Play Billing server communication |
| `ACCESS_NETWORK_STATE` | Ad network availability check |

---

## Known Limitations

- Nakshatra, Vedic Rashi, and Western Moon Sign calculations are **approximate** when birth time is not provided; the app defaults to 12:00 local time and labels results as *Approximate*.
- The ephemeris is hand-rolled from Meeus *Astronomical Algorithms* (Ch. 25 Sun, Ch. 47 Moon 60-term, Ch. 32/33 planets, Ch. 22 IAU 2000B nutation 50-term). Sun ≈ 0.01°, Moon ≈ 4″, planets ≈ 0.05°–0.5°. See [docs/ephemeris-upgrade.md](docs/ephemeris-upgrade.md) for the accuracy budget and JPL-Horizons reference values.
- Room DB uses explicit `Migration(1, 2)` — schema changes are safe, but `fallbackToDestructiveMigration()` is still present as a fallback.
- Unit tests cover all domain calculators; UI and instrumented tests are pending.

See [BUGS_AND_ISSUES.md](BUGS_AND_ISSUES.md) for the full list of known bugs and edge cases.

---

## Recent Updates

### v2.0.0 (Revamp) — 2026-05-22 (beta complete, production rollout in progress)
- **3-Step Onboarding** — Animated first-launch flow with date picker, instant zodiac reveal, and optional birth time.
- **Freemium Subscription Model** — Premium tier (₹49/mo or ₹299/yr) replaces ad-gated astrology. 7-day free trial with "N days left" chip.
- **Deep-Link Profile Sharing** — Share cosmic profiles as URLs (`agereveal://profile/[data]`); auto-populate compatibility on receive.
- **Celebrity Birthday Matching** — "You share a birthday with [Name]" from 375 curated celebrity entries (8 categories); shown in rotating highlight.
- **Animated MP4 Export** — 5-second ticking-seconds video for Reels/TikTok.
- **WhatsApp Sticker Pack Export** — Direct import of cosmic stickers into WhatsApp.
- **Daily Cosmic Fortune Push** — Delivered as a push notification instead of a silent card.
- **Cosmic Year Report Notification** — Rich birthday notification with Mahadasha + fortune summary.
- **Cosmic Twins Discovery** — Offline match users with identical Rashi + Nakshatra.
- **Progressive Disclosure** — Main screen: hero counter + rotating highlight (milestone / fortune / planet age / celebrity) + CTA.
- **Indian State Dropdown** — Searchable bottom-sheet location picker using state centroids; `isApproximate` flag for UI labelling.
- **Removed:** Rewarded/interstitial ads, Parallel Universe Birth, ASCII Art share, Work-weeks-on-main-screen, custom Hindi toggle, Badges as bottom-nav tab.
- **Renamed tab:** "You" → "My Cosmos".
- **Engine** — Meeus *Astronomical Algorithms* Ch. 25 (Sun), Ch. 47 (Moon, 60-term), Ch. 32/33 (planets), Ch. 22 (IAU 2000B nutation, 50 terms), with cubic-polynomial Lahiri ayanamsa. See [docs/ephemeris-upgrade.md](docs/ephemeris-upgrade.md).
- **Vedic depth** — Nakshatra metadata (lord/deity/Gana/symbol), Pratyantar Dasha, Navamsa (D-9) chart, Guna Milan (Ashtakoot, 36 pts), planetary aspects with orbs.

### v1.0.7 — 2026-05-03 (legacy)
- **Transparent Green-Screen Overlay** — 1080×1920 transparent PNG with outlined text for TikTok/Reels/Shorts green-screen import.
- **Daily Cosmic Fortune** — Deterministic daily fortune based on moon phase, sun sign, and Chinese stem-branch. 80+ curated messages. Cached in SharedPreferences with daily TTL.
- **Retro ASCII Art Share** — 5×5 block-art digits of total seconds alive, copied to clipboard for Discord/Reddit/terminal sharing.

### v1.0 — 2026-04 (legacy)
- Play Store launch with core age calculation, astrology, milestones, reminders, widgets, and sharing.

> **Status note:** v1.0.x releases were the early-access single-system versions. v2.0.0 is the current stable multi-system build (Western + Vedic + Korean Saju). The project is **active** — v2.1 development (Korean Saju Supremacy + Vedic depth UI) is ongoing on `feat/smaller-features-v2`. See [roadmap.md](roadmap.md) for the current 6-mission plan.

---

## Related Docs

- [TASKS.md](TASKS.md) — Pre-release checklist and upcoming implementation tasks
- [roadmap.md](roadmap.md) — Phase-by-phase development plan
- [CONTRIBUTING.md](CONTRIBUTING.md) — How to contribute
- [BUGS_AND_ISSUES.md](BUGS_AND_ISSUES.md) — Known bugs and edge cases
- [docs/ephemeris-upgrade.md](docs/ephemeris-upgrade.md) — Phase 6.5 ephemeris overhaul: Meeus chapter map, accuracy budget, JPL-Horizons reference values
