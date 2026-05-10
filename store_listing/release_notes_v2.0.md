# Release Notes

## v2.0.0 (versionCode 8) — Major Revamp

### New Features
- 🚀 **3-Step Animated Onboarding** — First-launch flow with date picker, instant zodiac reveal, and optional birth time. Sets the tone and maximises activation.
- 💎 **Freemium Subscription Model** — Premium tier (₹49/month or ₹299/year) unlocks full astrology depth, removes ads, enables MP4 exports, and grants exclusive theme packs. 7-day free trial.
- 🔗 **Deep-Link Profile Sharing** — Share your cosmic profile as a URL. Friends tap to view your profile or auto-populate compatibility comparison.
- 🌟 **Celebrity Birthday Matching** — "You share a birthday with [Name]" matched from a curated celebrity database.
- 🎬 **Animated MP4 Export** — 5-second ticking-seconds video rendered with MediaCodec for Reels/TikTok.
- 💬 **WhatsApp Sticker Pack Export** — Direct import of cosmic stickers into WhatsApp.
- 🔮 **Daily Cosmic Fortune Push Notification** — Delivered at your chosen time (default 8AM) instead of a silent card.
- 🎂 **Cosmic Year Report Notification** — Rich birthday notification with "You've lived [X] days" + Mahadasha + fortune summary.
- 👥 **Cosmic Twins Discovery** — Offline match users with identical Rashi + Nakshatra combos; generate dual share cards.
- 🪐 **Planet Age Hero CTA** — "On Mars, you're only 14" as a prominent shareable card on your profile.

### Redesigns
- 📐 **Progressive Disclosure** — Main screen now shows only hero counter + one rotating highlight card + "Explore full profile →" CTA. All other cards moved to a tabbed Details screen.
- 🏠 **"My Cosmos" Tab** — Formerly "You"; renamed to signal the astrology identity.
- 🎖️ **Badges Moved** — Badge collection now lives inside "My Cosmos" instead of a bottom-nav tab.

### Removed Features
- 🗑️ **Rewarded & Interstitial Ads** — Replaced by the freemium subscription. Only banner ads remain on the free tier.
- 🗑️ **Parallel Universe Birth** — Confusing concept with low shareability.
- 🗑️ **Retro ASCII Art Share** — Not shareable on modern platforms.
- 🗑️ **Work Weeks on Main Screen** — Moved to Life Stats in Details; too morbid as a first impression.
- 🗑️ **Custom Hindi Language Toggle** — Android system locale (API 33+) handles this natively.

### Premium-Only Content
- Full Vimshottari Dasha (Mahadasha / Antardasha)
- Ba Zi (Four Pillars) Year + Month
- Exact Lagna (Ascendant) with location
- Full planetary positions table
- Animated MP4 exports
- Exclusive theme packs (Vaporwave, Cottagecore, Y2K, Dark Academia, Cyberpunk)

## v1.0.7 (versionCode 7) — Play Store beta

Highlights
- 🌍 **Global Age Percentile** — "You're older than X% of people alive today" using UN World Population Prospects 2024 data.
- 🏖️ **Retirement Calculator** — configurable retirement age (55/60/65/70). Shows days until retirement, work weeks left, and % of work life complete.
- 🪐 **Parallel Universe Birth** — see what your age means in 8 historical contexts (Ancient Rome, 1920s India, 1980s Tokyo, etc.).
- 🔮 **Daily Cosmic Fortune** — a new fortune every day based on moon phase, sun sign, and Chinese stem-branch. One-tap share as a dedicated card.
- 🎬 **Transparent Green-Screen Overlay** — 1080×1920 transparent PNG for TikTok/Reels/YouTube Shorts.
- 🎞️ **9:16 Story Cards** — portrait share cards optimized for Instagram Stories and Snapchat.
- 📊 **Life Stats Dashboard** — heartbeats, breaths, meals, words spoken, steps walked, Fridays the 13th survived, full moons witnessed. Each stat is individually shareable.
- 🏅 **Badge System** — unlock 13 achievement badges (1M Seconds, Billion Seconds Club, 10K Days, Leap Baby, etc.). Confetti celebration + shareable unlock cards.
- 🌙 **Birth Moon Phase Visual** — Canvas-drawn moon phase showing exactly what the moon looked like on your birthday.
- 🚀 **Planet Ages** — discover your age on Mercury, Venus, Mars, Jupiter, Saturn, Uranus, Neptune, and Pluto.
- 🎂 **Yearly Re-engagement Notification** — "You've now lived X days!" message on your birthday every year. Auto-reschedules for next year.
- 👾 **Generation Badge** — Certified Gen Z, Millennial, Gen X, Baby Boomer, or Gen Alpha with seconds survived count.
- 🎨 **Accent Color Picker** — 6 preset swatches (Mint, Amber, Pink, Blue, Purple, Emerald). Applies to share cards and UI highlights.
- ⏳ **Time Remaining Visuals** — weekends left, Fridays, paychecks, full moons until your target age.
- 🖼️ **Retro ASCII Art Share** — copy your total seconds as monospace block art to clipboard.
- 🏠 **New Widgets** — 2×1 seconds counter, 4×1 lifespan progress bar, 2×1 milestone countdown ring.
- 🔒 **Enhanced Astrology** —
  - Exact Lagna (Ascendant) with optional latitude/longitude input
  - Vimshottari Dasha approximation (Mahadasha / Antardasha)
  - Tithi calculation (lunar day with Paksha)
  - Ba Zi (Four Pillars) Year + Month approximation
  - Full planetary positions table (Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn)
  - Western Moon Sign
  - Chinese Stem-Branch with Wu Xing element

Fixes
- All share cards are 900×900 square — no cropping on WhatsApp/Instagram
- Share compatibility/milestone no longer crash when context is ApplicationContext
- Milestone notification IDs raised above birthday ID range (no collision)
- Ephemeris calculations cached per birth moment for better performance
- Interstitial ad retry uses exponential backoff (1s → 2s → 4s)
- Room migration safe — no destructive fallback
- Hindi locale toggle works via Settings
- Rolling digits animation uses stable Compose keys (no composition leak)
- AdManager uses proper Activity reference during ad display
