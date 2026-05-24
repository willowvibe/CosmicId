package com.willowvibe.agereveal.ui.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.willowvibe.agereveal.ui.theme.SerifFamily
import com.willowvibe.agereveal.ui.viewmodel.PaywallViewModel
import com.willowvibe.agereveal.ui.theme.WarmAmber
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import com.willowvibe.agereveal.ui.theme.WarmTeal

/**
 * Premium paywall — subscription tiers + restore CTA.
 *
 * Reads products from [BillingManager] and delegates purchase flow.
 */
@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
) {
    val products by viewModel.products.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        viewModel.onPaywallShown()
        onDispose { viewModel.onPaywallDismiss() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Unlock the full cosmos",
            fontFamily = SerifFamily,
            fontSize = 30.sp,
            lineHeight = 36.sp,
            color = WarmInk,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Go Premium for deep Vedic insights, animated video exports, celebrity matches, and ad-free experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkDim,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        // Error banner
        if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarmAmber.copy(alpha = 0.2f))
                    .border(1.dp, WarmAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        error ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = WarmAmber,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            viewModel.clearError()
                            viewModel.restorePurchases()
                        },
                    ) {
                        Text("Retry", color = WarmTeal)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (!isConnected && error == null) {
            CircularProgressIndicator(color = WarmTeal)
            Spacer(Modifier.height(16.dp))
            Text("Connecting to Play Store…", color = WarmInkDim)
        } else if (isConnected && products.isEmpty() && error == null) {
            Text("No subscription products found.", color = WarmInkDim)
        } else if (isConnected) {
            products.forEach { product ->
                ProductCard(
                    product = product,
                    onClick = {
                        val activity = context as? Activity ?: return@ProductCard
                        viewModel.launchBillingFlow(activity, product)
                    },
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Restore purchases — mandatory for Play Store review
        TextButton(
            onClick = { viewModel.restorePurchases() },
        ) {
            Text("Restore purchases", color = WarmInkDim)
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onDismiss) {
            Text("Maybe later", color = WarmInkDim)
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductDetails,
    onClick: () -> Unit,
) {
    val offer = product.subscriptionOfferDetails?.firstOrNull()
    val price = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "—"
    val billingPeriod = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod ?: ""
    val isYearly = billingPeriod.contains("P1Y")
    val label = if (isYearly) "Yearly" else "Monthly"
    val badge = if (isYearly) "BEST VALUE" else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmSurface)
            .border(
                width = if (isYearly) 2.dp else 1.dp,
                color = if (isYearly) WarmAmber else WarmSurfaceSoft,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable { onClick() }
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        label,
                        fontFamily = SerifFamily,
                        fontSize = 20.sp,
                        color = WarmInk,
                    )
                    if (badge != null) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WarmAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmAmber,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    price,
                    style = MaterialTheme.typography.titleMedium,
                    color = WarmTeal,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmTeal,
                    contentColor = WarmBlack,
                ),
            ) {
                Text("Subscribe")
            }
        }
    }
}
