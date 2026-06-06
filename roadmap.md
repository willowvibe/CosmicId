# Cosmic ID Roadmap — Market-Driven Strategy v2.1

_Last updated: 2026-05-24 — Complete market research synthesis applied. Roadmap restructured around 5 strategic missions._

---

## Strategic Context

The astrology app market is a **$5.69B industry growing at 20% CAGR**, yet it is plagued by:
- **Predatory monetization** (Nebula's $9.99/week trial trap, The Pattern's cancel loop)
- **Fragmented experiences** (age calculators, astrology apps, and widgets are separate tools)
- **Shallow personalization** (most apps stop at "Big Three" Sun/Moon/Rising)
- **Vedic astrology neglect** (Co-Star and The Pattern have zero Vedic support; AstroSage has 80M downloads but dated UX)

**Cosmic ID's strategic moat:** The intersection of **exact age + tri-system astrology (Western + Vedic + **Korean Saju**) + numerology + widgets + shareable cards** — the only "Cosmic Identity" platform of its kind in the Play Store. K-drama / K-pop / K-diaspora audience is a searchable, monetisable niche; Vedic + Chinese Ba Zi alone are crowded.

**Primary market:** India (1.5B population, ₹163M FaithTech market, AstroSage charges ₹1,999/mo).  
**Secondary market:** US/UK Gen Z women (high social sharing, $4.99/mo tolerance).  
**Tertiary market:** **Global K-fandom (K-drama, K-pop, K-diaspora)** — drives the Korean Saju premium tier.  
**Viral mechanism:** Widget screenshots + shareable cards (age, compatibility, 오행 balance, Kundli) as organic UA.

---

## Mission Structure

| Mission | Focus | Status |
|---------|-------|--------|
| **Mission 1: Cosmic Identity Core** | The unified experience (age + astrology + numerology + widgets + sharing) | ✅ **LIVE in v2.0** |
| **Mission 2: Vedic Supremacy** | Own the Indian market with modern Vedic depth | 🔥 **ACTIVE** |
| **Mission 3: Viral Growth Engine** | Widget-first + shareable cards + social loops | 🔥 **ACTIVE** |
| **Mission 4: Trust & Monetization** | Ethical billing, transparent pricing, no trial traps | 🔥 **ACTIVE** |
| **Mission 5: Platform Ecosystem** | Wear OS, iOS, cloud backup, lock screen widgets | 📋 **PLANNED** |
| **Mission 6: AI & Advanced Depth** | Only if genuinely personal, not templated | ⏸️ **DEFERRED / CAUTIOUS** |
| **Mission 7: Korean Saju Supremacy** | Own the K-fandom niche with 사주 (천간/지지, 대운, 오행, 용신) | 🔥 **ACTIVE — v2.1** |

---

## Mission 1: Cosmic Identity Core — COMPLETE ✅

> **Goal:** Ship a unified "Cosmic Identity" app that combines age, astrology, numerology, widgets, and sharing — something no competitor has.

### What We Shipped in v2.0
- **Live Age Hero Counter** — Real-time years/months/days/hours/minutes/seconds
- **Multi-System Astrology** — Western (tropical), Vedic (Lahiri ayanamsa), Korean Saju (사주 — Hangul 천간·지지, 대운, 오행, 용신); Chinese Ba Zi (Four Pillars) retained as the data path for Day/Hour pillars
- **6+ Home Screen Widgets** — Seconds counter, birthday countdown, lifespan ring, milestone ring, wide birthday list, birthday count
- **Shareable Cards** — 1080×1920 PNG export for Instagram Stories (age card, compatibility card, zodiac card)
- **Birthday Reminders** — Saved birthdays, milestone alerts, CSV export
- **Celebrity Birthday Matching** — 375 curated celebrities across 8 categories
- **Daily Fortune Push** — WorkManager-based daily notifications with user-set hour
- **Premium Theme Packs** — Vaporwave, Cottagecore, Y2K, Dark Academia, Cyberpunk
- **3-Step Animated Onboarding** — Name + birth date → optional birth time + location → accent picker
- **Progressive Disclosure** — Hero counter + rotating highlight + "Explore full profile →" CTA; deep content in tabbed DetailsUnlockScreen
- **Freemium Monetization** — ₹49/mo or ₹299/yr (India); clear annual/monthly tiers; 7-day trial; no trial trap

### Why This Is a Sure Hit
- **No competitor** combines all these modalities in one app.
- **Widget-first engagement** drives daily habitual opens without push notification fatigue.
- **Shareable cards** are free billboards — every share is a new user acquisition.
- **India pricing** (₹299/yr) undercuts AstroSage's cloud software (₹1,999/mo) by **80×**.

---

## Mission 2: Vedic Supremacy — ACTIVE 🔥

> **Goal:** Become the default Vedic astrology app for the 1.5B Indian market and global diaspora. Co-Star and The Pattern ignore Vedic entirely. AstroSage has 80M downloads but a dated, nickel-and-diming UX.

### 2.1 Vedic Depth (Engine Layer) — COMPLETE (Phase 6.5 ephemeris overhaul)

| Feature | Status | Market Justification |
|---------|--------|---------------------|
| **Nakshatra + Pada + Lord + Deity + Gana + Symbol** | ✅ Complete | Table stakes for any Vedic app |
| **Dasha periods (Vimshottari)** | ✅ Complete | Users expect Mahadasha/Antardasha at minimum |
| **Pratyantar Dasha (sub-sub-period)** | ✅ Complete | Depth signal — shows we take Vedic seriously |
| **Ashtakoot / Guna Milan (36-point, 8 kootas)** | ✅ Complete | **#1 requested feature** by Indian users for compatibility; AstroSage charges ₹299–₹499 per report |
| **Navamsa (D-9) chart** | ✅ Complete | Standard for marriage analysis; AstroSage paywalls this |
| **Planetary dignities** (exaltation/debilitation/own/moolatrikona) | ✅ Complete | Differentiator from shallow apps |
| **Tropical rising sign (Western Lagna)** | ✅ Complete | Bridge feature for Western users curious about Vedic |
| **Planetary aspects (Western: conjunction/sextile/square/trine/opposition)** | ✅ Complete | Standard for both Western and Vedic interpretation |
| **Synastry (chart-to-chart cross-aspects + 0-100 score)** | ✅ Engine | `SynastryCalculator` shipped 2026-06-05 (Phase 6.5); UI card pending Phase E |
| **Meeus Ch. 47 Moon (60-term)** | ✅ Complete | Sub-arcminute Moon accuracy |
| **IAU 2000B nutation (50-term)** | ✅ Complete | Used for Sun, Moon, ascendant |
| **Meeus Ch. 32/33 planets** | ✅ Complete | Sign-level accuracy for all 8 planets |
| **Lahiri ayanamsa (cubic + quartic)** | ✅ Complete | Stays within 0.01° through 2100 |
| **Birth moon phase** | ✅ Complete | `BirthChart.birthMoonPhase` populated from snapshot (BUG-086) |
| **Daily fortune entertainment disclaimer** | ✅ Complete | `DailyFortuneGenerator.Fortune.disclaimer` (BUG-088) |
| **Lunar calendar Result-type API** | ✅ Complete | `LunarCalendarConverter.toLunarResult()` (BUG-082) |

See `docs/ephemeris-upgrade.md` for the full reference (Meeus chapter map, accuracy budgets, license analysis).

### 2.2 Vedic UI/UX — IN PROGRESS

| Feature | Status | Market Justification |
|---------|--------|---------------------|
| **Kundli-style visual chart** | ⬜ Not started | Indian users expect this visual language; but keep it **simple** (AstroSage's dense charts are unusable for beginners) |
| **"Marriage Compatibility" dedicated flow** | ⬜ Not started | High-intent use case; could be a standalone screen with Ashtakoot score prominently displayed |
| **Hindi / Tamil / Telugu / Kannada localization** | ⬜ Partial | System locale already supported; need to ensure all Vedic terms have localized strings |
| **Indian festival integration** | ⬜ Not started | Push notifications for Diwali, Raksha Bandhan, etc. tied to user's cosmic profile |

### 2.3 Why This Is a Sure Hit
- **1.5B addressable market** in India alone.
- **AstroSage has 80M downloads** but users complain about dated UI and extreme nickel-and-diming.
- **Co-Star = 0 Vedic support.** The Pattern = 0 Vedic support. Chani = 0 Vedic support. **This is a massive white space.**
- **Ashtakoot scoring** is the #1 reason Indian users open astrology apps — it's for marriage decisions.

---

## Mission 3: Viral Growth Engine — ACTIVE 🔥

> **Goal:** Turn every user into a free marketer through widget screenshots and shareable cards. Reduce paid UA to near-zero.

### 3.1 Widget-First Virality

| Feature | Status | Market Justification |
|---------|--------|---------------------|
| **Seconds Counter Widget** | ✅ Complete | Inherently viral — people screenshot and share "I've been alive for 631,233,091 seconds" |
| **Birthday Countdown Widget** | ✅ Complete | Drives habitual daily glances without push notifications |
| **Milestone Ring Widget** | ✅ Complete | Visual progress toward next milestone (10,000 days, etc.) |
| **Wide Birthday List Widget** | ✅ Complete | Shows upcoming birthdays at a glance |
| **Lifespan Progress Widget** | ✅ Complete | "You've lived 32% of an average lifespan" — shareable stat |
| **Lock Screen Widget (API 33+)** | ⬜ Planned | iOS 16+ and Android 14+ lock screen customization trend |
| **Widget theming** | ✅ Partial | Accent color theming; premium packs add Vaporwave/Cottagecore/etc. |

### 3.2 Shareable Cards as UA Engine

| Feature | Status | Market Justification |
|---------|--------|---------------------|
| **Age Card** (1080×1920) | ✅ Complete | "I am X years, Y months, Z days old" — inherently shareable |
| **Compatibility Card** | ✅ Complete | Couples/friends love posting compatibility scores |
| **Zodiac Card** | ✅ Complete | "My Big Three" card for Instagram Stories |
| **Celebrity Match Card** | ✅ Complete | "I share a birthday with [Celebrity]" — viral hook |
| **Milestone Card** | ✅ Complete | "10,000 days alive" celebration card |
| **Daily Fortune Card** | ⬜ Not started | Morning fortune as a shareable Story card |
| **Vedic Kundli Card** | ⬜ Not started | Simplified Kundli snapshot for sharing |
| **Theme pack designs** | ✅ Complete | 5 premium themes (Dark Academia, Cottagecore, etc.) — users *want* to post beautiful cards |

### 3.3 Social Loops & Deep Links

| Feature | Status | Market Justification |
|---------|--------|---------------------|
| **Profile deep-link sharing** | ✅ Complete | `agereveal://profile/[data]` — auto-populates compatibility screen |
| **HTTPS fallback for non-installed users** | ✅ Complete | `https://willowvibe.com/agereveal/profile/*` with HTML preview page |
| **WhatsApp Sticker Pack** | ✅ Complete | 12 cosmic-themed stickers; India's primary sharing surface |
| **Cosmic Twins Discovery** | ⬜ Deferred | Offline Rashi+Nakshatra matching; deferred to post-v2.0 |
| **Animated MP4 Export** | ⬜ Deferred | 5-second Reels/TikTok video; high dev cost, uncertain ROI |

### 3.4 Why This Is a Sure Hit
- **AstroCards** proves users will pay €1 for a shareable card — we give them **for free** as organic UA.
- **Widget screenshots** are the #1 organic discovery mechanism for age/astrology apps on TikTok and Instagram.
- **Every shared card is a free ad.** At 1% daily share rate with 100K MAU = 1,000 free impressions/day.
- **WhatsApp stickers** are India's #1 sharing surface — we already shipped this.

---

## Mission 4: Trust & Monetization — ACTIVE 🔥

> **Goal:** Be the "Stellium of the age+astrology space" — clear pricing, easy cancellation, meaningful free tier. In a category poisoned by predatory billing, trust is the ultimate differentiator.

### 4.1 Ethical Monetization (Already Shipped in v2.0)

| Feature | Status | Market Justification |
|---------|--------|---------------------|
| **Transparent pricing** | ✅ Complete | ₹49/mo, ₹299/yr — displayed clearly on PaywallScreen |
| **7-day free trial** | ✅ Complete | Explicitly disclosed; no hidden auto-renewal trickery |
| **Easy restore purchases** | ✅ Complete | "Restore purchases" in PaywallScreen + Settings |
| **Grace period for lapsed subs** | ✅ Complete | 3-day grace before features lock — user-friendly |
| **No weekly subscriptions** | ✅ Complete | RevenueCat data: weekly plans have highest churn in Lifestyle |
| **No paywall creep** | ✅ Complete | Free tier defined and locked; no moving goalposts |
| **No notification upsells** | ✅ Complete | Push notifications are content-only (fortune, birthday, milestone) |
| **One-time lifetime option** | ⬜ Not started | Stellium gets disproportionately positive reviews for avoiding subscriptions; add $49.99 lifetime SKU |
| **Tip jar / voluntary support** | ⬜ Not started | Indie-friendly; works well for apps with loyal user bases |

### 4.2 Pricing Strategy

| Tier | India | US/UK | What You Get |
|------|-------|-------|--------------|
| **Free** | Free | Free | Live age, basic Western zodiac, daily fortune, 1 widget, basic share cards, 3 saved birthdays |
| **Monthly** | ₹49 (~$0.58) | $4.99 | Full Vedic + Chinese + Numerology, all widgets, unlimited share card themes, unlimited birthdays, advanced compatibility, ad-free |
| **Yearly** | ₹299 (~$3.50) | $29.99 (~50% off monthly) | Same as monthly; best value badge |
| **Lifetime** | TBD | $49.99 | One-time unlock; appeals to subscription-fatigued users |

**Why this undercuts everyone:**
- Co-Star: $11.99/mo → we are **58% cheaper**
- The Pattern: $14.99/mo → we are **67% cheaper**
- Nebula: $9.99/week ($40/mo) → we are **88% cheaper**
- AstroSage Cloud: ₹1,999/mo → we are **87% cheaper** (and our yearly is ₹299 vs their monthly ₹1,999)

### 4.3 Why This Is a Sure Hit
- **#1 user complaint across ALL astrology apps** is predatory billing (Nebula refund refusal, The Pattern cancel loop, Co-Star paywall creep).
- **Stellium** (one-time purchase model) gets disproportionately positive reviews for avoiding subscriptions.
- **Transparency builds trust** — trust drives organic referrals, which drive lower CAC.
- **Indian pricing** at ₹299/yr is a no-brainer vs AstroSage's per-report pricing (₹299 per report, ₹1,999/mo for cloud).

---

## Mission 5: Platform Ecosystem — PLANNED 📋

> **Goal:** Expand Cosmic ID beyond Android phones into wearable, lock screen, and cross-platform ecosystems.

### 5.1 Wear OS Companion

| Feature | Status | Market Justification |
|---------|--------|---------------------|
| **Live seconds counter complication** | ⬜ Planned | Wearables are the ultimate "glanceable" surface for a live age counter |
| **Next-birthday complication** | ⬜ Planned | Most useful watch face data for birthday reminder users |
| **Dasha period glance** | ⬜ Planned | Vedic users want quick Mahadasha checks on watch |

### 5.2 Lock Screen Widget (Android 14+/iOS 16+)

| Feature | Status | Market Justification |
|---------|--------|---------------------|
| **API 33+ `WIDGET_FEATURE_RECONFIGURABLE`** | ⬜ Planned | Lock screen customization is a major iOS 16+ and Android 14+ trend |
| **Size-adjustable widget** | ⬜ Planned | Users resize widgets on lock screen |

### 5.3 Cloud Backup & Cross-Device

| Feature | Status | Market Justification |
|---------|--------|---------------------|
| **Firebase Firestore sync** | ⬜ Planned | Opt-in Google sign-in; sync saved birthdays and preferences |
| **Cross-device profile** | ⬜ Planned | User switches phones, profile transfers instantly |
| **Family sharing** | ⬜ Not started | Share premium subscription across family members |

### 5.4 iOS Port (Long-Term)

| Feature | Status | Market Justification |
|---------|--------|---------------------|
| **Tech stack decision** | ⬜ Planned | Flutter vs React Native vs native SwiftUI |
| **WidgetKit support** | ⬜ Planned | iOS 16+ home screen and lock screen widgets |
| **iOS share extensions** | ⬜ Planned | Share sheet integration for cosmic cards |

### 5.5 Why This Is Important (But Not Urgent)
- **Wear OS** has low install base but extremely high engagement per user.
- **iOS** is 50% of US/UK market — necessary for secondary market expansion.
- **Cloud sync** reduces churn when users switch phones (a major churn event for astrology apps).

---

## Mission 6: AI & Advanced Depth — DEFERRED ⏸️

> **Goal:** Only pursue AI if it is genuinely personalized with full chart context — not a "ChatGPT wrapper."

### What We Will NOT Build (Based on Market Research)

| Feature | Why We're Avoiding It |
|---------|----------------------|
| **AI chatbot astrologer** | Users see through templated AI responses. High dev cost, low retention value. Nebula's AI chat got 340% engagement lift initially but 30% faster churn. |
| **Live astrologer chat** | Sanctuary's model has billing fraud complaints, requires operational overhead (vetting astrologers, per-minute billing, quality control). Not scalable for indie dev. |
| **Face scan / brain age** | "How old am I?" face-scan app has 3.2★ with privacy concerns. Damages trust. |
| **Complex Vedic chart rendering** | AstroSage's dense Kundli charts are unusable for beginners. Progressive disclosure (simple → advanced) beats information density. |
| **Weekly subscription plans** | RevenueCat data: weekly plans have highest churn and lowest LTV in Lifestyle. Stick to monthly/annual. |

### What We MIGHT Build (With Strict Quality Gates)

| Feature | Conditions for Building |
|---------|------------------------|
| **AI-powered daily fortune** | Only if trained on user's full natal chart (all planets, houses, aspects) + current transits. Not templated. |
| **AI compatibility insights** | Only if it analyzes synastry (planet-to-planet aspects) with genuine astrological reasoning, not generic platitudes. |
| **AI transit forecasting** | Only if it uses real ephemeris data and explains *why* a transit matters for *this specific user*. |

### Why This Is the Right Call
- **AI lifestyle apps grew 691% YoY** in 2025 but suffer from **30% faster churn** — the hype drives sales but not lasting value.
- **28% of health/fitness apps** now bid on AI-themed keywords for ASO — the space is getting crowded.
- **Users can spot templated AI** immediately. One generic response destroys trust permanently.
- **Our current deterministic fortune generator** is already "good enough" for v2.x. AI is a v3.0+ bet only if we can do it right.

---

## Mission 7: Korean Saju Supremacy — ACTIVE 🔥 (v2.1)

> **Goal:** Become the **only tri-system astrology app in the Play Store** by owning Korean Saju (사주) as the East-Asian pillar. While every Western competitor (Co-Star, The Pattern, Chani) has zero Vedic AND zero Korean, and AstroSage owns Vedic but ignores Korean, Cosmic ID will own both Indian AND K-fandom audiences in a single install.

### 7.1 Why Korean Saju, Not Chinese Ba Zi?

Chinese Ba Zi is undifferentiated — dozens of apps in the Play Store offer it. Korean Saju is the **same math** (Four Pillars / 사주 / 四柱) but with a **distinct cultural + naming layer** that almost no app gets right:

| Layer | Chinese (Ba Zi) | Korean (Saju / 사주) |
|---|---|---|
| Heavenly Stems | 甲乙丙丁戊己庚辛壬癸 | 갑(甲)·을(乙)·병(丙)·정(丁)·무(戊)·기(己)·경(庚)·신(辛)·임(壬)·계(癸) in Hangul |
| Earthly Branches | 子丑寅卯辰巳午未申酉戌亥 | 자(子)·축(丑)·인(寅)·묘(卯)·진(辰)·사(巳)·오(午)·미(未)·신(申)·유(酉)·술(戌)·해(亥) in Hangul |
| Luck Periods | 大运 (Da Yun) | **대운 (Daeun)** — distinct UI/labels, different from Chinese and from Vedic Dasha |
| Element Colours | Generic red/blue/yellow | Korean cultural palette: 木(green), 火(red), 土(yellow), 金(white), 水(black/blue) |
| Favourable Element | 用神 (Yongshin) — same | **용신 (Yongshin)** — same concept, Korean presentation, emoji + colour suggestion card |
| Audience | Mainland Chinese expats (small) | **K-drama, K-pop, K-diaspora (global, growing, high-spend)** |

### 7.2 Korean Saju Engine — IN PROGRESS

| Feature | Status | Notes |
|---|---|---|
| **`SajuKoreanCalculator.kt`** (new file) | ⬜ Not started | Hangul 천간/지지만 — owns the Korean naming layer; do **not** collapse into `BaZiCalculator.kt` |
| Day Pillar + Hour Pillar math (complete Four Pillars) | ⬜ Not started | Currently only Year + Month in `BaZiCalculator.kt`; needed for real 사주 reading |
| Day Master (일간) + Ten Gods (십신) analysis | ⬜ Not started | Core interpretation layer |
| Solar term boundaries (절기) — astronomical | ⬜ Not started | Fixes month-pillar accuracy for birth dates near term transitions |
| **대운 (Daeun)** 10-year luck cycle calculator | ⬜ Not started | Distinct UI from Chinese Da Yun and from Vedic Vimshottari Dasha |
| **오행 (Five Element) balance** chart | ⬜ Not started | Wood·Fire·Earth·Metal·Water radar/bar; Korean cultural colours |
| **용신 (Yongshin)** rule-based suggestion | ⬜ Not started | "Your favourable element is 水" card with emoji + colour |
| Korean Saju compatibility scoring | ⬜ Not started | 사주 궁합 — element + stem-branch affinity; for the unified Match screen |

### 7.3 Korean Saju UI/UX — IN PROGRESS

| Feature | Status | Notes |
|---|---|---|
| **DetailsUnlockScreen — "Korean Saju" tab** (replaces "Chinese" tab) | ⬜ Not started | Hangul-first; toggle to Hanja for traditionalists |
| **오행 balance shareable card** (radar chart) | ⬜ Not started | Gen Z-friendly; "share my element balance" is a hook |
| **용신 suggestion card** with emoji + colour | ⬜ Not started | Light, shareable, K-fandom-friendly |
| **Daeun timeline visual** | ⬜ Not started | Horizontal timeline: current 대운 highlighted, upcoming marked |
| **Pillars display**: 년주·월주·일주·시주 in Hangul | ⬜ Not started | Visual distinct from any Western or Chinese app |
| Korean Hangul+Hanja typography in Compose | ⬜ Not started | Inter is the base; consider Noto Sans KR for Korean display |
| Korean locale strings (`values-ko/`) for 사주 UI | ⬜ Not started | `ko` is in supported locales but only Indian + general Asian strings exist; need a Saju-specific translation pass |

### 7.4 Korean Saju Monetisation — IN PROGRESS

| Stream | Price | Notes |
|---|---|---|
| **Korean Saju Premium Unlock** (one-time IAP) | ₹149 / $2.99 | Separate SKU from `premium_monthly`/`premium_yearly`; targets K-fandom willing to pay a small one-time fee; unlocks Daeun + 오행 + 용신 + shareable cards |
| **Cosmic Compatibility Report (PDF)** — Korean Saju edition | ₹49 / $0.99 | 사주 궁합 4-page PDF; pairs well with Korean Saju Unlock |

### 7.5 Why This Is a Sure Hit
- **No competitor** combines all three systems (Western + Vedic + Korean Saju). Co-Star, The Pattern, Chani, Sanctuary, Nebula = 0 Vedic, 0 Korean. AstroSage = strong Vedic, 0 Korean. This is white space.
- **Korean diaspora + K-fandom** is a global, growing, high-spend audience — K-drama and K-pop have driven a 6× increase in Korean cultural interest globally since 2020.
- **Hangul UI** signals "this app actually understands Korean Saju" — not a Ba Zi app with a Korean label.
- **One-time ₹149 IAP** unlocks the whole module — feels like a fair deal to fans used to dropping small amounts on K-merch.
- **오행 shareable card** is inherently viral on Instagram / Pinterest / TikTok.
- **Portfolio differentiator**: This is the kind of "intimidating domain depth, accessible UX" combination that makes Cosmic ID reference-worthy for consulting clients (see CLAUDE.md Portfolio Readiness Gaps).

### 7.6 Non-Goals (Deliberately Not Building for v2.1)
- ~~Live Korean astrologer chat~~ — Sanctuary billing fraud precedent; not indie-scalable.
- ~~Full 사주 PDF report with 20 pages~~ — overkill; the 4-page Compatibility Report PDF covers the high-intent use case.
- ~~Jeong-gan (정간 / Spirit Pillars) advanced system~~ — too niche even for K-fandom; add only if telemetry shows demand.

---

## Release Cadence

| Release | Focus | Target Date |
|---------|-------|-------------|
| **v2.0.0** | Cosmic Identity Core (complete) | ✅ 2026-05-22 (shipped) |
| **v2.1.0** | Korean Saju Supremacy (Mission 7) + Vedic depth (Ashtakoot + Manglik) | 2026-06-30 |
| **v2.2.0** | Viral Growth: Daily Fortune Card + Lock Screen Widget + Enhanced Shareable Cards (오행 + Kundli) | 2026-07-31 |
| **v2.3.0** | Trust & Monetization: Lifetime SKU + Tip Jar + Referral Program + Korean Saju Premium IAP (₹149) | 2026-08-31 |
| **v3.0.0** | Platform Ecosystem: Wear OS + Cloud Sync + iOS Port | 2026-Q4 |
| **v3.x** | AI (Conditional): Only if genuinely personalized with full chart context | 2027+ |

---

## Key Metrics & Success Criteria

| Metric | Target | Measurement |
|--------|--------|-------------|
| **MAU** | 100K by end of 2026 | Firebase Analytics |
| **Organic share rate** | 1%+ daily | Share event logging / widget screenshot proxies |
| **India DAU %** | 40%+ of total DAU | Geo-segmented analytics |
| **Free-to-paid conversion** | 5%+ | BillingManager purchase events |
| **Churn rate (monthly)** | <10% | Subscription cancellation tracking |
| **App Store rating** | 4.5★+ | Review monitoring |
| **ARPU (India)** | ₹15–₹30 per install | Revenue / downloads |
| **ARPU (US/UK)** | $2–$5 per install | Revenue / downloads |

---

## Appendix: Feature Priority Matrix

### Must-Have (Shipped or In Progress)
- Live age counter with widgets ✅
- Western + Vedic + **Korean Saju (사주)** astrology ✅ (Korean layer in v2.1)
- Shareable cards ✅
- Birthday reminders ✅
- Premium subscription with trial ✅
- Ashtakoot / Guna Milan compatibility ⏳
- 천간·지지 (Hangul) + 대운 + 오행 + 용신 ⏳

### Performance Drivers (High Impact, Build Next)
- **Korean Saju Premium IAP (₹149 one-time)** — K-fandom unlock
- **오행 balance shareable card** — viral radar chart
- Mangal Dosha detection
- Navamsa (D-9) chart
- Daily fortune shareable card
- Lock screen widget
- Lifetime purchase SKU
- Referral program ("Share Cosmic ID, get 1 month free")

### Delighters (Nice-to-Have, Build Later)
- Pratyantar Dasha
- Planetary dignities table
- Wear OS companion
- iOS port
- AI transit forecasting (conditional)

### Anti-Features (Explicitly Avoid)
- AI chatbot astrologer (unless genuinely personal)
- Live astrologer chat
- Face scan / brain age
- Weekly subscriptions
- Complex Vedic chart rendering (beginner-hostile)
- Paywall creep on existing free features
