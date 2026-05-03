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

## Phase 5: Gen Z Flexing & Viral Features 🔥 (Planned)

Designed for the "main character energy" generation — features built to screenshot, share, and flex.

### 5.1 Live Widgets (Home Screen & Lock Screen)
- **Live Seconds Counter Widget** ✅ — A compact 2×1 home-screen widget showing total seconds alive. Minimal, brutalist typography. Pure flex. (v1.0.5)
- **Lifespan Progress Bar Widget** ✅ — A 4×1 widget showing lifespan % with color-coded text (teal → amber → rose → red). Configurable target age in Settings. Updates once per day. (v1.0.5)
- **Milestone Countdown Widget** ✅ — "10,000 days · 1,234 days to go · 45%" compact 2×1 widget. Shows next upcoming milestone with progress percentage. Tap opens app. (v1.0.6)
- **Cosmic Clock Widget** — Digital-clock style widget showing current age in YY:MM:DD format, with a subtle zodiac icon that rotates based on current moon phase.

### 5.2 Shareable Formats (Stories, Reels, TikTok)
- **9:16 Story Cards** ✅ — Portrait share cards (1080×1920) with Instagram-safe margins. Square / Story format toggle in share sheet. (v1.0.5)
- **TikTok/Reels Green Screen** — Transparent-background stat cards (PNG with alpha) for users to drop into their own video content as overlays.
- **Animated Shareables** — MP4/GIF export of the seconds counter rolling, milestone fireworks, or compatibility score reveal with particle effects.
- **Daily Cosmic Fortune Card** — Auto-generated daily "vibe check" card mixing current moon phase + zodiac + a fortune-cookie style message. One-tap share every morning.

### 5.3 Social Proof & Comparison
- **Global Age Percentile** — "You're older than 73% of humans alive today" using UN population demographics data. Highly shareable stat.
- **Generational Badge** — Auto-detect Gen Z / Millennial / Gen X / Boomer / Alpha cohort and generate a shareable "Certified Gen Z — 1.2 billion seconds survived" badge.

### 5.4 Gamification & Achievements
- **Milestone Badges** ✅ — Unlockable collectible badges (13 total): "1M Seconds Club", "10K Days Society", "Billion Seconds Club", "Leap Baby", etc. Each badge is a shareable unlock card with confetti celebration. (v1.0.5)
- **Streaks** — "You've opened AgeReveal for 7 consecutive days." Small dopamine hit. Can be turned off in settings.
- **Life Stats Dashboard** ✅ — Aggregated fun facts: total full moons, Fridays the 13th survived, leap years, heartbeats, breaths, meals, words, steps. All shareable as individual cards. (v1.0.5)
- **Time Remaining Visuals** ✅ — "You have 1,247 weekends left until you turn 30." Configurable target age with Settings toggle. Morbid motivation. (v1.0.6)

### 5.5 Aesthetic & Vibes
- **Custom Accent Color Picker** ✅ — Six preset swatches (Mint, Amber, Pink, Blue, Purple, Emerald) in Settings → Appearance. Applied to share cards across DARK_COSMOS and MINIMAL_LIGHT themes. (v1.0.6)
- **Aesthetic Theme Packs** — Curated visual packs: Vaporwave (sunset gradients + chrome text), Cottagecore (floral + serif), Y2K (bling + bubblegum), Dark Academia (sepia + old maps), Cyberpunk (neon + grid).
- **Retro ASCII Art Share** — "YOUR AGE IN ASCII" — generate monospace art of your age digits for copy-paste to Discord, Reddit, or terminal screenshots.
- **Widget Transparency & Shape** — Round-corners, squircle, or pill-shaped widgets to match user home-screen aesthetic (iOS 18 / Samsung One UI style).

### 5.6 "What If" & Interactive Fun
- **Planet Age Converter** — "On Mars, you're only 14 years old. On Mercury, you're 249." Shareable planetary age cards for all 8 planets + Pluto (because Gen Z still considers Pluto a planet).
- **Time Machine** — "What did the moon look like when you were born?" with a visual moon-phase render. "What constellation was overhead?" with a star-map snippet.
- **Parallel Universe Birth** — "If you were born in 1920s India / 1980s Tokyo / ancient Rome..." Alternate historical context card.
- **Voice Narration** — One-tap "Read My Cosmic Profile" using Text-to-Speech with a dramatic/ASMR voice option. Can be exported as audio snippet.

---

## Build Info

| Item | Value |
|---|---|
| Version | 1.0.6 |
| minSdk | 26 (desugaring enables API 21+) |
| targetSdk | 35 |
| compileSdk | 36 |
| Status | ✅ Phase 5 Active — Badges, Widgets, Life Stats, Story Cards, Time Remaining, Accent Color, Milestone Ring shipped |
