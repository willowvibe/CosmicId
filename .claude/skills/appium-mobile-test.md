---
name: appium-mobile-test
description: Automate Android app UI testing using Appium MCP server with natural language commands.
---

# Appium Mobile Test Automation

Use the `appium` MCP server to drive UI tests on Android emulators and physical devices.

## Prerequisites

1. **Appium** installed globally: `npm install -g appium`
2. **Android SDK** with emulator or physical device connected
3. **Appium drivers** installed:
   ```bash
   appium driver install uiautomator2
   ```
4. **APK built**: `./gradlew :app:assembleDebug`

## Starting the Test Environment

```bash
# 1. Start Appium server (in a separate terminal)
appium

# 2. Start emulator (if not using physical device)
$ANDROID_HOME/emulator/emulator -avd <your_avd_name>

# 3. Verify device is connected
adb devices
```

## MCP Tools Available

| Tool | Purpose |
|------|---------|
| `appium_start_session` | Launch app on device/emulator |
| `appium_find_element` | Locate UI element by ID, text, content-desc, XPath |
| `appium_click` | Tap an element |
| `appium_send_keys` | Type text into input field |
| `appium_get_screenshot` | Capture screen for visual verification |
| `appium_scroll` | Swipe/scroll gestures |
| `appium_press_key` | Hardware buttons (back, home, volume) |
| `appium_get_page_source` | Read full accessibility hierarchy |
| `appium_terminate_app` | Close the app |
| `appium_ai` | AI vision-based element detection (set `AI_VISION_ENABLED=true`) |

## Example Test Flows

### Basic Age Calculator Flow

```
Use appium_start_session to launch the AgeReveal debug APK on the emulator.
Use appium_find_element with content-desc "Pick date" and click it.
Use appium_find_element with text "OK" and click it.
Use appium_get_screenshot to verify the age result is displayed.
```

### Unlock Profile Flow

```
Use appium_start_session with the APK.
Tap the date picker, select a birth date, confirm.
Tap "Watch & Reveal" to trigger the ad unlock flow.
Take a screenshot to verify the astro tile appears.
```

### Compatibility Screen Flow

```
Use appium_start_session.
Navigate to the "Match" tab.
Enter names and dates for both persons.
Verify the compatibility score card is displayed.
```

## Testing Strategy for AgeReveal

### Critical Paths to Automate

1. **CalculatorScreen**
   - Enter birth date → age displayed
   - Add birth time → precision chip shows time
   - Add location → precision chip shows coordinates
   - Tap share → share sheet opens

2. **DetailsUnlockScreen**
   - Locked state → placeholder visible
   - Unlock → astro tile renders with signs
   - Milestone timeline → scroll and verify

3. **CompatibilityScreen**
   - Enter two birth dates → result appears
   - Switch relationship type → score recalculates
   - Share match card → share sheet opens

### Element Locator Strategy

Use these Compose test tags in your Kotlin code to make Appium testing reliable:

```kotlin
// Add test tags to key composables
Modifier.testTag("birth_date_picker")
Modifier.testTag("watch_ad_button")
Modifier.testTag("share_button")
Modifier.testTag("compatibility_result")
```

Appium can find elements by:
- `accessibility id` → Compose `contentDescription`
- `text` → visible text on screen
- `class` → fully qualified class name
- `xpath` → full hierarchy path

## Tips

- Always use `appium_get_screenshot` after interactions to verify state
- Use `appium_get_page_source` when you can't find an element — it dumps the full accessibility tree
- Enable `AI_VISION_ENABLED=true` for elements without proper accessibility labels
- Run tests on the same APK built by `./gradlew :app:assembleDebug`
- Clean up sessions with `appium_terminate_app` after tests
