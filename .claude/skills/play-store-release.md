# Skill: Play Store Release Checklist

## When to use
When preparing a release build or updating store listing.

## Steps
1. Bump versionCode + versionName in app/build.gradle.kts
2. Update VERSION file in root
3. Run: ./gradlew :app:bundleRelease
4. Sign with keystore (keystore.properties must exist)
5. Update store_listing/ assets if UI changed
6. Update Play Console billing declaration (subscription products, free trial)
7. Update TASKS.md and roadmap.md to mark release tasks done
8. Git tag: git tag v{version} && git push --tags

## Notes for v2.0 (Cosmic ID)
- Package ID: com.willowvibe.cosmicid
- Subscription SKUs: premium_monthly (₹49), premium_yearly (₹299)
- 7-day free trial on yearly plan
- Per-app language support: en, hi, ta, te, kn, ko, vi, zh-rCN

## File locations
- store_listing/
- VERSION
- keystore.properties (gitignored)
