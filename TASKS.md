# Cosmic ID — Tasks & Implementation Checklist

_Last updated: 2026-05-24 — Market research applied. Roadmap restructured into 6 missions. Tasks reprioritized for "sure hit" features._

---

## 0. Decisions Log

| # | Decision | Choice | Why |
|---|----------|--------|-----|
| 0.1 | App display name | **Cosmic ID** | Distinct from generic calculator apps; cosmic/zodiac brand |
| 0.2 | Package ID | **Keep `com.willowvibe.agereveal`** | Existing installs auto-update; ratings retained |
| 0.3 | State vs city location | **Indian State dropdown (centroid)** | 80% of users don't know exact coordinates; state is "good enough" for Lagna |
| 0.4 | Vedic compatibility weight | **50% of composite score** | Indian audience trusts Kundali Milan above all else |
| 0.5 | Free trial duration | **7 days** | Industry standard; enough time to form habit |
| 0.6 | **No weekly subscriptions** | **Enforced** | RevenueCat data: weekly plans have highest churn & lowest LTV in Lifestyle |
| 0.7 | **No paywall creep** | **Enforced** | #1 churn driver across Co-Star, The Pattern, Nebula. Free tier is locked. |
| 0.8 | **No AI chatbot (for now)** | **Deferred to v3.x** | Users spot templated AI immediately. Only build if genuinely personal with full chart context. |
| 0.9 | **India-first, US/UK-second** | **Market priority** | India = 1.5B TAM, lowest CAC, proven demand. AstroSage has 80M downloads but dated UX. |
| 0.10 | **Widget-first virality** | **Core UA strategy** | Every widget screenshot is free organic marketing. Every shareable card is a free ad. |
| 0.11 | **East-Asian pillar = Korean Saju (사주)**, not Chinese Ba Zi | **Strategic** | Same Four Pillars math + Hangul 천간/지지 + 대운 + 오행 + 용신 + K-fandom audience. `BaZiCalculator.kt` retained as Day/Hour pillar data path; `SajuKoreanCalculator.kt` owns the Korean naming + Daeun layer. |

---

## Mission Status Index

| # | Mission | Status | Target |
|---|---------|--------|--------|
| 1 | Cosmic Identity Core | ✅ Complete | v2.0 (shipped 2026-05-22) |
| 2 | Vedic Supremacy | 🔥 Active | v2.1 (2026-06-30) — secondary |
| 3 | Viral Growth Engine | 🔥 Active | v2.2 (2026-07-31) |
| 4 | Trust & Monetization | 🔥 Active | v2.3 (2026-08-31) |
| 5 | Platform Ecosystem | 📋 Planned | v3.0 (2026-Q4) |
| 6 | AI & Advanced Depth | ⏸️ Deferred | v3.x (2027+) |
| 7 | **Korean Saju Supremacy** | 🔥 **Active** | **v2.1 (2026-06-30) — primary** |
| 8 | **Portfolio Readiness** | 🔥 **Active** | **4-week parallel sprint — ongoing** |

---

## Mission 1: Cosmic Identity Core — COMPLETE ✅

> **Goal:** The unified experience that no competitor has. Shipped in v2.0.

### 1a. Live Age & Hero Counter

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Real-time age (years → seconds) | ✅ | `AgeCalculator.kt` with `java.time` desugaring |
| 2 | Hero counter with `AgeNumeral` | ✅ | Serif font, `AnimatedContent` roll animation |
| 3 | Seconds strip with amber dot | ✅ | Live ticking every second |
| 4 | Mini stat chips (months, days, hours, minutes) | ✅ | Column layout, single-line values |
| 5 | Precision chips (Exact/Approximate indicator) | ✅ | Shows state + city name |

### 1b. Multi-System Astrology

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | **Western** — Sun sign, Moon sign, Rising (Lagna), planetary positions | ✅ | Meeus ephemeris, Lahiri ayanamsa |
| 2 | **Vedic** — Rashi, Nakshatra, Pada, Lord, Dasha | ✅ | 27 Nakshatras, Vimshottari Dasha |
| 3 | **Korean Saju (사주)** — 천간/지지 in Hangul, Four Pillars, 대운 (Daeun), 오행 (Five Element), 용신 (Yongshin) | ⏳ v2.1 | `BaZiCalculator.kt` (Day/Hour pillar data path) + new `SajuKoreanCalculator.kt` (Korean naming + Daeun layer) |
| 4 | **Numerology** — Life Path, Expression, Soul Urge, Personality | ✅ | Pythagorean numerology |
| 5 | Tabbed `DetailsUnlockScreen` (Overview | Western | Vedic | Chinese) | ✅ | Progressive disclosure; premium gates depth |

### 1c. Widgets (6+ Glance Widgets)

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | `SecondsCounterGlanceWidget` | ✅ | Live ticking seconds on home screen |
| 2 | `BirthdayCountGlanceWidget` | ✅ | Countdown to next birthday |
| 3 | `LifespanGlanceWidget` | ✅ | Progress bar toward target age |
| 4 | `MilestoneRingGlanceWidget` | ✅ | Circular progress to next milestone |
| 5 | `WideGlanceWidget` (4×2) | ✅ | Multiple upcoming birthdays |
| 6 | `BirthdayGlanceWidget` (2×2) | ✅ | Single next birthday |
| 7 | Widget theming with accent color | ✅ | Dynamic color from user prefs |
| 8 | Premium theme packs in widgets | ✅ | Vaporwave, Cottagecore, Y2K, Dark Academia, Cyberpunk |

### 1d. Shareable Cards

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | `ShareCardGenerator.kt` — Canvas bitmap renderer | ✅ | 1080×1920 PNG export |
| 2 | Age card theme | ✅ | Multiple dark/light/cosmos themes |
| 3 | Compatibility card | ✅ | Dual-profile side-by-side |
| 4 | Zodiac card | ✅ | "Big Three" snapshot |
| 5 | Celebrity match card | ✅ | "I share a birthday with…" |
| 6 | Milestone card | ✅ | "10,000 days alive" celebration |
| 7 | `ShareThemeSheet.kt` — bottom sheet picker | ✅ | Theme chips with preview |

### 1e. Birthday Reminders

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Room DB + DAO for saved birthdays | ✅ | `BirthdayDao.kt`, `BirthdayEntity.kt` |
| 2 | Add birthday bottom sheet | ✅ | Name + date picker |
| 3 | Notification scheduling | ✅ | WorkManager with exact alarm |
| 4 | CSV export via `FileProvider` | ✅ | `FileProvider` + `Intent.ACTION_SEND` |
| 5 | Google Calendar export | ✅ | `CalendarExport.kt` |
| 6 | Milestone alerts (18, 21, 30, 50…) | ✅ | Push notification on milestone birthdays |

### 1f. Onboarding & Activation

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | 3-step animated onboarding | ✅ | Name + birth date → optional time + location → accent picker |
| 2 | First-launch gate in `MainActivity` | ✅ | `MainViewModel.hasCompletedOnboarding` |
| 3 | Accent color persistence | ✅ | `UserPreferencesRepository.setAccentColor()` |
| 4 | Birth time support (optional) | ✅ | `TimePicker` dialog; exact vs approximate indicator |
| 5 | Indian state dropdown (centroid) | ✅ | 36 states/UTs in `assets/indian_states_coords.json` |

### 1g. Monetization (v2.0)

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Google Play Billing Library 7+ | ✅ | `billing-ktx` dependency |
| 2 | `BillingManager.kt` — `premium_monthly` (₹49) + `premium_yearly` (₹299) | ✅ | 7-day free trial |
| 3 | `PaywallScreen.kt` with tier cards | ✅ | "BEST VALUE" yearly badge |
| 4 | `isPremium` flag in DataStore | ✅ | Synced from BillingManager |
| 5 | Restore purchases flow | ✅ | PaywallScreen + Settings |
| 6 | Billing error handling UI | ✅ | Human-readable messages + retry CTA |
| 7 | Grace period (3 days) | ✅ | `gracePeriodStart` in DataStore |
| 8 | Free trial UX chip | ✅ | "N days left" in CalculatorScreen header |
| 9 | AdMob banner (free tier only) | ✅ | Test IDs; production swap pending |
| 10 | **One-time lifetime SKU** | ⬜ | `$49.99` — appeals to subscription-fatigued users |
| 11 | **Korean Saju Premium Unlock IAP** (one-time) | ⬜ v2.1 | `korean_saju_unlock` — ₹149 / $2.99 — K-fandom one-time IAP |

### 1h. Social & Viral

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Profile deep-link sharing | ✅ | `agereveal://profile/[data]` |
| 2 | HTTPS fallback page | ✅ | `willowvibe.com/agereveal/profile/*` |
| 3 | Celebrity birthday matching (375 entries) | ✅ | 8 categories; auto-rotates in highlight |
| 4 | WhatsApp sticker pack | ✅ | 12 stickers; `ContentProvider` |
| 5 | Daily fortune push notification | ✅ | `DailyFortuneWorker` + user-set hour |
| 6 | Cosmic Year Report notification | ✅ | Rich notification with Dasha + fortune |

---

## Mission 2: Vedic Supremacy — ACTIVE 🔥

> **Goal:** Own the Indian market. No Western competitor has Vedic. AstroSage has 80M downloads but dated UX and extreme nickel-and-diming.

### 2a. Vedic Compatibility (ASHTAKOOT / GUNA MILAN) — CRITICAL 🔴

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Create `VedicCompatibilityScorer.kt` | ⬜ | 🔴 Critical | 8-Koot Ashtakoot (36-point Guna Milan) |
| 2 | Implement Varna Koot (1 point) | ⬜ | 🔴 Critical | Spiritual compatibility |
| 3 | Implement Vasya Koot (2 points) | ⬜ | 🔴 Critical | Mutual attraction |
| 4 | Implement Tara Koot (3 points) | ⬜ | 🔴 Critical | Birth star compatibility |
| 5 | Implement Yoni Koot (4 points) | ⬜ | 🔴 Critical | Sexual compatibility |
| 6 | Implement Graha Maitri Koot (5 points) | ⬜ | 🔴 Critical | Planetary friendship |
| 7 | Implement Gana Koot (6 points) | ⬜ | 🔴 Critical | Temperament matching |
| 8 | Implement Bhakut Koot (7 points) | ⬜ | 🔴 Critical | Kuta / relationship health |
| 9 | Implement Nadi Koot (8 points) | ⬜ | 🔴 Critical | Health / progeny compatibility |
| 10 | Composite score display (X/36) | ⬜ | 🔴 Critical | Indian users expect this format prominently |
| 11 | Interpretation text for each Koot | ⬜ | 🟡 High | What the score means in plain language |
| 12 | `CosmicMatchScreen.kt` — unified 3-system UI | ⬜ | 🟡 High | Western 25% + Chinese 25% + Vedic 50% weighting |

### 2b. Mangal Dosha (Manglik) Detection — CRITICAL 🔴

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Create `ManglikCalculator.kt` | ⬜ | 🔴 Critical | Mars in houses 1, 4, 7, 8, 12 = Mangal Dosha |
| 2 | Show Manglik flag in profile | ⬜ | 🔴 Critical | Must be prominent for Indian marriage compatibility |
| 3 | Manglik-to-Manglik matching rule | ⬜ | 🟡 High | "Both are Manglik = neutralizes" explanation |
| 4 | Partial Manglik (from Lagna vs Moon vs Venus) | ⬜ | 🟢 Medium | Advanced; shows depth |

### 2c. Navamsa (D-9) Chart — HIGH 🟡

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Navamsa division calculation | ⬜ | 🟡 High | Standard for marriage analysis in India |
| 2 | Navamsa Lagna (rising sign in D-9) | ⬜ | 🟡 High | Critical for marriage timing |
| 3 | Simple visual representation | ⬜ | 🟡 High | **Keep simple** — AstroSage's dense charts are unusable for beginners |
| 4 | Integration with compatibility screen | ⬜ | 🟢 Medium | Show Navamsa compatibility alongside Ashtakoot |

### 2d. Vedic Engine Depth — MEDIUM 🟢

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Nakshatra lord + deity + guna metadata | ⬜ | 🟢 Medium | BUG-076 |
| 2 | Planetary dignities (exaltation/debilitation/own/moolatrikona) | ⬜ | 🟢 Medium | BUG-073; shows we take Vedic seriously |
| 3 | Dasha balance at birth exposure | ⬜ | 🟢 Medium | BUG-075 (partial) |
| 4 | Pratyantar Dasha (sub-sub-period) | ⬜ | 🟢 Medium | BUG-075 |
| 5 | Tropical rising sign (Western Lagna) | ⬜ | 🟢 Medium | BUG-083; bridge feature for Western users |

### 2e. Vedic UI/UX — HIGH 🟡

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Hindi / Tamil / Telugu / Kannada string review | ⬜ | 🟡 High | Ensure all Vedic terms are localized |
| 2 | "Marriage Compatibility" dedicated flow | ⬜ | 🟡 High | High-intent use case; could be standalone screen |
| 3 | Simplified Kundli visual | ⬜ | 🟡 High | 12-house wheel; beginner-friendly; **not** AstroSage-complex |
| 4 | Indian festival push notifications | ⬜ | 🟢 Medium | Diwali, Raksha Bandhan, etc. tied to user's profile |
| 5 | Vedic educational content | ⬜ | 🟢 Medium | "What is Nakshatra?" "What is Dasha?" in-app glossary |

### 2f. Chinese Engine Depth — HIGH 🟡

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Day and Hour Pillars (complete Four Pillars) | ⬜ | 🔴 Critical | BUG-078; currently only Year and Month |
| 2 | Day Master + Ten Gods analysis | ⬜ | 🟡 High | BUG-079; core of Ba Zi interpretation |
| 3 | Solar term boundaries (astronomical calculation) | ⬜ | 🟡 High | BUG-080; fixes month pillar accuracy |
| 4 | Luck Pillars (大运 / Da Yun, 10-year cycles) | ⬜ | 🟢 Medium | BUG-081; standard for Chinese astrology depth |
| 5 | LunarCalendarConverter error handling (Result type) | ⬜ | 🟢 Medium | BUG-082 |

---

## Mission 3: Viral Growth Engine — ACTIVE 🔥

> **Goal:** Turn every user into a free marketer. Reduce paid UA to near-zero.

### 3a. Shareable Cards — HIGH 🟡

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | **Daily Fortune Card** | ⬜ | 🟡 High | Morning fortune as shareable Story card; drives daily opens |
| 2 | **Vedic Kundli Card** | ⬜ | 🟡 High | Simplified Kundli snapshot for sharing |
| 3 | **Milestone celebration card** (enhanced) | ⬜ | 🟢 Medium | "10,000 days" with confetti animation in PNG |
| 4 | **Compatibility result card** (enhanced) | ⬜ | 🟢 Medium | Show 3-system scores + composite + share CTA |
| 5 | Card watermark / branding | ⬜ | 🟢 Medium | Small "Cosmic ID" logo — free attribution |
| 6 | Card analytics | ⬜ | 🟢 Medium | Track share events by card type for optimization |

### 3b. Widget Virality — MEDIUM 🟢

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | **Lock Screen Widget (API 33+)** | ⬜ | 🟢 Medium | Major iOS 16+ / Android 14+ trend |
| 2 | Widget "screenshot reminder" | ⬜ | 🟢 Medium | Subtle CTA: "Long-press widget → screenshot → share" |
| 3 | Widget configuration UI | ⬜ | 🟢 Medium | Let users choose what the widget shows |
| 4 | Animated widget preview in app | ⬜ | 🔵 Low | Show how widget looks before adding to home screen |

### 3c. Social Loops — MEDIUM 🟢

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | **Referral program** | ⬜ | 🟢 Medium | "Share Cosmic ID, get 1 month free" — both parties win |
| 2 | **Cosmic Twins Discovery** | ⬜ | 🟢 Medium | Offline Rashi+Nakshatra matching; deferred from v2.0 |
| 3 | **Group compatibility** | ⬜ | 🔵 Low | Compare 3+ friends; viral for friend groups |
| 4 | **Instagram Story integration** | ⬜ | 🔵 Low | Direct share to Instagram Stories (if API allows) |

---

## Mission 4: Trust & Monetization — ACTIVE 🔥

> **Goal:** Be the "Stellium of the age+astrology space" — ethical billing, transparent pricing, no trial traps.

### 4a. Ethical Monetization — HIGH 🟡

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | **One-time lifetime SKU** | ⬜ | 🟡 High | `$49.99` — appeals to subscription-fatigued users; Stellium model |
| 2 | **Tip jar / voluntary support** | ⬜ | 🟢 Medium | "Support the cosmos" — indie-friendly; loyal users pay extra |
| 3 | **Referral rewards** | ⬜ | 🟢 Medium | Free month for referrer + referred |
| 4 | **Family sharing support** | ⬜ | 🔵 Low | Share premium across family members |
| 5 | **Regional pricing expansion** | ⬜ | 🟢 Medium | Southeast Asia (₹49 equivalent), LATAM, MENA |

### 4b. Pricing Page Transparency — MEDIUM 🟢

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | **"What's included" comparison table** | ⬜ | 🟢 Medium | Free vs Premium side-by-side in PaywallScreen |
| 2 | **Cancellation instructions in app** | ⬜ | 🟢 Medium | "How to cancel" FAQ — builds trust |
| 3 | **Trial end reminder notification** | ⬜ | 🟢 Medium | "Your trial ends in 2 days — here's what you'll lose" |
| 4 | **Lifetime deal urgency** | ⬜ | 🔵 Low | Limited-time lifetime offer for launch |

### 4c. Store Listing & ASO — HIGH 🟡

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | **App icon redesign** | ⬜ | 🟡 High | Cosmic/zodiac motif; move away from generic calculator look |
| 2 | **Feature graphic (1024×500)** | ⬜ | 🟡 High | Play Store top banner |
| 3 | **Screenshot refresh** | ✅ | 🟡 High | 5–8 screenshots at 1080×2400; onboarding, paywall, celebrity, widgets |
| 4 | **Short description (80 chars)** | ⬜ | 🟢 Medium | "Your Cosmic ID: Age, Astrology & Numerology" |
| 5 | **Full description (4000 chars)** | ⬜ | 🟢 Medium | SEO keywords: age calculator, kundli, nakshatra, compatibility |
| 6 | **Video preview (30 sec)** | ⬜ | 🔵 Low | Hero counter + widget + share card in action |
| 7 | **Content rating questionnaire** | ⬜ | 🟢 Medium | Play Console submission requirement |

### 4d. Analytics & Attribution — MEDIUM 🟢

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | **Share event logging by card type** | ⬜ | 🟢 Medium | Which cards drive most organic traffic? |
| 2 | **Widget interaction logging** | ⬜ | 🟢 Medium | Which widgets are most popular? |
| 3 | **Paywall conversion funnel** | ✅ | 🟢 Medium | `logPaywallShown()`, `logPaywallSubscribeTap()` |
| 4 | **Trial-to-paid conversion tracking** | ✅ | 🟢 Medium | `logTrialStarted()`, `logTrialConverted()` |
| 5 | **Geo-segmented analytics** | ⬜ | 🟢 Medium | India vs US vs UK behavior differences |
| 6 | **Churn reason survey** | ⬜ | 🔵 Low | Exit survey: "Why are you canceling?" |

---

## Mission 5: Platform Ecosystem — PLANNED 📋

> **Goal:** Expand beyond Android phones. Wearables, lock screen, iOS.

### 5a. Wear OS Companion

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Create `app-wear/` module | ⬜ | 🔵 Low | Watch face + complication support |
| 2 | Live seconds counter complication | ⬜ | 🔵 Low | Ultimate glanceable surface |
| 3 | Next-birthday complication | ⬜ | 🔵 Low | Most useful watch data for reminders |
| 4 | Dasha period glance | ⬜ | 🔵 Low | Vedic users want quick Mahadasha checks |

### 5b. Cloud Backup & Sync

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Firebase Firestore dependency | ⬜ | 🔵 Low | `firebase-firestore-ktx` |
| 2 | `CloudSyncRepository.kt` | ⬜ | 🔵 Low | Sync birthdays + preferences |
| 3 | Google sign-in opt-in | ⬜ | 🔵 Low | Settings → "Sync to Cloud" |
| 4 | Cross-device profile restore | ⬜ | 🔵 Low | New phone = instant profile transfer |

### 5c. Lock Screen Widget

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | API 33+ `WIDGET_FEATURE_RECONFIGURABLE` | ⬜ | 🔵 Low | Android 14+ trend |
| 2 | Size-adjustable widget | ⬜ | 🔵 Low | User resizes on lock screen |

### 5d. iOS Port (Long-Term)

| # | Task | Status | Priority | Notes |
|---|------|--------|----------|-------|
| 1 | Tech stack decision | ⬜ | 🔵 Low | Flutter vs React Native vs native SwiftUI |
| 2 | WidgetKit support | ⬜ | 🔵 Low | iOS 16+ home/lock screen widgets |
| 3 | iOS share extensions | ⬜ | 🔵 Low | Share sheet integration |

---

## Mission 6: AI & Advanced Depth — DEFERRED ⏸️

> **Goal:** Only pursue if genuinely personalized with full chart context. Not a "ChatGPT wrapper."

### 6a. What We Will NOT Build

| # | Feature | Why |
|---|---------|-----|
| 1 | **AI chatbot astrologer** | Users spot templated AI. Nebula saw 340% engagement lift but 30% faster churn. High dev cost, low retention. |
| 2 | **Live astrologer chat** | Sanctuary has billing fraud complaints. Requires operational overhead. Not scalable for indie. |
| 3 | **Face scan / brain age** | "How old am I?" face-scan app = 3.2★. Privacy concerns. Damages trust. |
| 4 | **Weekly subscriptions** | RevenueCat: highest churn, lowest LTV in Lifestyle. |
| 5 | **Complex Vedic chart rendering** | AstroSage's dense charts are unusable for beginners. Progressive disclosure wins. |
| 6 | **Animated MP4 Export** | Deferred from v2.0. High dev cost (`MediaCodec` + `MediaMuxer`), uncertain ROI vs shareable PNG cards. |

### 6b. What We MIGHT Build (With Strict Quality Gates)

| # | Feature | Conditions | Priority |
|---|---------|------------|----------|
| 1 | **AI-powered daily fortune** | Must use full natal chart + real transits + genuine astrological reasoning. No templates. | ⏸️ v3.x |
| 2 | **AI compatibility insights** | Must analyze synastry (planet-to-planet aspects) with real reasoning. | ⏸️ v3.x |
| 3 | **AI transit forecasting** | Must use real ephemeris data and explain *why* for *this user*. | ⏸️ v3.x |

---

## Technical Debt & Infrastructure

### Testing

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Unit tests for domain calculators | ✅ | 152+ tests passing |
| 2 | UI tests (Compose) for CalculatorScreen | ✅ | Happy path + date validation |
| 3 | Instrumented Room DAO tests | ✅ | `MigrationTestHelper` |
| 4 | Billing tests with test SKUs | ⬜ | `BillingClient` test flow |
| 5 | Deep-link intent-filter tests | ⬜ | `agereveal://profile/*` |
| 6 | Appium E2E suite (`walkthrough.py`) | ✅ | Updated for v2.0; 14 screenshots |

### Performance & Error Handling

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Profile widget update frequency | ✅ | `notifyWidget()` immediate updates |
| 2 | Remove exact alarm permissions (unnecessary) | ✅ | Cleaned manifest |
| 3 | Timezone-aware astronomical defaults | ✅ | `julianDay(LocalDateTime)` |
| 4 | Firebase Analytics MVP | ✅ | Onboarding, paywall, share, deep-link, purchase events |
| 5 | ProGuard/R8 obfuscation rules | ⬜ | Before production release |

### Build & Release

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Bump `versionCode` → `8` | ⬜ | Must be higher than v1.0.7's `7` |
| 2 | Verify `versionName` = `2.0.0` | ✅ | VERSION file updated |
| 3 | Replace AdMob test IDs with production | ⬜ | Banner ID only |
| 4 | Play Console subscription pricing declaration | ✅ | `premium_monthly` (₹49) + `premium_yearly` (₹299) |
| 5 | Privacy Policy URL | ✅ | `willowvibe.com/agereveal/privacy` |

---

## Release Checklist

### v2.1.0 — Korean Saju Supremacy + Vedic Depth (Target: 2026-06-30)

**Korean Saju (Mission 7) — primary:**
- [ ] `SajuKoreanCalculator.kt` created (Hangul 천간/지지 + 대운)
- [ ] Day + Hour Pillars (complete Four Pillars)
- [ ] Day Master + Ten Gods analysis
- [ ] Solar term boundary fixes (절기, astronomical)
- [ ] 대운 (Daeun) 10-year luck cycle calculator
- [ ] 오행 (Five Element) balance chart
- [ ] 오행 balance shareable card (radar/bar)
- [ ] 용신 (Yongshin) rule-based suggestion card
- [ ] DetailsUnlockScreen — "Korean Saju" tab (replaces "Chinese")
- [ ] Noto Sans KR typography in Compose
- [ ] `korean_saju_unlock` IAP live (₹149 / $2.99)
- [ ] Korean locale strings (`values-ko/`) for Saju UI
- [ ] 사주 궁합 (Korean Saju compatibility) scoring

**Vedic (Mission 2) — secondary:**
- [ ] Ashtakoot / Guna Milan (36-point) compatibility
- [ ] Mangal Dosha detection
- [ ] Navamsa (D-9) chart (simple visual)
- [ ] "Marriage Compatibility" dedicated flow
- [ ] Hindi/Tamil/Telugu/Kannada string review for Vedic terms

### v2.2.0 — Viral Growth (Target: 2026-07-31)

- [ ] Daily Fortune shareable card
- [ ] Vedic Kundli shareable card
- [ ] Lock Screen widget (API 33+)
- [ ] Referral program ("Share Cosmic ID, get 1 month free")
- [ ] Card watermark/branding
- [ ] Widget configuration UI

### v2.3.0 — Trust & Monetization (Target: 2026-08-31)

- [ ] One-time lifetime SKU ($49.99)
- [ ] Tip jar / voluntary support
- [ ] "What's included" comparison table in PaywallScreen
- [ ] Trial end reminder notification
- [ ] Cancellation instructions in app
- [ ] App icon redesign (cosmic/zodiac motif)
- [ ] Store listing video preview
- [ ] Regional pricing expansion (SEA, LATAM, MENA)

### v3.0.0 — Platform Ecosystem (Target: 2026-Q4)

- [ ] Wear OS module
- [ ] Cloud backup (Firebase Firestore)
- [ ] Lock screen widget (full)
- [ ] iOS port decision + prototype

---

## Mission 8: Portfolio Readiness — ACTIVE 🔥 (parallel sprint)

> **Goal:** Turn Cosmic ID from "impressive app" into "client-pitching pre-call sales material." Per 2026-06-05 research report. Runs in parallel with Mission 7 — does **not** block Korean Saju work.

### 8a. Ship & Surface (Week 1)

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Add 4–6 real UI screenshots to `/screenshots/` | ⬜ | Portfolio repos without screenshots look abandoned |
| 2 | Replace AdMob test IDs with production banner ID | ⬜ | Required before Play Store push (see `§1`) |
| 3 | Push to Play Store as "Early Access" | ⬜ | Even beta visibility beats in-development |
| 4 | Rewrite README — align v1.0 / v2.0 / beta status | ⬜ | Currently contradictory: README says v1.0 launched April 2026 but app is in development |

### 8b. UI Tests (Week 3)

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | 3–5 Compose UI tests on onboarding flow | ⬜ | Clients who review repos check this |
| 2 | 1–2 Compose UI tests on CalculatorScreen | ⬜ | Hero counter + date validation happy path |

### 8c. Cross-Platform Demo (Week 4)

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1 | Tech stack decision: Next.js vs React | ⬜ | Breaks the "Android-only" perception for web/SaaS clients |
| 2 | REST API for astro output (Kotlin/Spring or Ktor backend) | ⬜ | Reuses `BaZiCalculator`, `SajuKoreanCalculator`, `ZodiacCalculator` |
| 3 | Vercel-hosted demo page | ⬜ | Free tier; pre-call sales material |

---

## Appendix: File Naming Conventions

| Feature Area | File Prefix | Example |
|---|---|---|
| Billing | `*BillingManager.kt` | `BillingManager.kt` |
| Onboarding | `Onboarding*.kt` | `OnboardingScreen.kt` |
| Paywall | `Paywall*.kt` | `PaywallScreen.kt` |
| Deep-link | `ProfileDeepLink*.kt` | `ProfileDeepLinkGenerator.kt` |
| Widgets | `*GlanceWidget.kt` | `SecondsCounterGlanceWidget.kt` |
| Share generators | `draw*()` in `ShareCardGenerator.kt` | `drawStoryDarkCosmos()` |
| Calculators | `*Calculator.kt` | `CelebrityMatchCalculator.kt`, `SajuKoreanCalculator.kt` |
| Compatibility | `*CompatibilityScorer.kt` | `VedicCompatibilityScorer.kt`, `SajuKoreanCompatibilityScorer.kt` |
| Vedic | `Vedic*.kt` | `VedicCompatibilityScorer.kt`, `ManglikCalculator.kt` |
| Korean Saju | `SajuKorean*.kt` | `SajuKoreanCalculator.kt`, `SajuKoreanCompatibilityScorer.kt` |
| Data models | `data/model/*.kt` | `Celebrity.kt`, `Milestone.kt` |
| Repository | `data/repository/*.kt` | `BadgeRepository.kt` |
| UI screens | `ui/screen/*.kt` | `OnboardingScreen.kt` |
| ViewModels | `ui/viewmodel/*ViewModel.kt` | `CalculatorViewModel.kt` |
| JSON assets | `assets/*.json` | `celebrities.json` |
