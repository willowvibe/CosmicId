package com.willowvibe.agereveal.domain

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.Milestone
import dagger.hilt.android.qualifiers.ApplicationContext
import android.os.Handler
import android.os.Looper
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Generates a shareable bitmap "age card" and launches the system share sheet.
 *
 * Three themes (as per build plan):
 *  - [CardTheme.DARK_COSMOS]  — Navy gradient, white text (default)
 *  - [CardTheme.MINIMAL_LIGHT] — White + black text (unlockable)
 *  - [CardTheme.FESTIVE_INDIA] — Saffron + green (unlockable)
 */
@Singleton
class ShareCardGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    enum class CardTheme { DARK_COSMOS, MINIMAL_LIGHT, FESTIVE_INDIA }

    private val sharingCard = AtomicBoolean(false)
    private val sharingMilestone = AtomicBoolean(false)

    companion object {
        const val CARD_WIDTH = 900
        const val CARD_HEIGHT = 600
        const val CACHE_FILE = "share_card.png"
        const val MILESTONE_CACHE_FILE = "milestone_card.png"
        const val FILE_AUTHORITY_SUFFIX = ".fileprovider"
        private val MILESTONE_DATE_FMT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    }

    /** Generate bitmap share card from [result] using the given [theme]. */
    fun generateBitmap(result: AgeResult, theme: CardTheme = CardTheme.DARK_COSMOS): Bitmap {
        val bmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (theme) {
            CardTheme.DARK_COSMOS  -> drawDarkCosmos(canvas, paint, result)
            CardTheme.MINIMAL_LIGHT -> drawMinimalLight(canvas, paint, result)
            CardTheme.FESTIVE_INDIA -> drawFestiveIndia(canvas, paint, result)
        }

        // Watermark — small, tasteful, bottom-right (passive install driver)
        drawWatermark(canvas, paint)

        return bmp
    }

    /** Share the generated bitmap via Android share sheet (WhatsApp, etc.). */
    fun share(result: AgeResult, theme: CardTheme = CardTheme.DARK_COSMOS) {
        if (!sharingCard.compareAndSet(false, true)) return
        var bitmap: Bitmap? = null
        try {
            bitmap = generateBitmap(result, theme)
            val uri = saveBitmapToCache(bitmap, CACHE_FILE)
            bitmap.recycle()
            bitmap = null
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            // startActivity must run on the main thread; the caller may be on Dispatchers.IO.
            // clipData must be on the chooser (not the inner intent) so WhatsApp receives the
            // URI read permission grant.
            Handler(Looper.getMainLooper()).post {
                try {
                    val chooser = Intent.createChooser(intent, "Share your age")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(chooser)
                } finally {
                    sharingCard.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingCard.set(false)
        }
    }

    /** Generate a dedicated milestone share card (e.g. "You'll turn 10,000 days old on…"). */
    fun generateMilestoneBitmap(milestone: Milestone, theme: CardTheme = CardTheme.FESTIVE_INDIA): Bitmap {
        val bmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (theme) {
            CardTheme.DARK_COSMOS -> {
                val gradient = LinearGradient(
                    0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
                    Color.parseColor("#1a1a2e"), Color.parseColor("#16213e"),
                    Shader.TileMode.CLAMP,
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
                paint.shader = null
                drawMilestoneContent(canvas, paint, milestone,
                    textColor = Color.WHITE, accentColor = Color.parseColor("#86efac"))
            }
            CardTheme.MINIMAL_LIGHT -> {
                paint.color = Color.WHITE
                canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
                drawMilestoneContent(canvas, paint, milestone,
                    textColor = Color.parseColor("#1c1917"), accentColor = Color.parseColor("#0f6e56"))
            }
            CardTheme.FESTIVE_INDIA -> {
                val gradient = LinearGradient(
                    0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
                    Color.parseColor("#FF9933"), Color.parseColor("#138808"),
                    Shader.TileMode.CLAMP,
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
                paint.shader = null
                // Use gold (#FFD700) as accent so the milestone number pops on the saffron-green gradient
                drawMilestoneContent(canvas, paint, milestone,
                    textColor = Color.WHITE, accentColor = Color.parseColor("#FFD700"))
            }
        }

        drawWatermark(canvas, paint)
        return bmp
    }

    /** Share a milestone card via Android share sheet. */
    fun shareMilestone(milestone: Milestone, theme: CardTheme = CardTheme.FESTIVE_INDIA) {
        if (!sharingMilestone.compareAndSet(false, true)) return
        var bitmap: Bitmap? = null
        try {
            bitmap = generateMilestoneBitmap(milestone, theme)
            val uri = saveBitmapToCache(bitmap, MILESTONE_CACHE_FILE)
            bitmap.recycle()
            bitmap = null
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            Handler(Looper.getMainLooper()).post {
                try {
                    val chooser = Intent.createChooser(intent, "Share milestone")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(chooser)
                } finally {
                    sharingMilestone.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingMilestone.set(false)
        }
    }

    // ---------------------------------------------------------------------------
    // Theme renderers
    // ---------------------------------------------------------------------------

    private fun drawDarkCosmos(canvas: Canvas, paint: Paint, result: AgeResult) {
        // Background gradient: #1a1a2e → #16213e
        val gradient = LinearGradient(
            0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
            Color.parseColor("#1a1a2e"), Color.parseColor("#16213e"),
            Shader.TileMode.CLAMP,
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
        paint.shader = null

        drawContent(canvas, paint, result, textColor = Color.WHITE, accentColor = Color.parseColor("#86efac"))
    }

    private fun drawMinimalLight(canvas: Canvas, paint: Paint, result: AgeResult) {
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
        drawContent(canvas, paint, result, textColor = Color.parseColor("#1c1917"), accentColor = Color.parseColor("#0f6e56"))
    }

    private fun drawFestiveIndia(canvas: Canvas, paint: Paint, result: AgeResult) {
        val gradient = LinearGradient(
            0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
            Color.parseColor("#FF9933"), Color.parseColor("#138808"),
            Shader.TileMode.CLAMP,
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
        paint.shader = null
        drawContent(canvas, paint, result, textColor = Color.WHITE, accentColor = Color.parseColor("#FFFFFF"))
    }

    // ---------------------------------------------------------------------------
    // Shared content layout
    // ---------------------------------------------------------------------------

    private fun drawContent(canvas: Canvas, paint: Paint, result: AgeResult, textColor: Int, accentColor: Int) {
        // Label
        paint.color = textColor; paint.alpha = 120; paint.textSize = 28f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("MY AGE TODAY", 60f, 80f, paint)

        // Primary age line — scale down if the text is wider than the canvas
        val ageText = "${result.years} yrs  ${result.months} mo  ${result.days} days"
        paint.alpha = 255; paint.textSize = 90f; paint.typeface = Typeface.DEFAULT_BOLD; paint.color = textColor
        val maxWidth = CARD_WIDTH - 120f
        val measured = paint.measureText(ageText)
        if (measured > maxWidth) paint.textSize = 90f * maxWidth / measured
        canvas.drawText(ageText, 60f, 175f, paint)

        // Born on
        paint.textSize = 32f; paint.typeface = Typeface.DEFAULT; paint.alpha = 160
        canvas.drawText(
            "Born ${result.dayOfWeekBorn.lowercase().replaceFirstChar { it.uppercase() }}, ${result.birthDate}",
            60f, 220f, paint,
        )

        // Stat cards row 1
        paint.alpha = 255
        drawStatCard(canvas, paint, 60f, 260f, "Total days", "${"%,d".format(result.totalDays)}", textColor, accentColor)
        drawStatCard(canvas, paint, 310f, 260f, "To birthday", "${result.daysToNextBirthday}d", textColor, accentColor)
        drawStatCard(canvas, paint, 560f, 260f, "Zodiac", result.westernZodiac.ifEmpty { "—" }, textColor, accentColor)
        drawStatCard(canvas, paint, 60f, 440f, "Rashi", result.rashi.ifEmpty { "—" }, textColor, accentColor)
    }

    private fun drawStatCard(
        canvas: Canvas, paint: Paint,
        x: Float, y: Float,
        label: String, value: String,
        textColor: Int, accentColor: Int,
    ) {
        // Card background
        paint.color = Color.argb(30, 255, 255, 255)
        canvas.drawRoundRect(RectF(x, y, x + 220f, y + 130f), 16f, 16f, paint)

        // Value
        paint.color = accentColor; paint.textSize = 40f; paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(value, x + 14f, y + 55f, paint)

        // Label
        paint.color = textColor; paint.alpha = 140; paint.textSize = 22f; paint.typeface = Typeface.DEFAULT
        canvas.drawText(label, x + 14f, y + 90f, paint)
        paint.alpha = 255
    }

    // ---------------------------------------------------------------------------
    // Milestone card layout
    // ---------------------------------------------------------------------------

    private fun drawMilestoneContent(
        canvas: Canvas, paint: Paint, milestone: Milestone,
        textColor: Int, accentColor: Int,
    ) {
        val formattedTarget = "%,d".format(milestone.targetDays)
        val formattedDate = milestone.date.format(MILESTONE_DATE_FMT)
        val prefix = if (milestone.isPast) "You turned" else "You'll turn"
        val daysText = when {
            milestone.daysAway == 0L -> "Today!"
            milestone.isPast -> "${abs(milestone.daysAway)} days ago"
            else -> "in ${milestone.daysAway} days"
        }

        // "MILESTONE" label — small, muted
        paint.color = textColor; paint.alpha = 120; paint.textSize = 28f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("MILESTONE", 60f, 75f, paint)

        // Prefix: "You'll turn" / "You turned"
        paint.alpha = 200; paint.textSize = 40f
        canvas.drawText(prefix, 60f, 145f, paint)

        // Main number line: "10,000 days old" — large accent
        paint.alpha = 255; paint.color = accentColor; paint.textSize = 72f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("$formattedTarget days old", 60f, 235f, paint)

        // Connector: "on"
        paint.color = textColor; paint.alpha = 200; paint.textSize = 40f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("on", 60f, 295f, paint)

        // Date — bold, large
        paint.alpha = 255; paint.textSize = 54f; paint.typeface = Typeface.DEFAULT_BOLD; paint.color = textColor
        canvas.drawText(formattedDate, 60f, 365f, paint)

        // Stat cards row
        paint.alpha = 255
        val countdownLabel = if (milestone.isPast) "reached" else "coming up"
        drawStatCard(canvas, paint, 60f, 415f, countdownLabel, daysText, textColor, accentColor)
        drawStatCard(canvas, paint, 310f, 415f, "milestone", "$formattedTarget days", textColor, accentColor)
    }

    private fun drawWatermark(canvas: Canvas, paint: Paint) {
        paint.color = Color.WHITE; paint.alpha = 60; paint.textSize = 22f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("Made with AgeReveal", CARD_WIDTH - 280f, CARD_HEIGHT - 25f, paint)
        paint.alpha = 255
    }

    // ---------------------------------------------------------------------------
    // File cache
    // ---------------------------------------------------------------------------

    private fun saveBitmapToCache(bitmap: Bitmap, fileName: String) = run {
        val cacheDir = File(context.cacheDir, "shared_images").also { it.mkdirs() }
        val file = File(cacheDir, fileName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        FileProvider.getUriForFile(context, "${context.packageName}$FILE_AUTHORITY_SUFFIX", file)
    }
}
