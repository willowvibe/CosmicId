# AgeReveal — Tasks & Implementation Checklist

_Last updated: 2026-05-10 — v2.0.0 Revamp (in progress)_

---

## 1. v2.0 Revamp — Critical Path

### 1a. Monetisation Overhaul 🔴

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Remove rewarded ad gate from astrology | ⬜ | Delete `RewardedAd` flow; make basic astrology free; gate depth behind `isPremium` |
| 2 | Remove interstitial ad triggers | ⬜ | Delete all `maybeShowInterstitial()` calls; keep banner only on free tier |
| 3 | Integrate Google Play Billing Library 6+ | ⬜ | Add `billing-ktx` dependency; `BillingClient` with `SUBS` product type |
| 4 | Create `BillingManager.kt` | ⬜ | SKU `premium_monthly` (₹49) + `premium_yearly` (₹299); 7-day free trial |
| 5 | Create `PaywallScreen.kt` | ⬜ | Show on: (a) tapping locked astrology, (b) 3rd open if no sub |
| 6 | Add `isPremium` flag to `UserPreferencesRepository` | ⬜ | DataStore boolean; checked before showing ads or gating features |
| 7 | Swap AdMob test IDs for production | ⬜ | Replace banner ID only (rewarded + interstitial IDs deleted) |

### 1b. Onboarding 🔴

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `OnboardingScreen.kt` | ⬜ | 3-step horizontal pager |
| 2 | Step 1: DatePicker only | ⬜ | "When were you born?" |
| 3 | Step 2: Animated zodiac + live counter reveal | ⬜ | Instant reward; build emotional connection |
| 4 | Step 3: Optional birth time | ⬜ | "Add birth time for exact Nakshatra (optional)"; skip saves to prefs |
| 5 | First-launch gate in `MainActivity` | ⬜ | Check `UserPreferencesRepository.hasCompletedOnboarding()` |
| 6 | Onboarding ViewModel | ⬜ | `OnboardingViewModel` with `StateFlow<OnboardingState>` |

### 1c. Deep-Link Profile Sharing 🟡

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `ProfileDeepLinkGenerator.kt` | ⬜ | Base64-encode birth date + name + zodiac into URL param |
| 2 | Register intent-filter in `AndroidManifest.xml` | ⬜ | `agereveal://profile/[data]` |
| 3 | Decode on receive in `MainActivity` | ⬜ | Show "View [Name]'s Cosmic Profile" screen |
| 4 | Auto-populate CompatibilityScreen | ⬜ | When receiving a deep-link, fill second slot and show score |

### 1d. Progressive Disclosure on Main Screen 🟡

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Refactor `CalculatorScreen.kt` | ⬜ | Show only: hero counter, one rotating highlight card, "Explore full profile →" CTA |
| 2 | Move all other cards to `DetailsUnlockScreen` tabs | ⬜ | Overview / Western / Vedic / Chinese tabbed layout |
| 3 | Remove "Work weeks until retirement" from main screen | ⬜ | Move to Life Stats in Details |
| 4 | Rotating highlight logic | ⬜ | Cycle through fortune / next milestone / planet age / celebrity match |

### 1e. Feature Removals 🔴

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Delete `ParallelUniverseCalculator.kt` and UI card | ⬜ | Keep file dormant if referenced elsewhere |
| 2 | Remove ASCII Art from `ShareThemeSheet.kt`; delete `AsciiArtGenerator.kt` | ⬜ | Not shareable on modern platforms |
| 3 | Delete custom Hindi language toggle | ⬜ | Rely on Android system locale (API 33+) |
| 4 | Move Badges from bottom nav to "My Cosmos" section | ⬜ | Free up nav slot; badges become a retention mechanic inside profile |
| 5 | Rename "You" tab to "My Cosmos" | ⬜ | String resource change |

---

## 2. v2.0 Revamp — Growth Features

### 2a. Celebrity Birthday Matching 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `celebrities.json` (~500 entries) | ⬜ | `[{name, dob, category}]` in `assets/celebrities.json` |
| 2 | Create `CelebrityMatchCalculator.kt` | ⬜ | Match by month+day |
| 3 | Add celebrity card to `CalculatorScreen` | ⬜ | Show in rotating highlight or Details |
| 4 | Generate shareable celebrity card | ⬜ | Via `ShareCardGenerator` |

### 2b. Daily Fortune Push Notification 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `DailyFortuneWorker.kt` | ⬜ | `PeriodicWorkRequest` at user-set time (default 8AM) |
| 2 | Build notification with fortune text + CTA | ⬜ | "Tap to see your full cosmic day" |
| 3 | Settings toggle for fortune time | ⬜ | Add to Settings → Notifications |

### 2c. Animated MP4 Export 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `VideoExportWorker.kt` | ⬜ | `MediaCodec` + `MediaMuxer`; 5-second Canvas animation |
| 2 | Render seconds counter ticking | ⬜ | Frame-by-frame Canvas draw |
| 3 | Export to `FileProvider` URI | ⬜ | Share via `ACTION_SEND` with `video/mp4` MIME |
| 4 | Add "Video (MP4)" to `ShareThemeSheet.kt` | ⬜ | Premium-only or free with watermark |

### 2d. Cosmic Year Report Notification 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Extend `BirthdayReminderWorker.kt` or new worker | ⬜ | One-shot WorkManager job on user's birthday |
| 2 | Generate rich notification | ⬜ | "You've lived [X] days — here's your cosmic year ahead" |
| 3 | Include Mahadasha + fortune summary in body | ⬜ | Reuse existing logic |

### 2e. Cosmic Twins Discovery 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `CosmicTwinScreen.kt` | ⬜ | Offline matching by Rashi + Nakshatra combo |
| 2 | Create `CosmicTwinShareCard` | ⬜ | Dual card showing both profiles side-by-side |
| 3 | No backend needed | ⬜ | Works entirely offline; deterministic matching |

### 2f. WhatsApp Sticker Pack 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `WhatsAppStickerProvider.kt` | ⬜ | `ContentProvider` following WhatsApp Sticker Pack API spec |
| 2 | Generate 512×512 PNG with transparent BG | ⬜ | Reuse green-screen overlay rendering |
| 3 | `stickerpack.json` manifest | ⬜ | Required by WhatsApp API |
| 4 | Register `ContentProvider` in manifest | ⬜ | Deep-link to WhatsApp sticker import |

---

## 3. UI/UX & Design Polish

### 3a. Tabbed Details Screen
- [ ] Refactor `DetailsUnlockScreen` into horizontal Pager/Tab layout: `Overview` | `Western` | `Vedic` | `Chinese`
- [ ] Expandable cards for deep educational text (Nakshatra meanings, Element descriptions)

### 3b. Micro-Interactions & Animations
- [x] Number Roll Animation — `AnimatedContent` with `slideInVertically` + `fadeIn`
- [x] Hero Stagger Entrance — enter/reveal stagger when age result first appears
- [x] Haptic Feedback Sweep — date/time picker scrolls + important button taps

### 3c. Theming
- [x] Custom Accent Color Picker — 6 swatches in Settings → Appearance
- [ ] **Premium Theme Packs** — Vaporwave, Cottagecore, Y2K, Dark Academia, Cyberpunk (premium-only)

---

## 4. App Rename & Brand

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Decide new name | ⬜ | Candidates: **Nakshatra**, **CosmAge**, **BornAt** |
| 2 | Update `strings.xml` app_name | ⬜ | Cheap marketing lever |
| 3 | Redesign icon to cosmic/zodiac motif | ⬜ | Move away from generic calculator look |
| 4 | Update all store listing assets | ⬜ | Feature graphic, screenshots, descriptions |

---

## 5. Technical Debt

### 5a. Testing
- [x] UI tests (Compose test) — Calculator screen happy path, date validation errors
- [x] Instrumented test for Room DAO
- [x] Migration test using `MigrationTestHelper`
- [ ] Add billing tests with `BillingClient` test SKUs
- [ ] Add deep-link intent-filter tests

### 5b. Performance & Error Handling
- [x] Profile widget update frequency — `notifyWidget()` triggers immediate updates
- [x] Hindi UI toggle removed — system locale handles this natively (API 33+)
- [x] Remove unnecessary exact alarm permissions from manifest
- [x] Replace noon-default with timezone-aware default for astronomical calculations

---

## 6. Play Store Submission (Post-Revamp)

### 6a. Required Assets
- [ ] App icon — 512 × 512 PNG (new cosmic/zodiac design)
- [ ] Feature graphic — 1024 × 500 PNG
- [ ] Phone screenshots — 5–8 portrait (showcasing onboarding, premium paywall, MP4 export, celebrity match)
- [ ] Short description — max 80 characters
- [ ] Full description — max 4 000 characters
- [ ] Content rating questionnaire
- [ ] Subscription pricing declaration

### 6b. Release Rollout
1. **Internal testing** — verify real AdMob impressions, Play Billing test purchases, widget behaviour, notifications
2. **Open testing** — gather reviews, iterate on paywall conversion
3. **Production** — staged rollout 20% → 50% → 100%

---

## Appendix: File Naming Conventions

| Feature Area | File Prefix | Example |
|---|---|---|
| Billing | `*BillingManager.kt` | `BillingManager.kt` |
| Onboarding | `Onboarding*.kt` | `OnboardingScreen.kt`, `OnboardingViewModel.kt` |
| Paywall | `Paywall*.kt` | `PaywallScreen.kt`, `PaywallViewModel.kt` |
| Deep-link | `ProfileDeepLink*.kt` | `ProfileDeepLinkGenerator.kt` |
| Video export | `VideoExport*.kt` | `VideoExportWorker.kt` |
| Widgets | `*GlanceWidget.kt` | `SecondsCounterGlanceWidget.kt` |
| Share generators | `draw*()` in `ShareCardGenerator.kt` | `drawStoryDarkCosmos()` |
| Calculators | `*Calculator.kt` | `CelebrityMatchCalculator.kt` |
| Data models | `data/model/*.kt` | `Celebrity.kt`, `AchievementBadge.kt` |
| Repository | `data/repository/*.kt` | `BadgeRepository.kt` |
| UI screens | `ui/screen/*.kt` | `OnboardingScreen.kt` |
| ViewModels | `ui/viewmodel/*ViewModel.kt` | `OnboardingViewModel.kt` |
| JSON assets | `assets/*.json` | `celebrities.json` |
