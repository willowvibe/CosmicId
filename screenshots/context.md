# AgeReveal UI Walkthrough - Session Context

## What Was Built

A complete automated UI walkthrough for the AgeReveal Android app using Appium + Python.

- **Script:** `screenshots/walkthrough.py`
- **Output:** 26 screenshots + `REPORT.md`
- **Status:** All tests passing, no bugs detected

## Key Artifacts

| File | Purpose |
|------|---------|
| `screenshots/walkthrough.py` | Main Appium automation script |
| `screenshots/REPORT.md` | Generated report of screens/interactions/bugs |
| `screenshots/tab{1-4}_*.png` | Tab-specific screenshots (naming convention below) |
| `screenshots/settings_*.png` | Settings screen screenshots |
| `screenshots/BUG_*.png` | Bug screenshots (if any detected) |

## Screenshot Naming Convention

- `tab{N}_{screen_name}_default.png` — Default state
- `tab{N}_{screen_name}_{action}.png` — After interaction
- `settings_{action}.png` — Settings screen interactions
- `BUG_{tab_or_feature}_{description}.png` — Bug captures (none found in last run)

Tabs: 1=Calculator(You), 2=Compatibility(Match), 3=Reminders(Bdays), 4=Timeline

## Technical Environment

- **Appium Server:** http://localhost:4723
- **Platform:** Android (UiAutomator2)
- **Emulator:** AVD with KVM acceleration
- **SDK Path:** `/home/harish/Android/Sdk`
- **Required env:** `ANDROID_HOME=/home/harish/Android/Sdk`, PATH includes `$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools`
- **Python deps:** `Appium-Python-Client`, `selenium` (installed with `--break-system-packages`)

## Critical Implementation Details

### Bottom Nav Navigation
TextViews inside bottom nav are NOT directly clickable. Use XPath to find clickable ancestor:
```python
f'//android.widget.TextView[@text="{tab_label}"]/ancestor::android.view.View[@clickable="true"]'
```

### Compose Element Mapping
- `OutlinedTextField` → `android.widget.EditText` (only when focused)
- Dynamic `contentDescription` strings change based on app state (e.g., birth date row, precision chips)
- Use `descriptionContains()` for partial matches on dynamic content

### Scrollable Content
Settings screen elements (Dark theme, Hindi, Export CSV, Clear All) are below the fold inside a ScrollView. Use `UiScrollable` + `scrollIntoView`:
```python
scrollable = driver.find_element_by_android_uiautomator(
    'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(...)'
)
```

### System Interference Handling
- **Notification shade:** Check page source for `com.android.systemui` and press Back to dismiss
- **Permission dialogs:** Detect and auto-dismiss at startup
- **Share chooser (Export CSV):** Do NOT tap — opens system share sheet that blocks subsequent automation. Only scroll-to and screenshot.
- **Locale changes (Hindi):** Do NOT select — changes app locale and breaks subsequent selectors.

### Post-Settings Navigation
After backing out of Settings, call `driver.activate_app(APP_PACKAGE)` to ensure the app returns to foreground before navigating to other tabs. Add 2s sleep after `go_back()`.

## Test Coverage

| Tab | Tests |
|-----|-------|
| Calculator (You) | Settings button, name input, birth date picker, time precision, location precision, results scroll |
| Compatibility (Match) | Romantic relationship type, Person A/B names + dates, results scroll |
| Reminders (Bdays) | Add birthday FAB, bottom sheet form (name, emoji, date), save, settings button |
| Timeline | Default view, header check, milestone scrolling |
| Settings | Dark theme toggle, Hindi (found only), Export CSV (found only), Clear All + cancel |
| Edge Cases | Empty name field, empty compatibility state, empty reminders state |

## Known Limitations

- Timeline screenshots are smaller (~42KB vs ~200KB) — verify visually that content renders fully
- Ad screens and network-dependent content are not explicitly tested
- No actual bug screenshots were captured in the last run (all flows passed)
- App state is reset (clear data) at script start, so tests run from clean state

## Running the Script

```bash
export ANDROID_HOME=/home/harish/Android/Sdk
export PATH=$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$PATH

# Ensure emulator is running and Appium server is started with ANDROID_HOME set
python3 screenshots/walkthrough.py
```

## Result Summary (Last Run: 2026-05-03)

- **Screens tested:** 13
- **Interactions tested:** 27
- **Screenshots captured:** 26
- **Bugs found:** 0
