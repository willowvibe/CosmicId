package com.willowvibe.agereveal.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * AppWidgetProvider for [MilestoneRingGlanceWidget].
 */
class MilestoneRingGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MilestoneRingGlanceWidget()
}
