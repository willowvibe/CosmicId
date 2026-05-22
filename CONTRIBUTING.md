# Contributing to Cosmic ID

Thank you for considering contributing to Cosmic ID! Whether you're reporting a bug, suggesting an enhancement, or submitting code, your involvement helps make the project better.

---

## Table of Contents

- [Reporting Bugs](#reporting-bugs)
- [Suggesting Enhancements](#suggesting-enhancements)
- [Pull Requests](#pull-requests)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Code Style](#code-style)

---

## Reporting Bugs

Before opening an issue:
1. Test against the **latest version** of the app.
2. Search existing issues to avoid duplicates.
3. Check [BUGS_AND_ISSUES.md](BUGS_AND_ISSUES.md) — the bug may already be tracked.

When filing a bug report, include:
- **Title:** Clear, specific, and reproducible (e.g., "Notification not firing for Feb 29 birthday in 2025")
- **Steps to reproduce** — numbered, step by step
- **Expected behaviour** — what should happen
- **Actual behaviour** — what actually happens
- **Device info** — Android version, manufacturer/model
- **App version** — found in Settings or the About section

---

## Suggesting Enhancements

- Check [TASKS.md](TASKS.md) and [roadmap.md](roadmap.md) — the idea may already be planned.
- Use a descriptive title for the issue.
- Explain **why** the enhancement would be useful, not just what it should do.
- Include mockups or examples where possible.

---

## Pull Requests

1. Fork the repository and create a branch from `main` (or the active feature branch, e.g., `feature/revamp-v2`).
2. Keep the PR focused — one feature or bug fix per PR.
3. Fill in the PR template (title, description, screenshots for UI changes).
4. Do **not** include issue numbers in the PR title.
5. Include screenshots or screen recordings for any UI changes.
6. Follow the code style guidelines below.
7. Ensure the project builds and runs before submitting.
8. Run `./gradlew lint` and fix all errors before submitting.

---

## Development Setup

### Requirements
- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** (bundled with Android Studio)
- An Android device or emulator running **API 26+** (API 21–25 supported via desugaring)

### Recent Updates
- **v2.0 (2026-05-16):** Major revamp beta-ready — freemium subscription model (Billing 7.1.1), 3-step onboarding, deep-link profile sharing, celebrity matching, daily fortune push notifications, Firebase Analytics MVP, tabbed DetailsUnlockScreen (Overview | Western | Vedic | Chinese), progressive disclosure UI, Indian state dropdown, grace period for lapsed subscriptions. Rewarded/interstitial ads removed. Remaining post-beta: animated MP4 export, WhatsApp sticker pack, cosmic twins discovery, cosmic year report notification.
- **v0.9.1 (2026-04-23):** Phase 3 complete - Birth time support, milestone notifications, life timeline visual, and consolidated Settings screen. Settings screen now correctly uses `SettingsViewModel` instead of `RemindersViewModel`. If you're building after pulling recent changes, ensure your local branch has the latest ViewModel updates.

### Steps
1. Clone the repository.
2. Open the project root in Android Studio.
3. Wait for Gradle sync to complete.
4. Run the `app` configuration on your device or emulator.

### AdMob
All bundled Ad Unit IDs are Google's safe test values. The app runs and shows test ads out of the box — no AdMob account needed for local development. See [TASKS.md §1a](TASKS.md) for how to swap in production IDs before a release build.

### Google Play Billing (v2.0)
The revamp introduces subscription billing via Google Play Billing Library 7.1.1.
- Test purchases use Google's test SKU `android.test.purchased` during development.
- Real product IDs (`premium_monthly`, `premium_yearly`) must be configured in the Play Console before release.
- `BillingManager.kt` handles all purchase flows; see `billing/` package.

### Optional — Custom Inter Typography
The app defaults to the system sans-serif font. To enable the Inter custom font locally:
1. Download Inter TTF files from rsms.me/inter
2. Place the following files in `app/src/main/res/font/`:
   - `inter_regular.ttf`
   - `inter_medium.ttf`
   - `inter_semibold.ttf`
   - `inter_bold.ttf`
3. Uncomment the `InterFamily` font-family block in `app/src/main/java/com/willowvibe/agereveal/ui/theme/Type.kt` (lines 20–25)
4. Replace `FontFamily.Default` with `InterFamily` in every `TextStyle` in that file

> These font files are not committed to the repo to avoid binary bloat; the fallback looks fine in practice.

---

## Project Structure

```
app/src/main/java/com/willowvibe/agereveal/
├── ads/          # AdMob lifecycle management (banner only on free tier)
├── analytics/    # Firebase Analytics MVP wrapper
├── billing/      # Google Play Billing 7+ (subscription handling)
├── data/         # Room DB, DAOs, models, repository, preferences
├── di/           # Hilt DI modules
├── domain/       # Pure Kotlin business logic (no Android deps)
├── notification/ # WorkManager workers and schedulers
├── ui/           # Compose screens, ViewModels, navigation, theme
└── widget/       # Jetpack Glance home screen widgets
```

Key architectural conventions:
- **Domain layer** (`domain/`) must stay free of Android framework imports; it holds pure Kotlin logic only.
- **ViewModels** expose `StateFlow<UiState>` and never hold a reference to a `Context` (use Hilt's `ApplicationContext` via constructor injection if needed).
- **Repository** is the single source of truth for persisted data; screens never talk to the DAO directly.
- `yearSafeBirthday()` is the canonical Feb 29 helper — reuse it wherever a birthday date is advanced by a year.
- **Billing layer** (`billing/`) is the only place that imports `com.android.billingclient.api`; no other layer should reference billing directly.

---

## Code Style

- Follow the official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use 4-space indentation (no tabs).
- Compose UI: prefer stateless composables that receive state and callbacks; keep stateful logic in the ViewModel.
- Write comments only when the **why** is non-obvious — avoid restating what the code already says.
- End all files with a newline.
- Run `./gradlew lint` before opening a PR; fix all errors and review warnings.
