package com.willowvibe.agereveal.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for the 4×1 lifespan progress bar widget.
 */
class LifespanProgressGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LifespanProgressGlanceWidget()
}
