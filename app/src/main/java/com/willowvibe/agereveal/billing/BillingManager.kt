package com.willowvibe.agereveal.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.willowvibe.agereveal.analytics.AnalyticsManager
import com.willowvibe.agereveal.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Billing wrapper for v2.0 subscription tiers + v2.1 one-time
 * Korean Saju unlock.
 *
 * Product IDs (must match Google Play Console):
 *   - "premium_monthly"  (₹49/month, subscription)
 *   - "premium_yearly"   (₹299/year, subscription)
 *   - "korean_saju_unlock" (₹149 one-time, in-app product)
 *
 * Call [startConnection] in [MainActivity.onCreate] and [endConnection] in [onDestroy].
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPrefs: UserPreferencesRepository,
    private val analytics: AnalyticsManager,
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob())

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _trialDaysRemaining = MutableStateFlow<Int?>(null)
    val trialDaysRemaining: StateFlow<Int?> = _trialDaysRemaining.asStateFlow()

    private val _graceDaysRemaining = MutableStateFlow<Int?>(null)
    val graceDaysRemaining: StateFlow<Int?> = _graceDaysRemaining.asStateFlow()

    /** True when the user has bought the korean_saju_unlock IAP. */
    private val _isKoreanSajuUnlocked = MutableStateFlow(false)
    val isKoreanSajuUnlocked: StateFlow<Boolean> = _isKoreanSajuUnlocked.asStateFlow()

    private var billingClient: BillingClient? = null

    private val subscriptionProductIds = listOf("premium_monthly", "premium_yearly")
    private val inAppProductIds = listOf("korean_saju_unlock")

    private val productIds get() = subscriptionProductIds + inAppProductIds

    /** Must be called before any billing operation (e.g. in MainActivity.onCreate). */
    fun startConnection() {
        if (billingClient?.isReady == true) return
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isConnected.value = true
                    _error.value = null
                    scope.launch {
                        queryProducts()
                        queryExistingPurchases()
                        queryExistingInAppPurchases()
                    }
                } else {
                    _error.value = BillingUtils.billingErrorMessage(result.responseCode)
                }
            }
            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
                // Auto-retry connection after 3s
                scope.launch {
                    kotlinx.coroutines.delay(3_000)
                    if (billingClient != null) startConnection()
                }
            }
        })
    }

    /** Tear down the billing client and cancel coroutines to avoid leaks. */
    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
        _isConnected.value = false
        scope.cancel()
    }

    /** Launch the Google Play purchase flow for a subscription [productDetails]. */
    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        billingClient?.launchBillingFlow(activity, flowParams)
    }

    /**
     * Launch the one-time IAP flow for the korean_saju_unlock SKU. Looks up
     * the matching [ProductDetails] from the cached [products] list, then
     * hands off to Play Billing. In-app products don't have offer tokens
     * — they go straight to the cart.
     */
    fun launchKoreanSajuUnlockFlow(activity: Activity) {
        val saju = _products.value.firstOrNull { it.productId == "korean_saju_unlock" } ?: return
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(saju)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        billingClient?.launchBillingFlow(activity, flowParams)
    }

    /** Re-query existing purchases — used by Settings → Restore Purchases. */
    fun restorePurchases() {
        if (!_isConnected.value) {
            _error.value = "Not connected to Play Store. Please retry."
            return
        }
        scope.launch {
            queryExistingPurchases()
            queryExistingInAppPurchases()
        }
    }

    fun clearError() { _error.value = null }

    /** Called by the system when a purchase finishes (or is cancelled). */
    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // No-op: user cancelled is not an error
        } else {
            _error.value = BillingUtils.billingErrorMessage(result.responseCode)
        }
    }

    // ---------------------------------------------------------------------------

    private suspend fun queryProducts() {
        // Query subscription products
        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                subscriptionProductIds.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            )
            .build()
        val subResult = billingClient?.queryProductDetails(subParams) ?: return
        val subList = if (subResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            subResult.productDetailsList ?: emptyList()
        } else emptyList()

        // Query in-app (one-time) products
        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                inAppProductIds.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            )
            .build()
        val inAppResult = billingClient?.queryProductDetails(inAppParams)
        val inAppList = if (inAppResult?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
            inAppResult.productDetailsList ?: emptyList()
        } else emptyList()

        _products.value = subList + inAppList
        _error.value = null
        // Extract trial duration from the first subscription that has a free pricing phase
        val trialDays = subList.firstNotNullOfOrNull { extractTrialDays(it) } ?: 0
        userPrefs.setTrialDurationDays(trialDays)
    }

    private suspend fun queryExistingInAppPurchases() {
        val result = billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) ?: return
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val purchases = result.purchasesList ?: emptyList()
            val ownedSaju = purchases.any {
                it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    inAppProductIds.contains(it.products.firstOrNull())
            }
            if (ownedSaju) {
                _isKoreanSajuUnlocked.value = true
                scope.launch { userPrefs.setKoreanSajuUnlocked(true) }
            }
        }
    }

    private suspend fun queryExistingPurchases() {
        val result = billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) ?: run {
            _error.value = "Billing client not ready"
            return
        }
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            handlePurchases(result.purchasesList ?: emptyList())
            _error.value = null
        } else {
            _error.value = BillingUtils.billingErrorMessage(result.billingResult.responseCode)
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        // Partition into subscriptions vs in-app products
        val (subs, inApps) = purchases.partition {
            it.products.any { id -> subscriptionProductIds.contains(id) }
        }
        handleSubscriptionPurchases(subs)
        handleInAppPurchases(inApps)
    }

    private fun handleSubscriptionPurchases(purchases: List<Purchase>) {
        var hasActive = false
        var latestPurchaseTime = 0L
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.acknowledgePurchase(params) { ackResult ->
                        if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                            _error.value = BillingUtils.billingErrorMessage(ackResult.responseCode)
                        }
                    }
                }
                hasActive = true
                if (purchase.purchaseTime > latestPurchaseTime) {
                    latestPurchaseTime = purchase.purchaseTime
                }
            }
        }

        scope.launch {
            if (hasActive) {
                // Active purchase found — clear any grace period and restore premium
                userPrefs.setGracePeriodStart(0L)
                _graceDaysRemaining.value = null
                _isPremium.value = true
                userPrefs.setPremium(true)
                val storedTime = userPrefs.premiumPurchaseTime.first()
                if (storedTime == 0L) {
                    userPrefs.setPremiumPurchaseTime(latestPurchaseTime)
                }
                analytics.logPurchaseComplete("premium_subscription")
                val trialDays = userPrefs.trialDurationDays.first()
                if (trialDays > 0) {
                    analytics.logTrialStarted("premium_subscription")
                }
                updateTrialDaysRemaining()
            } else {
                // No active purchase — check grace period
                val wasPremium = userPrefs.isPremium.first()
                if (wasPremium) {
                    // Subscription lapsed — start or continue grace period
                    var graceStart = userPrefs.gracePeriodStart.first()
                    if (graceStart == 0L) {
                        graceStart = System.currentTimeMillis()
                        userPrefs.setGracePeriodStart(graceStart)
                    }
                    val elapsedDays = ((System.currentTimeMillis() - graceStart) / 86_400_000).toInt()
                    val remaining = 3 - elapsedDays
                    if (remaining > 0) {
                        _isPremium.value = true
                        _graceDaysRemaining.value = remaining
                    } else {
                        // Grace period expired — hard lockout
                        _isPremium.value = false
                        userPrefs.setPremium(false)
                        _graceDaysRemaining.value = null
                        userPrefs.setGracePeriodStart(0L)
                    }
                } else {
                    // Never premium — no grace period
                    _isPremium.value = false
                    _graceDaysRemaining.value = null
                }
                _trialDaysRemaining.value = null
            }
        }
    }

    private fun handleInAppPurchases(purchases: List<Purchase>) {
        // The Korean Saju unlock is a one-time entitlement — once owned,
        // it stays owned forever. (Play Console handles refund-revocation
        // for us; we just observe the current state.)
        val ownsSaju = purchases.any { p ->
            p.purchaseState == Purchase.PurchaseState.PURCHASED &&
                inAppProductIds.contains(p.products.firstOrNull())
        }
        if (ownsSaju) {
            _isKoreanSajuUnlocked.value = true
            scope.launch { userPrefs.setKoreanSajuUnlocked(true) }
            analytics.logPurchaseComplete("korean_saju_unlock")
        } else {
            // Could be refund-revoked
            _isKoreanSajuUnlocked.value = false
            scope.launch { userPrefs.setKoreanSajuUnlocked(false) }
        }
        // Acknowledge any unacknowledged one-time purchases (required within
        // 3 days by Google Play; otherwise the purchase is refunded).
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(params) { ackResult ->
                    if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        _error.value = BillingUtils.billingErrorMessage(ackResult.responseCode)
                    }
                }
            }
        }
    }

    /** Parses a free-trial pricing phase from [productDetails] and returns its duration in days. */
    private fun extractTrialDays(productDetails: ProductDetails): Int? {
        val offer = productDetails.subscriptionOfferDetails?.firstOrNull() ?: return null
        val freePhase = offer.pricingPhases.pricingPhaseList.firstOrNull {
            it.priceAmountMicros == 0L
        } ?: return null
        return BillingUtils.parseBillingPeriodToDays(freePhase.billingPeriod)
    }

    private suspend fun updateTrialDaysRemaining() {
        val purchaseTime = userPrefs.premiumPurchaseTime.first()
        val trialDays = userPrefs.trialDurationDays.first()
        if (purchaseTime == 0L || trialDays <= 0) {
            _trialDaysRemaining.value = null
            return
        }
        val elapsedDays = ((System.currentTimeMillis() - purchaseTime) / (86_400_000)).toInt()
        val remaining = trialDays - elapsedDays
        _trialDaysRemaining.value = if (remaining > 0) remaining else null
    }

}
