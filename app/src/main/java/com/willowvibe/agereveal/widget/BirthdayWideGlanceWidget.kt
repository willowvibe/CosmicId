package com.willowvibe.agereveal.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.willowvibe.agereveal.data.db.AppDatabase
import com.willowvibe.agereveal.data.model.SavedBirthday
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 4×2 wide Glance widget — shows top-3 upcoming birthdays with days-remaining.
 * Shares the Room Flow safely with `.firstOrNull()` to never hang on an empty emission.
 */
class BirthdayWideGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val upcoming = runCatching {
            AppDatabase.getInstance(context).birthdayDao().getUpcomingForWidget()
                .firstOrNull() ?: emptyList()
        }.getOrDefault(emptyList())

        provideContent {
            WideWidgetBody(upcoming.take(3))
        }
    }
}

private val BgNavy = Color(0xFF1A1A2E)
private val AccentMint = Color(0xFF86EFAC)
private val AccentAmber = Color(0xFFFFB74D)
private val WhiteSoft = Color(0x99FFFFFF)
private val WhiteFaint = Color(0x80FFFFFF)
private val WhiteGhost = Color(0x66FFFFFF)

@Composable
private fun WideWidgetBody(list: List<SavedBirthday>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BgNavy)
            .padding(14.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "🎂 UPCOMING BIRTHDAYS",
            style = TextStyle(
                color = ColorProvider(WhiteFaint),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))

        if (list.isEmpty()) {
            Text(
                text = "Add birthdays in the app →",
                style = TextStyle(
                    color = ColorProvider(WhiteSoft),
                    fontSize = 12.sp,
                ),
            )
            return@Column
        }

        val today = LocalDate.now()
        list.forEachIndexed { index, birthday ->
            val daysLeft = ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(birthday.nextBirthdayEpochDay))
            val accent = if (index == 0) AccentMint else AccentAmber
            BirthdayRow(
                name = "${birthday.emoji} ${birthday.name}",
                days = daysLeft,
                accent = accent,
                highlight = index == 0,
            )
            if (index < list.size - 1) Spacer(modifier = GlanceModifier.height(6.dp))
        }
    }
}

@Composable
private fun BirthdayRow(name: String, days: Long, accent: Color, highlight: Boolean) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(if (highlight) WhiteSoft else WhiteGhost),
                fontSize = if (highlight) 13.sp else 12.sp,
                fontWeight = if (highlight) FontWeight.Medium else FontWeight.Normal,
            ),
            maxLines = 1,
        )
        Text(
            text = if (days == 0L) "TODAY" else "${days}d",
            style = TextStyle(
                color = ColorProvider(accent),
                fontSize = if (highlight) 16.sp else 13.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
