# Skill: Screenshot & UI Testing Workflow

## When to use
After any UI change, bug fix, or new screen added.

## Steps
1. Build debug APK: ./gradlew :app:assembleDebug
2. Install: adb install -r app/build/outputs/apk/debug/app-debug.apk
3. Launch Appium session on emulator-5554
4. Run full tab walkthrough (You → Match → Bdays → Badges → Timeline)
5. Screenshots → screenshots/ folder, overwrite existing files
6. Regenerate screenshots/REPORT.md with timestamp
7. Flag any BUG_ screenshots — prefix with BUG_, delete if fixed

## Naming
- tab{N}_{screen}_{action}.png
- BUG_{feature}_{description}.png
