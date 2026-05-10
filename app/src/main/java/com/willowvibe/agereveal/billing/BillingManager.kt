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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

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
                    GlobalScope.launch {
                        queryProducts()
                        queryExistingPurchases()
                    }
                }
            }
            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
            }
        })
    }

    /** Tear down the billing client to avoid leaks. */
    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
        _isConnected.value = false
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

    /** Called by the system when a purchase finishes (or is cancelled). */
    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
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
            _products.value = result.productDetailsList ?: emptyList()
        }
    }

    private suspend fun queryExistingPurchases() {
        val result = billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) ?: return
        handlePurchases(result.purchasesList)
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        var hasActive = false
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.acknowledgePurchase(params) { }
                }
                hasActive = true
            }
        }
        _isPremium.value = hasActive
        GlobalScope.launch { userPrefs.setPremium(hasActive) }
    }
}
