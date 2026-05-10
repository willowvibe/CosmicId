# Skill: AdMob Integration

## When to use
When working with banner ads or ad loading logic. In v2.0, only banner ads remain on the free tier; rewarded and interstitial ads were removed.

## Rules
- Ad unit IDs live in BuildConfig / strings.xml — never hardcode in Composables
- Test ads use: ca-app-pub-3940256099942544/... (Google test IDs)
- Banner ads: always wrap in AndroidView inside a dedicated BannerAdView composable
- In v2.0, banner ads are the only ad format; premium subscribers see no ads
- Always handle ad load failures gracefully — show fallback UI, never crash
- Replace test IDs with real AdMob IDs before Play Store release

## File locations
- app/src/main/java/.../ads/
