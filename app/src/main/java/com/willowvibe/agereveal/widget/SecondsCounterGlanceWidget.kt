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
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Jetpack Glance widget that displays the user's total seconds alive.
 *
 * Reads the birth date from the same SharedPreferences key used by
 * [com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel].
 * Since widgets cannot tick every second reliably on modern Android,
 * the seconds value is computed at render time — accurate whenever
 * the widget is redrawn (screen unlock, home-screen swipe, periodic refresh).
 */
class SecondsCounterGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val totalSeconds = readTotalSeconds(context)
        provideContent {
            WidgetBody(context, totalSeconds)
        }
    }

    private fun readTotalSeconds(context: Context): Long? {
        val prefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
        val birthDateStr = prefs.getString("birth_date", null) ?: return null
        val birthDate = runCatching { LocalDate.parse(birthDateStr) }.getOrNull() ?: return null
        return ChronoUnit.SECONDS.between(birthDate.atStartOfDay(), LocalDateTime.now())
    }
}

private val BgNavy = Color(0xFF1A1A2E)
private val AccentMint = Color(0xFF86EFAC)
private val WhiteFaint = Color(0x99FFFFFF)

@Composable
private fun WidgetBody(context: Context, totalSeconds: Long?) {
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
            text = "SECONDS ALIVE",
            style = TextStyle(
                color = ColorProvider(WhiteFaint),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))

        val numberText = totalSeconds?.let { "%,d".format(it) } ?: "—"
        Text(
            text = numberText,
            style = TextStyle(
                color = ColorProvider(AccentMint),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
