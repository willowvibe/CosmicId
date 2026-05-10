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
 * Google Play Billing wrapper for v2.0 subscription tiers.
 *
 * Product IDs (must match Google Play Console):
 *   - "premium_monthly"  (₹49/month)
 *   - "premium_yearly"   (₹299/year)
 *
 * Call [startConnection] in [MainActivity.onCreate] and [endConnection] in [onDestroy].
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPrefs: UserPreferencesRepository,
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

    private var billingClient: BillingClient? = null

    private val productIds = listOf("premium_monthly", "premium_yearly")

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
                    }
                } else {
                    _error.value = billingErrorMessage(result.responseCode)
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

    /** Launch the Google Play purchase flow for the given [productDetails]. */
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

    /** Re-query existing purchases — used by Settings → Restore Purchases. */
    fun restorePurchases() {
        if (!_isConnected.value) {
            _error.value = "Not connected to Play Store. Please retry."
            return
        }
        scope.launch {
            queryExistingPurchases()
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
            _error.value = billingErrorMessage(result.responseCode)
        }
    }

    // ---------------------------------------------------------------------------

    private suspend fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                productIds.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            )
            .build()
        val result = billingClient?.queryProductDetails(params) ?: return
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val list = result.productDetailsList ?: emptyList()
            _products.value = list
            _error.value = null
            // Extract trial duration from the first product that has a free pricing phase
            val trialDays = list.firstNotNullOfOrNull { extractTrialDays(it) } ?: 0
            userPrefs.setTrialDurationDays(trialDays)
        } else {
            _error.value = billingErrorMessage(result.billingResult.responseCode)
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
            handlePurchases(result.purchasesList)
            _error.value = null
        } else {
            _error.value = billingErrorMessage(result.billingResult.responseCode)
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
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
                            _error.value = billingErrorMessage(ackResult.responseCode)
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

    /** Parses a free-trial pricing phase from [productDetails] and returns its duration in days. */
    private fun extractTrialDays(productDetails: ProductDetails): Int? {
        val offer = productDetails.subscriptionOfferDetails?.firstOrNull() ?: return null
        val freePhase = offer.pricingPhases.pricingPhaseList.firstOrNull {
            it.priceAmountMicros == 0L
        } ?: return null
        return parseBillingPeriodToDays(freePhase.billingPeriod)
    }

    /** Converts an ISO 8601 billing period (e.g. "P7D", "P1W", "P1M") to an approximate day count. */
    private fun parseBillingPeriodToDays(period: String): Int? {
        val regex = Regex("""P(\d+)([DWMY])""")
        val match = regex.matchEntire(period.uppercase()) ?: return null
        val value = match.groupValues[1].toIntOrNull() ?: return null
        return when (match.groupValues[2]) {
            "D" -> value
            "W" -> value * 7
            "M" -> value * 30
            "Y" -> value * 365
            else -> null
        }
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

    private fun billingErrorMessage(code: Int): String = when (code) {
        BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> "Play Store timed out. Please retry."
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Can't reach Play Store. Check your connection."
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Billing is not available on this device."
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "This subscription is not available."
        BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "Billing configuration error."
        BillingClient.BillingResponseCode.ERROR -> "An unexpected billing error occurred."
        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "You already own this subscription."
        BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "Subscription not found."
        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "This feature is not supported on your device."
        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "Play Store disconnected. Reconnecting..."
        BillingClient.BillingResponseCode.USER_CANCELED -> "Purchase cancelled."
        else -> "Billing error (code $code). Please retry."
    }
}
