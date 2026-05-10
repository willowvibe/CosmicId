package com.willowvibe.agereveal.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willowvibe.agereveal.domain.ShareCardGenerator
import com.willowvibe.agereveal.ui.theme.SerifFamily
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import com.willowvibe.agereveal.ui.theme.WarmTeal

private data class ThemeVisual(
    val label: String,
    val description: String,
    val bgBrush: Brush,
    val textColor: Color,
)

private fun themeVisualFor(theme: ShareCardGenerator.CardTheme) = when (theme) {
    ShareCardGenerator.CardTheme.DARK_COSMOS -> ThemeVisual(
        label = "Dark Cosmos",
        description = "Deep navy gradient — perfect for night sharing",
        bgBrush = Brush.linearGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E))),
        textColor = Color.White,
    )
    ShareCardGenerator.CardTheme.MINIMAL_LIGHT -> ThemeVisual(
        label = "Minimal Light",
        description = "Clean white with dark ink — crisp and elegant",
        bgBrush = Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF0EDE8))),
        textColor = Color(0xFF1C1917),
    )
    ShareCardGenerator.CardTheme.FESTIVE_INDIA -> ThemeVisual(
        label = "Festive India",
        description = "Saffron to green gradient — vibrant celebration",
        bgBrush = Brush.linearGradient(listOf(Color(0xFFFF9933), Color(0xFF138808))),
        textColor = Color.White,
    )
}

enum class ShareFormat { SQUARE, STORY, GREEN_SCREEN }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareThemeSheet(
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismiss: () -> Unit,
    onThemeSelected: (ShareCardGenerator.CardTheme, ShareFormat) -> Unit,
    showFormatOptions: Boolean = true,
) {
    var selectedFormat by remember { mutableStateOf(ShareFormat.SQUARE) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmSurface,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Choose a Style",
                fontFamily = SerifFamily,
                fontSize = 22.sp,
                color = WarmInk,
                fontWeight = FontWeight.Normal,
            )
            Text(
                "Pick a card theme for your share image",
                style = MaterialTheme.typography.bodySmall,
                color = WarmInkDim,
            )

            if (showFormatOptions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FormatChip("Square", selected = selectedFormat == ShareFormat.SQUARE) { selectedFormat = ShareFormat.SQUARE }
                    FormatChip("Story", selected = selectedFormat == ShareFormat.STORY) { selectedFormat = ShareFormat.STORY }
                    FormatChip("Green", selected = selectedFormat == ShareFormat.GREEN_SCREEN) { selectedFormat = ShareFormat.GREEN_SCREEN }
                }
            }

            ShareCardGenerator.CardTheme.entries.forEach { theme ->
                ThemeOptionRow(
                    theme = theme,
                    onSelected = { onThemeSelected(theme, selectedFormat) },
                )
            }
        }
    }
}

@Composable
private fun FormatChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) WarmTeal else WarmSurfaceSoft)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) WarmBlack else WarmInk,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ThemeOptionRow(
    theme: ShareCardGenerator.CardTheme,
    onSelected: () -> Unit,
) {
    val visual = themeVisualFor(theme)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurfaceSoft)
            .clickable { onSelected() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Color swatch preview
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(visual.bgBrush),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Aa",
                color = visual.textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                visual.label,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInk,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                visual.description,
                style = MaterialTheme.typography.bodySmall,
                color = WarmInkDim,
            )
        }

        Text(
            "→",
            color = WarmTeal,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
