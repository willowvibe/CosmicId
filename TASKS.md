# Cosmic ID — Tasks & Implementation Checklist

_Last updated: 2026-05-22 — v2.0.0 Revamp (horoscope audit + architecture improvements + 5 smaller features complete)_

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
| 8 | **Restore purchases flow** | ✅ | "Restore purchases" button in PaywallScreen + Settings; delegates to `BillingManager.restorePurchases()` |
| 9 | **Billing error handling UI** | ✅ | `BillingManager` exposes `error: StateFlow<String?>` with human-readable messages; PaywallScreen shows error banner with retry CTA |
| 10 | **Grace period for lapsed subscriptions** | ✅ | 3-day grace tracked via `gracePeriodStart` in DataStore; BillingManager exposes `isInGracePeriod` |
| 11 | **Free trial UX** | ✅ | `BillingManager` parses free pricing phase → `trialDaysRemaining: StateFlow<Int?>`. CalculatorScreen header shows "N days left" chip |
| 12 | **Acknowledge purchases + setPremium sync** | ✅ | `BillingManager.handlePurchases()` mirrors to DataStore; stores purchase timestamp for trial calculation |

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
| 5 | **Fallback for non-installed users** | ✅ | Android App Link (`https://willowvibe.com/agereveal/profile/*`) + HTML fallback page; `generateShareUrl()` for HTTPS sharing; `assetlinks.json` template created |
| 6 | Share button generates deep-link | ✅ | "Copy link" button in CalculatorScreen copies `ProfileDeepLinkGenerator.generate()` URL to clipboard with Snackbar confirmation |

### 1d. Progressive Disclosure on Main Screen 🟡

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Refactor `CalculatorScreen.kt` | ✅ | Hero counter + rotating highlight + "Explore full profile →" CTA; share card + copy link side-by-side |
| 2 | Move all other cards to `DetailsUnlockScreen` tabs | ✅ | Overview / Western / Vedic / Chinese tabbed layout implemented |
| 3 | Remove "Work weeks until retirement" from main screen | ✅ | Retirement card removed from main screen (now only in Details if enabled) |
| 4 | Rotating highlight logic | ✅ | 4-second auto-rotate through Milestone / Fortune / Planet Age / Celebrity Match; `remember(fortune != null)` to prevent timer restart |

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
| 1 | Create `assets/indian_states_coords.json` | ✅ | 36 states/UTs with centroid lat/lon |
| 2 | Replace lat/lon free-text input with State dropdown | ✅ | Searchable bottom-sheet picker with state names; persists centroid coordinates |
| 3 | Add "(Approximate — state centroid)" label | ✅ | `PrecisionChip` shows state name + "*" when `isApproximate = true` |
| 4 | Optional "Add exact city" secondary input | ✅ | Collapsible section below state picker in location bottom sheet; city name appends to state label |
| 5 | Update `GeoLocation` model | ✅ | `isApproximate: Boolean = false`; serialized as 4th segment in prefs |

---

## 2. v2.0 Revamp — Growth Features

### 2a. Celebrity Birthday Matching 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `assets/celebrities.json` (~375 entries) | ✅ | `[{name, dob, category}]` — 8 categories: Bollywood, Cricket, Sports, Global, Politics, South Indian, Music, Business |
| 2 | **Data source & curation strategy** | ✅ | Curated static JSON baked into APK; sorted by exact-year-first, then year, then name |
| 3 | Create `CelebrityMatchCalculator.kt` | ✅ | Singleton with `findMatches(birthDate, limit = 3)`; parses JSON from assets; matches month+day |
| 4 | Add celebrity card to `CalculatorScreen` | ✅ | Shows in `RotatingHighlightCard` when matches exist; auto-hides when empty |
| 5 | Generate shareable celebrity card | ✅ | `CalculatorViewModel.shareCelebrityCard()` wired; highlight rotates through matches |

### 2b. Daily Fortune Push Notification 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `DailyFortuneWorker.kt` | ✅ | `OneTimeWorkRequest` with exact daily rescheduling; fires at user-set hour (default 8AM) |
| 2 | Build notification with fortune text + CTA | ✅ | "Tap to see your full cosmic day" — opens MainActivity |
| 3 | Settings toggle for fortune time | ✅ | Settings → Notifications section with hour picker and master toggle |

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
| 1 | Extend `YearlyReengagementWorker.kt` with Dasha + fortune | ✅ | Injects `DashaCalculator` + `DailyFortuneGenerator`; rich `BigTextStyle` notification |
| 2 | Generate rich notification | ✅ | "You've lived [X] days — [Dasha period] — tap for your cosmic year ahead" |
| 3 | Include Mahadasha + fortune summary in body | ✅ | Dasha info from `getDashaInfo()`, fortune from `DailyFortuneGenerator.generate()` |

### 2e. Cosmic Twins Discovery 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `CosmicTwinScreen.kt` | ⬜ | Offline matching by Rashi + Nakshatra combo |
| 2 | Create `CosmicTwinShareCard` | ⬜ | Dual card showing both profiles side-by-side |
| 3 | No backend needed | ⬜ | Works entirely offline; deterministic matching |

### 2f. WhatsApp Sticker Pack 🟢

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create `WhatsAppStickerProvider.kt` | ✅ | `ContentProvider` with Hilt `@EntryPoint` pattern; serves sticker metadata + PNG files |
| 2 | Generate 512×512 PNG | ✅ | `StickerGenerator` creates 12 cosmic-themed stickers via Canvas drawing |
| 3 | `contents.json` manifest | ✅ | Metadata returned via ContentProvider cursor (identifier, name, publisher, tray, stickers) |
| 4 | Register `ContentProvider` in manifest | ✅ | `com.willowvibe.cosmicid.stickercontentprovider` authority; exported for WhatsApp access |

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
- [x] Refactor `DetailsUnlockScreen` into horizontal Pager/Tab layout: `Overview` | `Western` | `Vedic` | `Chinese`
- [ ] Expandable cards for deep educational text (Nakshatra meanings, Element descriptions)

### 3b. Micro-Interactions & Animations
- [x] Number Roll Animation — `AnimatedContent` with `slideInVertically` + `fadeIn`
- [x] Hero Stagger Entrance — enter/reveal stagger when age result first appears
- [x] Haptic Feedback Sweep — date/time picker scrolls + important button taps

### 3c. Theming
- [x] Custom Accent Color Picker — 6 swatches in Settings → Appearance
- [x] **Premium Theme Packs** — Vaporwave, Cottagecore, Y2K, Dark Academia, Cyberpunk (premium-only) — `PremiumTheme` enum, dynamic `ColorScheme`, Settings UI with premium lock gating

---

## 4. App Rename & Brand

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Decide new name | ✅ | **Cosmic ID** — distinct, cosmic, identity-focused |
| 2 | Update `strings.xml` app_name | ✅ | `Cosmic ID` |
| 3 | Update README.md, CONTRIBUTING.md, all store docs | ✅ | "AgeReveal" → "Cosmic ID" rename applied to README, CONTRIBUTING, DESIGN, TASKS, BUGS_AND_ISSUES, privacy policy, and store docs. Source filenames (e.g. `AgeRevealApp.kt`) kept for compatibility. |
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
| 1 | Add `firebase-analytics` dependency | ✅ | `firebase-bom:33.12.0` + `firebase-analytics-ktx` added to `build.gradle.kts` |
| 2 | Log onboarding funnel | ✅ | `AnalyticsManager` exposes `logOnboardingStep{1,2,3}Complete()` and `logOnboardingComplete()` |
| 3 | Log paywall funnel | ✅ | `logPaywallShown()`, `logPaywallSubscribeTap(sku)`, `logPaywallDismiss()` |
| 4 | Log share events | ✅ | `logShareInitiated(format)` with `format` parameter |
| 5 | Log deep-link events | ✅ | `logDeepLinkReceived()`, `logDeepLinkProfileViewed()` |
| 6 | Log premium conversion | ✅ | `logPurchaseComplete(sku)`, `logTrialStarted(sku)`, `logTrialConverted(sku)` |

---

## 6. Play Store Submission

### 6a. Pre-build Version Bump 🔴
- [x] **Bump `VERSION` file** → `2.0.0` ✅ 2026-05-22
- [ ] **Bump `versionCode`** in `app/build.gradle.kts` → `8` (must be higher than v1.0.7's versionCode 7)
- [ ] **Verify `versionName` resolves to `2.0.0`** after VERSION file update

---

### 6b. Required Assets
- [ ] App icon — 512 × 512 PNG (new cosmic/zodiac design)
- [ ] Feature graphic — 1024 × 500 PNG
- [ ] Phone screenshots — 5–8 portrait (showcasing onboarding, premium paywall, celebrity match, tabbed details)
- [ ] Short description — max 80 characters
- [ ] Full description — max 4 000 characters
- [ ] Content rating questionnaire
- [x] **Subscription pricing declaration in Play Console** | ✅ | `premium_monthly` (₹49) + `premium_yearly` (₹299) declared in Play Console |
- [x] **"Subscription Terms" link in app settings** | ✅ | "Terms & Privacy" row in Settings → About links to privacy policy |
- [x] **Privacy Policy URL in Play Console** | ✅ | `store_listing/privacy_policy.md` hosted at `https://willowvibe.com/agereveal/privacy` |

### 6c. Release Rollout
1. **Internal testing** — verify real AdMob impressions, Play Billing test purchases, widget behaviour, notifications
2. **Open testing** — gather reviews, iterate on paywall conversion
3. **Production** — staged rollout 20% → 50% → 100%

---

## 7. Infrastructure & Architecture Improvements (2026-05-22)

### 7a. Horoscope Engine Audit & Fixes ✅

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Audit Western zodiac calculation engine | ✅ | AstronomicalCalculator uses Meeus Ch.25 for Sun (~0.01°), Ch.47 for Moon (~0.1°); Lahiri ayanamsa with secular quadratic term — accurate for sign/nakshatra level |
| 2 | Audit Vedic calculation engine | ✅ | NakshatraCalculator (27 equal divisions), DashaCalculator (120-year Vimshottari cycle) — mathematically correct |
| 3 | Audit Chinese calculation engine | ✅ | BaZiCalculator 五虎遁月 month stem rule correct; CNY_DATES lookup table 1900–2100 verified against known dates |
| 4 | Fix Western zodiac inconsistency in compatibility | ✅ | ZodiacCompatibilityCalculator now uses astronomical ephemeris via getWesternSignIndex() instead of hardcoded date ranges — cusp dates now consistent |
| 5 | Fix LunarCalendarConverter Throwable catch | ✅ | Changed to catch Exception instead of Throwable |
| 6 | Remove dead julianDayNoon() code | ✅ | Replaced with julianDay(LocalDateTime) in the one caller |

### 7b. SharedPreferences Consolidation ✅

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Add user profile keys to UserPreferencesRepository | ✅ | Added birth_date, birth_time, birth_location, user_name, notification_hour, fortune_date, fortune_json — all mirrored to calculator_prefs for widget/worker access |
| 2 | Remove Context from CalculatorViewModel | ✅ | All prefs reads/writes now via UserPreferencesRepository suspend functions |
| 3 | Remove Context from RemindersViewModel | ✅ | cachedUserBirthDate loaded via StateFlow; notification_hour persisted centrally |
| 4 | Fix RemindersScreen direct SharedPreferences reads | ✅ | Now uses viewModel.cachedUserBirthDate.collectAsState() |

### 7c. AI Integration Provision ✅

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Create ai/ package | ✅ | ai/AiModels.kt, ai/AiService.kt, ai/NoOpAiServiceImpl.kt, ai/AiDiModule.kt |
| 2 | Define AiService interface | ✅ | generateFortune(), generateCompatibilityInsight(), generateTransitForecast() — all suspend functions |
| 3 | Create Hilt DI binding | ✅ | AiDiModule binds NoOpAiServiceImpl as default; swap to real AI backend without consumer changes |

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
