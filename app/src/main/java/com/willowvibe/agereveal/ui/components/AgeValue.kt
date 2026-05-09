package com.willowvibe.agereveal.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.willowvibe.agereveal.ui.theme.InterFamily
import com.willowvibe.agereveal.ui.theme.WarmInk

/**
 * Semibold value text for key numbers (20sp, Inter SemiBold).
 */
@Composable
fun AgeValue(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
) {
    Text(
        text = text,
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = accentColor ?: WarmInk,
        modifier = modifier,
    )
}
