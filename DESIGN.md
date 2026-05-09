---
name: AgeReveal

colors:
  # Core warm-dark palette
  background: "#14120F"
  surface: "#1F1B16"
  surface-variant: "#272219"
  surface-elevated: "#2E2920"

  # Text
  on-background: "#F2EADF"
  on-surface: "#F2EADF"
  on-surface-variant: "#A89B86"
  on-surface-dim: "#6E6554"

  # Primary accent
  primary: "#3D7A6E"
  on-primary: "#14120F"
  primary-container: "#1F5A52"
  on-primary-container: "#F2EADF"

  # Secondary accent (amber / birthday)
  secondary: "#DEB84A"
  on-secondary: "#14120F"
  secondary-container: "#B07828"
  on-secondary-container: "#F2EADF"

  # Tertiary (reuses amber for warmth)
  tertiary: "#DEB84A"
  on-tertiary: "#14120F"

  # Error
  error: "#BF3D2D"
  on-error: "#F2EADF"

  # Divider / outline
  outline: "#6E6554"
  outline-variant: "rgba(53, 48, 38, 0.25)"

  # Cosmos share-card palette
  cosmos-deep: "#1A1A2E"
  cosmos-shell: "#16213E"

  # Light-theme seeds (fallback)
  light-background: "#FAF9FF"
  light-surface: "#FFFFFF"
  light-on-surface: "#1C1B1F"

  # Legacy brand (light mode)
  brand-green: "#0F6E56"
  brand-green-light: "#1A9F7A"
  brand-gold: "#B45309"
  brand-gold-light: "#D97706"

  # Share-card accent defaults
  accent-mint: "#86EFAC"
  accent-amber: "#FCD34D"
  accent-red: "#EF4444"

  # Share-card theme backgrounds
  share-cosmos-start: "#1A1A2E"
  share-cosmos-end: "#16213E"
  share-light-start: "#FFFFFF"
  share-light-end: "#F0EDE8"
  share-festive-start: "#FF9933"
  share-festive-end: "#138808"
  share-festive-gold: "#FFD700"

  # Utility
  transparent: "transparent"
  ad-slot-bg: "#2A2A3A"
  ad-slot-border: "#3A3A50"

typography:
  display-lg:
    fontFamily: "Serif"
    fontSize: 78px
    fontWeight: 300
    lineHeight: 74px
    letterSpacing: -0.025em

  display-md:
    fontFamily: "Serif"
    fontSize: 46px
    fontWeight: 400
    lineHeight: 44px
    letterSpacing: -0.032em

  display-sm:
    fontFamily: "Serif"
    fontSize: 38px
    fontWeight: 400
    lineHeight: 38px
    letterSpacing: -0.026em

  headline-lg:
    fontFamily: "Serif"
    fontSize: 22px
    fontWeight: 400
    lineHeight: 28px
    letterSpacing: -0.013em

  headline-md:
    fontFamily: "Serif"
    fontSize: 20px
    fontWeight: 400
    lineHeight: 26px
    letterSpacing: -0.01em

  headline-sm:
    fontFamily: "Serif"
    fontSize: 15px
    fontWeight: 400
    lineHeight: 20px
    letterSpacing: -0.02em

  body-lg:
    fontFamily: "Inter"
    fontSize: 16px
    fontWeight: 400
    lineHeight: 24px
    letterSpacing: 0

  body-md:
    fontFamily: "Inter"
    fontSize: 14px
    fontWeight: 500
    lineHeight: 20px
    letterSpacing: 0

  body-sm:
    fontFamily: "Inter"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 16px
    letterSpacing: 0

  label-lg:
    fontFamily: "Inter"
    fontSize: 14px
    fontWeight: 600
    lineHeight: 20px
    letterSpacing: 0.107em
    textTransform: uppercase

  label-md:
    fontFamily: "Inter"
    fontSize: 12px
    fontWeight: 500
    lineHeight: 16px
    letterSpacing: 0.083em
    textTransform: uppercase

  label-sm:
    fontFamily: "Inter"
    fontSize: 10px
    fontWeight: 600
    lineHeight: 14px
    letterSpacing: 0.15em
    textTransform: uppercase

spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 20px
  2xl: 24px
  3xl: 32px
  4xl: 40px
  section-gap: 14px
  card-padding: 16px
  card-gap: 12px
  screen-edge: 24px
  sheet-edge: 24px
  bottom-sheet-bottom: 40px

rounded:
  none: 0px
  xs: 3px
  sm: 4px
  md: 8px
  lg: 10px
  xl: 12px
  2xl: 14px
  3xl: 16px
  4xl: 18px
  full: 9999px

shadow:
  none: "none"
  # Cards rely on tonal separation (surface colors) rather than drop shadows.
  # The only elevation cue is background-to-surface color lift.
  card: "0 0 0 rgba(0,0,0,0)"

motion:
  duration-fast: 200ms
  duration-default: 350ms
  duration-slow: 500ms
  duration-badge: 3000ms
  easing-default: "cubic-bezier(0.4, 0, 0.2, 1)"
  easing-in: "cubic-bezier(0.4, 0, 1, 1)"
  easing-out: "cubic-bezier(0, 0, 0.2, 1)"

components:
  card-primary:
    backgroundColor: "{colors.surface}"
    borderRadius: "{rounded.2xl}"
    padding: "{spacing.md} {spacing.lg}"

  card-secondary:
    backgroundColor: "{colors.surface-variant}"
    borderRadius: "{rounded.lg}"
    padding: "{spacing.sm} {spacing.md}"

  card-elevated:
    backgroundColor: "{colors.surface-elevated}"
    borderRadius: "{rounded.2xl}"
    padding: "{spacing.md} {spacing.lg}"

  button-primary:
    backgroundColor: "{colors.primary}"
    color: "{colors.on-primary}"
    borderRadius: "{rounded.full}"
    padding: "{spacing.sm} {spacing.lg}"
    fontFamily: "{typography.label-md.fontFamily}"
    fontSize: "{typography.label-md.fontSize}"
    fontWeight: "{typography.label-md.fontWeight}"
    letterSpacing: "{typography.label-md.letterSpacing}"

  button-secondary:
    backgroundColor: "transparent"
    color: "{colors.primary}"
    border: "1.5px solid {colors.primary}"
    borderRadius: "{rounded.full}"
    padding: "{spacing.sm} {spacing.lg}"
    fontFamily: "{typography.label-md.fontFamily}"
    fontSize: "{typography.label-md.fontSize}"
    fontWeight: "{typography.label-md.fontWeight}"
    letterSpacing: "{typography.label-md.letterSpacing}"

  chip-active:
    backgroundColor: "{colors.primary}"
    color: "{colors.on-primary}"
    borderRadius: "{rounded.lg}"
    padding: "{spacing.sm} {spacing.md}"
    fontFamily: "{typography.label-md.fontFamily}"
    fontSize: "{typography.label-md.fontSize}"

  chip-inactive:
    backgroundColor: "{colors.surface-variant}"
    color: "{colors.on-surface}"
    borderRadius: "{rounded.lg}"
    padding: "{spacing.sm} {spacing.md}"
    fontFamily: "{typography.label-md.fontFamily}"
    fontSize: "{typography.label-md.fontSize}"

  divider:
    height: "1px"
    backgroundColor: "{colors.outline-variant}"

  icon-default:
    size: "24px"
    strokeWidth: "1.5px"
    strokeColor: "{colors.on-surface}"
    fill: "transparent"
    strokeLinecap: "round"
    strokeLinejoin: "round"

  icon-large:
    size: "32px"

  icon-small:
    size: "20px"

  progress-bar:
    height: "7px"
    backgroundColor: "{colors.surface-variant}"
    fillColor: "{colors.primary}"
    borderRadius: "{rounded.full}"

  share-card-cosmos:
    width: "900px"
    height: "900px"
    contentWidth: "900px"
    contentHeight: "600px"
    verticalPadding: "150px"
    background: "linear-gradient(135deg, {colors.share-cosmos-start}, {colors.share-cosmos-end})"
    textColor: "{colors.on-surface}"
    accentColor: "{colors.accent-mint}"
    cornerRadius: "28px"

  share-card-light:
    width: "900px"
    height: "900px"
    background: "linear-gradient(135deg, {colors.share-light-start}, {colors.share-light-end})"
    textColor: "#1C1917"
    accentColor: "{colors.accent-mint}"
    cornerRadius: "28px"

  share-card-festive:
    width: "900px"
    height: "900px"
    background: "linear-gradient(135deg, {colors.share-festive-start}, {colors.share-festive-end})"
    textColor: "{colors.on-surface}"
    accentColor: "{colors.share-festive-gold}"
    cornerRadius: "28px"

  share-story:
    width: "1080px"
    height: "1920px"
    safeTop: "250px"
    safeBottom: "350px"

  bottom-nav:
    backgroundColor: "{colors.background}"
    tonalElevation: "0px"
    iconSize: "22px"
    labelSize: "{typography.label-sm.fontSize}"
    activeColor: "{colors.primary}"
    inactiveColor: "{colors.on-surface-dim}"

  ad-slot:
    backgroundColor: "{colors.ad-slot-bg}"
    border: "1px solid {colors.ad-slot-border}"
    borderRadius: "{rounded.lg}"

---

# AgeReveal — Design System

## Overview

AgeReveal is a warm-dark, astrology-infused Android app built on Jetpack Compose and Material 3. The design language balances **intimacy** (this is your personal timeline) with **wonder** (the cosmos, milestones, and hidden stats). The UI feels like a personal diary that happens to know astrophysics.

The dominant visual mode is a **deep warm black** (#14120F) paired with **cream ink** (#F2EADF) text and **teal** (#3D7A6E) plus **amber** (#DEB84A) accents. Cards float on slightly lighter surfaces (#1F1B16, #272219) with generous rounding and zero drop-shadow elevation — depth is communicated through **color lift**, not shadow.

## Philosophy

### Warmth over coldness
Most astrology apps default to purple/blue "cosmic" palettes. AgeReveal deliberately uses earth tones — warm blacks, cream text, teal as a muted sea-glass accent, and amber as a birthday-candle highlight. This makes the app feel grounded and personal rather than mystical and distant.

### Serif for numbers, Sans for labels
Age numerals (the hero display) use a **serif family** (Georgia, with Fraunces intended for the future) at light weight with tight negative letter-spacing. This gives large numbers an editorial, almost typographic-poster quality. All UI labels, buttons, and metadata use **Inter** — clean, neutral, and highly legible at small sizes.

### Cards as the atomic unit
Almost every piece of information lives inside a rounded card. The calculator screen is a stack of cards. The details screen is a scrollable column of cards. Settings rows are card-shaped. This creates visual rhythm and makes the UI feel like flipping through a deck of personal stats.

### Zero elevation, maximum separation
Cards have no drop shadows. Separation is achieved through:
1. **Background-to-surface color lift** (#14120F → #1F1B16)
2. **Rounded corners** (14px default, 16px for dialogs)
3. **Generous internal padding** (16px) and **section gaps** (14px)

## Color Usage

### Dark mode (primary experience)
The app defaults to dark mode and most users stay there. The warm-black background (#14120F) is just warm enough to avoid the clinical feel of pure #000000. Cards use #1F1B16 — barely lighter, but perceptibly "raised." Subtle variants (#272219, #2E2920) are used for chips, inactive states, and nested elements.

Text follows a three-tier hierarchy:
- **Primary** (#F2EADF) — headlines, ages, values
- **Secondary** (#A89B86) — descriptions, metadata, timestamps
- **Tertiary** (#6E6554) — labels, hints, disabled states

### Light mode (fallback)
Light mode exists but is not the primary focus. It uses a near-white background (#FAF9FF) with brand green (#0F6E56) as primary and brand gold (#B45309) as secondary. The warm-dark palette is the app's identity.

### Accent system
- **Teal** (#3D7A6E) is the functional accent — buttons, toggles, progress bars, active chips, share-row backgrounds.
- **Amber** (#DEB84A) is the emotional accent — birthdays, countdowns, milestones, achievement badges, heartbeats. It represents celebration.
- **User-defined accent** (default mint #86EFAC) overrides the teal in share cards and can be picked from 6 swatches in Settings.

### Share card themes
Three share-card themes extend the palette into shareable media:

1. **Dark Cosmos** — navy gradient (#1A1A2E → #16213E) with white text and the user's chosen accent. Feels like a star chart.
2. **Minimal Light** — white-to-cream gradient with dark ink text. Clean and editorial.
3. **Festive India** — saffron-to-green gradient (#FF9933 → #138808) with gold (#FFD700) accents. Vibrant and celebratory.

## Typography

### Display scale (Serif, Light/Normal)
Display typography is reserved for the hero moment — the live age counter. `display-lg` (78px, Light, -2px letter-spacing) is used for the year numeral. `display-md` (46px) handles months and days. The tight negative tracking makes multi-digit numbers feel like a single typographic block rather than separate glyphs.

### Headline scale (Serif, Normal)
`headline-lg` (22px) is used for screen titles and bottom-sheet headers. `headline-md` (20px) for app bar titles. `headline-sm` (15px) for card stat values (e.g., "10,000 days" inside a milestone card).

### Body scale (Inter, Normal/Medium)
`body-lg` (16px) is the workhorse for readable paragraphs and row labels. `body-md` (14px, Medium) for secondary values and inline emphasis. `body-sm` (12px) for captions, helper text, and timestamps.

### Label scale (Inter, SemiBold/Medium, uppercase)
Labels are always **ALL CAPS** with wide positive letter-spacing (1.5px for `label-lg`, 1px for `label-md`). This creates maximum contrast against body text and makes functional metadata ("BORN", "SECONDS ALIVE", "MILESTONE") feel like editorial marginalia.

## Spacing

### 8px base grid
All spacing derives from an 8px base unit:
- `xs` = 4px (half-unit, tiny gaps)
- `sm` = 8px (standard small gap)
- `md` = 12px (card internal padding, chip padding)
- `lg` = 16px (card padding, section internal spacing)
- `xl` = 20px (medium section spacing)
- `2xl` = 24px (screen edge padding, sheet horizontal padding)
- `3xl` = 32px (large icon sizes)
- `4xl` = 40px (bottom sheet bottom padding)

### Section rhythm
Cards are separated by **14px** (`section-gap`) — enough to feel distinct, not so much that the screen feels sparse. Inside cards, elements are spaced by **8–12px**. The overall effect is a relaxed vertical rhythm where every card breathes.

### Screen edges
Standard screen edge padding is **24px** on both sides. Cards fill the width minus these edges, creating a consistent frame around all content.

## Shape

### Rounded corners
The corner-radius scale is continuous rather than stepped:
- **3px** (`xs`) — tiny rounding for bar chart segments
- **4px** (`sm`) — thumbnails, stat images, small badges
- **8px** (`md`) — chips, toggles, theme swatches
- **10px** (`lg`) — rows, reminder items, format chips
- **12px** (`xl`) — medium cards, dialog rows
- **14px** (`2xl`) — **primary card radius** — used on 80% of cards
- **16px** (`3xl`) — dialogs, large sections, reminder cards
- **18px** (`4xl`) — large settings toggles
- **9999px** (`full`) — pill buttons, progress bars, avatars, capsule chips

The 14px radius is the "signature" — instantly recognizable as AgeReveal.

## Motion

### Entrance choreography
The calculator screen uses a **staggered entrance** where elements fade in and slide up with increasing delay:

1. **0ms** — ClockFaceHero (the big age numeral)
2. **70ms** — SecondsStrip (the ticking seconds)
3. **140ms** — MiniStatRow (days/hours/minutes)
4. **210ms** — NextMilestoneChip
5. **245–280ms** — StatCards (three cards, 7ms apart)
6. **350ms** — NextBirthdayCard and unlock teaser

Each element uses `350ms` duration with `FastOutSlowInEasing`.

### Tab switching
`AnimatedContent` with crossfade handles bottom-nav tab changes. No jarring cuts — content dissolves and reappears smoothly.

### Color transitions
`animateColorAsState` drives badge selection backgrounds and chip active states. Duration is typically `200ms` for interactive feedback.

### Badge progress
The badge unlock progress bar fills over `3000ms` — deliberately slow to build anticipation and give the user time to notice the achievement.

### Haptic pairing
Every significant tap triggers haptic feedback:
- `LongPress` — share buttons, unlock actions, save actions
- `TextHandleMove` — date/time picker scrolls

## Components

### Cards
Cards are the core UI primitive. They always have:
- 14px or 16px corner radius
- 16px internal padding
- A background color one step lighter than the surrounding surface
- No border, no shadow, no outline

Primary cards (`card-primary`) use `surface` (#1F1B16). Secondary cards (`card-secondary`) use `surface-variant` (#272219) for nested or less-important content.

### Buttons
There are two button styles:
1. **Filled pill** (`button-primary`) — teal background, black text, full pill radius. Used for primary actions (Save, Share, Watch & Reveal).
2. **Outlined pill** (`button-secondary`) — transparent background, teal text, 1.5px teal border. Used for secondary actions (Cancel, Skip, secondary share options).

Both use `label-md` typography (12px, Inter Medium, uppercase, 1px letter-spacing).

### Chips
Chips are compact toggle-like elements with 10px radius. Active chips (`chip-active`) use teal. Inactive chips (`chip-inactive`) use `surface-variant`. They feel like soft pills rather than hard rectangles.

### Icons
All icons are 24×24px vector drawables with:
- 1.5px stroke width
- Round caps and joins
- Transparent fill
- White stroke (#FFFFFF) on dark backgrounds

Bottom nav icons render at 22px. Inline action icons at 20px. Large decorative icons at 32px.

### Progress bars
The life-progress bar and milestone progress bars are 7px tall with full-pill radius. Background is `surface-variant`, fill is `primary` (teal).

### Share cards (generated bitmaps)
Share cards are the app's "export" design system — a parallel visual language for social media:
- **Square** format: 900×900px, content area 900×600px centered with 150px vertical padding filled by the theme gradient. No cropping on any platform.
- **Story** format: 1080×1920px portrait, 250px top safe zone and 350px bottom safe zone for Instagram/Snapchat UI overlays.
- **Transparent** format: 1080×1920px with fully transparent background, white text with black outline for green-screen use.

All three formats use the same three themes (Dark Cosmos, Minimal Light, Festive India).

## Imagery & Widgets

### Home screen widgets
Widgets extend the dark-cosmos palette into the Android home screen:
- **2×2 seconds counter** — large monospace-ish digits, "SECONDS ALIVE" label, mint accent, dark background (#1A1A2E).
- **4×1 lifespan progress** — horizontal teal progress bar with percentage text, color-coded (teal → amber → rose → red as age increases).
- **2×1 milestone ring** — compact countdown ring for next milestone.
- **4×2 birthday widget** — next 3 upcoming birthdays with days-remaining countdown.

### App icon
The app icon is not defined in code but the design intent is a warm-dark rounded square with a teal/amber motif — simple enough to read at 48dp, distinctive enough to stand out on a home screen.

## Accessibility

### Contrast
All text meets WCAG AA against the dark background:
- Primary text (#F2EADF) on #14120F = **15.5:1** (AAA)
- Secondary text (#A89B86) on #14120F = **7.2:1** (AA)
- Tertiary text (#6E6554) on #14120F = **4.1:1** (AA, borderline)

### Touch targets
All interactive elements meet the 48×48dp minimum. Chips and small buttons use generous padding to enlarge the invisible tap area beyond the visible bounds.

### Semantic labels
Every icon-only button has a `contentDescription`. Custom clickable elements declare `Role.Button`. Astro grid items merge descendants and expose `stateDescription` for screen readers.

## Localization

The app supports English and Hindi (हिन्दी). Typography handles Devanagari script via Inter's multilingual coverage. Labels in Hindi may run longer; the card-based layout with `fillMaxWidth` and generous internal padding accommodates script expansion without truncation.
