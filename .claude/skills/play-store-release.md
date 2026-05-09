# Skill: Play Store Release Checklist

## When to use
When preparing a release build or updating store listing.

## Steps
1. Bump versionCode + versionName in app/build.gradle.kts
2. Update VERSION file in root
3. Run: ./gradlew :app:bundleRelease
4. Sign with keystore (keystore.properties must exist)
5. Update store_listing/ assets if UI changed
6. Update TASKS.md and roadmap.md to mark release tasks done
7. Git tag: git tag v{version} && git push --tags

## File locations
- store_listing/
- VERSION
- keystore.properties (gitignored)
