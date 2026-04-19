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
import com.willowvibe.agereveal.domain.CompatibilityResult
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
 *
 * Output format is always a square ([SQUARE_SIZE] × [SQUARE_SIZE]) bitmap so that the
 * image is never cropped by WhatsApp (16:9 preview), Instagram Stories (9:16), or any
 * other platform. The 900×600 content area is centred vertically within the square and
 * the theme background fills the remaining top/bottom margins (BUG-011).
 */
@Singleton
class ShareCardGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    enum class CardTheme { DARK_COSMOS, MINIMAL_LIGHT, FESTIVE_INDIA }

    private val sharingCard = AtomicBoolean(false)
    private val sharingMilestone = AtomicBoolean(false)

    companion object {
        /** Logical content width/height — all coordinate maths inside draw* functions uses these. */
        const val CARD_WIDTH = 900
        const val CARD_HEIGHT = 600
        /** Output bitmap is square; content is centred with (SQUARE_SIZE-CARD_HEIGHT)/2 top margin. */
        const val SQUARE_SIZE = CARD_WIDTH          // 900 × 900
        private val VERTICAL_PAD = (SQUARE_SIZE - CARD_HEIGHT) / 2  // 150 px top & bottom

        const val CACHE_FILE = "share_card.png"
        const val MILESTONE_CACHE_FILE = "milestone_card.png"
        const val COMPATIBILITY_CACHE_FILE = "compatibility_card.png"
        const val FILE_AUTHORITY_SUFFIX = ".fileprovider"
        private val MILESTONE_DATE_FMT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    }

    private val sharingCompatibility = AtomicBoolean(false)

    // ---------------------------------------------------------------------------
    // Public API — generate
    // ---------------------------------------------------------------------------

    /** Generate a square share card from [result] using the given [theme]. */
    fun generateBitmap(result: AgeResult, theme: CardTheme = CardTheme.DARK_COSMOS): Bitmap {
        // 1. Draw content at native 900×600
        val contentBmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (theme) {
            CardTheme.DARK_COSMOS -> drawDarkCosmos(Canvas(contentBmp), paint, result)
            CardTheme.MINIMAL_LIGHT -> drawMinimalLight(Canvas(contentBmp), paint, result)
            CardTheme.FESTIVE_INDIA -> drawFestiveIndia(Canvas(contentBmp), paint, result)
        }
        // 2. Embed in square with matching background
        return embedInSquare(contentBmp, theme, paint)
    }

    /** Generate a dedicated milestone share card (e.g. "You'll turn 10,000 days old on…"). */
    fun generateMilestoneBitmap(milestone: Milestone, theme: CardTheme = CardTheme.FESTIVE_INDIA): Bitmap {
        val contentBmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(contentBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (theme) {
            CardTheme.DARK_COSMOS -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawMilestoneContent(canvas, paint, milestone, Color.WHITE, Color.parseColor("#86efac"))
            }
            CardTheme.MINIMAL_LIGHT -> {
                paint.color = Color.WHITE
                canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
                drawMilestoneContent(canvas, paint, milestone,
                    Color.parseColor("#1c1917"), Color.parseColor("#0f6e56"))
            }
            CardTheme.FESTIVE_INDIA -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawMilestoneContent(canvas, paint, milestone, Color.WHITE, Color.parseColor("#FFD700"))
            }
        }
        return embedInSquare(contentBmp, theme, paint)
    }

    fun generateCompatibilityBitmap(result: CompatibilityResult, theme: CardTheme = CardTheme.DARK_COSMOS): Bitmap {
        val contentBmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(contentBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (theme) {
            CardTheme.DARK_COSMOS -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawCompatibilityContent(canvas, paint, result, Color.WHITE, Color.parseColor("#86efac"))
            }
            CardTheme.MINIMAL_LIGHT -> {
                paint.color = Color.WHITE
                canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
                drawCompatibilityContent(canvas, paint, result,
                    Color.parseColor("#1c1917"), Color.parseColor("#0f6e56"))
            }
            CardTheme.FESTIVE_INDIA -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawCompatibilityContent(canvas, paint, result, Color.WHITE, Color.parseColor("#FFD700"))
            }
        }
        return embedInSquare(contentBmp, theme, paint)
    }

    // ---------------------------------------------------------------------------
    // Public API — share
    // ---------------------------------------------------------------------------

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
            }
            // startActivity must run on the main thread; the caller may be on Dispatchers.IO.
            // clipData must be on the chooser (not the inner intent) so WhatsApp receives the
            // URI read permission grant.
            Handler(Looper.getMainLooper()).post {
                try {
                    val chooser = Intent.createChooser(intent, "Share your age")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (context is android.app.Activity) {
                        context.startActivity(chooser)
                    } else {
                        context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                } finally {
                    sharingCard.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingCard.set(false)
        }
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

    fun shareCompatibility(result: CompatibilityResult, theme: CardTheme = CardTheme.DARK_COSMOS) {
        if (!sharingCompatibility.compareAndSet(false, true)) return
        var bitmap: Bitmap? = null
        try {
            bitmap = generateCompatibilityBitmap(result, theme)
            val uri = saveBitmapToCache(bitmap, COMPATIBILITY_CACHE_FILE)
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
                    val chooser = Intent.createChooser(intent, "Share compatibility")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(chooser)
                } finally {
                    sharingCompatibility.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingCompatibility.set(false)
        }
    }

    // ---------------------------------------------------------------------------
    // Square-output helper (BUG-011)
    // ---------------------------------------------------------------------------

    /**
     * Embeds [contentBmp] (900×600) into a [SQUARE_SIZE]×[SQUARE_SIZE] output bitmap.
     * The theme background fills the full square so the top/bottom margins blend
     * seamlessly, then the content is drawn centred at y=[VERTICAL_PAD].
     * [contentBmp] is recycled after compositing.
     */
    private fun embedInSquare(contentBmp: Bitmap, theme: CardTheme, paint: Paint): Bitmap {
        val size = SQUARE_SIZE.toFloat()
        val outBmp = Bitmap.createBitmap(SQUARE_SIZE, SQUARE_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outBmp)
        // Full-square background
        drawThemeBackground(canvas, paint, size, size, theme)
        // Content centred vertically
        canvas.drawBitmap(contentBmp, 0f, VERTICAL_PAD.toFloat(), null)
        contentBmp.recycle()
        // Watermark at bottom of the square
        drawWatermark(canvas, paint)
        return outBmp
    }

    /** Draws the theme background over a [w]×[h] rect on [canvas]. */
    private fun drawThemeBackground(canvas: Canvas, paint: Paint, w: Float, h: Float, theme: CardTheme) {
        when (theme) {
            CardTheme.DARK_COSMOS -> {
                paint.shader = LinearGradient(0f, 0f, w, h,
                    Color.parseColor("#1a1a2e"), Color.parseColor("#16213e"), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null
            }
            CardTheme.MINIMAL_LIGHT -> {
                paint.color = Color.WHITE
                canvas.drawRect(0f, 0f, w, h, paint)
            }
            CardTheme.FESTIVE_INDIA -> {
                paint.shader = LinearGradient(0f, 0f, w, h,
                    Color.parseColor("#FF9933"), Color.parseColor("#138808"), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, w, h, paint)
                paint.shader = null
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Theme renderers (draw onto 900×600 content canvas)
    // ---------------------------------------------------------------------------

    private fun drawDarkCosmos(canvas: Canvas, paint: Paint, result: AgeResult) {
        drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), CardTheme.DARK_COSMOS)
        drawContent(canvas, paint, result, textColor = Color.WHITE, accentColor = Color.parseColor("#86efac"))
    }

    private fun drawMinimalLight(canvas: Canvas, paint: Paint, result: AgeResult) {
        drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), CardTheme.MINIMAL_LIGHT)
        drawContent(canvas, paint, result,
            textColor = Color.parseColor("#1c1917"),
            accentColor = Color.parseColor("#0f6e56"))
    }

    private fun drawFestiveIndia(canvas: Canvas, paint: Paint, result: AgeResult) {
        drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), CardTheme.FESTIVE_INDIA)
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

        paint.color = textColor; paint.alpha = 120; paint.textSize = 28f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("MILESTONE", 60f, 75f, paint)

        paint.alpha = 200; paint.textSize = 40f
        canvas.drawText(prefix, 60f, 145f, paint)

        paint.alpha = 255; paint.color = accentColor; paint.textSize = 72f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("$formattedTarget days old", 60f, 235f, paint)

        paint.color = textColor; paint.alpha = 200; paint.textSize = 40f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("on", 60f, 295f, paint)

        paint.alpha = 255; paint.textSize = 54f; paint.typeface = Typeface.DEFAULT_BOLD; paint.color = textColor
        canvas.drawText(formattedDate, 60f, 365f, paint)

        paint.alpha = 255
        val countdownLabel = if (milestone.isPast) "reached" else "coming up"
        drawStatCard(canvas, paint, 60f, 415f, countdownLabel, daysText, textColor, accentColor)
        drawStatCard(canvas, paint, 310f, 415f, "milestone", "$formattedTarget days", textColor, accentColor)
    }

    // ---------------------------------------------------------------------------
    // Compatibility card layout
    // ---------------------------------------------------------------------------

    private fun drawCompatibilityContent(
        canvas: Canvas, paint: Paint, result: CompatibilityResult,
        textColor: Int, accentColor: Int,
    ) {
        paint.color = textColor; paint.alpha = 120; paint.textSize = 28f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("COSMIC MATCH", 60f, 80f, paint)

        val scoreText = "${result.overallScore}%"
        paint.alpha = 255; paint.color = accentColor; paint.textSize = 110f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(scoreText, 60f, 195f, paint)

        paint.color = textColor; paint.alpha = 220; paint.textSize = 34f; paint.typeface = Typeface.DEFAULT
        canvas.drawText(result.headline, 60f, 245f, paint)

        paint.color = textColor; paint.alpha = 40; paint.strokeWidth = 1.5f
        canvas.drawLine(60f, 265f, CARD_WIDTH - 60f, 265f, paint)

        paint.alpha = 255
        drawCompatibilityRow(canvas, paint, 60f, 305f, "Western",
            result.personAWestern, result.personBWestern, textColor, accentColor)
        drawCompatibilityRow(canvas, paint, 60f, 355f, "Element",
            result.personAElement, result.personBElement, textColor, accentColor)
        drawCompatibilityRow(canvas, paint, 60f, 405f, "Chinese",
            result.personAChinese, result.personBChinese, textColor, accentColor)

        paint.alpha = 255
        drawStatCard(canvas, paint, 60f, 440f, "Western", "${result.westernScore}%", textColor, accentColor)
        drawStatCard(canvas, paint, 310f, 440f, "Chinese", "${result.chineseScore}%", textColor, accentColor)
    }

    private fun drawCompatibilityRow(
        canvas: Canvas, paint: Paint,
        x: Float, y: Float,
        label: String, valueA: String, valueB: String,
        textColor: Int, accentColor: Int,
    ) {
        paint.color = textColor; paint.alpha = 120; paint.textSize = 22f; paint.typeface = Typeface.DEFAULT
        canvas.drawText(label, x, y, paint)
        paint.alpha = 255; paint.color = accentColor; paint.textSize = 24f; paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("$valueA  ·  $valueB", x + 140f, y, paint)
    }

    // ---------------------------------------------------------------------------
    // Watermark
    // ---------------------------------------------------------------------------

    private fun drawWatermark(canvas: Canvas, paint: Paint) {
        // Position relative to the square output bottom edge
        paint.color = Color.WHITE; paint.alpha = 60; paint.textSize = 22f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("Made with AgeReveal", SQUARE_SIZE - 280f, SQUARE_SIZE - 25f, paint)
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
