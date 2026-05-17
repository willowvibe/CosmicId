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

## Phase 6: Platform Ecosystem (Planned)

- **Cloud Backup** — Firebase Firestore sync for saved profiles (opt-in Google sign-in).
- **Lock Screen Widget** — API 33+ `AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE`.
- **Wear OS Companion** — Live seconds counter and next-birthday complication.
- **iOS Port** — Flutter or React Native with WidgetKit (long-term).

---

## Build Info

| Item | Value |
|---|---|
| Version | 2.0.0 (beta-ready) |
| minSdk | 26 (desugaring enables API 21+) |
| targetSdk | 35 |
| compileSdk | 36 |
| Status | 🚧 Phase 5 Active — Beta branch `tasks-to-beta`; core v2.0 features complete; remaining: MP4 export, WhatsApp stickers, Cosmic Twins, Cosmic Match engine |
