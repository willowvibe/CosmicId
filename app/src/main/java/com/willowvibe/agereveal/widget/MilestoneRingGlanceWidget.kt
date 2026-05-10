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
import com.willowvibe.agereveal.notification.MilestoneNotificationScheduler
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Jetpack Glance widget showing countdown to the next upcoming milestone.
 *
 * Reads birth date from SharedPreferences, computes the next unpassed milestone
 * from the standard target list, and displays the target plus days remaining.
 * Tap opens the app at the Calculator screen.
 */
class MilestoneRingGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val birthDate = readBirthDate(context)
        val milestone = birthDate?.let { findNextMilestone(it) }
        provideContent {
            WidgetBody(context, milestone)
        }
    }

    private fun readBirthDate(context: Context): LocalDate? {
        val prefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
        val str = prefs.getString("birth_date", null) ?: return null
        return runCatching { LocalDate.parse(str) }.getOrNull()
    }

    private fun findNextMilestone(birthDate: LocalDate): NextMilestone? {
        val today = LocalDate.now()
        val targets = MilestoneNotificationScheduler.MILESTONE_TARGETS
        val totalDays = ChronoUnit.DAYS.between(birthDate, today)
        val nextTarget = targets.firstOrNull { it > totalDays } ?: return null
        val milestoneDate = birthDate.plusDays(nextTarget.toLong())
        val daysRemaining = ChronoUnit.DAYS.between(today, milestoneDate)
        val prevTarget = targets.lastOrNull { it <= totalDays } ?: 0
        val progress = if (nextTarget > prevTarget) {
            (totalDays - prevTarget).toFloat() / (nextTarget - prevTarget).toFloat()
        } else 0f
        return NextMilestone(
            targetDays = nextTarget,
            daysRemaining = daysRemaining.coerceAtLeast(0),
            progress = progress.coerceIn(0f, 1f),
        )
    }

    data class NextMilestone(
        val targetDays: Int,
        val daysRemaining: Long,
        val progress: Float,
    )
}

private val BgNavy = Color(0xFF1A1A2E)
private val AccentMint = Color(0xFF86EFAC)
private val WhiteFaint = Color(0x99FFFFFF)

@Composable
private fun WidgetBody(context: Context, milestone: MilestoneRingGlanceWidget.NextMilestone?) {
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
            text = "NEXT MILESTONE",
            style = TextStyle(
                color = ColorProvider(WhiteFaint),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))

        if (milestone != null) {
            Text(
                text = "%,d days".format(milestone.targetDays),
                style = TextStyle(
                    color = ColorProvider(AccentMint),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = "%,d days to go · %d%%".format(milestone.daysRemaining, (milestone.progress * 100).toInt()),
                style = TextStyle(
                    color = ColorProvider(WhiteFaint),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        } else {
            Text(
                text = "—",
                style = TextStyle(
                    color = ColorProvider(AccentMint),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = "Set your birth date in Cosmic ID",
                style = TextStyle(
                    color = ColorProvider(WhiteFaint),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}
