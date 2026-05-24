# Appium Screenshot Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rewrite the Appium E2E script for Cosmic ID v2.0, build a fresh debug APK, execute the automated walkthrough, and replace all stale screenshots with a categorized set of 13 new ones.

**Architecture:** A single Python Appium script (`screenshots/walkthrough.py`) orchestrates UI navigation via UiAutomator2. It uses semantic `contentDescription` selectors for Compose icon buttons, ancestor-XPath for bottom nav, and explicit `WebDriverWait` for animations. The script clears app data to force onboarding, captures screenshots by category, then deletes old uncategorized images and writes `REPORT.md`.

**Tech Stack:** Python 3, Appium 2.x (UiAutomator2), Selenium WebDriver, Android Gradle Plugin, Android Emulator.

---

## File Structure

| File | Responsibility |
|---|---|
| `screenshots/walkthrough.py` | Main Appium test script — rewritten for v2.0 selectors and screenshot catalog |
| `app/build/outputs/apk/debug/app-debug.apk` | Fresh debug APK built by Gradle |
| `screenshots/REPORT.md` | Auto-generated summary of screens, interactions, and hard-to-locate elements |
| `screenshots/*.png` | Output directory for the 13 categorized screenshots |

---

## Task 1: Build Fresh Debug APK

**Files:**
- Output: `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 1: Clean and assemble debug APK**

Run:
```bash
./gradlew clean assembleDebug
```
Expected: BUILD SUCCESSFUL; APK exists at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 2: Verify APK exists**

Run:
```bash
ls -lh app/build/outputs/apk/debug/app-debug.apk
```
Expected: File size > 10 MB.

---

## Task 2: Rewrite Appium Script (`screenshots/walkthrough.py`)

**Files:**
- Modify: `screenshots/walkthrough.py` (complete rewrite)

- [ ] **Step 1: Write the new script header and configuration**

Replace lines 1–44 with:
```python
#!/usr/bin/env python3
"""
Cosmic ID v2.0 — Automated UI Walkthrough & Screenshot Capture
Captures 13 categorized screenshots for the new Material 3 Compose UI.
"""

import os
import sys
import time
import traceback
from datetime import datetime
from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import (
    NoSuchElementException,
    TimeoutException,
    WebDriverException,
)

# Configuration
SCREENSHOTS_DIR = "/mnt/data2/git_repos/AgeReveal/screenshots"
APPIUM_URL = "http://localhost:4723"
APP_PACKAGE = "com.willowvibe.cosmicid.debug"
APP_ACTIVITY = "com.willowvibe.agereveal.MainActivity"
DEVICE_UDID = "emulator-5554"
APK_PATH = "/mnt/data2/git_repos/AgeReveal/app/build/outputs/apk/debug/app-debug.apk"

# Tracking
screenshots_taken = []
interactions_tested = []
difficult_elements = []
screens_tested = []
```

- [ ] **Step 2: Write helper functions**

Replace lines 45–199 with:
```python
def log(msg):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] {msg}")

def safe_screenshot(name):
    path = os.path.join(SCREENSHOTS_DIR, name)
    try:
        driver.save_screenshot(path)
        screenshots_taken.append(name)
        log(f"Screenshot saved: {name}")
        return True
    except Exception as e:
        log(f"ERROR saving screenshot {name}: {e}")
        return False

def wait_for_element(by, value, timeout=10):
    return WebDriverWait(driver, timeout).until(
        EC.presence_of_element_located((by, value))
    )

def safe_tap(by, value, description, timeout=10):
    try:
        el = wait_for_element(by, value, timeout)
        el.click()
        interactions_tested.append(description)
        log(f"Tapped: {description}")
        time.sleep(0.5)
        return True
    except Exception as e:
        log(f"FAILED to tap {description}: {e}")
        difficult_elements.append(description)
        return False

def safe_input(by, value, text, description, timeout=10):
    try:
        el = wait_for_element(by, value, timeout)
        el.clear()
        el.send_keys(text)
        interactions_tested.append(description)
        log(f"Input: {description} = '{text}'")
        time.sleep(0.3)
        return True
    except Exception as e:
        log(f"FAILED to input {description}: {e}")
        difficult_elements.append(description)
        return False

def navigate_to_tab(tab_label):
    try:
        xpath = f'//android.widget.TextView[@text="{tab_label}"]/ancestor::android.view.View[@clickable="true"]'
        el = driver.find_element(AppiumBy.XPATH, xpath)
        el.click()
        log(f"Navigated to tab: {tab_label}")
        time.sleep(1.0)
        return True
    except Exception as e:
        try:
            el = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                f'new UiSelector().text("{tab_label}")')
            el.click()
            log(f"Navigated to tab: {tab_label}")
            time.sleep(1.0)
            return True
        except Exception as e2:
            log(f"FAILED to navigate to tab {tab_label}: {e2}")
            difficult_elements.append(f"Tab navigation: {tab_label}")
            return False

def dismiss_dialog_if_present():
    try:
        allow_btn = driver.find_element(AppiumBy.ID,
            "com.android.permissioncontroller:id/permission_allow_button")
        allow_btn.click()
        log("Dismissed permission dialog (Allow)")
        time.sleep(0.5)
        return True
    except NoSuchElementException:
        pass
    try:
        deny_btn = driver.find_element(AppiumBy.ID,
            "com.android.permissioncontroller:id/permission_deny_button")
        deny_btn.click()
        log("Dismissed permission dialog (Deny)")
        time.sleep(0.5)
        return True
    except NoSuchElementException:
        pass
    return False

def go_back():
    driver.press_keycode(4)
    time.sleep(0.5)

def hide_keyboard():
    try:
        driver.hide_keyboard()
    except:
        pass

def tap_by_text(text, description, scroll=False, timeout=10):
    try:
        if scroll:
            el = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                f'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("{text}"))')
        else:
            el = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                f'new UiSelector().textContains("{text}")')
        el.click()
        interactions_tested.append(description)
        log(f"Tapped: {description}")
        time.sleep(0.5)
        return True
    except Exception as e:
        log(f"FAILED to tap {description}: {e}")
        difficult_elements.append(description)
        return False
```

- [ ] **Step 3: Write onboarding flow test**

Replace `test_onboarding_flow()` (lines 205–281) with:
```python
def test_onboarding_flow():
    log("=== Testing Onboarding Flow ===")
    driver.execute_script("mobile: shell", {
        "command": "pm clear",
        "args": [APP_PACKAGE],
    })
    time.sleep(2)
    driver.activate_app(APP_PACKAGE)
    time.sleep(3)

    # Permission dialog
    source = driver.page_source
    if "permission" in source.lower() or "Allow" in source:
        safe_screenshot("onboarding_01_permission.png")
        screens_tested.append("Onboarding Permission")
        dismiss_dialog_if_present()
        time.sleep(1)

    # Step 1: Birth date
    source = driver.page_source
    if "Let\'s build your Cosmic ID" in source or "Tap to pick your birth date" in source:
        log("Onboarding screen detected")
        safe_screenshot("onboarding_02_birthdate.png")
        screens_tested.append("Onboarding Birth Date")

    try:
        date_box = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().textContains("Tap to pick your birth date")')
        date_box.click()
        log("Tapped birth date box")
        interactions_tested.append("Onboarding birth date box tap")
        time.sleep(1)
    except Exception as e:
        log(f"Onboarding date box tap failed: {e}")

    try:
        ok_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("OK")')
        ok_btn.click()
        log("Selected birth date (OK)")
        interactions_tested.append("Onboarding birth date OK")
        time.sleep(2)
    except Exception as e:
        log(f"Onboarding date OK failed: {e}")

    # Step 2: Time/Location
    try:
        skip_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().textContains("don\'t know my birth time")')
        skip_btn.click()
        log("Skipped optional birth time")
        interactions_tested.append("Onboarding Skip birth time")
        time.sleep(1)
    except Exception as e:
        log(f"Onboarding Skip failed: {e}")

    try:
        next_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Next")')
        next_btn.click()
        log("Tapped Next on onboarding")
        interactions_tested.append("Onboarding Next")
        time.sleep(2)
        safe_screenshot("onboarding_03_time_location.png")
        screens_tested.append("Onboarding Time Location")
    except Exception as e:
        log(f"Onboarding Next failed: {e}")

    # Enter My Cosmos
    try:
        enter_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Enter My Cosmos")')
        enter_btn.click()
        log("Tapped Enter My Cosmos")
        interactions_tested.append("Onboarding Enter My Cosmos")
        time.sleep(2)
    except Exception as e:
        log(f"Enter My Cosmos failed: {e}")
```

- [ ] **Step 4: Write My Cosmos tab test**

Replace `test_calculator_tab()` (lines 283–443) with:
```python
def test_mycosmos_tab():
    log("=== Testing My Cosmos Tab ===")
    navigate_to_tab("My Cosmos")
    time.sleep(2)  # Staggered entrance animations
    safe_screenshot("tab1_mycosmos_default.png")
    screens_tested.append("My Cosmos (default)")

    # Wait for rotating highlight — fortune card
    time.sleep(4)
    safe_screenshot("tab1_mycosmos_highlight_fortune.png")
    screens_tested.append("My Cosmos Highlight Fortune")

    # Explore full profile → DetailsUnlockScreen
    try:
        explore_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().textContains("Explore full profile")')
        explore_btn.click()
        log("Tapped Explore full profile")
        interactions_tested.append("Explore full profile CTA")
        time.sleep(2)
        safe_screenshot("tab1_details_astrology.png")
        screens_tested.append("Details Astrology")
        go_back()
        time.sleep(0.5)
    except Exception as e:
        log(f"Explore full profile CTA failed: {e}")
        difficult_elements.append("Explore full profile CTA")
```

- [ ] **Step 5: Write Match tab test**

Replace `test_compatibility_tab()` (lines 591–684) with:
```python
def test_match_tab():
    log("=== Testing Match Tab ===")
    navigate_to_tab("Match")
    time.sleep(1)
    safe_screenshot("tab2_match_empty.png")
    screens_tested.append("Match Empty")

    # Fill Person A
    try:
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if len(edit_texts) >= 2:
            edit_texts[0].send_keys("Alice")
            log("Entered Person A name: Alice")
            interactions_tested.append("Person A name input")
            time.sleep(0.3)
    except Exception as e:
        log(f"Person A input failed: {e}")

    hide_keyboard()
    time.sleep(0.3)

    tap_by_text("Tap to set birthday", "Person A date picker")
    time.sleep(1)
    try:
        ok_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("OK")')
        ok_btn.click()
        log("Selected Person A date")
        interactions_tested.append("Person A date selection")
        time.sleep(0.5)
    except Exception as e:
        log(f"Person A date OK failed: {e}")

    # Fill Person B
    try:
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if len(edit_texts) >= 2:
            edit_texts[1].send_keys("Bob")
            log("Entered Person B name: Bob")
            interactions_tested.append("Person B name input")
            time.sleep(0.3)
    except Exception as e:
        log(f"Person B input failed: {e}")

    hide_keyboard()
    time.sleep(0.3)

    try:
        date_rows = driver.find_elements(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().textContains("Tap to set birthday")')
        if len(date_rows) >= 2:
            date_rows[1].click()
            log("Tapped Person B date row")
            time.sleep(1)
            ok_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().text("OK")')
            ok_btn.click()
            log("Selected Person B date")
            interactions_tested.append("Person B date selection")
            time.sleep(0.5)
    except Exception as e:
        log(f"Person B date failed: {e}")

    # Scroll to results
    try:
        driver.swipe(540, 1800, 540, 800, 500)
        time.sleep(0.5)
        safe_screenshot("tab2_match_results.png")
        screens_tested.append("Match Results")
    except:
        pass
```

- [ ] **Step 6: Write Reminders and Timeline tests**

Replace `test_reminders_tab()` (lines 686–756) with:
```python
def test_reminders_tab():
    log("=== Testing Reminders Tab ===")
    navigate_to_tab("Bdays")
    time.sleep(1)
    safe_screenshot("tab3_bdays_default.png")
    screens_tested.append("Bdays Default")

    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Add birthday", "Add birthday FAB")
    time.sleep(1)
    safe_screenshot("tab3_bdays_add_sheet.png")
    screens_tested.append("Bdays Add Sheet")

    # Cancel out
    try:
        cancel_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Cancel")')
        cancel_btn.click()
        log("Cancelled add sheet")
        time.sleep(0.5)
    except Exception as e:
        log(f"Cancel add sheet failed: {e}")
```

Replace `test_timeline_tab()` (lines 758–778) with:
```python
def test_timeline_tab():
    log("=== Testing Timeline Tab ===")
    navigate_to_tab("Timeline")
    time.sleep(1)
    try:
        driver.swipe(540, 1800, 540, 800, 500)
        time.sleep(0.5)
        safe_screenshot("tab4_timeline_scrolled.png")
        screens_tested.append("Timeline Scrolled")
    except:
        pass
```

- [ ] **Step 7: Write Settings and Paywall tests**

Replace `test_settings_screen()` (lines 780–892) with:
```python
def test_settings_screen():
    log("=== Testing Settings Screen ===")
    navigate_to_tab("My Cosmos")
    time.sleep(2)
    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Settings", "Settings button")
    time.sleep(1)
    safe_screenshot("settings_unified_view.png")
    screens_tested.append("Settings Unified")

    # Scroll to theme picker
    try:
        theme_row = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Theme"))')
        log("Found Theme picker")
        interactions_tested.append("Theme picker found")
        time.sleep(0.3)
        safe_screenshot("settings_theme_picker.png")
        screens_tested.append("Settings Theme Picker")
    except Exception as e:
        log(f"Theme picker find failed: {e}")
        difficult_elements.append("Theme picker")

    go_back()
    time.sleep(1)

def test_paywall_modal():
    log("=== Testing Paywall Modal ===")
    navigate_to_tab("My Cosmos")
    time.sleep(2)
    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Settings", "Settings button")
    time.sleep(1)

    try:
        locked_theme = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Vaporwave"))')
        locked_theme.click()
        log("Tapped locked Vaporwave theme")
        interactions_tested.append("Locked theme tap")
        time.sleep(2)
        safe_screenshot("paywall_modal.png")
        screens_tested.append("Paywall Modal")
        go_back()
        time.sleep(0.5)
    except Exception as e:
        log(f"Paywall test failed: {e}")
        difficult_elements.append("Paywall trigger")
```

- [ ] **Step 8: Write cleanup, report generation, and main**

Replace `generate_report()` (lines 962–1019) with:
```python
def cleanup_old_screenshots():
    log("=== Cleaning Up Old Screenshots ===")
    valid_names = {
        "onboarding_01_permission.png",
        "onboarding_02_birthdate.png",
        "onboarding_03_time_location.png",
        "paywall_modal.png",
        "tab1_mycosmos_default.png",
        "tab1_mycosmos_highlight_fortune.png",
        "tab1_details_astrology.png",
        "tab2_match_empty.png",
        "tab2_match_results.png",
        "tab3_bdays_default.png",
        "tab3_bdays_add_sheet.png",
        "tab4_timeline_scrolled.png",
        "settings_unified_view.png",
        "settings_theme_picker.png",
        "REPORT.md",
        "walkthrough.py",
        "walkthrough_log.txt",
    }
    deleted = 0
    for fname in os.listdir(SCREENSHOTS_DIR):
        fpath = os.path.join(SCREENSHOTS_DIR, fname)
        if os.path.isfile(fpath) and fname not in valid_names:
            os.remove(fpath)
            deleted += 1
            log(f"Deleted old screenshot: {fname}")
    log(f"Total old files deleted: {deleted}")

def generate_report():
    report_path = os.path.join(SCREENSHOTS_DIR, "REPORT.md")
    with open(report_path, "w") as f:
        f.write("# Cosmic ID v2.0 UI Walkthrough Report\n\n")
        f.write(f"**Date:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        f.write("## Summary\n\n")
        f.write(f"- **Total screens tested:** {len(screens_tested)}\n")
        f.write(f"- **Total interactions tested:** {len(interactions_tested)}\n")
        f.write(f"- **Total screenshots captured:** {len(screenshots_taken)}\n")
        f.write(f"- **Difficult-to-locate elements:** {len(difficult_elements)}\n\n")

        f.write("## Screens Tested\n\n")
        for screen in screens_tested:
            f.write(f"- {screen}\n")
        f.write("\n")

        f.write("## Screenshots\n\n")
        for screenshot in screenshots_taken:
            f.write(f"- `{screenshot}`\n")
        f.write("\n")

        if difficult_elements:
            f.write("## Difficult-to-Locate Elements\n\n")
            for el in difficult_elements:
                f.write(f"- {el}\n")
            f.write("\n")
        else:
            f.write("## Difficult-to-Locate Elements\n\nNone.\n\n")

        f.write("## Notes\n\n")
        f.write("- Report generated automatically via Appium UI testing.\n")
        f.write("- v2.0 changes: Badges moved to My Cosmos header, Settings consolidated, Hindi locale toggle removed.\n")

    log(f"Report generated: {report_path}")
```

Replace `main()` (lines 1025–1104) with:
```python
def main():
    global driver
    log("Starting Cosmic ID v2.0 UI Walkthrough")
    os.makedirs(SCREENSHOTS_DIR, exist_ok=True)

    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.udid = DEVICE_UDID
    options.app = APK_PATH
    options.app_package = APP_PACKAGE
    options.app_activity = APP_ACTIVITY
    options.no_reset = False
    options.new_command_timeout = 300
    options.automation_name = "UiAutomator2"
    options.set_capability("uiautomator2ServerLaunchTimeout", 120000)
    options.set_capability("uiautomator2ServerInstallTimeout", 120000)
    options.set_capability("adbExecTimeout", 60000)

    log(f"Connecting to Appium at {APPIUM_URL}")
    driver = webdriver.Remote(APPIUM_URL, options=options)
    log("Session created successfully")
    time.sleep(3)

    source = driver.page_source
    if "com.android.systemui" in source and "notification" in source.lower():
        log("Notification shade detected, pressing back to close")
        driver.press_keycode(4)
        time.sleep(1)

    dismiss_dialog_if_present()
    time.sleep(1)
    driver.activate_app(APP_PACKAGE)
    time.sleep(2)

    try:
        test_onboarding_flow()
        test_mycosmos_tab()
        test_match_tab()
        test_reminders_tab()
        test_timeline_tab()
        test_settings_screen()
        test_paywall_modal()
        cleanup_old_screenshots()
        generate_report()

        log("=== Walkthrough Complete ===")
        log(f"Screens: {len(screens_tested)}")
        log(f"Interactions: {len(interactions_tested)}")
        log(f"Screenshots: {len(screenshots_taken)}")
        log(f"Difficult elements: {len(difficult_elements)}")

        # Self-test: validate all 13 screenshots exist
        expected = [
            "onboarding_01_permission.png",
            "onboarding_02_birthdate.png",
            "onboarding_03_time_location.png",
            "paywall_modal.png",
            "tab1_mycosmos_default.png",
            "tab1_mycosmos_highlight_fortune.png",
            "tab1_details_astrology.png",
            "tab2_match_empty.png",
            "tab2_match_results.png",
            "tab3_bdays_default.png",
            "tab3_bdays_add_sheet.png",
            "tab4_timeline_scrolled.png",
            "settings_unified_view.png",
            "settings_theme_picker.png",
        ]
        missing = [name for name in expected if name not in screenshots_taken]
        if missing:
            log(f"WARNING: Missing screenshots: {missing}")
        else:
            log("All expected screenshots captured.")

    except Exception as e:
        log(f"CRITICAL ERROR during walkthrough: {e}")
        traceback.print_exc()
        try:
            generate_report()
        except:
            pass
    finally:
        log("Quitting driver")
        driver.quit()

if __name__ == "__main__":
    main()
```

---

## Task 3: Execute Walkthrough

**Files:**
- Input: `screenshots/walkthrough.py`, `app/build/outputs/apk/debug/app-debug.apk`
- Output: `screenshots/*.png`, `screenshots/REPORT.md`

- [ ] **Step 1: Verify emulator is online**

Run:
```bash
adb devices
```
Expected: `emulator-5554 device`.

- [ ] **Step 2: Start Appium server (if not running)**

Run:
```bash
appium --allow-insecure chromedriver_autodownload
```
Or verify:
```bash
curl http://localhost:4723/status
```
Expected: JSON with `"status": 0`.

- [ ] **Step 3: Run the walkthrough script**

Run:
```bash
cd /mnt/data2/git_repos/AgeReveal/screenshots && python3 walkthrough.py
```
Expected: Logs show each step, 14 screenshots saved, report generated.

- [ ] **Step 4: Validate outputs**

Run:
```bash
ls -lh /mnt/data2/git_repos/AgeReveal/screenshots/*.png | wc -l
```
Expected: 14 `.png` files (13 screenshots + possible extras).

Run:
```bash
ls -lh /mnt/data2/git_repos/AgeReveal/screenshots/REPORT.md
```
Expected: File exists and size > 0.

---

## Spec Coverage Check

| Spec Requirement | Plan Task |
|---|---|
| Build fresh debug APK | Task 1 |
| Rewrite script with v2.0 selectors | Task 2 |
| Use `contentDescription` for icon buttons | Task 2, Step 2 |
| Use ancestor-XPath for bottom nav | Task 2, Step 2 |
| Wait for staggered animations (2 s) | Task 2, Step 4 |
| Wait 4 s for rotating highlight | Task 2, Step 4 |
| Capture 13 categorized screenshots | Task 2, Steps 3–7 |
| Delete old uncategorized screenshots | Task 2, Step 8 (`cleanup_old_screenshots`) |
| Generate REPORT.md with difficult elements | Task 2, Step 8 (`generate_report`) |
| Validate all screenshots exist | Task 2, Step 8 (self-test in `main`) |

## Placeholder Scan

- No "TBD", "TODO", or "implement later" found.
- All code blocks contain complete, runnable Python.
- All file paths are exact.
- All commands include expected output.

## Type Consistency

- `safe_tap`, `safe_input`, `safe_screenshot` signatures unchanged from v1.x script — consistent.
- `navigate_to_tab` still uses `tab_label: str` — consistent.
- Global tracking lists (`screenshots_taken`, `interactions_tested`, `difficult_elements`, `screens_tested`) initialized once in header — consistent.
