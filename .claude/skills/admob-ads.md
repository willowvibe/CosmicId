# Skill: AdMob Integration

## When to use
When working with banner ads, interstitial ads, rewarded ads, or ad loading logic.

## Rules
- Ad unit IDs live in BuildConfig / strings.xml — never hardcode in Composables
- Test ads use: ca-app-pub-3940256099942544/... (Google test IDs)
- Banner ads: always wrap in AndroidView inside a dedicated BannerAdView composable
- Rewarded ads: gate behind AdManager singleton, never trigger from ViewModel directly
- "Watch & Reveal" flow: RewardedAd must call onUserEarnedReward before unlocking content
- Always handle ad load failures gracefully — show fallback UI, never crash

## File locations
- app/src/main/java/.../ads/
