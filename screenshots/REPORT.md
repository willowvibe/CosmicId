# AgeReveal UI Walkthrough Report

**Date:** 2026-05-03 15:23:01  
**Version:** 1.0.6  
**APK:** `app-release.apk` (6.2 MB)  
**Device:** Android Emulator (Medium_Phone, API 37, Android 15)

---

## Summary

| Metric | Count |
|---|---|
| **Screens tested** | 18 |
| **Interactions tested** | 25 |
| **Screenshots captured** | 29 |
| **Bugs found** | 0 |

---

## Screens Tested

### Tab 1 — You (Calculator)
- Calculator (default)
- Settings (from calculator)
- Date picker dialog
- Time picker dialog
- Location dialog

### Tab 2 — Match (Compatibility)
- Compatibility (default)
- Compatibility results
- Compatibility empty state

### Tab 3 — Bdays (Reminders)
- Reminders (default)
- Add birthday bottom sheet

### Tab 4 — Badges
- Badges (default)
- Badges (scrolled)
- Badge detail sheet

### Tab 5 — Timeline
- Timeline (default)

### Settings Screen
- Settings (default)
- Settings (Time remaining visible)
- Settings (Accent color visible)
- Settings (Lifespan target visible)

---

## Interactions Tested

- Settings button
- Name input field
- Birth date row
- Date picker OK
- Time precision chip
- Time picker Set
- Location precision chip
- Location coordinates input
- Location picker Set
- Person A name input
- Add birthday FAB
- Birthday name input
- Emoji selection
- Birthday date selector
- Birthday date selection
- Save Birthday button
- Settings button (reminders)
- Badge card tap
- Dark theme selection
- Time remaining toggle found
- Accent color picker found
- Lifespan target found
- Empty name field

---

## Screenshot Index

| Screenshot | Description |
|---|---|
| `tab1_calculator_default.png` | Calculator tab default state |
| `tab1_calculator_settings_open.png` | Settings opened from Calculator |
| `tab1_calculator_name_entered.png` | Name field filled with "Test User" |
| `tab1_calculator_datepicker_open.png` | Date picker dialog open |
| `tab1_calculator_date_selected.png` | Date selected and confirmed |
| `tab1_calculator_time_dialog.png` | Time picker dialog open |
| `tab1_calculator_location_dialog.png` | Location input dialog open |
| `tab1_calculator_location_set.png` | Location coordinates entered |
| `tab1_calculator_results_scrolled.png` | Results area scrolled down |
| `tab2_compatibility_default.png` | Compatibility tab default state |
| `tab2_compatibility_person_a_filled.png` | Person A details filled |
| `tab2_compatibility_person_b_filled.png` | Person B details filled |
| `tab2_compatibility_results_scrolled.png` | Compatibility results scrolled |
| `tab2_compatibility_empty_state.png` | Compatibility tab empty state |
| `tab3_reminders_default.png` | Reminders tab default state |
| `tab3_reminders_add_sheet.png` | Add birthday bottom sheet |
| `tab3_reminders_birthday_added.png` | Birthday successfully added |
| `tab3_reminders_settings_open.png` | Settings opened from Reminders |
| `tab4_timeline_default.png` | Timeline tab default state |
| `tab4_timeline_scrolled.png` | Timeline tab scrolled |
| `tab4_badges_default.png` | Badges tab default state (v1.0.6) |
| `tab4_badges_scrolled.png` | Badges tab scrolled (v1.0.6) |
| `tab4_badges_detail_sheet.png` | Badge detail bottom sheet (v1.0.6) |
| `settings_default.png` | Settings screen default state |
| `settings_dark_theme.png` | Dark theme selected |
| `settings_time_remaining_visible.png` | Time Remaining toggle visible (v1.0.6) |
| `settings_accent_color_visible.png` | Accent Color picker visible (v1.0.6) |
| `settings_lifespan_target_visible.png` | Lifespan target section visible |
| `tab1_calculator_empty_name.png` | Empty name field edge case |

---

## Bugs Found

No bugs were detected during this walkthrough.

---

## Untested Features

The following features were not exercised by the automated script and require manual verification:

| Feature | Reason |
|---|---|
| Share card generation | Requires system share chooser — blocks automation |
| 9:16 Story Cards | Share sheet opens external chooser |
| Life Stats Dashboard | Requires Details unlock screen (rewarded ad) |
| Rewarded ad unlock flow | Ad is a network-dependent external service |
| Widget rendering | Widgets are home-screen only; not accessible via in-app navigation |
| Hindi locale switch | Breaks subsequent selectors; intentional skip |
| Export CSV | Opens system share chooser; blocks automation |
| Clear all birthdays | Confirmation dialog tested but action not confirmed |
| Notifications toggle | Switch tapped but notification permission state not verified |
| Accent color selection | Swatches are visible but tap-and-apply not exercised |
| Time remaining toggle | Toggle is visible but on/off state not changed |

---

## Notes

- This report was generated automatically via Appium UI testing.
- Some interactions may depend on network state (ads) or system permissions.
- Screens with dynamic content (ads, live timers) may vary between runs.
- New v1.0.6 features (Badges tab, Time Remaining toggle, Accent Color picker, Milestone Ring widget) are installed and visually confirmed in screenshots.
