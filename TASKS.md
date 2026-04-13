# AgeReveal — Remaining Tasks & Placeholders

_Last updated: 2026-04-13_

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
| `app/src/main/java/com/willowvibe/agereveal/notification/BirthdayReminderWorker.kt` | 44 | `// TODO: add ic_cake vector drawable to res/` | **Outdated** — `res/drawable/ic_cake.xml` already exists. Remove the comment. |

---

## 3. Remaining Phase 1 Work (Day 4 Polish & Launch)

These items are tracked in `roadmap.md` under the Day 4 milestone.

- [ ] **Animations** — Add enter/reveal animation when age result appears on the main screen
- [ ] **Date picker UX** — Smooth out the date picker interactions (scroll snap, haptic feedback)
- [ ] **Edge cases:**
  - Leap year birthdays (Feb 29) — ensure correct handling in years when Feb 29 does not exist
  - Future date input — validate and show a clear error instead of negative age
  - Today's date — verify zero-duration display is handled gracefully
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

Not yet started. Tracked in `roadmap.md`.

- [ ] **Themed share cards** — Light / festive themes as additional rewarded ad unlocks
- [ ] **Hindi UI toggle** — In-app language switch between English and Hindi
- [ ] **4×2 home screen widget** — Wider widget showing 3 upcoming birthdays
- [ ] **In-app review prompt** — Trigger the Play Store review sheet after a user shares their card
- [ ] **Remove Ads IAP** — One-time ₹99 purchase to disable all ads (needs Play Billing Library integration)

---

## 5. Future Technical Improvements (Nice-to-Have)

- [ ] **Migrate widget to Jetpack Glance** — `BirthdayWidgetProvider.kt` line 21 notes that the current `RemoteViews`-based widget should eventually be rewritten using Glance (Compose-based widgets API)
- [ ] **Enable Inter font** — See Section 1 font placeholder above
