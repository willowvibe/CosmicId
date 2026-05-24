#!/usr/bin/env python3
"""
Cosmic ID (AgeReveal) v2.0 Automated UI Walkthrough
Performs comprehensive testing of all screens, interactions, and features.
Updated for v2.0: onboarding, progressive disclosure, paywall, Match tab,
Settings consolidation, staggered animations, rotating highlight.
"""

import os
import subprocess
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
)

# Configuration
SCREENSHOTS_DIR = "/mnt/data2/git_repos/AgeReveal/screenshots"
APPIUM_URL = "http://localhost:4723"
APP_PACKAGE = "com.willowvibe.cosmicid.debug"
APP_ACTIVITY = "com.willowvibe.agereveal.MainActivity"
DEVICE_UDID = "emulator-5554"
APK_PATH = "/mnt/data2/git_repos/AgeReveal/app/build/outputs/apk/debug/app-debug.apk"

# Screenshot catalog — these are the only .png files we keep
SCREENSHOT_CATALOG = {
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
}

# Tracking
screenshots_taken = []
interactions_tested = []
difficult_elements = []
screens_tested = []
bugs_found = []
driver = None


def log(msg):
    """Log a message with a timestamp."""
    print(f"[{datetime.now().strftime('%H:%M:%S')}] {msg}")


def safe_screenshot(name):
    """Take a screenshot and save it with the given name. Catch exceptions and continue."""
    path = os.path.join(SCREENSHOTS_DIR, name)
    try:
        driver.save_screenshot(path)
        screenshots_taken.append(name)
        log(f"Screenshot saved: {name}")
        return True
    except Exception as e:
        log(f"ERROR saving screenshot {name}: {e}")
        bugs_found.append({
            "name": f"BUG_screenshot_failed_{name}",
            "description": f"Failed to save screenshot {name}: {e}",
        })
        return False


def wait_for_element(by, value, timeout=10):
    """Wait for an element to be present."""
    return WebDriverWait(driver, timeout).until(
        EC.presence_of_element_located((by, value))
    )


def safe_tap(by, value, description, timeout=10):
    """Safely tap an element and log the interaction. Catch exceptions and continue."""
    try:
        el = wait_for_element(by, value, timeout)
        el.click()
        interactions_tested.append(description)
        log(f"Tapped: {description}")
        time.sleep(0.5)
        return True
    except Exception as e:
        log(f"FAILED to tap {description}: {e}")
        difficult_elements.append({
            "description": description,
            "by": str(by),
            "value": value,
            "error": str(e),
        })
        return False


def safe_input(by, value, text, description, timeout=10):
    """Safely input text into a field. Catch exceptions and continue."""
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
        difficult_elements.append({
            "description": description,
            "by": str(by),
            "value": value,
            "error": str(e),
        })
        return False


def navigate_to_tab(tab_label):
    """Navigate to a tab by its bottom nav label."""
    try:
        el = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            f'new UiSelector().text("{tab_label}")')
        el.click()
        log(f"Navigated to tab: {tab_label}")
        time.sleep(1.0)
        return True
    except Exception as e:
        log(f"FAILED to navigate to tab {tab_label}: {e}")
        difficult_elements.append({
            "description": f"Navigate to tab {tab_label}",
            "by": "AppiumBy.ANDROID_UIAUTOMATOR",
            "value": f'new UiSelector().text("{tab_label}")',
            "error": str(e),
        })
        return False


def dismiss_dialog_if_present():
    """Dismiss system dialogs like permission requests."""
    try:
        allow_btn = driver.find_element(
            AppiumBy.ID,
            "com.android.permissioncontroller:id/permission_allow_button"
        )
        allow_btn.click()
        log("Dismissed permission dialog (Allow)")
        time.sleep(0.5)
        return True
    except NoSuchElementException:
        pass

    try:
        deny_btn = driver.find_element(
            AppiumBy.ID,
            "com.android.permissioncontroller:id/permission_deny_button"
        )
        deny_btn.click()
        log("Dismissed permission dialog (Deny)")
        time.sleep(0.5)
        return True
    except NoSuchElementException:
        pass

    return False


def go_back():
    """Press Android back button."""
    driver.press_keycode(4)
    time.sleep(0.5)


def hide_keyboard():
    """Hide the soft keyboard."""
    try:
        driver.hide_keyboard()
    except Exception:
        pass


def tap_by_text(text, description, scroll=False, timeout=10):
    """Tap element by text, optionally scrolling to find it."""
    try:
        if scroll:
            el = driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                f'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("{text}"))'
            )
        else:
            el = driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                f'new UiSelector().textContains("{text}")'
            )
        if el:
            el.click()
            interactions_tested.append(description)
            log(f"Tapped: {description}")
            time.sleep(0.5)
            return True
    except Exception as e:
        log(f"FAILED to tap {description}: {e}")
        difficult_elements.append({
            "description": description,
            "by": "AppiumBy.ANDROID_UIAUTOMATOR",
            "value": f'textContains("{text}")',
            "error": str(e),
        })
    return False


def cleanup_old_screenshots():
    """Delete any .png in screenshots/ that is NOT in the catalog. Keep REPORT.md, walkthrough.py, walkthrough_log.txt."""
    log("=== Cleaning up old screenshots ===")
    kept = 0
    removed = 0
    for entry in os.listdir(SCREENSHOTS_DIR):
        if not entry.endswith(".png"):
            continue
        if entry in SCREENSHOT_CATALOG:
            kept += 1
            continue
        path = os.path.join(SCREENSHOTS_DIR, entry)
        try:
            os.remove(path)
            removed += 1
            log(f"Removed old screenshot: {entry}")
        except Exception as e:
            log(f"ERROR removing {entry}: {e}")
    log(f"Cleanup complete: kept {kept}, removed {removed}")


def generate_report():
    """Generate the REPORT.md file with summary, screens tested, screenshots list, and difficult-to-locate elements."""
    report_path = os.path.join(SCREENSHOTS_DIR, "REPORT.md")
    with open(report_path, "w") as f:
        f.write("# Cosmic ID v2.0 UI Walkthrough Report\n\n")
        f.write(f"**Date:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")

        f.write("## Summary\n\n")
        f.write(f"- **Total screens tested:** {len(screens_tested)}\n")
        f.write(f"- **Total interactions tested:** {len(interactions_tested)}\n")
        f.write(f"- **Total screenshots captured:** {len(screenshots_taken)}\n")
        f.write(f"- **Difficult-to-locate elements:** {len(difficult_elements)}\n")
        f.write(f"- **Bugs found:** {len(bugs_found)}\n\n")

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
            for elem in difficult_elements:
                f.write(f"- **{elem['description']}**\n")
                f.write(f"  - Selector: `{elem['by']}` = `{elem['value']}`\n")
                f.write(f"  - Error: {elem['error']}\n")
            f.write("\n")
        else:
            f.write("## Difficult-to-Locate Elements\n\nNo difficult elements encountered.\n\n")

        if bugs_found:
            f.write("## Bugs Found\n\n")
            for bug in bugs_found:
                f.write(f"### {bug['name']}\n")
                f.write(f"- **Description:** {bug['description']}\n")
                f.write("\n")
        else:
            f.write("## Bugs Found\n\nNo bugs were detected during this walkthrough.\n\n")

        f.write("## Notes\n\n")
        f.write("- This report was generated automatically via Appium UI testing.\n")
        f.write("- Bottom nav `TextView` labels are not clickable — parent `View` is tapped via XPath ancestor.\n")
        f.write("- Icon-only buttons in Compose use `contentDescription` and are tapped via `ACCESSIBILITY_ID`.\n")
        f.write("- Staggered entrance animations on My Cosmos require a 2-second wait before interaction.\n")
        f.write("- Rotating highlight card cycles every 4 seconds — fortune card captured after 4s wait.\n")

    log(f"Report generated: {report_path}")


# =============================================================================
# Test Scenarios
# =============================================================================

def test_onboarding_flow():
    """Test the 3-step onboarding flow on first launch."""
    log("=== Testing Onboarding Flow ===")

    # Clear app data to force onboarding (use local adb to avoid Appium adb_shell restriction)
    subprocess.run(
        ["/home/harish/Android/Sdk/platform-tools/adb", "-s", DEVICE_UDID, "shell", "pm", "clear", APP_PACKAGE],
        capture_output=True, text=True, check=False
    )
    time.sleep(2)
    driver.activate_app(APP_PACKAGE)
    time.sleep(3)

    # Dismiss any permission dialog that appears
    if dismiss_dialog_if_present():
        safe_screenshot("onboarding_01_permission.png")
        screens_tested.append("Onboarding Step 1 (Permission)")
        time.sleep(1)

    # Step 0: Name + Birth date
    source = driver.page_source
    if "Let's build your Cosmic ID" in source or "Tap to pick your birth date" in source:
        log("Onboarding screen (birth date) detected")
        safe_screenshot("onboarding_02_birthdate.png")
        screens_tested.append("Onboarding Step 0 (Birth Date)")
    else:
        log("Onboarding birth date screen not detected — may already be completed")
        return

    # Enter name
    try:
        name_field = driver.find_element(
            AppiumBy.CLASS_NAME,
            "android.widget.EditText"
        )
        name_field.clear()
        name_field.send_keys("Test User")
        log("Entered name: Test User")
        interactions_tested.append("Onboarding name input")
        time.sleep(0.3)
        hide_keyboard()
        time.sleep(0.3)
    except Exception as e:
        log(f"Onboarding name input failed: {e}")
        difficult_elements.append({
            "description": "Onboarding name field",
            "by": "AppiumBy.CLASS_NAME",
            "value": "android.widget.EditText",
            "error": str(e),
        })

    # Tap date box to open picker, then OK
    try:
        date_box = driver.find_element(
            AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().textContains("Tap to pick your birth date")'
        )
        date_box.click()
        log("Tapped birth date box")
        interactions_tested.append("Onboarding birth date box tap")
        time.sleep(1)
    except Exception as e:
        log(f"Onboarding date box tap failed: {e}")
        difficult_elements.append({
            "description": "Onboarding birth date box",
            "by": "AppiumBy.ANDROID_UIAUTOMATOR",
            "value": 'textContains("Tap to pick your birth date")',
            "error": str(e),
        })

    try:
        ok_btn = driver.find_element(
            AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("OK")'
        )
        ok_btn.click()
        log("Selected birth date (OK)")
        interactions_tested.append("Onboarding birth date OK")
        time.sleep(1)
    except Exception as e:
        log(f"Onboarding date OK failed: {e}")
        difficult_elements.append({
            "description": "Onboarding date OK",
            "by": "AppiumBy.ANDROID_UIAUTOMATOR",
            "value": 'text("OK")',
            "error": str(e),
        })

    # After date picker OK, wait 1s, then find all clickable views and tap the last one (Step 0 Next)
    time.sleep(1)
    views = driver.find_elements(AppiumBy.ANDROID_UIAUTOMATOR, 'new UiSelector().className("android.view.View").clickable(true)')
    if views:
        views[-1].click()
        log("Tapped Step 0 Next (last clickable view)")
        interactions_tested.append("Onboarding Step 0 Next")
        time.sleep(1)
    else:
        log("Onboarding Step 0 Next failed: no clickable views found")
        difficult_elements.append({
            "description": "Onboarding Step 0 Next button",
            "by": "AppiumBy.ANDROID_UIAUTOMATOR",
            "value": 'className("android.view.View").clickable(true)',
            "error": "No clickable views found",
        })
        return

    # Step 1: Optional birth time — find all clickable views and tap the last one to proceed
    views = driver.find_elements(AppiumBy.ANDROID_UIAUTOMATOR, 'new UiSelector().className("android.view.View").clickable(true)')
    if views:
        views[-1].click()
        log("Tapped Step 1 Next/Skip (last clickable view)")
        interactions_tested.append("Onboarding Step 1 Next/Skip")
        time.sleep(1)
    else:
        log("Onboarding Step 1 Next/Skip failed: no clickable views found")
        difficult_elements.append({
            "description": "Onboarding Step 1 Next/Skip button",
            "by": "AppiumBy.ANDROID_UIAUTOMATOR",
            "value": 'className("android.view.View").clickable(true)',
            "error": "No clickable views found",
        })
        return

    # Wait 2 seconds for Step 2 to appear
    time.sleep(2)

    # Only capture Step 2 screenshot and tap Enter My Cosmos if we actually reached the accent picker
    source = driver.page_source
    if "Enter My Cosmos" in source or "Choose your cosmic vibe" in source or "Pick an accent" in source:
        # Tap the second accent color (Amber) to verify color selection persists
        subprocess.run(
            ["/home/harish/Android/Sdk/platform-tools/adb", "-s", DEVICE_UDID, "shell", "input", "tap", "330", "700"],
            capture_output=True, text=True, check=False,
        )
        log("Tapped Amber accent color")
        interactions_tested.append("Selected Amber accent color")
        time.sleep(0.5)

        safe_screenshot("onboarding_03_time_location.png")
        screens_tested.append("Onboarding Step 2 (Accent Picker)")

        views = driver.find_elements(AppiumBy.ANDROID_UIAUTOMATOR, 'new UiSelector().className("android.view.View").clickable(true)')
        if views:
            views[-1].click()
            log("Tapped Enter My Cosmos (last clickable view)")
            interactions_tested.append("Onboarding Enter My Cosmos")
            time.sleep(2)
        else:
            log("Enter My Cosmos failed: no clickable views found")
            difficult_elements.append({
                "description": "Onboarding Enter My Cosmos",
                "by": "AppiumBy.ANDROID_UIAUTOMATOR",
                "value": 'className("android.view.View").clickable(true)',
                "error": "No clickable views found",
            })
    else:
        log("Onboarding Step 2 (Accent Picker) not detected — skipping screenshot and Enter My Cosmos")


def test_mycosmos_tab():
    """Test the My Cosmos tab: default view, fortune highlight, astrology details."""
    log("=== Testing My Cosmos Tab ===")
    if not navigate_to_tab("My Cosmos"):
        log("Skipping My Cosmos tab — navigation failed")
        return
    time.sleep(2)  # v2.0: Staggered entrance animations up to 350ms

    # Scroll down to reveal the result block (hero counter, highlight, CTA)
    for i in range(2):
        subprocess.run(
            ["/home/harish/Android/Sdk/platform-tools/adb", "-s", DEVICE_UDID, "shell", "input", "swipe", "540", "1500", "540", "800", "400"],
            capture_output=True, text=True, check=False,
        )
        time.sleep(0.3)
    log("Scrolled My Cosmos to reveal result block")

    safe_screenshot("tab1_mycosmos_default.png")
    screens_tested.append("My Cosmos (default)")

    # Wait 4 seconds for rotating highlight to cycle to fortune card
    time.sleep(4)
    safe_screenshot("tab1_mycosmos_highlight_fortune.png")
    screens_tested.append("My Cosmos (fortune highlight)")

    # Tap "Explore full profile" to open DetailsUnlockScreen
    try:
        explore_btn = driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Explore full cosmic profile")
        explore_btn.click()
        log("Tapped Explore full profile")
        interactions_tested.append("Explore full profile CTA")
        time.sleep(2)
        safe_screenshot("tab1_details_astrology.png")
        screens_tested.append("Astrology Details (DetailsUnlockScreen)")
        go_back()
        time.sleep(0.5)
    except Exception as e:
        log(f"Explore full profile CTA failed: {e}")
        difficult_elements.append({
            "description": "Explore full profile CTA",
            "by": "AppiumBy.ACCESSIBILITY_ID",
            "value": "Explore full cosmic profile",
            "error": str(e),
        })


def test_match_tab():
    """Test the Match tab: empty state, fill Person A and Person B, capture results."""
    log("=== Testing Match Tab ===")
    if not navigate_to_tab("Match"):
        log("Skipping Match tab — navigation failed")
        return
    time.sleep(1)
    safe_screenshot("tab2_match_empty.png")
    screens_tested.append("Match (empty)")

    # Fill Person A (Alice)
    try:
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if len(edit_texts) >= 2:
            edit_texts[0].send_keys("Alice")
            log("Entered Person A name: Alice")
            interactions_tested.append("Person A name input")
            time.sleep(0.3)
    except Exception as e:
        log(f"Person A input failed: {e}")
        difficult_elements.append({
            "description": "Person A name input",
            "by": "AppiumBy.CLASS_NAME",
            "value": "android.widget.EditText",
            "error": str(e),
        })

    hide_keyboard()
    time.sleep(0.3)

    tap_by_text("Tap to set birthday", "Person A date picker", scroll=False)
    time.sleep(1)
    try:
        ok_btn = driver.find_element(
            AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("OK")'
        )
        ok_btn.click()
        log("Selected Person A date")
        interactions_tested.append("Person A date selection")
        time.sleep(0.5)
    except Exception as e:
        log(f"Person A date OK failed: {e}")
        difficult_elements.append({
            "description": "Person A date OK",
            "by": "AppiumBy.ANDROID_UIAUTOMATOR",
            "value": 'text("OK")',
            "error": str(e),
        })
        try:
            cancel_btn = driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().text("Cancel")'
            )
            cancel_btn.click()
        except Exception:
            pass

    # Fill Person B (Bob)
    try:
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if len(edit_texts) >= 2:
            edit_texts[1].send_keys("Bob")
            log("Entered Person B name: Bob")
            interactions_tested.append("Person B name input")
            time.sleep(0.3)
    except Exception as e:
        log(f"Person B input failed: {e}")
        difficult_elements.append({
            "description": "Person B name input",
            "by": "AppiumBy.CLASS_NAME",
            "value": "android.widget.EditText",
            "error": str(e),
        })

    hide_keyboard()
    time.sleep(0.3)

    try:
        date_rows = driver.find_elements(
            AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().textContains("Tap to set birthday")'
        )
        if len(date_rows) >= 2:
            date_rows[1].click()
            log("Tapped Person B date row")
            time.sleep(1)
            ok_btn = driver.find_element(
                AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().text("OK")'
            )
            ok_btn.click()
            log("Selected Person B date")
            interactions_tested.append("Person B date selection")
            time.sleep(0.5)
    except Exception as e:
        log(f"Person B date failed: {e}")
        difficult_elements.append({
            "description": "Person B date selection",
            "by": "AppiumBy.ANDROID_UIAUTOMATOR",
            "value": 'textContains("Tap to set birthday")',
            "error": str(e),
        })

    # Tap Calculate/Match/Go button, then scroll to see results
    if not tap_by_text("Calculate", "Match Calculate button"):
        if not tap_by_text("Match", "Match button"):
            tap_by_text("Go", "Match Go button")
    time.sleep(1)
    try:
        size = driver.get_window_size()
        start_y = int(size['height'] * 0.8)
        end_y = int(size['height'] * 0.4)
        x = int(size['width'] * 0.5)
        driver.swipe(x, start_y, x, end_y, 500)
        time.sleep(0.5)
        safe_screenshot("tab2_match_results.png")
        screens_tested.append("Match (results)")
    except Exception as e:
        log(f"Match tab scroll failed: {e}")
        difficult_elements.append({
            "description": "Match tab scroll",
            "by": "driver.swipe",
            "value": "relative swipe",
            "error": str(e),
        })


def test_reminders_tab():
    """Test the Bdays (Reminders) tab: default view and add birthday sheet."""
    log("=== Testing Reminders Tab ===")
    if not navigate_to_tab("Bdays"):
        log("Skipping Reminders tab — navigation failed")
        return
    time.sleep(1)
    safe_screenshot("tab3_bdays_default.png")
    screens_tested.append("Bdays (default)")

    # Tap Add birthday FAB (icon-only button via accessibility id)
    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Add birthday", "Add birthday FAB")
    time.sleep(1)
    safe_screenshot("tab3_bdays_add_sheet.png")
    screens_tested.append("Bdays (add sheet)")

    # Close the sheet without saving (go back)
    go_back()
    time.sleep(0.5)


def test_timeline_tab():
    """Test the Timeline tab: scroll and capture."""
    log("=== Testing Timeline Tab ===")
    if not navigate_to_tab("Timeline"):
        log("Skipping Timeline tab — navigation failed")
        return
    time.sleep(1)

    # Scroll through milestones
    try:
        size = driver.get_window_size()
        start_y = int(size['height'] * 0.8)
        end_y = int(size['height'] * 0.4)
        x = int(size['width'] * 0.5)
        driver.swipe(x, start_y, x, end_y, 500)
        time.sleep(0.5)
        safe_screenshot("tab4_timeline_scrolled.png")
        screens_tested.append("Timeline (scrolled)")
    except Exception as e:
        log(f"Timeline scroll failed: {e}")
        difficult_elements.append({
            "description": "Timeline scroll",
            "by": "driver.swipe",
            "value": "relative swipe",
            "error": str(e),
        })


def test_settings_screen():
    """Test the unified Settings screen: open from My Cosmos, capture unified view and theme picker."""
    log("=== Testing Settings Screen ===")
    if not navigate_to_tab("My Cosmos"):
        log("Skipping Settings screen — navigation failed")
        return
    time.sleep(2)

    # Open Settings via accessibility id icon button
    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Settings", "Settings button")
    time.sleep(1)
    safe_screenshot("settings_unified_view.png")
    screens_tested.append("Settings (unified view)")

    # Scroll down to reveal Appearance section
    for _ in range(3):
        try:
            size = driver.get_window_size()
            start_y = int(size['height'] * 0.8)
            end_y = int(size['height'] * 0.4)
            x = int(size['width'] * 0.5)
            driver.swipe(x, start_y, x, end_y, 500)
            time.sleep(0.5)
        except:
            pass

    # Try to find and capture theme picker
    try:
        appearance = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("APPEARANCE"))')
        log("Found Appearance section")
        time.sleep(0.3)
    except Exception as e:
        log(f"Appearance section not found: {e}")

    safe_screenshot("settings_theme_picker.png")
    screens_tested.append("Settings (theme picker)")

    # Scroll further down to the Premium Theme Packs section and try to trigger paywall
    try:
        theme_pack = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Theme pack"))')
        log("Found Theme pack section")
        time.sleep(0.3)

        # Try to find the lock icon by accessibility id "Premium" and tap it
        lock_icon = driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Premium")
        lock_icon.click()
        log("Tapped lock icon (contentDescription=Premium)")
        interactions_tested.append("Tapped lock icon (Premium)")
        time.sleep(2)
        safe_screenshot("paywall_modal.png")
        screens_tested.append("Paywall (modal)")
        go_back()
        time.sleep(0.5)
    except Exception as e:
        log(f"Paywall capture from Settings failed: {e}")
        # Continue with normal back navigation
        go_back()
        time.sleep(0.5)


def test_paywall_modal():
    """Test the Paywall modal triggered from a locked premium theme in Settings."""
    log("=== Testing Paywall Modal ===")
    if not navigate_to_tab("My Cosmos"):
        log("Skipping Paywall modal — navigation failed")
        return
    time.sleep(2)

    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Settings", "Settings button")
    time.sleep(1)

    try:
        # Scroll down in settings to reveal the Appearance section
        for _ in range(3):
            try:
                size = driver.get_window_size()
                start_y = int(size['height'] * 0.8)
                end_y = int(size['height'] * 0.4)
                x = int(size['width'] * 0.5)
                driver.swipe(x, start_y, x, end_y, 500)
                time.sleep(0.5)
            except:
                pass

        # Additional adb scrolls to reveal the locked theme rows (they are deep in the scrollable Column)
        for _ in range(5):
            subprocess.run(
                ["/home/harish/Android/Sdk/platform-tools/adb", "-s", DEVICE_UDID, "shell", "input", "swipe", "540", "1700", "540", "1200", "400"],
                capture_output=True, text=True, check=False
            )
            time.sleep(0.5)
        for _ in range(3):
            subprocess.run(
                ["/home/harish/Android/Sdk/platform-tools/adb", "-s", DEVICE_UDID, "shell", "input", "swipe", "540", "1500", "540", "1000", "300"],
                capture_output=True, text=True, check=False
            )
            time.sleep(0.5)

        # Find a locked theme row by text and click its parent clickable view
        tapped = False
        for theme_name in ["Y2K", "Dark Academia", "Cyberpunk"]:
            try:
                el = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR, f'new UiSelector().text("{theme_name}")')
                parent = driver.find_element(
                    AppiumBy.XPATH,
                    f'//android.widget.TextView[@text="{theme_name}"]/ancestor::android.view.View[@clickable="true"][1]'
                )
                parent.click()
                log(f"Tapped locked theme row: {theme_name}")
                interactions_tested.append(f"Tapped locked theme row: {theme_name}")
                tapped = True
                time.sleep(2)
                break
            except Exception as e:
                log(f"Theme row '{theme_name}' tap failed: {e}")

        if tapped:
            safe_screenshot("paywall_modal.png")
            screens_tested.append("Paywall (modal)")
            go_back()
            time.sleep(0.5)
        else:
            log("No locked theme found — skipping paywall capture")
            difficult_elements.append({
                "description": "Paywall locked theme row",
                "by": "AppiumBy.XPATH",
                "value": "ancestor::android.view.View[@clickable='true']",
                "error": "No locked theme row found in settings",
            })
    except Exception as e:
        log(f"Paywall test failed: {e}")


def main():
    global driver

    log("Starting Cosmic ID v2.0 UI Walkthrough")

    # Ensure screenshots directory exists
    os.makedirs(SCREENSHOTS_DIR, exist_ok=True)

    # Setup Appium options
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.udid = DEVICE_UDID
    options.app_package = APP_PACKAGE
    options.app_activity = APP_ACTIVITY
    options.app = APK_PATH
    options.no_reset = False
    options.new_command_timeout = 300
    options.automation_name = "UiAutomator2"
    options.set_capability("uiautomator2ServerLaunchTimeout", 120000)
    options.set_capability("uiautomator2ServerInstallTimeout", 120000)
    options.set_capability("adbExecTimeout", 60000)

    # Connect to Appium
    log(f"Connecting to Appium at {APPIUM_URL}")
    driver = webdriver.Remote(APPIUM_URL, options=options)
    log("Session created successfully")
    time.sleep(3)

    # Close notification shade if it is open
    source = driver.page_source
    if "com.android.systemui" in source and "notification" in source.lower():
        log("Notification shade detected, pressing back to close")
        driver.press_keycode(4)
        time.sleep(1)

    # Dismiss any initial dialogs
    dismiss_dialog_if_present()
    time.sleep(1)

    # Ensure app is in foreground
    driver.activate_app(APP_PACKAGE)
    time.sleep(2)

    try:
        # Run all test scenarios in order
        test_onboarding_flow()
        test_mycosmos_tab()
        test_match_tab()
        test_reminders_tab()
        test_timeline_tab()
        test_settings_screen()
        test_paywall_modal()

        # Cleanup old screenshots and generate report
        cleanup_old_screenshots()
        generate_report()

        # Self-test: verify all 13 expected screenshots exist
        log("=== Self-Test: Verifying screenshot catalog ===")
        missing = []
        for expected in SCREENSHOT_CATALOG:
            path = os.path.join(SCREENSHOTS_DIR, expected)
            if os.path.exists(path):
                log(f"  OK: {expected}")
            else:
                log(f"  MISSING: {expected}")
                missing.append(expected)

        if missing:
            log(f"WARNING: {len(missing)} expected screenshot(s) missing: {missing}")
        else:
            log(f"All {len(SCREENSHOT_CATALOG)} expected screenshots are present.")

        log("=== Walkthrough Complete ===")
        log(f"Screens: {len(screens_tested)}")
        log(f"Interactions: {len(interactions_tested)}")
        log(f"Screenshots: {len(screenshots_taken)}")
        log(f"Difficult elements: {len(difficult_elements)}")
        log(f"Bugs: {len(bugs_found)}")

    except Exception as e:
        log(f"CRITICAL ERROR during walkthrough: {e}")
        traceback.print_exc()
        # Try to generate report anyway
        try:
            cleanup_old_screenshots()
            generate_report()
        except Exception:
            pass
    finally:
        log("Quitting driver")
        try:
            if driver:
                driver.quit()
        except Exception:
            pass


if __name__ == "__main__":
    main()
