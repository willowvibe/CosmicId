package com.willowvibe.agereveal.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for the seconds-counter home-screen widget.
 */
class SecondsCounterGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SecondsCounterGlanceWidget()
}
