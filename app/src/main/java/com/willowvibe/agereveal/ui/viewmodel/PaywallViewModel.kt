package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
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
    val products: StateFlow<List<com.android.billingclient.api.ProductDetails>> = billingManager.products

    fun launchBillingFlow(activity: android.app.Activity, product: com.android.billingclient.api.ProductDetails) {
        billingManager.launchBillingFlow(activity, product)
    }
}
