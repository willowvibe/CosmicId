# AgeReveal Roadmap

---

## Phase 1: Core MVP (Complete) ✅

- Main screen UI: date picker, live age ticking seconds, basic calculators.
- Core logic: years, months, days, hours.
- AdMob Integrations: Banner, Interstitial, Rewarded.
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

## Phase 4: Scale & Ecosystem 🌐 (Active 🚧)

- **App Store Release Polish** — Replacing test IDs, generating Play Store assets, and final UI sweeps.
- **Premium Model** — Remove Ads IAP via Play Billing Library.
- **Cloud Backup** — Firebase Firestore sync for saved profiles.
- **Social Hooks** — WhatsApp sticker cards & age trivia party mode.
- **Advanced Astrology** — Lat/Lon location support for exact Ascendant/Lagna, Dasha periods, and Chinese Ba Zi pillars.

---

## Build Info

| Item | Value |
|---|---|
| Version | 1.0.0-rc1 |
| minSdk | 26 (desugaring enables API 21+) |
| targetSdk | 35 |
| compileSdk | 36 |
| Status | ✅ Release Candidate Prep |