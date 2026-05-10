# Cosmic ID — Tasks & Implementation Checklist

_Last updated: 2026-05-10 — v2.0.0 Revamp (in progress)_

---

## 0. Decisions Log

| # | Decision | Choice | Why |
|---|----------|--------|-----|
| 0.1 | App display name | **Cosmic ID** | Distinct from generic calculator apps; cosmic/zodiac brand |
| 0.2 | Package ID | **Keep `com.willowvibe.agereveal`** | Existing installs auto-update; ratings retained |
| 0.3 | State vs city location | **Indian State dropdown (centroid)** | 80% of users don't know exact coordinates; state is "good enough" for Lagna |
| 0.4 | Vedic compatibility weight | **50% of composite score** | Indian audience trusts Kundali Milan above all else |
| 0.5 | Free trial duration | **7 days** | Industry standard; enough time to form habit |

---

## 1. v2.0 Revamp — Critical Path

### 1a. Monetisation Overhaul 🔴

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Remove rewarded ad gate from astrology | ✅ | Basic astrology free; premium gates depth (Dasha, Ba Zi, planetary table) |
| 2 | Remove interstitial ad triggers | ✅ | Deleted all `maybeShowInterstitial()` calls; banner-only on free tier |
| 3 | Integrate Google Play Billing Library 7+ | ✅ | `billing-ktx` dependency added; `BillingClient` with `SUBS` product type |
| 4 | Create `BillingManager.kt` | ✅ | SKU `premium_monthly` (₹49) + `premium_yearly` (₹299); 7-day free trial |
| 5 | Create `PaywallScreen.kt` | ✅ | Shows subscription tiers with "BEST VALUE" yearly badge |
| 6 | Add `isPremium` flag to `UserPreferencesRepository` | ✅ | DataStore boolean; synced from BillingManager purchase state |
| 7 | Swap AdMob test IDs for production | ⬜ | Replace banner ID only (rewarded + interstitial IDs deleted) |
| 8 | **Restore purchases flow** | ⬜ | Mandatory for Play Store review; add "Restore" button in Settings → Premium |
| 9 | **Billing error handling UI** | ⬜ | Play Store unreachable → show "Can't reach Play Store" with retry CTA |
| 10 | **Grace period for lapsed subscriptions** | ⬜ | 3-day grace; show "Renew to keep premium" banner instead of hard lockout |
| 11 | **Free trial UX** | ⬜ | During trial: show "Premium expires in N days" chip; day-6 conversion push |
| 12 | **Acknowledge purchases + setPremium sync** | ✅ | `BillingManager.handlePurchases()` mirrors to DataStore |

### 1b. Onboarding 🔴

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `OnboardingScreen.kt` | ✅ | 3-step horizontal animated pager |
| 2 | **Step 1: Name + Birth date** | ✅ | Name is basic input; date is required; "Let's build your Cosmic ID" |
| 3 | **Step 2: Optional birth time + location** | ✅ | "Fine-tune your chart"; skip saves null to prefs |
| 4 | Step 3: Accent colour picker | ✅ | 5 swatches; "Enter My Cosmos" CTA |
| 5 | First-launch gate in `MainActivity` | ✅ | `MainViewModel.hasCompletedOnboarding` → conditional start destination |
| 6 | `MainViewModel.kt` | ✅ | `completeOnboarding()` writes to DataStore; exposes `hasCompletedOnboarding` |

### 1c. Deep-Link Profile Sharing 🟡

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `ProfileDeepLinkGenerator.kt` | ✅ | Base64-URL-encoded JSON: `{d, n, t}` → `agereveal://profile/[data]` |
| 2 | Register intent-filter in `AndroidManifest.xml` | ✅ | `agereveal://profile/*` |
| 3 | Decode on receive in `MainActivity` | ✅ | `ProfileDeepLinkGenerator.parse(intent?.data)` passed to `AppNavGraph` |
| 4 | Auto-populate CalculatorScreen | ✅ | `LaunchedEffect(deepLinkProfile)` → `onBirthDateSelected`, `onNameChanged`, `onBirthTimeSelected` |
| 5 | **Fallback for non-installed users** | ⬜ | Register `https://` App Link OR Firebase Dynamic Link → Play Store fallback |
| 6 | Share button generates deep-link | ⬜ | "Share your Cosmic ID" button copies `ProfileDeepLinkGenerator.generate()` URL |

### 1d. Progressive Disclosure on Main Screen 🟡

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Refactor `CalculatorScreen.kt` | ⬜ | Hero counter + rotating highlight + "Explore full profile →" CTA |
| 2 | Move all other cards to `DetailsUnlockScreen` tabs | ⬜ | Overview / Western / Vedic / Chinese tabbed layout |
| 3 | Remove "Work weeks until retirement" from main screen | ⬜ | Move to Life Stats in Details |
| 4 | Rotating highlight logic | ⬜ | Cycle through fortune / next milestone / planet age / celebrity match |

### 1e. Feature Removals 🔴

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Delete `ParallelUniverseCalculator.kt` and UI card | ✅ | Card removed from `DetailsUnlockScreen`; file kept dormant |
| 2 | Remove ASCII Art from `ShareThemeSheet.kt` | ✅ | `ShareFormat` enum trimmed; `ASCII_ART` chip deleted |
| 3 | Delete custom Hindi language toggle | ✅ | Rely on Android system locale (API 33+); `setLanguage()` is no-op |
| 4 | Move Badges from bottom nav to "My Cosmos" section | ✅ | 4 tabs: My Cosmos, Match, Bdays, Timeline |
| 5 | Rename "You" tab to "My Cosmos" | ✅ | String resource + `Screen.Calculator.label` |

### 1f. Location Input — Indian State Dropdown 🔴

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `assets/indian_states_coords.json` | ⬜ | 36 states/UTs with centroid lat/lon |
| 2 | Replace lat/lon free-text input with State dropdown | ⬜ | Show state name; persist centroid coordinates internally |
| 3 | Add "(Approximate — state centroid)" label | ⬜ | When state is used vs exact city coords |
| 4 | Optional "Add exact city" secondary input | ⬜ | Collapsible section below state picker for power users |
| 5 | Update `GeoLocation` model | ⬜ | Add `isApproximate: Boolean` flag for UI labelling |

---

## 2. v2.0 Revamp — Growth Features

### 2a. Celebrity Birthday Matching 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `assets/celebrities.json` (~500 entries) | ⬜ | `[{name, dob, category}]` — **India-first curation** |
| 2 | **Data source & curation strategy** | ⬜ | Bollywood (150), Cricket (100), Politics (50), Global (200); static JSON baked into APK |
| 3 | Create `CelebrityMatchCalculator.kt` | ⬜ | Match by month+day; return top 3 matches |
| 4 | Add celebrity card to `CalculatorScreen` | ⬜ | Show in rotating highlight or Details |
| 5 | Generate shareable celebrity card | ⬜ | Via `ShareCardGenerator` — "You share a birthday with [Name]" |

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

### 2g. Cosmic Match — Triple System Engine 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | `ChineseCompatibilityScorer.kt` | ⬜ | 12×12 animal matrix + Five Elements clash/support logic |
| 2 | `VedicCompatibilityScorer.kt` | ⬜ | Full 8-Koot Ashtakoot (36-point Guna Milan) |
| 3 | `ManglikCalculator.kt` | ⬜ | Mars in houses 1,4,7,8,12 = Mangal Dosha flag |
| 4 | `CosmicMatchEngine.kt` | ⬜ | Weighted composite: Western 25% + Chinese 25% + Vedic 50% |
| 5 | `CosmicMatchScreen.kt` | ⬜ | Unified UI showing all 3 system scores + composite |
| 6 | Show raw Ashtakoot score (X/36) prominently | ⬜ | Indian users expect this format |
| 7 | "Cosmic Twins" result state | ⬜ | Ashtakoot ≥ 28 + Western ≥ 75 + Chinese harmony |
| 8 | Dual share card with both profiles | ⬜ | Composite score + individual system breakdowns |

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
| 1 | Decide new name | ✅ | **Cosmic ID** — distinct, cosmic, identity-focused |
| 2 | Update `strings.xml` app_name | ✅ | `Cosmic ID` |
| 3 | Update README.md, CONTRIBUTING.md, all store docs | ⬜ | Replace "AgeReveal" with "Cosmic ID" where referring to display name |
| 4 | Redesign icon to cosmic/zodiac motif | ⬜ | Move away from generic calculator look |
| 5 | Update all store listing assets | ⬜ | Feature graphic, screenshots, descriptions |
| 6 | **Package ID decision** | ✅ | Keep `com.willowvibe.agereveal` — existing installs auto-update |

---

## 5. Technical Debt

### 5a. Testing
- [x] UI tests (Compose test) — Calculator screen happy path, date validation errors
- [x] Instrumented test for Room DAO
- [x] Migration test using `MigrationTestHelper`
- [ ] Add billing tests with `BillingClient` test SKUs
- [ ] Add deep-link intent-filter tests
- [ ] **Update `screenshots/walkthrough.py` Appium suite for v2.0** | ⬜ | Onboarding flow, paywall screen, new tab names, removed language toggle |

### 5b. Performance & Error Handling
- [x] Profile widget update frequency — `notifyWidget()` triggers immediate updates
- [x] Hindi UI toggle removed — system locale handles this natively (API 33+)
- [x] Remove unnecessary exact alarm permissions from manifest
- [x] Replace noon-default with timezone-aware default for astronomical calculations

### 5c. Analytics — Firebase (Minimum Viable) 🟡

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Add `firebase-analytics` dependency | ⬜ | Free; no account limits for basic events |
| 2 | Log onboarding funnel | ⬜ | `onboarding_step_1_complete`, `onboarding_step_2_complete`, `onboarding_complete` |
| 3 | Log paywall funnel | ⬜ | `paywall_shown`, `paywall_subscribe_tap`, `paywall_dismiss` |
| 4 | Log share events | ⬜ | `share_initiated` with param `format: square/story/mp4/whatsapp` |
| 5 | Log deep-link events | ⬜ | `deep_link_received`, `deep_link_profile_viewed` |
| 6 | Log premium conversion | ⬜ | `purchase_complete`, `trial_started`, `trial_converted` |

---

## 6. Play Store Submission (Post-Revamp)

### 6a. Required Assets
- [ ] App icon — 512 × 512 PNG (new cosmic/zodiac design)
- [ ] Feature graphic — 1024 × 500 PNG
- [ ] Phone screenshots — 5–8 portrait (showcasing onboarding, premium paywall, MP4 export, celebrity match)
- [ ] Short description — max 80 characters
- [ ] Full description — max 4 000 characters
- [ ] Content rating questionnaire
- [ ] **Subscription pricing declaration in Play Console** | ⬜ | Mandatory for billing apps |
- [ ] **"Subscription Terms" link in app settings** | ⬜ | Google policy requirement |
- [ ] **Privacy Policy URL in Play Console** | ⬜ | Required for any app with billing |

### 6b. Release Rollout
1. **Internal testing** — verify real AdMob impressions, Play Billing test purchases, widget behaviour, notifications
2. **Open testing** — gather reviews, iterate on paywall conversion
3. **Production** — staged rollout 20% → 50% → 100%

---

## Appendix: File Naming Conventions

| Feature Area | File Prefix | Example |
|---|---|---|
| Billing | `*BillingManager.kt` | `BillingManager.kt` |
| Onboarding | `Onboarding*.kt` | `OnboardingScreen.kt` |
| Paywall | `Paywall*.kt` | `PaywallScreen.kt`, `PaywallViewModel.kt` |
| Deep-link | `ProfileDeepLink*.kt` | `ProfileDeepLinkGenerator.kt` |
| Video export | `VideoExport*.kt` | `VideoExportWorker.kt` |
| Widgets | `*GlanceWidget.kt` | `SecondsCounterGlanceWidget.kt` |
| Share generators | `draw*()` in `ShareCardGenerator.kt` | `drawStoryDarkCosmos()` |
| Calculators | `*Calculator.kt` | `CelebrityMatchCalculator.kt`, `ManglikCalculator.kt` |
| Compatibility | `*CompatibilityScorer.kt` | `ChineseCompatibilityScorer.kt`, `VedicCompatibilityScorer.kt` |
| Data models | `data/model/*.kt` | `Celebrity.kt`, `AchievementBadge.kt` |
| Repository | `data/repository/*.kt` | `BadgeRepository.kt` |
| UI screens | `ui/screen/*.kt` | `OnboardingScreen.kt`, `CosmicMatchScreen.kt` |
| ViewModels | `ui/viewmodel/*ViewModel.kt` | `OnboardingViewModel.kt` |
| JSON assets | `assets/*.json` | `celebrities.json`, `indian_states_coords.json` |
