# Play Store Submission Checklist — AgeReveal v1.0

Use this list as you upload the AAB. Everything in `store_listing/` is ready to paste into the Play Console.

## 1. Build the release AAB

```bash
# 1. Generate a release keystore (ONE TIME — keep the .jks backed up securely)
keytool -genkey -v -keystore release.jks -alias age_reveal \
  -keyalg RSA -keysize 2048 -validity 10000

# 2. Copy keystore.properties.example → keystore.properties and fill in real values
cp keystore.properties.example keystore.properties
# edit keystore.properties

# 3. Replace the 4 AdMob test IDs with your real production IDs:
#    - app/build.gradle.kts line ~47 → "admobAppId" manifest placeholder
#    - app/src/main/java/.../ads/AdManager.kt lines 37-39 → BANNER/REWARDED/INTERSTITIAL ids

# 4. Build
./gradlew bundleRelease

# The signed AAB is at: app/build/outputs/bundle/release/app-release.aab
```

## 2. Play Console → Create app

| Field | Value |
|---|---|
| App name | Age Calculator: Birthday Days |
| Default language | English (United States) |
| App category | Lifestyle |
| Free / Paid | Free |

## 3. Main store listing

| Field | Where to find it |
|---|---|
| Short description (80 chars max) | `store_listing/short_description.txt` |
| Full description (4 000 chars max) | `store_listing/full_description.txt` |
| App icon 512×512 PNG | `store_listing/icon_512.png` ✅ generated |
| Feature graphic 1024×500 PNG | `store_listing/feature_graphic.png` ✅ generated |
| Phone screenshots (2–8, portrait) | `store_listing/screenshots/01–07_*.png` ✅ generated (1080×2400) |

> **Note:** The generated PNGs are programmatic marketing mocks in the app's exact
> palette and typography — ready to upload as-is. Replace any with real captures
> from an Android device later. Regenerate anytime with:
>
> ```bash
> python3 scripts/render_store_assets.py
> ```

## 4. Privacy policy

Host `store_listing/privacy_policy.md` at a public URL (e.g. GitHub Pages, Notion, your own site)
and paste the URL into Play Console → App content → Privacy policy.

Default target URL in the app settings screen: `https://willowvibe.com/agereveal/privacy` — edit
`PRIVACY_POLICY_URL` in `SettingsScreen.kt` if you host it elsewhere.

## 5. Data safety form

Declare:
- **Data collected**: *None*.
- **Data processed but not collected**: *Approximate advertising ID (AdMob)*.
- **Security practices**: Data is encrypted in transit (HTTPS for ad traffic only). Users can
  clear all saved data via Settings → Clear all birthdays.

## 6. Content rating questionnaire

Expected rating: **Everyone**.
- No violence, sexual content, profanity.
- Shares user-generated content via Android share sheet (not to any server).
- Contains advertising.

## 7. Target audience

- Target age: 13+.
- App does not primarily target children.

## 8. Release notes

Paste contents of `store_listing/release_notes_v1.0.md`.

## 9. Track: Closed testing → Open testing → Production

Recommended rollout:
1. **Internal testing** with ≤20 testers — verify real AdMob impressions, Room migration,
   widget behaviour, notifications.
2. **Open testing** for 1 week — gather reviews, iterate on descriptions.
3. **Production** release at 20% → 50% → 100% staged rollout.
