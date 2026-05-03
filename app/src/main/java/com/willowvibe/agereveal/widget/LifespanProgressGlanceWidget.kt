package com.willowvibe.agereveal.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.willowvibe.agereveal.MainActivity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Jetpack Glance widget showing lifespan progress as a percentage.
 *
 * Reads birth date from SharedPreferences and target age from the mirrored
 * SharedPreferences value (updated whenever the user changes it in Settings).
 * Refreshes once per day — no per-second updates needed.
 */
class LifespanProgressGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val birthDate = readBirthDate(context)
        val targetAge = readTargetAge(context)
        val totalDays = birthDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) } ?: 0L
        val progress = (totalDays.toFloat() / (targetAge * 365f)).coerceIn(0f, 1.2f)
        provideContent {
            WidgetBody(context, progress, targetAge, totalDays)
        }
    }

    private fun readBirthDate(context: Context): LocalDate? {
        val prefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
        val str = prefs.getString("birth_date", null) ?: return null
        return runCatching { LocalDate.parse(str) }.getOrNull()
    }

    private fun readTargetAge(context: Context): Int {
        val prefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("target_age", 80)
    }
}

private val BgNavy = Color(0xFF1A1A2E)
private val WhiteFaint = Color(0x99FFFFFF)

@Composable
private fun WidgetBody(context: Context, progress: Float, targetAge: Int, totalDays: Long) {
    val accent = when {
        progress < 0.33f -> Color(0xFF86EFAC) // teal
        progress < 0.66f -> Color(0xFFFBBF24) // amber
        progress < 1.0f  -> Color(0xFFF87171) // rose
        else             -> Color(0xFFEF4444) // red
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BgNavy)
            .padding(12.dp)
            .clickable(
                actionStartActivity(
                    ComponentName(context, MainActivity::class.java)
                )
            ),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "LIFESPAN",
            style = TextStyle(
                color = ColorProvider(WhiteFaint),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "${(progress * 100).toInt()}%",
            style = TextStyle(
                color = ColorProvider(accent),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = "%,d days · $targetAge yr target".format(totalDays),
            style = TextStyle(
                color = ColorProvider(WhiteFaint),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
