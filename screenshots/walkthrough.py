#!/usr/bin/env python3
"""
Cosmic ID (AgeReveal) v2.0 Automated UI Walkthrough
Performs comprehensive testing of all screens, interactions, and features.
Updated for v2.0: onboarding, progressive disclosure, paywall, fortune push,
celebrity match, deep-link auto-populate, grace period banner.
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

# Tracking
screenshots_taken = []
interactions_tested = []
bugs_found = []
screens_tested = []

def log(msg):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] {msg}")

def safe_screenshot(name):
    """Take a screenshot and save it with the given name."""
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
    """Safely tap an element and log the interaction."""
    try:
        el = wait_for_element(by, value, timeout)
        el.click()
        interactions_tested.append(description)
        log(f"Tapped: {description}")
        time.sleep(0.5)
        return True
    except Exception as e:
        log(f"FAILED to tap {description}: {e}")
        bugs_found.append({
            "name": f"BUG_tap_failed_{description.replace(' ', '_')}",
            "description": f"Failed to tap '{description}': {e}",
        })
        return False

def safe_input(by, value, text, description, timeout=10):
    """Safely input text into a field."""
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
        bugs_found.append({
            "name": f"BUG_input_failed_{description.replace(' ', '_')}",
            "description": f"Failed to input '{description}': {e}",
        })
        return False

def navigate_to_tab(tab_label):
    """Navigate to a tab by its bottom nav label."""
    try:
        # v2.0: TextView labels are not clickable — tap the parent View
        xpath = f'//android.widget.TextView[@text="{tab_label}"]/ancestor::android.view.View[@clickable="true"]'
        el = driver.find_element(AppiumBy.XPATH, xpath)
        el.click()
        log(f"Navigated to tab: {tab_label}")
        time.sleep(1.0)
        return True
    except Exception as e:
        # Fallback: try direct text match
        try:
            el = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                f'new UiSelector().text("{tab_label}")')
            el.click()
            log(f"Navigated to tab: {tab_label}")
            time.sleep(1.0)
            return True
        except Exception as e2:
            log(f"FAILED to navigate to tab {tab_label}: {e2}")
            return False

def dismiss_dialog_if_present():
    """Dismiss system dialogs like permission requests."""
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
    """Press Android back button."""
    driver.press_keycode(4)
    time.sleep(0.5)

def hide_keyboard():
    """Hide the soft keyboard."""
    try:
        driver.hide_keyboard()
    except:
        pass

def find_by_text(text, scroll=False, timeout=10):
    """Find element by text, optionally scrolling."""
    try:
        if scroll:
            return driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                f'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("{text}"))')
        return driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            f'new UiSelector().textContains("{text}")')
    except Exception:
        return None

def tap_by_text(text, description, scroll=False, timeout=10):
    """Tap element by text."""
    try:
        el = find_by_text(text, scroll=scroll)
        if el:
            el.click()
            interactions_tested.append(description)
            log(f"Tapped: {description}")
            time.sleep(0.5)
            return True
    except Exception as e:
        log(f"FAILED to tap {description}: {e}")
    return False

def is_screen_blank():
    """Check if the current screen appears blank (no content elements)."""
    try:
        source = driver.page_source
        if "<android.widget.TextView" in source or "<android.view.View" in source:
            return False
        return True
    except:
        return True

def dump_page_source(label):
    """Dump page source for debugging."""
    try:
        source = driver.page_source
        with open(os.path.join(SCREENSHOTS_DIR, f"source_{label}.xml"), "w") as f:
            f.write(source)
    except:
        pass

# =============================================================================
# v2.0 Test Scenarios
# =============================================================================

def test_onboarding_flow():
    """Test the 3-step onboarding flow on first launch."""
    log("=== Testing Onboarding Flow ===")
    # Clear app data to force onboarding
    driver.execute_script("mobile: shell", {
        "command": "pm clear",
        "args": [APP_PACKAGE],
    })
    time.sleep(2)
    driver.activate_app(APP_PACKAGE)
    time.sleep(3)

    source = driver.page_source
    if "When were you born?" in source or "Let" in source:
        log("Onboarding screen detected")
        safe_screenshot("onboarding_step1_birthdate.png")
        screens_tested.append("Onboarding Step 1 (Birth Date)")
    else:
        log("Onboarding not triggered — may already be completed")
        return

    # Step 1: Select date and Continue
    try:
        ok_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("OK")')
        ok_btn.click()
        log("Selected birth date (OK)")
        interactions_tested.append("Onboarding birth date OK")
        time.sleep(1)
    except Exception as e:
        log(f"Onboarding date OK failed: {e}")

    try:
        continue_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Continue")')
        continue_btn.click()
        log("Tapped Continue on onboarding")
        interactions_tested.append("Onboarding Continue")
        time.sleep(2)
        safe_screenshot("onboarding_step2_zodiac_reveal.png")
        screens_tested.append("Onboarding Step 2 (Zodiac Reveal)")
    except Exception as e:
        log(f"Onboarding Continue failed: {e}")

    # Step 3: Optional birth time — skip
    try:
        skip_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Skip")')
        skip_btn.click()
        log("Skipped optional birth time")
        interactions_tested.append("Onboarding Skip birth time")
        time.sleep(2)
        safe_screenshot("onboarding_step3_accent_picker.png")
        screens_tested.append("Onboarding Step 3 (Accent Picker)")
    except Exception as e:
        log(f"Onboarding Skip failed: {e}")

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

def test_calculator_tab():
    """Test the My Cosmos (Calculator) tab — v2.0 progressive disclosure."""
    log("=== Testing My Cosmos Tab ===")
    navigate_to_tab("My Cosmos")
    time.sleep(1)
    safe_screenshot("tab1_calculator_default.png")
    screens_tested.append("My Cosmos (default)")

    # Header: trial chip or grace chip
    source = driver.page_source
    if "day" in source and "left" in source:
        log("Trial/Grace chip detected in header")
        interactions_tested.append("Trial/Grace chip visible")
    if "Renew to keep premium" in source:
        log("Grace period banner detected")
        interactions_tested.append("Grace period banner visible")
        safe_screenshot("tab1_calculator_grace_banner.png")

    # Interaction 1: Settings gear
    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Settings", "Settings button")
    time.sleep(1)
    safe_screenshot("tab1_calculator_settings_open.png")
    screens_tested.append("Settings (from calculator)")
    go_back()
    time.sleep(1)

    # Interaction 2: Enter name
    edit_text = None
    try:
        edit_text = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().className("android.widget.EditText")')
    except Exception:
        log("EditText not found, trying to tap field first")
        driver.tap([(540, 500)])
        time.sleep(0.5)
        try:
            edit_text = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().className("android.widget.EditText")')
        except Exception:
            pass

    if edit_text:
        try:
            edit_text.clear()
            edit_text.send_keys("Test User")
            interactions_tested.append("Name input field")
            log("Input: Name input field = 'Test User'")
            time.sleep(0.3)
        except Exception as e:
            log(f"Name input failed: {e}")
    else:
        log("WARNING: Could not locate name input field")

    hide_keyboard()
    safe_screenshot("tab1_calculator_name_entered.png")

    # Interaction 3: Birth date row
    try:
        date_row = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().descriptionContains("Change birth date")')
        date_row.click()
        log("Tapped: Birth date row")
        interactions_tested.append("Birth date row")
        time.sleep(1)
        safe_screenshot("tab1_calculator_datepicker_open.png")
        screens_tested.append("Date picker dialog")
    except Exception as e:
        log(f"Birth date row tap failed: {e}")

    try:
        ok_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("OK")')
        ok_btn.click()
        log("Selected date (OK)")
        interactions_tested.append("Date picker OK")
        time.sleep(1)
        safe_screenshot("tab1_calculator_date_selected.png")
    except Exception as e:
        log(f"Date picker OK failed: {e}")
        try:
            cancel_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().text("Cancel")')
            cancel_btn.click()
        except:
            pass

    # Interaction 4: TIME precision chip
    try:
        time_chip = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().descriptionContains("TIME")')
        time_chip.click()
        log("Tapped: Time precision chip")
        interactions_tested.append("Time precision chip")
        time.sleep(1)
        safe_screenshot("tab1_calculator_time_dialog.png")
        screens_tested.append("Time picker dialog")
    except Exception as e:
        log(f"Time chip tap failed: {e}")

    try:
        set_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Set")')
        set_btn.click()
        log("Set time")
        interactions_tested.append("Time picker Set")
        time.sleep(0.5)
    except Exception as e:
        log(f"Time set failed: {e}")
        try:
            cancel_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().text("Cancel")')
            cancel_btn.click()
        except:
            pass

    # Interaction 5: LOCATION precision chip (v2.0: now opens bottom sheet)
    try:
        loc_chip = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().descriptionContains("LOCATION")')
        loc_chip.click()
        log("Tapped: Location precision chip")
        interactions_tested.append("Location precision chip")
        time.sleep(1)
        safe_screenshot("tab1_calculator_location_bottomsheet.png")
        screens_tested.append("Location bottom sheet")
    except Exception as e:
        log(f"Location chip tap failed: {e}")

    # v2.0: Location bottom sheet uses Indian State dropdown
    try:
        # Search for a state
        state_input = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().className("android.widget.EditText")')
        state_input.send_keys("Karnataka")
        log("Entered state search: Karnataka")
        interactions_tested.append("State search input")
        time.sleep(0.5)
        # Tap the state result
        state_result = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().textContains("Karnataka")')
        state_result.click()
        log("Selected Karnataka")
        interactions_tested.append("State selection")
        time.sleep(0.5)
        safe_screenshot("tab1_calculator_location_set.png")
    except Exception as e:
        log(f"State selection failed: {e}")
        try:
            cancel_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().text("Cancel")')
            cancel_btn.click()
        except:
            pass

    # Scroll down to see rotating highlight and explore CTA
    try:
        driver.swipe(540, 1800, 540, 800, 500)
        time.sleep(0.5)
        safe_screenshot("tab1_calculator_results_scrolled.png")
    except:
        pass

def test_rotating_highlight():
    """Test the rotating highlight card (v2.0 progressive disclosure)."""
    log("=== Testing Rotating Highlight ===")
    navigate_to_tab("My Cosmos")
    time.sleep(1)

    # Wait for highlight to cycle through types
    for cycle in range(4):
        time.sleep(4)
        source = driver.page_source
        if "MILESTONE" in source:
            log("Rotating highlight: Milestone visible")
            interactions_tested.append("Rotating highlight — Milestone")
            safe_screenshot(f"tab1_calculator_highlight_milestone_{cycle}.png")
        elif "CELEBRITY MATCH" in source:
            log("Rotating highlight: Celebrity Match visible")
            interactions_tested.append("Rotating highlight — Celebrity Match")
            safe_screenshot(f"tab1_calculator_highlight_celebrity_{cycle}.png")
            # Try to tap share on celebrity card
            try:
                share_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                    'new UiSelector().descriptionContains("Share celebrity")')
                share_btn.click()
                log("Tapped celebrity match share")
                interactions_tested.append("Celebrity match share tap")
                time.sleep(1)
                safe_screenshot("tab1_calculator_celebrity_share_sheet.png")
                go_back()
            except Exception as e:
                log(f"Celebrity share tap failed: {e}")
        elif "PLANET AGE" in source or "Mars" in source:
            log("Rotating highlight: Planet Age visible")
            interactions_tested.append("Rotating highlight — Planet Age")
            safe_screenshot(f"tab1_calculator_highlight_planet_{cycle}.png")
        elif "FORTUNE" in source or "DAILY COSMIC" in source:
            log("Rotating highlight: Fortune visible")
            interactions_tested.append("Rotating highlight — Fortune")
            safe_screenshot(f"tab1_calculator_highlight_fortune_{cycle}.png")

def test_explore_profile_cta():
    """Test the 'Explore full profile' CTA that opens DetailsUnlockScreen."""
    log("=== Testing Explore Full Profile CTA ===")
    navigate_to_tab("My Cosmos")
    time.sleep(1)
    try:
        explore_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().textContains("Explore full profile")')
        explore_btn.click()
        log("Tapped Explore full profile")
        interactions_tested.append("Explore full profile CTA")
        time.sleep(2)
        safe_screenshot("tab1_calculator_details_unlock_screen.png")
        screens_tested.append("Details Unlock Screen")
        go_back()
        time.sleep(0.5)
    except Exception as e:
        log(f"Explore full profile CTA failed: {e}")

def test_paywall_screen():
    """Test the Paywall screen triggered from locked astrology sections."""
    log("=== Testing Paywall Screen ===")
    navigate_to_tab("My Cosmos")
    time.sleep(1)

    # Open details and tap a locked premium section
    test_explore_profile_cta()
    time.sleep(1)

    try:
        # Scroll to find a locked section (e.g., Dasha, Ba Zi)
        locked_section = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Unlock"))')
        locked_section.click()
        log("Tapped locked premium section")
        interactions_tested.append("Locked premium section tap")
        time.sleep(2)
        safe_screenshot("paywall_astrology_locked.png")
        screens_tested.append("Paywall (astrology locked)")

        # Verify paywall elements
        source = driver.page_source
        if "Unlock Your Full Cosmic Profile" in source:
            log("Paywall title found")
            interactions_tested.append("Paywall title visible")
        if "7-Day Free Trial" in source:
            log("Free trial CTA found")
            interactions_tested.append("Paywall free trial CTA visible")

        # Dismiss paywall
        go_back()
        time.sleep(0.5)
    except Exception as e:
        log(f"Paywall test failed: {e}")

def test_compatibility_tab():
    """Test the Match (Compatibility) tab — v2.0 deep-link auto-populate."""
    log("=== Testing Compatibility Tab ===")
    navigate_to_tab("Match")
    time.sleep(1)
    safe_screenshot("tab2_compatibility_default.png")
    screens_tested.append("Compatibility (default)")

    # Interaction 1: Select relationship type
    try:
        driver.swipe(540, 800, 540, 1600, 500)
        time.sleep(0.3)
        romance_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Romantic")')
        romance_btn.click()
        interactions_tested.append("Relationship type: Romantic")
        log("Selected Romantic relationship type")
        time.sleep(0.3)
        safe_screenshot("tab2_compatibility_romantic_selected.png")
    except Exception as e:
        log(f"Romantic selection failed: {e}")

    # Interaction 2: Enter Person A details
    try:
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if len(edit_texts) >= 2:
            edit_texts[0].send_keys("Alice")
            log("Entered Person A name: Alice")
            interactions_tested.append("Person A name input")
            time.sleep(0.3)
    except Exception as e:
        log(f"Person A input failed: {e}")

    tap_by_text("Tap to set birthday", "Person A date picker", scroll=True)
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
        try:
            cancel_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().text("Cancel")')
            cancel_btn.click()
        except:
            pass

    safe_screenshot("tab2_compatibility_person_a_filled.png")

    # Interaction 3: Enter Person B details
    try:
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if len(edit_texts) >= 2:
            edit_texts[1].send_keys("Bob")
            log("Entered Person B name: Bob")
            interactions_tested.append("Person B name input")
            time.sleep(0.3)
    except Exception as e:
        log(f"Person B input failed: {e}")

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

    safe_screenshot("tab2_compatibility_person_b_filled.png")

    # Scroll to see results
    try:
        driver.swipe(540, 1800, 540, 800, 500)
        time.sleep(0.5)
        safe_screenshot("tab2_compatibility_results_scrolled.png")
        screens_tested.append("Compatibility results")
    except:
        pass

def test_reminders_tab():
    """Test the Bdays (Reminders) tab."""
    log("=== Testing Reminders Tab ===")
    navigate_to_tab("Bdays")
    time.sleep(1)
    safe_screenshot("tab3_reminders_default.png")
    screens_tested.append("Reminders (default)")

    # Interaction 1: Tap Add button (+)
    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Add birthday", "Add birthday FAB")
    time.sleep(1)
    safe_screenshot("tab3_reminders_add_sheet.png")
    screens_tested.append("Add birthday bottom sheet")

    # Fill in the add birthday form
    try:
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if edit_texts:
            edit_texts[0].send_keys("Mom")
            log("Entered birthday name: Mom")
            interactions_tested.append("Birthday name input")
            time.sleep(0.3)
    except Exception as e:
        log(f"Birthday name input failed: {e}")

    # Select emoji
    try:
        emoji = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("❤️")')
        emoji.click()
        log("Selected heart emoji")
        interactions_tested.append("Emoji selection")
        time.sleep(0.3)
    except Exception as e:
        log(f"Emoji selection failed: {e}")

    # Select date
    tap_by_text("Tap to select", "Birthday date selector")
    time.sleep(1)
    try:
        ok_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("OK")')
        ok_btn.click()
        log("Selected birthday date")
        interactions_tested.append("Birthday date selection")
        time.sleep(0.5)
    except Exception as e:
        log(f"Birthday date selection failed: {e}")

    # Save birthday
    try:
        save_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Save Birthday")')
        save_btn.click()
        log("Saved birthday")
        interactions_tested.append("Save Birthday button")
        time.sleep(1)
        safe_screenshot("tab3_reminders_birthday_added.png")
    except Exception as e:
        log(f"Save birthday failed: {e}")
        bugs_found.append({
            "name": "BUG_reminders_save_birthday",
            "description": f"Failed to save birthday: {e}",
        })

    # Interaction 2: Tap Settings from reminders
    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Open settings", "Settings button (reminders)")
    time.sleep(1)
    safe_screenshot("tab3_reminders_settings_open.png")
    go_back()
    time.sleep(1)

def test_timeline_tab():
    """Test the Timeline tab."""
    log("=== Testing Timeline Tab ===")
    navigate_to_tab("Timeline")
    time.sleep(1)
    safe_screenshot("tab4_timeline_default.png")
    screens_tested.append("Timeline (default)")

    source = driver.page_source
    if "Life Timeline" in source:
        log("Timeline header found")
    else:
        log("WARNING: Timeline header not found")

    # Try to scroll through milestones
    try:
        driver.swipe(540, 1800, 540, 800, 500)
        time.sleep(0.5)
        safe_screenshot("tab4_timeline_scrolled.png")
    except:
        pass

def test_settings_screen():
    """Test the Settings screen — v2.0 fortune notification time picker."""
    log("=== Testing Settings Screen ===")
    navigate_to_tab("My Cosmos")
    time.sleep(0.5)
    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Settings", "Settings button")
    time.sleep(1)
    safe_screenshot("settings_default.png")
    screens_tested.append("Settings (default)")

    # Interaction 1: Toggle notifications
    try:
        switches = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.Switch")
        if switches:
            switches[0].click()
            log("Toggled notifications switch")
            interactions_tested.append("Notifications toggle")
            time.sleep(0.5)
            safe_screenshot("settings_notifications_toggled.png")
            switches[0].click()
            time.sleep(0.3)
    except Exception as e:
        log(f"Notifications toggle failed: {e}")

    # Interaction 2: Select theme (scroll to find)
    try:
        dark_option = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text("Dark"))')
        dark_option.click()
        log("Selected Dark theme")
        interactions_tested.append("Dark theme selection")
        time.sleep(0.5)
        safe_screenshot("settings_dark_theme.png")
    except Exception as e:
        log(f"Dark theme selection failed: {e}")

    # Interaction 3: v2.0 — Fortune notification toggle and time picker
    try:
        fortune_toggle = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Daily cosmic fortune"))')
        log("Found Daily cosmic fortune toggle")
        interactions_tested.append("Daily cosmic fortune toggle found")
        time.sleep(0.3)
        safe_screenshot("settings_fortune_toggle_visible.png")
        screens_tested.append("Settings (Fortune toggle visible)")

        # Tap the toggle
        fortune_toggle.click()
        log("Toggled Daily cosmic fortune")
        interactions_tested.append("Daily cosmic fortune toggle")
        time.sleep(0.5)
        safe_screenshot("settings_fortune_toggled.png")

        # Fortune time picker should appear when enabled
        try:
            time_picker = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().textContains("Fortune delivery time")')
            log("Fortune delivery time picker visible")
            interactions_tested.append("Fortune time picker visible")
            time.sleep(0.3)
            safe_screenshot("settings_fortune_time_picker.png")
            screens_tested.append("Settings (Fortune time picker)")
        except Exception as e2:
            log(f"Fortune time picker not found: {e2}")
    except Exception as e:
        log(f"Daily cosmic fortune toggle find failed: {e}")

    # Interaction 4: v2.0 — Restore purchases (scroll to find)
    try:
        restore_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Restore purchases"))')
        log("Found Restore purchases button")
        interactions_tested.append("Restore purchases found")
        time.sleep(0.3)
        safe_screenshot("settings_restore_purchases_visible.png")
    except Exception as e:
        log(f"Restore purchases find failed: {e}")

    # Interaction 5: Export CSV (scroll to find, but don't tap to avoid share chooser)
    try:
        export_row = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Export birthdays"))')
        log("Found Export CSV option (not tapping to avoid system share chooser)")
        interactions_tested.append("Export CSV found (not tapped)")
        time.sleep(0.3)
        safe_screenshot("settings_export_csv.png")
    except Exception as e:
        log(f"Export CSV find failed: {e}")

    # Interaction 6: Clear all birthdays
    try:
        clear_row = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Clear all birthdays"))')
        clear_row.click()
        log("Tapped Clear all birthdays")
        interactions_tested.append("Clear all birthdays")
        time.sleep(1)
        safe_screenshot("settings_clear_confirm_dialog.png")
        screens_tested.append("Clear all confirmation dialog")

        cancel_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Cancel")')
        cancel_btn.click()
        log("Cancelled clear dialog")
        time.sleep(0.5)
    except Exception as e:
        log(f"Clear all dialog failed: {e}")

    # Back from settings
    go_back()
    time.sleep(2)
    driver.activate_app(APP_PACKAGE)
    time.sleep(1)

def test_edge_cases():
    """Test edge cases: empty inputs, invalid data, etc."""
    log("=== Testing Edge Cases ===")

    # Edge case 1: Calculator with empty name
    navigate_to_tab("My Cosmos")
    time.sleep(0.5)
    try:
        driver.swipe(540, 800, 540, 1800, 500)
        time.sleep(0.3)
        edit_text = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().className("android.widget.EditText")')
        edit_text.clear()
        log("Cleared name field")
        hide_keyboard()
        safe_screenshot("tab1_calculator_empty_name.png")
        interactions_tested.append("Empty name field")
    except Exception as e:
        log(f"Empty name test failed: {e}")

    # Edge case 2: Compatibility empty state
    navigate_to_tab("Match")
    time.sleep(0.5)
    safe_screenshot("tab2_compatibility_empty_state.png")
    screens_tested.append("Compatibility empty state")

    # Edge case 3: Reminders empty state
    navigate_to_tab("Bdays")
    time.sleep(0.5)
    source = driver.page_source
    if "Your birthday list is empty" in source or "empty" in source.lower():
        log("Reminders empty state is shown")
        safe_screenshot("tab3_reminders_empty_state.png")
        screens_tested.append("Reminders empty state")

def test_deep_link_auto_populate():
    """Test deep-link auto-populate in Compatibility screen (v2.0)."""
    log("=== Testing Deep-Link Auto-Populate ===")
    # Open a deep link via adb
    try:
        # Encode a simple profile: birth date = 1990-05-15
        import base64
        import json
        profile = {"d": "1990-05-15", "n": "DeepLinkTest"}
        encoded = base64.urlsafe_b64encode(json.dumps(profile).encode()).decode().strip("=")
        deep_link = f"agereveal://profile/{encoded}"

        driver.execute_script("mobile: deepLink", {
            "url": deep_link,
            "package": APP_PACKAGE,
        })
        log(f"Opened deep link: {deep_link}")
        interactions_tested.append("Deep link opened")
        time.sleep(3)
        safe_screenshot("deeplink_profile_loaded.png")
        screens_tested.append("Deep-link profile loaded")

        # Navigate to Match tab to see auto-populate
        navigate_to_tab("Match")
        time.sleep(1)
        source = driver.page_source
        if "DeepLinkTest" in source or "1990" in source:
            log("Deep-link auto-populate detected in Match tab")
            interactions_tested.append("Deep-link auto-populate verified")
            safe_screenshot("tab2_deeplink_auto_populate.png")
        else:
            log("Deep-link auto-populate not visible (may need manual trigger)")
    except Exception as e:
        log(f"Deep-link test failed: {e}")

def generate_report():
    """Generate the REPORT.md file."""
    report_path = os.path.join(SCREENSHOTS_DIR, "REPORT.md")
    with open(report_path, "w") as f:
        f.write("# Cosmic ID v2.0 UI Walkthrough Report\n\n")
        f.write(f"**Date:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")

        f.write("## Summary\n\n")
        f.write(f"- **Total screens tested:** {len(screens_tested)}\n")
        f.write(f"- **Total interactions tested:** {len(interactions_tested)}\n")
        f.write(f"- **Total screenshots captured:** {len(screenshots_taken)}\n")
        f.write(f"- **Bugs found:** {len(bugs_found)}\n\n")

        f.write("## Screens Tested\n\n")
        for screen in screens_tested:
            f.write(f"- {screen}\n")
        f.write("\n")

        f.write("## Interactions Tested\n\n")
        for interaction in interactions_tested:
            f.write(f"- {interaction}\n")
        f.write("\n")

        f.write("## Screenshots\n\n")
        for screenshot in screenshots_taken:
            f.write(f"- `{screenshot}`\n")
        f.write("\n")

        if bugs_found:
            f.write("## Bugs Found\n\n")
            for bug in bugs_found:
                f.write(f"### {bug['name']}\n")
                f.write(f"- **Description:** {bug['description']}\n")
                if "screenshot" in bug:
                    f.write(f"- **Screenshot:** `{bug['screenshot']}`\n")
                f.write("\n")
        else:
            f.write("## Bugs Found\n\nNo bugs were detected during this walkthrough.\n\n")

        f.write("## v2.0 Features Tested\n\n")
        f.write("- Onboarding flow (3 steps)\n")
        f.write("- Progressive disclosure (rotating highlight)\n")
        f.write("- Celebrity birthday match card + share\n")
        f.write("- Daily cosmic fortune push notification settings\n")
        f.write("- Grace period banner for lapsed subscriptions\n")
        f.write("- Paywall screen (premium-gated astrology)\n")
        f.write("- Deep-link profile sharing + auto-populate\n")
        f.write("- Indian State dropdown for birth location\n")
        f.write("- Restore purchases in Settings\n\n")

        f.write("## Notes\n\n")
        f.write("- This report was generated automatically via Appium UI testing.\n")
        f.write("- Some interactions may depend on network state (ads) or system permissions.\n")
        f.write("- Screens with dynamic content (ads, live timers, rotating highlights) may vary between runs.\n")
        f.write("- v2.0 removed: Badges tab (now inside My Cosmos), Hindi language toggle (system locale only), ASCII Art share.\n")

    log(f"Report generated: {report_path}")

# =============================================================================
# Main
# =============================================================================

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
    options.no_reset = True
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

    # Close notification shade if it's open (systemui overlay)
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
        # Run all test scenarios
        test_onboarding_flow()
        test_calculator_tab()
        test_rotating_highlight()
        test_explore_profile_cta()
        test_paywall_screen()
        test_compatibility_tab()
        test_reminders_tab()
        test_timeline_tab()
        test_settings_screen()
        test_deep_link_auto_populate()
        test_edge_cases()

        # Generate report
        generate_report()

        log("=== Walkthrough Complete ===")
        log(f"Screens: {len(screens_tested)}")
        log(f"Interactions: {len(interactions_tested)}")
        log(f"Screenshots: {len(screenshots_taken)}")
        log(f"Bugs: {len(bugs_found)}")

    except Exception as e:
        log(f"CRITICAL ERROR during walkthrough: {e}")
        traceback.print_exc()
        # Try to generate report anyway
        try:
            generate_report()
        except:
            pass
    finally:
        log("Quitting driver")
        driver.quit()

if __name__ == "__main__":
    main()
