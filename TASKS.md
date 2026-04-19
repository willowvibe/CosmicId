# AgeReveal — Remaining Tasks & Placeholders

_Last updated: 2026-04-19 — Phase 3 features branch updated with completed features_

---

## 1. Placeholders That Must Be Replaced Before Release

### AdMob IDs (currently using Google's safe test values)

All four IDs below generate **no real revenue** and must be swapped for production values obtained from your AdMob account before submitting to the Play Store.

| # | File | Line | Current test value | What to replace with |
|---|------|------|--------------------|----------------------|
| 1 | `app/build.gradle.kts` | 23 | `ca-app-pub-3940256099942544~3347511713` | Your real AdMob **App ID** |
| 2 | `app/src/main/java/com/willowvibe/agereveal/ads/AdManager.kt` | 34 | `ca-app-pub-3940256099942544/6300978111` | Your real **Banner** ad unit ID |
| 3 | `app/src/main/java/com/willowvibe/agereveal/ads/AdManager.kt` | 35 | `ca-app-pub-3940256099942544/5224354917` | Your real **Rewarded** ad unit ID |
| 4 | `app/src/main/java/com/willowvibe/agereveal/ads/AdManager.kt` | 36 | `ca-app-pub-3940256099942544/1033173712` | Your real **Interstitial** ad unit ID |

Steps:
1. Log in to [admob.google.com](https://admob.google.com)
2. Add app → get the App ID (format `ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX`)
3. Create three ad units (Banner, Rewarded, Interstitial) → get each ad unit ID
4. Replace the four values above

---

### Custom Inter Font (currently disabled / falling back to system default)

`app/src/main/java/com/willowvibe/agereveal/ui/theme/Type.kt` lines 20-25 contain the `InterFamily` font family declaration commented out. It will remain the system sans-serif until the font files are added.

**Action:**
1. Download the Inter font from [rsms.me/inter](https://rsms.me/inter) or Google Fonts
2. Place the following files in `app/src/main/res/font/`:
   - `inter_regular.ttf`
   - `inter_medium.ttf`
   - `inter_semibold.ttf`
   - `inter_bold.ttf`
3. Uncomment the `InterFamily` block in `Type.kt` (lines 20-25)
4. Replace `FontFamily.Default` → `InterFamily` in every `TextStyle` in that file

---

## 2. Stale TODO Comments to Clean Up

| File | Line | Comment | Status |
|------|------|---------|--------|
| `app/src/main/java/com/willowvibe/agereveal/notification/BirthdayReminderWorker.kt` | 44 | `// TODO: add ic_cake vector drawable to res/` | ✅ **Resolved** — comment removed. |

---

## 3. Remaining Phase 1 Work (Day 4 Polish & Launch)

These items are tracked in `roadmap.md` under the Day 4 milestone.

- [ ] **Animations** — Add enter/reveal animation when age result appears on the main screen
- [ ] **Date picker UX** — Smooth out the date picker interactions (scroll snap, haptic feedback)
- [ ] **Edge cases:**
  - ✅ Leap year birthdays (Feb 29) — `yearSafeBirthday()` helper added to `AgeCalculator` and `BirthdayRepository`; maps to Mar 1 in non-leap years instead of crashing
  - ✅ **Leap year in notification scheduler** — `BirthdayNotificationScheduler.computeNextBirthday()` was using raw `.withYear()` and `.plusYears(1)`, which throws `DateTimeException` for Feb 29 birthdays in non-leap years. Fixed by extracting the same `yearSafeBirthday()` pattern used elsewhere; also fixed the fallback reschedule path (`scheduleFor` line 61) to use `yearSafeBirthday(birthDate, nextBirthday.year + 1)`.
  - ✅ Future date input — validated in `CalculatorViewModel` (existing), `CompareViewModel` (added), and `RemindersScreen`/`RemindersViewModel` (added); all show clear errors
  - ✅ **Silent date default in Add Birthday sheet** — `RemindersScreen` save button was silently using `LocalDate.now()` when the user had not selected a date (`selectedDate ?: LocalDate.now()`). Fixed to treat a missing date as a validation error (`dateError = true`) instead.
  - ✅ Equal-age comparison — `CompareViewModel` now detects equal dates and shows "Same birthday!" instead of mislabelling Person B as older
  - ✅ Today's date — `Period.between(today, today)` correctly returns 0; no fix needed (confirmed safe)
- [ ] **Play Store listing assets:**
  - App icon (512×512 PNG)
  - Feature graphic (1024×500 PNG)
  - At least 2 phone screenshots
  - Short description (80 chars)
  - Full description
- [ ] **Switch all four AdMob IDs** (see Section 1 above)
- [ ] **Submit app for Play Store review**

---

## 4. Planned Phase 2 Features (Post-Launch)

Completed:
- ✅ **Zodiac Compatibility Screen** — 5th tab with Western + Chinese compatibility score
- ✅ **Notification Time Customization** — Settings gear on Birthdays tab for reminder hour selection

Still pending:
- [ ] **Themed share cards** — Light / festive themes as additional rewarded ad unlocks
- [ ] **Hindi UI toggle** — In-app language switch between English and Hindi
- [ ] **4×2 home screen widget** — Wider widget showing 3 upcoming birthdays
- [ ] **In-app review prompt** — Trigger the Play Store review sheet after a user shares their card
- [ ] **Remove Ads IAP** — One-time ₹99 purchase to disable all ads (needs Play Billing Library integration)

---

## 5. Future Technical Improvements (Nice-to-Have)

- ✅ **Widget already uses Jetpack Glance** — `BirthdayGlanceWidget.kt` and `BirthdayGlanceWidgetReceiver.kt` are fully Glance-based
- [ ] **Enable Inter font** — See Section 1 font placeholder above
- [ ] **Add `Migration` objects before next schema change** — `AppDatabase` currently uses `fallbackToDestructiveMigration()` which silently drops all user data on version bumps. Must add explicit `Migration` objects before shipping any schema change in Phase 2.

---

## 6. Phase 3 Features (In Progress)

### Completed ✅
- **Google Calendar Export** — `CalendarExport.kt` utility for one-tap Intent to add birthdays to Google Calendar
- **Astrology Explanations** — `AstroInfoDialog.kt` with educational dialogs for Western Zodiac, Rashi, Nakshatra, Chinese Zodiac, and Moon Phase

### High Priority (In Progress)
- [ ] **Birth Time Support** — Optional time picker for precise Nakshatra + Rashi calculation
- [ ] **Milestone Push Notifications** — Schedule WorkManager jobs for upcoming day-milestones
- [ ] **Zodiac Compatibility Share Card** — Generate shareable bitmap for compatibility result
- [ ] **Life Timeline Visual** — Scrollable timeline of milestones with achievement badges
- [ ] **Settings Screen** — Dedicated settings tab with theme, language, notification defaults

See [`feature/phase-3-depth-retention`](https://github.com/willowvibe/AgeReveal/tree/feature/phase-3-depth-retention) branch for active development.

### Build Status
- **APK Location:** `AgeReveal-0.3-debug.apk`
- **Version:** 0.3 (updated from 0.2)
