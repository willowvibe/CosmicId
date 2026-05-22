# Android Automated UI Test Prompt — Cosmic ID

Use this guide when running Appium UI walkthroughs on Cosmic ID. It documents the app's specific UI patterns, accessibility labels, and interaction quirks so tests can be precise and reliable.

---

## Setup

1. Ensure Appium server is running (`appium` or `appium --relaxed-security`).
2. Ensure an Android emulator or device is connected (`adb devices`).
3. The app package is `com.willowvibe.cosmicid`.
4. Create a `screenshots/` folder in the project root if it does not already exist.
5. This walkthrough is **iterative** — overwrite existing screenshots. Never skip a step because a file exists.

---

## Bottom Navigation (4 tabs)

| Tab | Label | Route | Icon Description |
|-----|-------|-------|-----------------|
| 1 | My Cosmos | `calculator` | Profile icon |
| 2 | Match | `compatibility` | Heart icon |
| 3 | Bdays | `reminders` | Cake icon |
| 4 | Timeline | `timeline` | Clock icon |

**v2.0 change:** The "Badges" tab has been removed from bottom nav. Badges now live as a section inside the "My Cosmos" tab. The "You" tab is renamed to "My Cosmos".

**Selection indicator:** A small teal pill (3dp tall, 16dp wide) appears **above** the active icon. There is no full background fill on the selected item.

**Tap target:** Each item is 48dp minimum. The `TextView` labels are **not clickable** — tap the parent `View` container or use the icon.

**Navigation:** Use `UiScrollable` + `scrollIntoView()` if a tab is off-screen on small devices.

---

## Onboarding (First Launch)

**v2.0 new flow:** If `hasCompletedOnboarding` is false, the app shows `OnboardingScreen` instead of the main calculator.

### Step 1 — Name + Birth Date
- Title: "Let's build your Cosmic ID"
- Name `OutlinedTextField` at top
- Birth date row with calendar icon: `contentDescription` contains "Change birth date, currently..."
- "Continue" button (filled pill, teal)

### Step 2 — Optional Birth Time + Location
- Title: "Fine-tune your chart"
- Optional TimePicker row
- Indian State dropdown (searchable bottom-sheet picker) for approximate location
- "Next" button (filled pill, teal)
- "Skip" — ghost button (saves null time/location)

### Step 3 — Accent Colour Picker
- Title: "Choose your cosmic vibe"
- 6 colour swatch pills (Mint, Amber, Pink, Blue, Purple, Emerald)
- "Enter My Cosmos" CTA button (filled pill, teal)
- Completing saves accent to prefs and dismisses onboarding

**Testing note:** To re-test onboarding, clear app data (`pm clear com.willowvibe.cosmicid`) or manually set `hasCompletedOnboarding = false` in SharedPreferences.

---

## Tab 1 — My Cosmos (CalculatorScreen)

### Header
- Title: "Cosmic ID" (18sp, left-aligned)
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

### Card types on this screen (v2.0 progressive disclosure)
| Card | Label text | How to identify |
|------|-----------|-----------------|
| Seconds alive | "SECONDS ALIVE" | Amber dot + rolling digits |
| Rotating highlight | Varies | One card only: fortune / milestone / planet age / celebrity match |
| Explore CTA | "Explore full profile →" | Teal arrow, right-aligned |

**Removed from main screen in v2.0:** Time remaining, work life, daily cosmic fortune (now a push notification). These are all inside the Details screen.

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
- **v2.0:** Only shown on free tier. Hidden for premium subscribers.

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

### Deep-link auto-populate (v2.0)
- When receiving a profile deep-link (`agereveal://profile/...`), the second person slot auto-fills.
- Score appears immediately without manual input.

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

## Tab 4 — Timeline (LifeTimelineScreen)

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
- Settings options may be below the fold — use `UiScrollable.scrollIntoView()`

**v2.0 changes:**
- Removed: Hindi language toggle (system locale only), Time remaining toggle (moved to Details)
- Added: Premium status section (manage subscription), Fortune notification time picker
- Notification section expanded: birthday reminders, milestone reminders, daily fortune time, cosmic year toggle

---

## Paywall Screen (v2.0)

### Trigger conditions
- Tapping locked astrology section in Details
- 3rd app open if no subscription

### UI elements
- Backdrop scrim: 60% black, tap-to-dismiss
- Sheet title: "Unlock Your Full Cosmic Profile"
- Feature bullet list with check icons
- Pricing: "₹49/month" primary + "₹299/year — Save 49%" secondary
- CTA: "Start 7-Day Free Trial" (gradient gold pill)
- Terms text: "Cancel anytime. Billed via Google Play."

**Testing note:** Use Google Play test SKU `android.test.purchased` for automated purchase flow validation.

---

## Screenshot Naming Convention

Always overwrite on re-run:

| Pattern | Example |
|---------|---------|
| `onboarding_{step}_{action}.png` | `onboarding_step2_zodiac_reveal.png` |
| `tab{N}_{screen_name}_default.png` | `tab1_calculator_default.png` |
| `tab{N}_{screen_name}_{action}.png` | `tab1_calculator_datepicker_open.png` |
| `paywall_{variant}.png` | `paywall_astrology_locked.png` |
| `BUG_{tab_or_feature}_{description}.png` | `BUG_tab2_submit_crash.png` |

---

## Known Testing Quirks

1. **OutlinedTextField** exposed as `android.widget.EditText` **only when focused**. Tap first, then interact.
2. **Bottom-nav labels** are not clickable — use parent `View` or icon.
3. **Dynamic `contentDescription`** strings change with app state. Use `descriptionContains()` with partial matches.
4. **Settings screen** scrolls — use `UiScrollable.scrollIntoView()` for items below the fold.
5. **Export CSV** opens a system share chooser that blocks automation. Scroll-to and screenshot only; do not tap.
6. **Hindi locale selection** is now handled via Android system settings, not in-app. Test by changing emulator locale.
7. **Notification shade** can appear on launch after `pm clear` — detect via `com.android.systemui` in page source and press Back.
8. **Birth location** is now a bottom sheet, not a dialog. Dismiss by tapping outside or swiping down.
9. **Onboarding gate** blocks access to main UI on first launch. Clear app data to re-trigger.
10. **Paywall** may appear unexpectedly on 3rd open. Handle with back-press or tap scrim to dismiss.
11. **Ad banner** only visible on free tier. Premium test accounts won't show it.

---

## Report Generation

After the walkthrough, regenerate `screenshots/REPORT.md` (overwrite if exists):

- Total screens tested
- Total interactions tested
- Bugs still present (with screenshot references)
- Bugs resolved since last run (previously had BUG_ file, now fixed)
- Any features that could not be tested and why
- Timestamp of this run
