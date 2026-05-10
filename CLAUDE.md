# CLAUDE.md — Cosmic ID

Quick-start guide for Claude Code sessions on the Cosmic ID (formerly AgeReveal) Android project.

---

## What This Is

Cosmic ID is a native Android app (Kotlin + Jetpack Compose) that calculates your exact age in real time and enriches it with astrological insights, shareable cards, milestone tracking, saved birthday reminders, and home screen widgets. Currently **v2.0**.

**Display name:** Cosmic ID  
**Package ID:** `com.willowvibe.cosmicid` (namespace remains `com.willowvibe.agereveal` for source compatibility)  
**Min SDK:** 26 (API 21–25 via core library desugaring)  
**Compile SDK:** 36  
**Supported locales:** en, hi, ta, te, kn, ko, vi, zh-Hans (system-level per-app language on Android 13+)

---

## Architecture

| Layer | Contents |
|-------|----------|
| **UI** (`ui/screen/`, `ui/viewmodel/`) | Compose screens + MVVM ViewModels (`StateFlow<UiState>`) |
| **Navigation** (`ui/navigation/`) | Compose Navigation with single-activity + bottom nav (4 tabs) |
| **Domain** (`domain/`) | Pure Kotlin business logic — no Android framework imports |
| **Data** (`data/db/`, `data/repository/`, `data/model/`) | Room DB, DAOs, repository (single source of truth) |
| **DI** (`di/`) | Hilt modules (`DatabaseModule.kt`) |
| **Billing** (`billing/`) | Google Play Billing 7+ wrapper (`BillingManager.kt`) |
| **Ads** (`ads/`) | Banner-only AdManager (rewarded + interstitial removed in v2.0) |
| **Notifications** (`notification/`) | WorkManager workers + schedulers |
| **Widget** (`widget/`) | Jetpack Glance 2×2 + wide widgets |

**Key rules:**
- Domain layer must stay Android-framework-free.
- ViewModels never hold `Context`; inject `ApplicationContext` via Hilt if needed.
- Screens never talk to DAOs directly — always go through Repository.
- No manual language toggle — Android 13+ system per-app locale only.

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Navigation:** Compose Navigation (`NavHost` + `NavigationBar`)
- **DI:** Dagger Hilt
- **DB:** Room (KSP codegen; schema in `app/schemas/`)
- **Date/Time:** `java.time` (desugared for API < 26)
- **Background:** WorkManager
- **Widget:** Jetpack Glance
- **Ads:** Google AdMob (banner only on free tier; test IDs bundled)
- **Billing:** Google Play Billing Library 7+ (subscriptions: `premium_monthly`, `premium_yearly`)
- **Astro maths:** Meeus low-precision ephemeris + Lahiri ayanamsa

---

## Build & Test

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lint

# Install debug build on connected device/emulator
./gradlew :app:installDebug
```

**Required:** JDK 17 (bundled with Android Studio Hedgehog+)

---

## Project Map

### Entry Points
- `MainActivity.kt` — Single-activity host, notification permission, edge-to-edge, BillingManager lifecycle, deep-link receiver
- `AgeRevealApp.kt` — Application class, Hilt + AdMob init (banner-only)
- `ui/navigation/AppNavGraph.kt` — NavHost + bottom bar wiring; onboarding gate; deep-link auto-populate

### Screens (Composables)
- `OnboardingScreen.kt` — 3-step first-launch flow: Name+Birth Date → Optional Birth Time → Accent Picker
- `CalculatorScreen.kt` — Live age, hero counter, rotating highlight, "Explore full profile →" CTA, banner ad (free tier)
- `CompatibilityScreen.kt` — Zodiac compatibility; two-person comparison; deep-link auto-fill
- `RemindersScreen.kt` — Saved birthdays list, AddBirthday bottom sheet, FAB
- `LifeTimelineScreen.kt` — Milestone timeline
- `DetailsUnlockScreen.kt` — Astrology details (basic free; depth premium-gated)
- `PaywallScreen.kt` — Subscription tiers (monthly/yearly); restore purchases CTA
- `SettingsScreen.kt` — Theme, accent color, notifications, export CSV, clear all, language → system settings

### ViewModels
- `MainViewModel` — App-wide onboarding gate (`hasCompletedOnboarding`)
- `CalculatorViewModel` — Age calc, ephemeris, share card generation, premium status
- `CompatibilityViewModel` — Two-person comparison + compatibility scoring
- `RemindersViewModel` — Birthday CRUD + bottom sheet state
- `SettingsViewModel` — Theme, notification prefs, clear/export, restore purchases
- `PaywallViewModel` — BillingManager state exposure for PaywallScreen

### Domain Logic (Pure Kotlin)
- `AgeCalculator.kt` — Core age + milestone logic
- `AstronomicalCalculator.kt` — Sun/Moon sidereal positions
- `ZodiacCalculator.kt` — Western, Vedic Rashi, Chinese Zodiac + Stem-Branch
- `NakshatraCalculator.kt` — 27 lunar mansions + Pada
- `ZodiacCompatibilityCalculator.kt` — Western compatibility scoring
- `ShareCardGenerator.kt` — Bitmap card renderer
- `ProfileDeepLinkGenerator.kt` — `agereveal://profile/[data]` encode/decode

### Billing
- `BillingManager.kt` — Google Play Billing 7+ wrapper; SKU `premium_monthly` (₹49) + `premium_yearly` (₹299); 7-day free trial; purchase acknowledge + DataStore sync

---

## Navigation Tabs

| Tab Label | Route | Screen | Icon |
|-----------|-------|--------|------|
| My Cosmos | `calculator` | CalculatorScreen | Calculate |
| Match | `compatibility` | CompatibilityScreen | Favorite |
| Bdays | `reminders` | RemindersScreen | Cake |
| Timeline | `timeline` | LifeTimelineScreen | Cake |

Settings opens as a full-screen destination (no bottom bar change) from Calculator or Reminders.
Onboarding is the start destination on first launch.

---

## Important Conventions

- **Stateless composables:** Pass state + callbacks down; keep state in ViewModel.
- **Comments:** Only when the *why* is non-obvious. No restating the obvious.
- **Feb 29 helper:** `yearSafeBirthday()` is the canonical utility — reuse it everywhere.
- **Font:** Defaults to system sans-serif. Optional Inter font (see CONTRIBUTING.md § Custom Inter Typography).
- **End files with newline.**
- **Run `./gradlew lint` before PR.**

---

## Testing

| Type | Location | Runner |
|------|----------|--------|
| Unit tests | `app/src/test/java/.../domain/` | JUnit (no Android framework) |
| UI tests (Compose) | `app/src/androidTest/java/.../ui/` | AndroidJUnit + Hilt (`HiltTestRunner`) |
| DB migration tests | `app/src/androidTest/java/.../db/` | AndroidJUnit |
| Appium E2E | `screenshots/walkthrough.py` | Appium + UiAutomator2 |

**Test runner in `build.gradle.kts`:** `com.willowvibe.agereveal.HiltTestRunner`

---

## Known Gotchas

### Compose + UiAutomator2 (Appium)
- `OutlinedTextField` exposed as `android.widget.EditText` **only when focused**.
- Bottom-nav `TextView` labels are **not clickable** — parent `View` is. Use XPath ancestor selector.
- Dynamic `contentDescription` strings (birth date row, precision chips) change with app state. Use `descriptionContains()` with partial matches.
- Settings options (Dark, Export CSV, Clear All) are below the fold inside a `ScrollView` — use `UiScrollable.scrollIntoView()`.

### System Interference
- **Export CSV** opens a system share chooser that blocks automation. Scroll-to and screenshot only; do not tap in tests.
- **Per-app language** changes app locale and breaks subsequent selectors. Isolate locale tests.
- **Notification shade** can appear on launch after `pm clear` — detect via `com.android.systemui` in page source and press Back.

### Ads
- All bundled AdMob IDs are Google test values — no revenue, no account needed.
- Replace before Play Store release (see `TASKS.md §1`).
- Only banner ads remain; rewarded + interstitial removed in v2.0.

### Billing
- Test purchases use Google Play's test flow during development.
- Always call `BillingManager.startConnection()` in `MainActivity.onCreate()` and `endConnection()` in `onDestroy()`.

---

## Related Docs

| File | Purpose |
|------|---------|
| `README.md` | Full feature list, tech stack, project structure |
| `CONTRIBUTING.md` | PR process, code style, setup steps |
| `TASKS.md` | Pre-release checklist, upcoming tasks, AdMob ID swap guide |
| `roadmap.md` | Phase-by-phase development plan |
| `BUGS_AND_ISSUES.md` | Tracked bugs and edge cases |
| `screenshots/walkthrough.py` | Appium E2E automation script |
| `screenshots/REPORT.md` | Generated UI walkthrough report |
| `docs/superpowers/plans/` | Superpowers implementation plans |
| `store_listing/PLAYSTORE_CHECKLIST.md` | Play Store submission assets |
| `store_listing/privacy_policy.md` | Privacy policy for store listing |
| `store_listing/release_notes_v2.0.md` | v2.0 release notes |

---

## Current Session Context

- Branch: `feat/cosmic-id-polish` (ahead of `origin/main`)
- Recently added: v2.0 revamp — freemium model, onboarding, paywall, deep-links, 8-language localization
- Package ID: `com.willowvibe.cosmicid` (applicationId changed; namespace kept for source compatibility)
- Display name: Cosmic ID
- Build compiles and all unit tests pass
