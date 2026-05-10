package com.willowvibe.agereveal.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.ProductDetails
import com.willowvibe.agereveal.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin ViewModel that exposes [BillingManager] state to [PaywallScreen].
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
) : ViewModel() {

    val isConnected: StateFlow<Boolean> = billingManager.isConnected
    val products: StateFlow<List<ProductDetails>> = billingManager.products
    val error: StateFlow<String?> = billingManager.error

    fun launchBillingFlow(activity: Activity, product: ProductDetails) {
        billingManager.launchBillingFlow(activity, product)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    fun clearError() {
        billingManager.clearError()
    }
}
