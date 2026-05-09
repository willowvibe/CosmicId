package com.willowvibe.agereveal.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.willowvibe.agereveal.ui.theme.WarmInkMute

/**
 * Body text for descriptions and subtitles (14sp, Inter Medium).
 * Never uppercase.
 */
@Composable
fun AgeBody(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = WarmInkMute,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = modifier,
    )
}
