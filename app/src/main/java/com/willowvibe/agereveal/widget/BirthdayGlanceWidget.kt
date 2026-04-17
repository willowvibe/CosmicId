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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.willowvibe.agereveal.data.db.AppDatabase
import com.willowvibe.agereveal.data.model.SavedBirthday
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Jetpack Glance replacement for the legacy RemoteViews-based BirthdayWidgetProvider.
 * Renders the same 2×2 "dark cosmos" countdown but via Compose-style declarative UI,
 * which makes future visual changes (themes, content variants) trivial.
 */
class BirthdayGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val upcoming = runCatching {
            AppDatabase.getInstance(context).birthdayDao().getUpcomingForWidget().first()
        }.getOrDefault(emptyList())
        val state = buildState(upcoming)

        provideContent {
            WidgetBody(state)
        }
    }

    private fun buildState(list: List<SavedBirthday>): WidgetState {
        if (list.isEmpty()) return WidgetState.Empty
        val today = LocalDate.now()
        val first = list.first()
        val daysLeft = ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(first.nextBirthdayEpochDay))
        val nextLine = list.getOrNull(1)?.let { second ->
            val daysToSecond = ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(second.nextBirthdayEpochDay))
            "Next: ${second.name} in ${daysToSecond}d"
        }.orEmpty()
        return WidgetState.Loaded(
            personHeadline = "${first.emoji} ${first.name}'s birthday",
            daysLeft = daysLeft,
            nextLine = nextLine,
        )
    }
}

private sealed interface WidgetState {
    data object Empty : WidgetState
    data class Loaded(val personHeadline: String, val daysLeft: Long, val nextLine: String) : WidgetState
}

private val BgNavy = Color(0xFF1A1A2E)
private val AccentMint = Color(0xFF86EFAC)
private val WhiteSoft = Color(0x99FFFFFF)
private val WhiteFaint = Color(0x80FFFFFF)
private val WhiteGhost = Color(0x66FFFFFF)

@Composable
private fun WidgetBody(state: WidgetState) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BgNavy)
            .padding(14.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "🎂 BIRTHDAY IN",
            style = TextStyle(
                color = ColorProvider(WhiteFaint),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))

        val daysText = when (state) {
            is WidgetState.Empty -> "—"
            is WidgetState.Loaded -> state.daysLeft.toString()
        }
        Text(
            text = daysText,
            style = TextStyle(
                color = ColorProvider(AccentMint),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        Text(
            text = when (state) {
                is WidgetState.Empty -> "Add birthdays →"
                is WidgetState.Loaded -> state.personHeadline
            },
            style = TextStyle(
                color = ColorProvider(WhiteSoft),
                fontSize = 11.sp,
            ),
            maxLines = 1,
        )

        if (state is WidgetState.Loaded && state.nextLine.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(6.dp))
            Text(
                text = state.nextLine,
                style = TextStyle(
                    color = ColorProvider(WhiteGhost),
                    fontSize = 10.sp,
                ),
                maxLines = 1,
            )
        }
    }
}

