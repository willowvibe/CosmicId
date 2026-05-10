# CLAUDE.md — Cosmic ID

Quick-start guide for Claude Code sessions on the Cosmic ID Android project.

---

## What This Is

Cosmic ID is a native Android app (Kotlin + Jetpack Compose) that calculates exact age in real time and enriches it with astrological insights, shareable cards, milestone tracking, saved birthday reminders, and a home screen widget. Currently **v2.0**.

**Package:** `com.willowvibe.agereveal`
**Min SDK:** 26 (API 21–25 via core library desugaring)
**Compile SDK:** 36

---

## Architecture

| Layer | Contents |
|-------|----------|
| **UI** (`ui/screen/`, `ui/viewmodel/`) | Compose screens + MVVM ViewModels (`StateFlow<UiState>`) |
| **Navigation** (`ui/navigation/`) | Compose Navigation with single-activity + bottom nav (4 tabs) |
| **Domain** (`domain/`) | Pure Kotlin business logic — no Android framework imports |
| **Data** (`data/db/`, `data/repository/`, `data/model/`) | Room DB, DAOs, repository (single source of truth) |
| **DI** (`di/`) | Hilt modules (`DatabaseModule.kt`) |
| **Ads** (`ads/`) | AdManager wrapping Banner / Rewarded / Interstitial |
| **Notifications** (`notification/`) | WorkManager workers + schedulers |
| **Widget** (`widget/`) | Jetpack Glance 2×2 + wide widgets |

**Key rules:**
- Domain layer must stay Android-framework-free.
- ViewModels never hold `Context`; inject `ApplicationContext` via Hilt if needed.
- Screens never talk to DAOs directly — always go through Repository.

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
- **Ads:** Google AdMob (test IDs bundled)
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
- `MainActivity.kt` — Single-activity host, notification permission, edge-to-edge
- `AgeRevealApp.kt` — Application class, Hilt + AdMob init
- `ui/navigation/AppNavGraph.kt` — NavHost + bottom bar wiring

### Screens (Composables)
- `CalculatorScreen.kt` — Live age, date/time pickers, precision chips, banner ad, settings button
- `CompatibilityScreen.kt` — Zodiac compatibility (Romantic/Sibling/Friendship/Regular), two-person comparison
- `RemindersScreen.kt` — Saved birthdays list, AddBirthday bottom sheet, FAB, settings button
- `LifeTimelineScreen.kt` — Milestone timeline (driven by CalculatorViewModel results)
- `DetailsUnlockScreen.kt` — Astrology details (rewarded-ad unlock)
- `SettingsScreen.kt` — Theme, language, notifications, export CSV, clear all

### ViewModels
- `CalculatorViewModel` — Age calc, ephemeris, share card generation, ad unlock
- `CompatibilityViewModel` — Two-person comparison + compatibility scoring
- `RemindersViewModel` — Birthday CRUD + bottom sheet state
- `SettingsViewModel` — Theme, language, notification prefs, clear/export

### Domain Logic (Pure Kotlin)
- `AgeCalculator.kt` — Core age + milestone logic
- `AstronomicalCalculator.kt` — Sun/Moon sidereal positions
- `ZodiacCalculator.kt` — Western, Vedic Rashi, Chinese Zodiac + Stem-Branch
- `NakshatraCalculator.kt` — 27 lunar mansions + Pada
- `ZodiacCompatibilityCalculator.kt` — Compatibility scoring
- `ShareCardGenerator.kt` — Bitmap card renderer

---

## Navigation Tabs

| Tab Label | Route | Screen | Icon |
|-----------|-------|--------|------|
| You | `calculator` | CalculatorScreen | Calculate |
| Match | `compatibility` | CompatibilityScreen | Favorite |
| Bdays | `reminders` | RemindersScreen | Cake |
| Timeline | `timeline` | LifeTimelineScreen | Cake |

Settings opens as a full-screen destination (no bottom bar change) from Calculator or Reminders.

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
- Settings options (Dark, Hindi, Export CSV, Clear All) are below the fold inside a `ScrollView` — use `UiScrollable.scrollIntoView()`.

### System Interference
- **Export CSV** opens a system share chooser that blocks automation. Scroll-to and screenshot only; do not tap in tests.
- **Hindi locale selection** changes app locale and breaks subsequent selectors. Found-only in tests.
- **Notification shade** can appear on launch after `pm clear` — detect via `com.android.systemui` in page source and press Back.

### Ads
- All bundled AdMob IDs are Google test values — no revenue, no account needed.
- Replace before Play Store release (see `TASKS.md §1`).

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

---

## Current Session Context

- Branch: `feat/appium-walkthrough` (ahead of `origin/main`)
- Recently added: comprehensive Appium UI walkthrough (`screenshots/walkthrough.py`)
- 26 screenshots captured, 27 interactions tested, 0 bugs detected
- Phase 5 Gen Z features recently shipped (Generational Badge, Planet Ages, Moon Phase)
