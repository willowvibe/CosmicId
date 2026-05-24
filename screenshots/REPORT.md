# Cosmic ID v2.0 UI Walkthrough Report

**Date:** 2026-05-24 23:14:43

## Summary

- **Total screens tested:** 14
- **Total interactions tested:** 16
- **Total screenshots captured:** 14
- **Difficult-to-locate elements:** 3
- **Bugs found:** 0

## Screens Tested

- Onboarding Step 1 (Permission)
- Onboarding Step 0 (Birth Date)
- Onboarding Step 2 (Accent Picker)
- My Cosmos (default)
- My Cosmos (fortune highlight)
- Astrology Details (DetailsUnlockScreen)
- Match (empty)
- Match (results)
- Bdays (default)
- Bdays (add sheet)
- Timeline (scrolled)
- Settings (unified view)
- Settings (theme picker)
- Paywall (modal)

## Screenshots

- `onboarding_01_permission.png`
- `onboarding_02_birthdate.png`
- `onboarding_03_time_location.png`
- `tab1_mycosmos_default.png`
- `tab1_mycosmos_highlight_fortune.png`
- `tab1_details_astrology.png`
- `tab2_match_empty.png`
- `tab2_match_results.png`
- `tab3_bdays_default.png`
- `tab3_bdays_add_sheet.png`
- `tab4_timeline_scrolled.png`
- `settings_unified_view.png`
- `settings_theme_picker.png`
- `paywall_modal.png`

## Difficult-to-Locate Elements

- **Match Calculate button**
  - Selector: `AppiumBy.ANDROID_UIAUTOMATOR` = `textContains("Calculate")`
  - Error: Message: An element could not be located on the page using the given search parameters.; For documentation on this error, please visit: https://www.selenium.dev/documentation/webdriver/troubleshooting/errors#nosuchelementexception
Stacktrace:
NoSuchElementError: An element could not be located on the page using the given search parameters.
    at AndroidUiautomator2Driver.findElOrEls (/home/harish/.appium/node_modules/appium-uiautomator2-driver/node_modules/appium-android-driver/build/lib/commands/find.js:62:15)
    at process.processTicksAndRejections (node:internal/process/task_queues:105:5)
    at async AndroidUiautomator2Driver.findElOrElsWithProcessing (/home/harish/.nvm/versions/node/v22.22.0/lib/node_modules/appium/node_modules/@appium/base-driver/build/lib/basedriver/commands/find.js:12:16)
    at async AndroidUiautomator2Driver.findElement (/home/harish/.nvm/versions/node/v22.22.0/lib/node_modules/appium/node_modules/@appium/base-driver/build/lib/basedriver/commands/find.js:27:16)
- **Settings button**
  - Selector: `accessibility id` = `Settings`
  - Error: Message: 
Stacktrace:
NoSuchElementError: An element could not be located on the page using the given search parameters.
    at AndroidUiautomator2Driver.findElOrEls (/home/harish/.appium/node_modules/appium-uiautomator2-driver/node_modules/appium-android-driver/build/lib/commands/find.js:62:15)
    at process.processTicksAndRejections (node:internal/process/task_queues:105:5)
    at async AndroidUiautomator2Driver.findElOrElsWithProcessing (/home/harish/.nvm/versions/node/v22.22.0/lib/node_modules/appium/node_modules/@appium/base-driver/build/lib/basedriver/commands/find.js:12:16)
    at async AndroidUiautomator2Driver.findElement (/home/harish/.nvm/versions/node/v22.22.0/lib/node_modules/appium/node_modules/@appium/base-driver/build/lib/basedriver/commands/find.js:27:16)
- **Paywall locked theme row**
  - Selector: `AppiumBy.XPATH` = `ancestor::android.view.View[@clickable='true']`
  - Error: No locked theme row found in settings

## Bugs Found

No bugs were detected during this walkthrough.

## Notes

- This report was generated automatically via Appium UI testing.
- Bottom nav `TextView` labels are not clickable — parent `View` is tapped via XPath ancestor.
- Icon-only buttons in Compose use `contentDescription` and are tapped via `ACCESSIBILITY_ID`.
- Staggered entrance animations on My Cosmos require a 2-second wait before interaction.
- Rotating highlight card cycles every 4 seconds — fortune card captured after 4s wait.
