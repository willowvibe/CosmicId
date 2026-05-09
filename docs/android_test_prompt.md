# Android Automated UI Test Prompt — AgeReveal

Use this guide when running Appium UI walkthroughs on AgeReveal. It documents the app's specific UI patterns, accessibility labels, and interaction quirks so tests can be precise and reliable.

---

## Setup

1. Ensure Appium server is running (`appium` or `appium --relaxed-security`).
2. Ensure an Android emulator or device is connected (`adb devices`).
3. The app package is `com.willowvibe.agereveal`.
4. Create a `screenshots/` folder in the project root if it does not already exist.
5. This walkthrough is **iterative** — overwrite existing screenshots. Never skip a step because a file exists.

---

## Bottom Navigation (5 tabs)

| Tab | Label | Route | Icon Description |
|-----|-------|-------|-----------------|
| 1 | You | `calculator` | Profile icon |
| 2 | Match | `compatibility` | Heart icon |
| 3 | Bdays | `reminders` | Cake icon |
| 4 | Badges | `badges` | Trophy icon |
| 5 | Timeline | `timeline` | Clock icon |

**Selection indicator:** A small teal pill (3dp tall, 16dp wide) appears **above** the active icon. There is no full background fill on the selected item.

**Tap target:** Each item is 48dp minimum. The `TextView` labels are **not clickable** — tap the parent `View` container or use the icon.

**Navigation:** Use `UiScrollable` + `scrollIntoView()` if a tab is off-screen on small devices.

---

## Tab 1 — You (CalculatorScreen)

### Header
- Title: "AgeReveal" (18sp, left-aligned)
- LIVE badge: Pill chip with amber dot + "LIVE" text, right of title
- Settings gear: 32dp circle, 14dp icon size, `contentDescription = "Settings"`

### Input fields
- Name `OutlinedTextField`: `contentDescription` not set; find by `className = "android.widget.EditText"`
- Birth date row: `contentDescription` contains "Change birth date, currently..."
- Precision chips (TIME / LOCATION): `contentDescription = "TIME: ..."` or `"LOCATION: ..."`

### Stat row (3 pill chips)
- Shape: Full pill (`CircleShape`), compact horizontal Row
- Labels: "DAYS", "HOURS", "NEXT BDAY" — uppercase, 10sp
- Values: 20sp semibold
- Amber accent on "NEXT BDAY" value only

### Cards (all use unified AgeCard style)
- **Corner radius:** 12dp
- **Border:** 1dp white at 8% opacity
- **Background:** WarmSurface (#1F1B16)
- **Padding:** 16dp horizontal, 14dp vertical
- **Gap between cards:** 10dp

### Card types on this screen
| Card | Label text | How to identify |
|------|-----------|-----------------|
| Seconds alive | "SECONDS ALIVE" | Amber dot + rolling digits |
| Next milestone | "NEXT MILESTONE" | Amber label, countdown text |
| Time remaining | "TIME REMAINING" | Amber accent label |
| Work life | "WORK LIFE" | Teal accent label |
| Daily cosmic fortune | "DAILY COSMIC FORTUNE" | Emoji + headline + body |
| Unlock banner | "Unlock full profile" | Teal 4dp left accent strip |
| Share profile row | "Share your cosmic profile" | Share icon, right-aligned |

### Birth location bottom sheet
- Triggered by tapping LOCATION precision chip
- **Not** an `AlertDialog` — it is a `ModalBottomSheet`
- Drag handle: 36dp wide, 4dp tall, centered at top
- Title: "Birth location (optional)" with `HorizontalDivider` underneath
- Latitude field: `OutlinedTextField` with trailing clear icon (✕) when text is present
- Longitude field: `OutlinedTextField` without clear icon
- Buttons: Ghost "Cancel" (left) + filled pill "Set" (right, teal bg)

### Banner ad
- Separated from content by a 1dp `HorizontalDivider` + 4dp spacer
- 52dp height, full width

---

## Tab 2 — Match (CompatibilityScreen)

### Empty state
- Gradient orb + "Compare two birthdays" text
- Tap either person card to begin

### Person cards
- AgeCard style (12dp radius, subtle border)
- Name field: `className = "android.widget.EditText"`
- Date picker trigger: row with calendar icon

### Relationship selector
- Pills: "Romantic", "Sibling", "Friendship", "Regular"
- Selected state: teal border + teal tint background

### Results
- Score hero: large number in AgeCard
- Zodiac pairing cards: AgeCard style
- Element reading: AgeCard style

---

## Tab 3 — Bdays (RemindersScreen)

### Empty state
- Centered graphic + "No birthdays saved yet"

### Birthday list items
- Plain Row (not a card), 10dp vertical padding
- Divider line below each item: 1dp, WarmSurfaceSoft

### Add birthday bottom sheet
- Triggered by FAB (circular, plus icon)
- Name input: `OutlinedTextField`
- Emoji picker: horizontal `LazyRow` of emoji buttons
- Date trigger card: 12dp radius, WarmSurfaceSoft bg
- Save button: filled pill, teal bg

---

## Tab 4 — Badges (BadgeScreen)

### Toggle pills
- "Grid" / "Timeline" — 99dp radius, bordered
- Active: teal border + teal tint

### Badge cards (grid)
- 16dp radius (slightly larger than AgeCard for grid density)
- WarmSurface bg
- Border only when unlocked: 1.5dp teal
- Rarity chip: 4dp radius, colored bg at 15% opacity

### Badge detail bottom sheet
- Triggered by tapping any badge card
- Share button: 16dp radius, teal bg
- Locked placeholder: WarmSurfaceSoft bg

---

## Tab 5 — Timeline (LifeTimelineScreen)

### Milestone rows
- AgeCard wrapper (12dp radius, subtle border)
- Leading dot: 8dp CircleShape
- Color: teal (past), amber (today), WarmSurfaceSoft (future)
- Status label: "TODAY ✦", "✓", or "IN {N}D" — uppercase, 10sp

---

## Settings Screen

### Access
- Opened from CalculatorScreen gear icon or RemindersScreen gear icon
- Full-screen destination, no bottom nav change

### Sections
- Wrapped in AgeCard (12dp radius, subtle border)
- Section titles: AgeLabel style (uppercase, 10sp, WarmInkDim)
- Switch rows: title + subtitle + Switch
- Option rows: selectable pill inside AgeCard, teal border when selected
- Action rows: WarmSurfaceSoft bg, 10dp radius, clickable

### Scroll requirement
- Settings options (Dark, Hindi, Export CSV, Clear All) may be below the fold — use `UiScrollable.scrollIntoView()`

---

## Screenshot Naming Convention

Always overwrite on re-run:

| Pattern | Example |
|---------|---------|
| `tab{N}_{screen_name}_default.png` | `tab1_calculator_default.png` |
| `tab{N}_{screen_name}_{action}.png` | `tab1_calculator_datepicker_open.png` |
| `BUG_{tab_or_feature}_{description}.png` | `BUG_tab2_submit_crash.png` |

---

## Known Testing Quirks

1. **OutlinedTextField** exposed as `android.widget.EditText` **only when focused**. Tap first, then interact.
2. **Bottom-nav labels** are not clickable — use parent `View` or icon.
3. **Dynamic `contentDescription`** strings change with app state. Use `descriptionContains()` with partial matches.
4. **Settings screen** scrolls — use `UiScrollable.scrollIntoView()` for items below the fold.
5. **Export CSV** opens a system share chooser that blocks automation. Scroll-to and screenshot only; do not tap.
6. **Hindi locale selection** changes app locale and breaks subsequent selectors. Test this last.
7. **Notification shade** can appear on launch after `pm clear` — detect via `com.android.systemui` in page source and press Back.
8. **Birth location** is now a bottom sheet, not a dialog. Dismiss by tapping outside or swiping down.

---

## Report Generation

After the walkthrough, regenerate `screenshots/REPORT.md` (overwrite if exists):

- Total screens tested
- Total interactions tested
- Bugs still present (with screenshot references)
- Bugs resolved since last run (previously had BUG_ file, now fixed)
- Any features that could not be tested and why
- Timestamp of this run
