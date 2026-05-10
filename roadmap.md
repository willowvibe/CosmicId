# AgeReveal Roadmap

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
- **Remove Ads IAP** ⬜ — One-time purchase option (₹199) as an alternative to subscription.
- **Paywall Screen** ⬜ — Beautiful upsell shown when tapping locked features or on 3rd open.

### 5.2 Onboarding & Activation
- **3-Step Animated Onboarding** ⬜ — Date picker → instant zodiac reveal → optional birth time. Critical for <10% install-to-active conversion.
- **Progressive Disclosure** ⬜ — Main screen reduced to hero counter + rotating highlight + CTA. All other cards moved to tabbed Details screen.

### 5.3 Social & Viral Loops
- **Deep-Link Profile Sharing** ⬜ — `agereveal://profile/[data]` enables organic viral loops; auto-populates compatibility.
- **Celebrity Birthday Matching** ⬜ — "You share a birthday with [Name]" — most retweetable stat.
- **Animated MP4 Export** ⬜ — 5-second ticking-seconds video for Reels/TikTok.
- **WhatsApp Sticker Pack** ⬜ — Direct import into WhatsApp; India's primary sharing surface.
- **Cosmic Twins Discovery** ⬜ — Offline match by Rashi + Nakshatra; dual share card.

### 5.4 Retention & Notifications
- **Daily Cosmic Fortune Push** ⬜ — Delivered as push notification at user-set time instead of silent card.
- **Cosmic Year Report Notification** ⬜ — Rich birthday notification with Mahadasha + fortune summary.
- **Re-engagement Notifications** ✅ — "You've lived X days!" on birthday; milestone reminders.

### 5.5 Brand & Identity
- **App Rename** ⬜ — Evaluate **Nakshatra**, **CosmAge**, or **BornAt** to signal experience over utility.
- **Icon Redesign** ⬜ — Cosmic/zodiac motif instead of generic calculator look.
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
| Version | 2.0.0 (revamp in progress) |
| minSdk | 26 (desugaring enables API 21+) |
| targetSdk | 35 |
| compileSdk | 36 |
| Status | 🚧 Phase 5 Active — Freemium, onboarding, viral features |
