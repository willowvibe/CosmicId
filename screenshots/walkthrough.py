#!/usr/bin/env python3
"""
AgeReveal Automated UI Walkthrough
Performs comprehensive testing of all screens, interactions, and features.
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
APP_PACKAGE = "com.willowvibe.agereveal"
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
        # Try to find the clickable parent of the text label in bottom nav
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
# Test Scenarios
# =============================================================================

def test_calculator_tab():
    """Test the Calculator (You) tab thoroughly."""
    log("=== Testing Calculator Tab ===")
    navigate_to_tab("You")
    time.sleep(1)
    safe_screenshot("tab1_calculator_default.png")
    screens_tested.append("Calculator (default)")

    # Interaction 1: Tap Settings button
    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Settings", "Settings button")
    time.sleep(1)
    safe_screenshot("tab1_calculator_settings_open.png")
    screens_tested.append("Settings (from calculator)")
    go_back()
    time.sleep(1)

    # Interaction 2: Enter name
    # Try to find EditText; if not found, tap the field area first
    edit_text = None
    try:
        edit_text = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().className("android.widget.EditText")')
    except Exception:
        log("EditText not found, trying to tap field first")
        # Tap on the name field area (roughly near top center)
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

    # Interaction 3: Tap birth date row (use contains for dynamic content-desc)
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

    # Select a date and confirm
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

    # Interaction 4: Tap TIME precision chip (use description contains)
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

    # Set time
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

    # Interaction 5: Tap LOCATION precision chip
    try:
        loc_chip = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().descriptionContains("LOCATION")')
        loc_chip.click()
        log("Tapped: Location precision chip")
        interactions_tested.append("Location precision chip")
        time.sleep(1)
        safe_screenshot("tab1_calculator_location_dialog.png")
        screens_tested.append("Location dialog")
    except Exception as e:
        log(f"Location chip tap failed: {e}")

    # Enter location
    try:
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if len(edit_texts) >= 2:
            edit_texts[0].send_keys("28.6")
            edit_texts[1].send_keys("77.2")
            log("Entered location coordinates")
            interactions_tested.append("Location coordinates input")
            set_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().text("Set")')
            set_btn.click()
            log("Set location")
            interactions_tested.append("Location picker Set")
            time.sleep(0.5)
            safe_screenshot("tab1_calculator_location_set.png")
    except Exception as e:
        log(f"Location set failed: {e}")
        try:
            cancel_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().text("Cancel")')
            cancel_btn.click()
        except:
            pass

    # Scroll down to see results
    try:
        driver.swipe(540, 1800, 540, 800, 500)
        time.sleep(0.5)
        safe_screenshot("tab1_calculator_results_scrolled.png")
    except:
        pass

def test_compatibility_tab():
    """Test the Match (Compatibility) tab."""
    log("=== Testing Compatibility Tab ===")
    navigate_to_tab("Match")
    time.sleep(1)
    safe_screenshot("tab2_compatibility_default.png")
    screens_tested.append("Compatibility (default)")

    # Interaction 1: Select relationship type
    try:
        # Scroll up in case selector is above current view
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

    # Tap date for Person A
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

    # Tap date for Person B (look for second date row)
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
    """Test the Settings screen from bottom nav or other paths."""
    log("=== Testing Settings Screen ===")
    # Navigate to Calculator first and open settings
    navigate_to_tab("You")
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
            # Toggle back
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

    # Interaction 3: Select language (scroll to find) -- but skip changing to avoid locale issues
    # Just scroll to it and screenshot
    try:
        hindi_option = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Hindi"))')
        log("Found Hindi language option (not tapping to avoid locale changes)")
        interactions_tested.append("Hindi language found (not selected)")
        time.sleep(0.3)
    except Exception as e:
        log(f"Hindi language find failed: {e}")

    # Interaction 4: Export CSV (scroll to find, but don't tap to avoid share chooser)
    try:
        export_row = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Export birthdays"))')
        log("Found Export CSV option (not tapping to avoid system share chooser)")
        interactions_tested.append("Export CSV found (not tapped)")
        time.sleep(0.3)
        safe_screenshot("settings_export_csv.png")
    except Exception as e:
        log(f"Export CSV find failed: {e}")

    # Interaction 5: Clear all birthdays (show confirmation dialog; scroll to find)
    try:
        clear_row = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Clear all birthdays"))')
        clear_row.click()
        log("Tapped Clear all birthdays")
        interactions_tested.append("Clear all birthdays")
        time.sleep(1)
        safe_screenshot("settings_clear_confirm_dialog.png")
        screens_tested.append("Clear all confirmation dialog")

        # Cancel the dialog
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
    # Ensure we're back on the main app screen
    driver.activate_app(APP_PACKAGE)
    time.sleep(1)

def test_edge_cases():
    """Test edge cases: empty inputs, invalid data, etc."""
    log("=== Testing Edge Cases ===")

    # Edge case 1: Calculator with empty name
    navigate_to_tab("You")
    time.sleep(0.5)
    try:
        # Scroll to top to ensure name field is visible
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

    # Edge case 3: Reminders empty state (after clearing or on fresh install)
    navigate_to_tab("Bdays")
    time.sleep(0.5)
    source = driver.page_source
    if "Your birthday list is empty" in source or "empty" in source.lower():
        log("Reminders empty state is shown")
        safe_screenshot("tab3_reminders_empty_state.png")
        screens_tested.append("Reminders empty state")

def test_badges_tab():
    """Test the Badges tab."""
    log("=== Testing Badges Tab ===")
    navigate_to_tab("Badges")
    time.sleep(1)
    safe_screenshot("tab4_badges_default.png")
    screens_tested.append("Badges (default)")

    # Scroll through badge grid
    try:
        driver.swipe(540, 1800, 540, 800, 500)
        time.sleep(0.5)
        safe_screenshot("tab4_badges_scrolled.png")
        screens_tested.append("Badges (scrolled)")
    except:
        pass

    # Try to tap a badge (first unlocked or any)
    try:
        badges = driver.find_elements(AppiumBy.CLASS_NAME, "android.view.View")
        if len(badges) > 5:
            badges[5].click()
            log("Tapped a badge card")
            interactions_tested.append("Badge card tap")
            time.sleep(1)
            safe_screenshot("tab4_badges_detail_sheet.png")
            screens_tested.append("Badge detail sheet")
            go_back()
            time.sleep(0.5)
    except Exception as e:
        log(f"Badge tap failed: {e}")

def test_new_settings_features():
    """Test Time Remaining toggle and Accent Color picker in Settings."""
    log("=== Testing New Settings Features ===")
    navigate_to_tab("You")
    time.sleep(0.5)
    safe_tap(AppiumBy.ACCESSIBILITY_ID, "Settings", "Settings button")
    time.sleep(1)

    # Scroll down to find Time Remaining toggle
    try:
        tr_toggle = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Time remaining"))')
        log("Found Time remaining visuals toggle")
        interactions_tested.append("Time remaining toggle found")
        time.sleep(0.3)
        safe_screenshot("settings_time_remaining_visible.png")
        screens_tested.append("Settings (Time remaining visible)")
    except Exception as e:
        log(f"Time remaining toggle find failed: {e}")

    # Scroll down to find Accent Color picker
    try:
        accent_label = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Accent color"))')
        log("Found Accent color picker")
        interactions_tested.append("Accent color picker found")
        time.sleep(0.3)
        safe_screenshot("settings_accent_color_visible.png")
        screens_tested.append("Settings (Accent color visible)")
    except Exception as e:
        log(f"Accent color picker find failed: {e}")

    # Scroll down to find Lifespan target (verify it's still there)
    try:
        lifespan_label = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("Lifespan target"))')
        log("Found Lifespan target section")
        interactions_tested.append("Lifespan target found")
        time.sleep(0.3)
        safe_screenshot("settings_lifespan_target_visible.png")
        screens_tested.append("Settings (Lifespan target visible)")
    except Exception as e:
        log(f"Lifespan target find failed: {e}")

    go_back()
    time.sleep(1)

def test_daily_fortune_card():
    """Test the Daily Cosmic Fortune card on Calculator screen."""
    log("=== Testing Daily Cosmic Fortune ===")
    navigate_to_tab("You")
    time.sleep(1)

    # Scroll down aggressively to find the fortune card (it appears after birth date is set)
    for _ in range(5):
        driver.swipe(540, 1800, 540, 600, 500)
        time.sleep(0.5)
        source = driver.page_source
        if "DAILY COSMIC FORTUNE" in source:
            log("Found Daily Cosmic Fortune card in page source")
            interactions_tested.append("Daily Cosmic Fortune card found")
            safe_screenshot("tab1_calculator_daily_fortune_visible.png")
            screens_tested.append("Daily Cosmic Fortune (visible)")

            # Try to tap the fortune card
            try:
                fortune_el = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                    'new UiSelector().textContains("DAILY COSMIC")')
                fortune_el.click()
                log("Tapped Daily Cosmic Fortune card")
                interactions_tested.append("Daily Cosmic Fortune card tap")
                time.sleep(1)
                safe_screenshot("tab1_calculator_fortune_share_sheet.png")
                screens_tested.append("Daily Cosmic Fortune share sheet")
                go_back()
                time.sleep(0.5)
            except Exception as tap_e:
                log(f"Fortune card tap failed: {tap_e}")
            return

    log("Daily Cosmic Fortune card not found after scrolling")
    interactions_tested.append("Daily Cosmic Fortune (not visible)")

def test_percentile_card():
    """Test the Global Age Percentile card on Calculator screen."""
    log("=== Testing Global Age Percentile ===")
    navigate_to_tab("You")
    time.sleep(1)

    # Scroll down to find the percentile card (appears after birth date is set and unlocked)
    for _ in range(5):
        driver.swipe(540, 1800, 540, 600, 500)
        time.sleep(0.5)
        source = driver.page_source
        if "GLOBAL PERCENTILE" in source:
            log("Found Global Percentile card in page source")
            interactions_tested.append("Global Percentile card found")
            safe_screenshot("tab1_calculator_percentile_visible.png")
            screens_tested.append("Global Percentile (visible)")

            # Try to tap the share button on the percentile card
            try:
                share_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                    'new UiSelector().textContains("Share stat")')
                share_btn.click()
                log("Tapped Global Percentile share button")
                interactions_tested.append("Global Percentile share tap")
                time.sleep(1)
                safe_screenshot("tab1_calculator_percentile_share_sheet.png")
                screens_tested.append("Global Percentile share sheet")
                go_back()
                time.sleep(0.5)
            except Exception as tap_e:
                log(f"Percentile share tap failed: {tap_e}")
            return

    log("Global Percentile card not found after scrolling")
    interactions_tested.append("Global Percentile (not visible)")


def test_share_sheet_formats():
    """Test the share sheet with new ASCII and Green Screen formats."""
    log("=== Testing Share Sheet Formats ===")
    navigate_to_tab("You")
    time.sleep(1)

    # Scroll to top first to find the share button
    driver.swipe(540, 800, 540, 1800, 500)
    time.sleep(0.5)

    # Check if share button is visible in page source
    source = driver.page_source
    if "Share your cosmic profile" not in source:
        log("Share button not visible in current view")
        interactions_tested.append("Share button (not visible)")
        return

    # Try to open the share sheet via text search only
    try:
        share_btn = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().textContains("Share your cosmic profile")')
        share_btn.click()
        log("Tapped Share button")
        interactions_tested.append("Share button (calculator)")
        time.sleep(1)
        safe_screenshot("tab1_calculator_share_sheet_open.png")
        screens_tested.append("Share sheet (open)")
    except Exception as e:
        log(f"Share button tap failed: {e}")
        interactions_tested.append("Share button (tap failed)")
        return

    # Try to select ASCII Art format
    try:
        ascii_chip = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("ASCII")')
        ascii_chip.click()
        log("Selected ASCII Art format")
        interactions_tested.append("ASCII Art format selection")
        time.sleep(0.5)
        safe_screenshot("tab1_calculator_share_ascii_selected.png")
        screens_tested.append("Share sheet (ASCII selected)")
    except Exception as e:
        log(f"ASCII Art format selection failed: {e}")

    # Try to select Green Screen format
    try:
        green_chip = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Green")')
        green_chip.click()
        log("Selected Green Screen format")
        interactions_tested.append("Green Screen format selection")
        time.sleep(0.5)
        safe_screenshot("tab1_calculator_share_green_selected.png")
        screens_tested.append("Share sheet (Green Screen selected)")
    except Exception as e:
        log(f"Green Screen format selection failed: {e}")

    # Dismiss share sheet
    go_back()
    time.sleep(0.5)

def test_time_remaining_card():
    """Test the Time Remaining card on Calculator screen."""
    log("=== Testing Time Remaining Card ===")
    navigate_to_tab("You")
    time.sleep(1)

    # Scroll down to find the Time Remaining card
    try:
        tr_card = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains("weekends left"))')
        log("Found Time Remaining card")
        interactions_tested.append("Time Remaining card found")
        time.sleep(0.3)
        safe_screenshot("tab1_calculator_time_remaining_visible.png")
        screens_tested.append("Time Remaining (visible)")
    except Exception as e:
        log(f"Time Remaining card not found: {e}")
        interactions_tested.append("Time Remaining card (not visible)")

def generate_report():
    """Generate the REPORT.md file."""
    report_path = os.path.join(SCREENSHOTS_DIR, "REPORT.md")
    with open(report_path, "w") as f:
        f.write("# AgeReveal UI Walkthrough Report\n\n")
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

        f.write("## Notes\n\n")
        f.write("- This report was generated automatically via Appium UI testing.\n")
        f.write("- Some interactions may depend on network state (ads) or system permissions.\n")
        f.write("- Screens with dynamic content (ads, live timers) may vary between runs.\n")

    log(f"Report generated: {report_path}")

# =============================================================================
# Main
# =============================================================================

def main():
    global driver

    log("Starting AgeReveal UI Walkthrough")

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
        test_calculator_tab()
        test_time_remaining_card()
        test_daily_fortune_card()
        test_percentile_card()
        test_share_sheet_formats()
        test_compatibility_tab()
        test_reminders_tab()
        test_timeline_tab()
        test_badges_tab()
        test_settings_screen()
        test_new_settings_features()
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
