# Cosmic ID Roadmap

---

## Phase 1: Core MVP (Complete) ✅

- Main screen UI: date picker, live age ticking seconds, basic calculators.
- Core logic: years, months, days, hours.
- AdMob Integrations: Banner.
- Share cards via Canvas bitmap rendering.
- Saved birthdays with Room DB & 2×2 Glance widget.
- Compare birthdays tool.

---

## Phase 2: Post-Launch Additions (Complete) ✅

- **Zodiac Compatibility Screen** — Western + Chinese compatibility scores.
- **Notification Customisation** — Settings to choose reminder hour.
- **4 × 2 Home Screen Widget** — Wider Glance widget showing multiple upcoming birthdays.
- **In-app Review Prompt** — Triggered after user shares their first card.

---

## Phase 3: Depth & Retention (Complete) ✅

- **Export Functions** — Google Calendar Intent & CSV export via `FileProvider`.
- **Educational Content** — `AstroInfoDialog` for Western, Vedic, Chinese, and Moon Phases.
- **Birth Time Support** — Precision calculations for Rashi and Nakshatra with *Exact* vs *Approximate* indicators.
- **Settings Consolidation** — Unified tab for Appearance, Notifications, Data, and About.
- **Milestone Timelines & Push UI** — Scrollable life timeline visual and per-milestone notification toggles.
- **Automated Tests** — JUnit 4 unit tests for all domain calculators.
- **Astrology Depth** — Nakshatra Pada, Rashi Lord, Western Moon Sign, and Chinese Stem-Branch added.

---

## Phase 4: Scale & Ecosystem (Complete) ✅

- **App Store Release Polish** — Replacing test IDs, generating Play Store assets, and final UI sweeps.
- **Advanced Astrology** — Lat/Lon location support for exact Ascendant/Lagna, Dasha periods, and Chinese Ba Zi pillars.
- **Gen Z Flexing Features** — Badges, widgets, life stats, story cards, time remaining, accent color, milestone ring, transparent overlay, daily fortune.

---

## Phase 5: v2.0 Revamp — Freemium & Viral Growth 🔥 (Active 🚧)

### 5.1 Monetisation Overhaul
- **Freemium Subscription** ✅ — Premium tier (₹49/mo or ₹299/yr) replaces ad-gated astrology. 7-day free trial.
- **Billing error handling** ✅ — Human-readable error messages for all BillingResponseCode values; retry CTA in PaywallScreen.
- **Restore purchases** ✅ — Mandatory "Restore purchases" button in PaywallScreen and Settings.
- **Free trial UX chip** ✅ — "N days left" chip in CalculatorScreen header during trial period.
- **Grace period for lapsed subscriptions** ✅ — 3-day grace tracked via `gracePeriodStart` in DataStore.
- **Remove Ads IAP** ⬜ — One-time purchase option (₹199) as an alternative to subscription.
- **Paywall Screen** ✅ — Subscription tiers with "BEST VALUE" yearly badge; error banner + retry + restore CTA.

### 5.2 Onboarding & Activation
- **3-Step Animated Onboarding** ✅ — Name + birth date → optional birth time + location → accent picker. Conditional start destination via `MainViewModel.hasCompletedOnboarding`.
- **Progressive Disclosure** ✅ — Main screen: hero counter + rotating highlight + "Explore full profile →" CTA. All other content in tabbed DetailsUnlockScreen (Overview | Western | Vedic | Chinese).

### 5.3 Social & Viral Loops
- **Deep-Link Profile Sharing** ✅ — `agereveal://profile/[data]` enables organic viral loops; auto-populates compatibility. "Copy link" button in CalculatorScreen.
- **Celebrity Birthday Matching** ✅ — 375 curated celebrities across 8 categories; matched by month+day; shown in rotating highlight card.
- **Animated MP4 Export** ⬜ — 5-second ticking-seconds video for Reels/TikTok (deferred to post-beta).
- **WhatsApp Sticker Pack** ⬜ — Direct import into WhatsApp; India's primary sharing surface (deferred to post-beta).
- **Cosmic Twins Discovery** ⬜ — Offline match by Rashi + Nakshatra; dual share card (deferred to post-beta).

### 5.4 Retention & Notifications
- **Daily Cosmic Fortune Push** ✅ — `DailyFortuneWorker` + `DailyFortuneScheduler` with user-set hour (default 8AM) and master toggle in Settings.
- **Cosmic Year Report Notification** ⬜ — Rich birthday notification with Mahadasha + fortune summary (currently only basic yearly re-engagement "You've lived X days" exists).
- **Re-engagement Notifications** ✅ — "You've lived X days!" on birthday; milestone reminders.

### 5.5 Brand & Identity
- **App Rename** ✅ — Renamed to **Cosmic ID**; display name, strings, and store docs updated.
- **Icon Redesign** ⬜ — Cosmic/zodiac motif instead of generic calculator look (planned for post-beta).
- **Premium Theme Packs** ⬜ — Vaporwave, Cottagecore, Y2K, Dark Academia, Cyberpunk.

---

## Phase 5.6: Calculation Engine Improvements (Complete) ✅

Audit of Vedic, Chinese, and Western engines (2026-05-23) identified 22 gaps. This phase addresses them in priority order. See [BUGS_AND_ISSUES.md](BUGS_AND_ISSUES.md) §Engine Architecture Audit for full details.

### 5.6.1 Foundation (enables everything else)

| Task | Priority | Effort | Bug IDs | Status |
|------|----------|--------|---------|--------|
| Add `AstronomicalCalculatorTest` — known-epoch verification | 🔴 Critical | S | BUG-071 | ✅ Complete |
| Introduce `BirthChart` model — decouple age math from astrology | 🔴 Critical | M | BUG-068 | ✅ Complete |
| Add `isRetrograde(planet, jd)` to AstronomicalCalculator | 🟡 High | S | BUG-074 | ✅ Complete |
| Consolidate `Planet` enums into shared `CelestialBody` | 🟢 Medium | S | BUG-069 | ✅ Complete |
| Add Uranus, Neptune, Pluto to `AstronomicalCalculator.Planet` | 🟢 Medium | S | BUG-084 | ✅ Complete |
| Add `LifeStatsCalculator` DI annotations (`@Singleton`, `@Inject`) | 🟢 Low | XS | BUG-089 | ✅ Complete |

**Files created/modified:**
- `domain/model/BirthChart.kt` — new comprehensive birth chart model
- `domain/model/CelestialBody.kt` — new consolidated celestial body enum
- `domain/AstronomicalCalculator.kt` — added `isRetrograde()` method and outer planets (Uranus, Neptune, Pluto)
- `domain/PlanetAgeCalculator.kt` — updated to use `CelestialBody` enum
- `domain/LifeStatsCalculator.kt` — added DI annotations
- `domain/ZodiacCalculator.kt` — added `getWesternSignNames()` and `getWesternSignName()` methods
- `ui/screen/CalculatorScreen.kt` — updated to use `CelestialBody`
- `app/src/test/java/com/willowvibe/agereveal/domain/AstronomicalCalculatorTest.kt` — new test file

### 5.6.2 Vedic Depth

| Task | Priority | Effort | Bug IDs |
|------|----------|--------|---------|
| Add Nakshatra lord + deity + guna metadata | 🟡 High | S | BUG-076 |
| Add planetary dignities (exaltation/debilitation/own/moolatrikona) | 🟡 High | M | BUG-073 |
| Expose Dasha balance at birth to UI | 🟢 Medium | XS | BUG-075 (partial) |
| Add Pratyantar Dasha (sub-sub-period) | 🟢 Medium | S | BUG-075 |
| Add Navamsa (D-9) divisional chart | 🟢 Medium | M | BUG-072 |
| Add Vedic compatibility (Ashtakoot/Guna Milan, 36-point) | 🔵 Low | L | BUG-077 |

### 5.6.3 Chinese Depth

| Task | Priority | Effort | Bug IDs |
|------|----------|--------|---------|
| Add Day and Hour Pillars (complete Four Pillars) | 🔴 Critical | M | BUG-078 |
| Add Day Master + Ten Gods analysis | 🟡 High | M | BUG-079 |
| Fix solar term boundaries to use astronomical calculation | 🟡 High | M | BUG-080 |
| Add Luck Pillars (大运 / Da Yun, 10-year cycles) | 🟢 Medium | L | BUG-081 |
| Improve LunarCalendarConverter error handling (Result type) | 🟢 Medium | XS | BUG-082 |

### 5.6.4 Western Depth

| Task | Priority | Effort | Bug IDs |
|------|----------|--------|---------|
| Add tropical (Western) rising sign | 🟡 High | XS | BUG-083 |
| Add planetary aspects (conjunction/sextile/square/trine/opposition) | 🟡 High | M | BUG-085 |
| Add birth moon phase to profile | 🟢 Medium | XS | BUG-086 |
| Add chart-to-chart synastry for compatibility | 🔵 Low | L | BUG-087 |

### 5.6.5 Refactoring (improves maintainability, no user-facing changes)

| Task | Priority | Effort | Bug IDs |
|------|----------|--------|---------|
| Split `ZodiacCalculator` (350 lines) into focused calculators | 🟡 High | M | BUG-070 |
| Add transit computation to DailyFortuneGenerator | 🔵 Low | L | BUG-088 |

### Effort Key

| Label | Meaning |
|-------|---------|
| XS | Trivial — < 30 min |
| S | Small — 1–2 hours |
| M | Medium — half a day |
| L | Large — 1–2 days |

---

## Phase 6: Platform Ecosystem (Planned)

| Feature | Status | Notes |
|---------|--------|-------|
| Cloud Backup (Firebase Firestore) | ⬜ Planned | Opt-in Google sign-in sync for saved profiles |
| Lock Screen Widget | ⬜ Planned | API 33+ `WIDGET_FEATURE_RECONFIGURABLE` |
| Wear OS Companion | ⬜ Planned | Live seconds counter, next-birthday complication |
| iOS Port | ⬜ Planned | Flutter or React Native with WidgetKit (long-term) |
| Animated MP4 Export | ⬜ Deferred | 5-second Reels/TikTok video (deferred from v2.0) |
| Cosmic Twins Discovery | ⬜ Deferred | Offline Rashi+Nakshatra matching (deferred from v2.0) |

---

## Build Info

| Item | Value |
|---|---|
| Version | 2.0.0 (production rollout in progress) |
| Current branch | `feat/smaller-features-v2` |
| Next phase | **Phase 6** — Platform Ecosystem |
| minSdk | 26 (desugaring enables API 21+) |
| targetSdk | 35 |
| compileSdk | 36 |
| Build status | ✅ Compiles; 152+ unit tests passing |
