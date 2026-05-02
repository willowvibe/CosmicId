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
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Jetpack Glance replacement for the legacy RemoteViews-based BirthdayWidgetProvider.
 * Renders the same 2x2 "dark cosmos" countdown but via Compose-style declarative UI,
 * which makes future visual changes (themes, content variants) trivial.
 *
 * Uses .firstOrNull() to never hang on empty Flow emissions (BUG-024 fix).
 */
class BirthdayGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Use firstOrNull() to prevent hanging on empty emissions (BUG-024)
        // Room's Flow will emit the latest cached data immediately on subsequent calls
        val upcoming = runCatching {
            AppDatabase.getInstance(context).birthdayDao().getUpcomingForWidget().firstOrNull() ?: emptyList()
        }.getOrDefault(emptyList())

        provideContent {
            WidgetBody(buildState(upcoming))
        }
    }

    private fun buildState(list: List<SavedBirthday>): WidgetState {
        if (list.isEmpty()) return WidgetState.Empty
        val today = LocalDate.now()
        val first = list.first()
        val daysLeft = ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(first.nextBirthdayEpochDay))
        val secondLine = list.getOrNull(1)?.let { second ->
            val days = ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(second.nextBirthdayEpochDay))
            "${second.emoji} ${second.name} in ${days}d"
        }.orEmpty()
        val thirdLine = list.getOrNull(2)?.let { third ->
            val days = ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(third.nextBirthdayEpochDay))
            "${third.emoji} ${third.name} in ${days}d"
        }.orEmpty()
        return WidgetState.Loaded(
            personHeadline = "${first.emoji} ${first.name}'s birthday",
            daysLeft = daysLeft,
            secondLine = secondLine,
            thirdLine = thirdLine,
        )
    }
}

private sealed interface WidgetState {
    data object Empty : WidgetState
    data class Loaded(
        val personHeadline: String,
        val daysLeft: Long,
        val secondLine: String = "",
        val thirdLine: String = "",
    ) : WidgetState
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
            is WidgetState.Empty -> ""
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
                is WidgetState.Empty -> "Add birthdays"
                is WidgetState.Loaded -> state.personHeadline
            },
            style = TextStyle(
                color = ColorProvider(WhiteSoft),
                fontSize = 11.sp,
            ),
            maxLines = 1,
        )

        if (state is WidgetState.Loaded && state.secondLine.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = state.secondLine,
                style = TextStyle(
                    color = ColorProvider(WhiteGhost),
                    fontSize = 10.sp,
                ),
                maxLines = 1,
            )
        }
        if (state is WidgetState.Loaded && state.thirdLine.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = state.thirdLine,
                style = TextStyle(
                    color = ColorProvider(WhiteGhost),
                    fontSize = 10.sp,
                ),
                maxLines = 1,
            )
        }
    }
}
