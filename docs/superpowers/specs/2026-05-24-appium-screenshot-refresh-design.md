# Appium Screenshot Refresh — Design Spec

**Date:** 2026-05-24
**Topic:** Update Appium E2E suite for Cosmic ID v2.0, build debug APK, capture categorized screenshots
**Author:** Claude Code

---

## 1. Problem Statement

The `screenshots/` directory contains ~40 stale screenshots from the pre-v2.0 UI. The existing Appium script (`screenshots/walkthrough.py`) fails with `NoSuchElementError` because the app migrated to Jetpack Compose Material 3, changing the accessibility tree and navigation structure significantly.

## 2. Goals

1. Rewrite `walkthrough.py` with v2.0-aware selectors.
2. Build a fresh debug APK.
3. Execute the script on an emulator, capturing exactly 13 categorized screenshots.
4. Delete all old/invalid screenshots and generate a summary report.

## 3. Non-Goals

- No changes to app source code (script-only work).
- No release APK or Play Store upload.
- No modification of the v2.0 UI itself.

## 4. v2.0 UI Changes Affecting Selectors

| v1.x Element | v2.0 Replacement | Selector Strategy |
|---|---|---|
| "Badges" bottom-nav tab | Removed; nested inside "My Cosmos" | Tap "Open badges" `contentDescription` from My Cosmos header |
| Split Settings screens | Single consolidated Settings screen | `ACCESSIBILITY_ID "Settings"` anywhere |
| XML path-based icon buttons | Compose icon-only buttons | `ACCESSIBILITY_ID` (e.g., `"Settings"`, `"Share profile"`, `"Refresh calculations"`) |
| Dropdown date pickers in Match | Material 3 `DatePicker` inside elevated cards | Scroll to card → tap date field → wait for `DatePicker` → tap `OK` |
| Instant layout | Staggered entrance (350 ms) + rotating highlight (4 s cycle) | `WebDriverWait` with ≥2 s settling; 4 s wait for fortune card |

## 5. Screenshot Catalog (Exact File Names)

### Category 1 — Onboarding & Paywall
- `onboarding_01_permission.png` — Notification permission dialog
- `onboarding_02_birthdate.png` — Name and Date input
- `onboarding_03_time_location.png` — Time and Location inputs
- `paywall_modal.png` — Premium subscription bottom sheet

### Category 2 — My Cosmos (Main Tab)
- `tab1_mycosmos_default.png` — Default state with age counter
- `tab1_mycosmos_highlight_fortune.png` — Wait 4 s for fortune card to cycle in
- `tab1_details_astrology.png` — Astrology breakdown overview

### Category 3 — Match (Compatibility)
- `tab2_match_empty.png` — Default empty state with person cards
- `tab2_match_results.png` — After inputting two distinct valid dates

### Category 4 — Reminders & Timeline
- `tab3_bdays_default.png` — Empty state
- `tab3_bdays_add_sheet.png` — Add birthday bottom sheet
- `tab4_timeline_scrolled.png` — Milestone timeline view

### Category 5 — Settings & Customization
- `settings_unified_view.png` — Consolidated settings screen
- `settings_theme_picker.png` — Accent color/theme selection

## 6. Script Architecture

### 6.1 Driver Setup
- **Capabilities:** `UiAutomator2Options`, `no_reset=False` (ensures clean state), `newCommandTimeout=300`.
- **Device:** `emulator-5554` (assumed running; script will fail fast if unreachable).
- **App:** `app/build/outputs/apk/debug/app-debug.apk`.

### 6.2 Test Flow
1. `pm clear com.willowvibe.cosmicid.debug` — force onboarding.
2. Launch app → dismiss notification permission → capture `onboarding_01_permission.png`.
3. Step through onboarding (Name/Date → Time/Location → Accent) → capture `onboarding_02_birthdate.png`, `onboarding_03_time_location.png`.
4. Enter My Cosmos → wait 2 s for staggered animation → capture `tab1_mycosmos_default.png`.
5. Wait 4 s → capture `tab1_mycosmos_highlight_fortune.png`.
6. Tap "Explore full profile" → capture `tab1_details_astrology.png`.
7. Navigate to Match → capture `tab2_match_empty.png`.
8. Fill Person A & B dates → capture `tab2_match_results.png`.
9. Navigate to Bdays → capture `tab3_bdays_default.png`.
10. Tap "Add birthday" → capture `tab3_bdays_add_sheet.png`.
11. Navigate to Timeline → scroll → capture `tab4_timeline_scrolled.png`.
12. Open Settings → capture `settings_unified_view.png`.
13. Scroll to theme picker → capture `settings_theme_picker.png`.
14. Trigger paywall (tap locked theme) → capture `paywall_modal.png`.
15. Delete all old `.png` files in `screenshots/` that are **not** in the 13-name catalog.
16. Generate `REPORT.md` with execution summary and list of hard-to-locate elements.

### 6.3 Selector Patterns

```python
# Icon-only buttons (Compose)
safe_tap(AppiumBy.ACCESSIBILITY_ID, "Settings", "Settings button")

# Bottom nav (text label non-clickable; ancestor View is)
xpath = f'//android.widget.TextView[@text="{tab_label}"]/ancestor::android.view.View[@clickable="true"]'

# DatePicker inside card
tap_by_text("Tap to set birthday")
wait_for_element(AppiumBy.ANDROID_UIAUTOMATOR, 'new UiSelector().text("OK")')
```

## 7. Error Handling & Resilience

- `safe_tap`, `safe_input`, `safe_screenshot` wrappers catch exceptions, log, and continue.
- If an element is not found after timeout, log as "difficult to locate" in the report.
- If Appium server is unreachable, exit immediately with clear message.
- If `pm clear` fails, attempt to proceed (app may already be in onboarding state).

## 8. Output Artifacts

| Artifact | Location |
|---|---|
| Updated script | `screenshots/walkthrough.py` |
| New screenshots | `screenshots/<catalog_name>.png` (13 files) |
| Execution report | `screenshots/REPORT.md` |
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |

## 9. Testing Strategy

- Script self-test: validate all 13 screenshot files exist after run.
- File-size check: each PNG > 1 KB (catches blank/corrupt captures).
- Report includes: pass/fail per step, total interactions, elements that required retries.

## 10. Open Questions / Risks

- **Emulator state:** Script assumes `emulator-5554` is online. If not, user must start it before running.
- **Permission dialog timing:** System permission controller varies by API level; script uses both `permission_allow_button` and `permission_deny_button` IDs.
- **Paywall trigger:** Locked theme tap depends on current theme list order; if Vaporwave is already unlocked on this device, fallback to any theme with a lock icon.
