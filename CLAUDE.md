# CLAUDE.md — Cosmic ID

Quick-start guide for Claude Code sessions on the Cosmic ID (formerly AgeReveal) Android project.

---

## What This Is

Cosmic ID is a native Android app (Kotlin + Jetpack Compose) that calculates your exact age in real time and enriches it with astrological insights across **Western + Vedic + Korean Saju (사주)**, shareable cards, milestone tracking, saved birthday reminders, and home screen widgets. Currently **v2.0**.

**Display name:** Cosmic ID  
**Package ID:** `com.willowvibe.cosmicid` (namespace remains `com.willowvibe.agereveal` for source compatibility)  
**Min SDK:** 26 (API 21–25 via core library desugaring)  
**Compile SDK:** 36  
**Supported locales:** en, hi, ta, te, kn, ko, vi, zh-Hans (system-level per-app language on Android 13+)

**Strategic positioning (v2.1+):** the only tri-system app in the Play Store. **Korean Saju** is the East-Asian pillar (not Chinese Ba Zi) — targeted at K-drama / K-pop / K-diaspora audiences with 천간·지지 in Hangul, 대운 (Daeun) luck periods, 오행 balance chart, and 용신 (Yongshin) suggestion. `BaZiCalculator.kt` remains for the Day/Hour pillar data path but is rendered through the Korean Saju UI; a separate **`SajuKoreanCalculator.kt`** owns the Korean naming layer + Daeun logic. See `roadmap.md` Mission 7 — Korean Saju Supremacy.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **UI** (`ui/screen/`, `ui/viewmodel/`, `ui/components/`) | Compose screens + MVVM ViewModels (`StateFlow<UiState>`) + reusable composables |
| **Navigation** (`ui/navigation/`) | Compose Navigation with single-activity + bottom nav (4 tabs) |
| **Theme** (`ui/theme/`) | Material 3 theme, colors, typography (Inter font family) |
| **Domain** (`domain/`) | Business logic — some files have Android imports (ShareCardGenerator, CalendarExport, CelebrityMatchCalculator) |
| **Data** (`data/db/`, `data/repository/`, `data/model/`, `data/preferences/`) | Room DB, DAOs, repository (single source of truth), DataStore preferences |
| **DI** (`di/`) | Hilt modules (`DatabaseModule.kt`) |
| **Billing** (`billing/`) | Google Play Billing 7+ wrapper (`BillingManager.kt`) |
| **Ads** (`ads/`) | Banner-only AdManager (rewarded + interstitial removed in v2.0) |
| **Notifications** (`notification/`) | WorkManager workers + schedulers |
| **Widget** (`widget/`) | Jetpack Glance widgets: `BirthdayGlanceWidget`, `WideGlanceWidget`, `SecondsCounterGlanceWidget`, `LifespanGlanceWidget`, `MilestoneRingGlanceWidget`, `BirthdayCountGlanceWidget` |
| **Analytics** (`analytics/`) | Firebase Analytics (`AnalyticsManager.kt`) |
| **AI** (`ai/`) | AI service abstraction layer (`AiService`, `NoOpAiServiceImpl`, Hilt module) |
| **Utilities** (`util/`) | CSV export, locale manager, review helper |

**Key rules:**
- Domain layer should minimize Android framework imports (known exceptions: `ShareCardGenerator`, `CalendarExport`, `CelebrityMatchCalculator`, `LunarCalendarConverter`, `ProfileDeepLinkGenerator`, `BadgeDefinitions`).
- ViewModels must not hold `Context` directly; all persistence goes through `UserPreferencesRepository` (DataStore with SharedPreferences mirroring for widget/worker access).
- Screens never talk to DAOs directly — always go through Repository.
- No manual language toggle — Android 13+ system per-app locale only.
- Inter font is compiled from resources and used as default (not optional).

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
- **Billing:** Google Play Billing Library 7+ (subscriptions: `premium_monthly` ₹49, `premium_yearly` ₹299; 7-day trial)
- **Themes:** Material 3 + premium theme packs (Vaporwave, Cottagecore, Y2K, Dark Academia, Cyberpunk)
- **Astro maths:** Meeus low-precision ephemeris + Lahiri ayanamsa

---

## Build & Test

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Run unit tests (JVM)
./gradlew testDebugUnitTest

# Run UI tests (instrumented)
./gradlew connectedAndroidTest

# Run all tests
./gradlew check

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
- `CalculatorScreen.kt` — Live age, hero counter, rotating highlight, "Explore full profile →" CTA, banner ad (free tier), refresh button
- `CompatibilityScreen.kt` — Zodiac compatibility; two-person comparison; deep-link auto-fill
- `RemindersScreen.kt` — Saved birthdays list, AddBirthday bottom sheet, FAB
- `LifeTimelineScreen.kt` — Milestone timeline
- `DetailsUnlockScreen.kt` — Tabbed astrology details (Overview | Western | Vedic | Korean Saju); premium-gated deep content
- `PaywallScreen.kt` — Subscription tiers (monthly/yearly); restore purchases CTA
- `SettingsScreen.kt` — Theme, accent color, notifications, export CSV, clear all, language → system settings
- `BadgeScreen.kt` — Unlocked achievement badges
- `ShareThemeSheet.kt` — Share card theme picker bottom sheet
- `AstroInfoDialog.kt` — Astrology info dialog
- `CompareScreen.kt` — Age comparison (accessible from Compatibility; not in bottom nav)

### ViewModels
- `MainViewModel` — App-wide onboarding gate (`hasCompletedOnboarding`)
- `CalculatorViewModel` — Age calc, ephemeris, share card generation, premium status, fortune caching
- `CompatibilityViewModel` — Two-person comparison + compatibility scoring
- `RemindersViewModel` — Birthday CRUD + bottom sheet state
- `SettingsViewModel` — Theme, notification prefs, clear/export, restore purchases
- `PaywallViewModel` — BillingManager state exposure for PaywallScreen
- `BadgeViewModel` — Badge unlock state and progress
- `CompareViewModel` — Age comparison logic (used by CompareScreen)

### Domain Logic
- `AgeCalculator.kt` — Core age + milestone logic
- `AgePercentileCalculator.kt` — Age percentile vs global population
- `AstronomicalCalculator.kt` — Sun/Moon sidereal positions
- `BaZiCalculator.kt` — Four Pillars of Destiny (Ba Zi) — Day/Hour pillar math; consumed by `SajuKoreanCalculator.kt` for the Korean Saju UI layer
- `SajuKoreanCalculator.kt` — **Korean Saju (사주) layer** — 천간/지지 in Hangul, 대운 (Daeun 10-year luck periods), 오행 balance, 용신 (Yongshin) rule-based suggestion. Distinct from `BaZiCalculator.kt`; do not collapse.
- `BadgeDefinitions.kt` — Achievement badge definitions
- `CalendarExport.kt` — Calendar event export
- `CelebrityMatchCalculator.kt` — Load `celebrities.json`, match by month+day, return top N matches
- `DailyFortuneGenerator.kt` — Deterministic daily fortune/vibe check
- `DashaCalculator.kt` — Vimshottari Dasha periods
- `EphemerisSnapshot.kt` — Current planetary snapshot
- `GenerationCalculator.kt` — Generational cohort (Gen Z, Millennial, etc.)
- `LifeStatsCalculator.kt` — Life statistics dashboard
- `LunarCalendarConverter.kt` — Gregorian to Chinese lunar calendar (uses android.icu); also reused for Korean lunar/sexagenary calculations
- `MoonPhaseCalculator.kt` — Moon phase and illumination
- `NakshatraCalculator.kt` — 27 lunar mansions + Pada
- `ParallelUniverseGenerator.kt` — "What if" age in different historical eras
- `PlanetAgeCalculator.kt` — Age on other planets
- `ProfileDeepLinkGenerator.kt` — `agereveal://profile/[data]` encode/decode
- `RelationshipType.kt` — Compatibility relationship types
- `RetirementCalculator.kt` — Retirement stats
- `ShareCardGenerator.kt` — Bitmap card renderer (has Android graphics imports); add `drawSajuKoreanBalanceCard()` for 오행 shareable
- `TimeRemainingCalculator.kt` — Time-remaining-until-target-age stats
- `ZodiacCalculator.kt` — Western, Vedic Rashi, Chinese Zodiac + Stem-Branch; `getWesternSignIndex()` for compatibility use
- `ZodiacCompatibilityCalculator.kt` — Western + Korean Saju compatibility scoring (composite: Western 50% + Korean 50% for the unified K-content audience; Vedic is a separate flow via `VedicCompatibilityScorer`)

### Billing
- `BillingManager.kt` — Google Play Billing 7+ wrapper; SKU `premium_monthly` (₹49) + `premium_yearly` (₹299); 7-day free trial; purchase acknowledge + DataStore sync

### Premium Themes (v2.0)
- `PremiumTheme.kt` — Enum: `Vaporwave`, `Cottagecore`, `Y2K`, `DarkAcademia`, `Cyberpunk`
- `ui/theme/Theme.kt` — Dynamic `ColorScheme` based on selected theme; premium-gated
- `SettingsScreen.kt` → Appearance section — theme pack picker with lock icons

---

## Navigation Tabs

| Tab Label | Route | Screen | Icon |
|-----------|-------|--------|------|
| My Cosmos | `calculator` | CalculatorScreen | ic_tab_you (person) |
| Match | `compatibility` | CompatibilityScreen | ic_tab_match (heart) |
| Bdays | `reminders` | RemindersScreen | ic_tab_bdays (cake) |
| Timeline | `timeline` | LifeTimelineScreen | ic_tab_timeline |

Settings opens as a full-screen destination (no bottom bar change) from Calculator or Reminders.
Onboarding is the start destination on first launch.

---

## Important Conventions

- **Stateless composables:** Pass state + callbacks down; keep state in ViewModel.
- **Comments:** Only when the *why* is non-obvious. No restating the obvious.
- **Feb 29 helper:** `yearSafeBirthday()` is the canonical utility — reuse it everywhere.
- **Font:** Inter is compiled from `res/font/` and used as the default typeface for all body/label text.
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

### Testing Commands
| Type | Command | Notes |
|------|---------|-------|
| Unit tests | `./gradlew testDebugUnitTest` | JVM tests, no Android framework |
| UI tests | `./gradlew connectedAndroidTest` | Instrumented tests on device/emulator |
| All tests | `./gradlew check` | Runs both unit and instrumented tests |

### Appium Automated UI Testing
The project includes an Appium E2E suite in `screenshots/walkthrough.py`. Update required for v2.0 selectors:
- Onboarding flow (blocks tests until completed)
- Paywall screen (new premium upsell)
- "My Cosmos" tab rename (was "You")
- Badges moved from bottom nav to My Cosmos section
- Hindi locale toggle removed (system locale only)

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

- **Current branch:** `feat/smaller-features-v2`
- **Current phase:** Phase 6 — Platform Ecosystem (Planned) / Post-v2.0
- **v2.0 release:** 2026-05-22 — Beta complete (Branch: `beta-release-v2`); production rollout pending
- **Active development:** Phase 6 features (Cloud Backup, Wear OS, iOS port) — see [roadmap.md](roadmap.md)

**Recently added in v2.0:**
- Celebrity birthday matching (375 curated entries)
- Free trial UX chip (7-day trial, N days left indicator)
- Indian state dropdown with centroid coordinates
- Billing error handling with human-readable messages
- Restore purchases flow (PaywallScreen + Settings)
- Progressive disclosure UI (hero counter + rotating highlight + DetailsUnlockScreen tabs)
- Tabbed DetailsUnlockScreen (Overview | Western | Vedic | Korean Saju)
- CalculatorScreen refresh button
- Firebase Analytics MVP (onboarding, paywall, share, deep-link, purchase events)
- Daily fortune push notifications
- Premium theme packs (Vaporwave, Cottagecore, Y2K, Dark Academia, Cyberpunk)
- WhatsApp sticker pack ContentProvider
- Cosmic Year Report notification
- 7-day free trial grace period tracking

**Planned for v2.1 (Korean Saju Supremacy):**
- `SajuKoreanCalculator.kt` — 천간/지지 in Hangul, 대운 (Daeun 10-year luck periods), 오행 (Five Element) balance, 용신 (Yongshin) rule-based suggestion
- Korean Saju Premium Unlock IAP (one-time ₹149, K-fandom targeting)
- 오행 balance shareable card (radar/bar chart, Gen Z-friendly)
- DetailsUnlockScreen "Korean Saju" tab replacing "Chinese" tab

**v2.0 audit fixes (2026-05-22):**
- Western compatibility now uses astronomical ephemeris (not hardcoded date ranges)
- SharedPreferences consolidated into UserPreferencesRepository with DataStore mirroring
- ViewModels cleansed of direct Context references
- Horoscope engines audited (Western/Vedic/Chinese — all mathematically sound)
- **Korean Saju (사주) layer** planned for v2.1 — distinct from Ba Zi, Hangul 천간·지지, 대운, 오행, 용신 (see `roadmap.md` Mission 7)

**Package ID:** `com.willowvibe.cosmicid` (applicationId changed; namespace `com.willowvibe.agereveal` kept for source compatibility)  
**Display name:** Cosmic ID  
**Version:** 2.0.0 (production rollout in progress)  
**Build status:** Compiles and all 137 unit tests pass

---

## Next Development Phase (Phase 6)

See [roadmap.md](roadmap.md) for details.

| Feature | Status | Notes |
|---------|--------|-------|
| Cloud Backup (Firebase Firestore) | ⬜ Planned | Opt-in Google sign-in sync for saved profiles |
| Wear OS Companion | ⬜ Planned | Live seconds counter, next-birthday complication |
| Lock Screen Widget | ⬜ Planned | API 33+ `WIDGET_FEATURE_RECONFIGURABLE` |
| iOS Port | ⬜ Planned | Flutter/React Native with WidgetKit |
| Animated MP4 Export | ⬜ Deferred | 5-second Reels/TikTok video (deferred from v2.0) |
| Cosmic Twins Discovery | ⬜ Deferred | Offline Rashi+Nakshatra matching (deferred from v2.0) |

**Testing commands:**
```bash
# Unit tests (JVM)
./gradlew testDebugUnitTest

# UI tests (instrumented)
./gradlew connectedAndroidTest

# Run all tests
./gradlew check
```
