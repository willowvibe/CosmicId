package com.willowvibe.agereveal.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.willowvibe.agereveal.ui.theme.WarmInkMute

/**
 * Uppercase section label (10sp, Inter SemiBold, 1.5sp letter-spacing).
 */
@Composable
fun AgeLabel(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = accentColor ?: WarmInkMute,
        modifier = modifier,
    )
}
