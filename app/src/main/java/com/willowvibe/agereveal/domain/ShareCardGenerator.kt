package com.willowvibe.agereveal.domain

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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

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

    companion object {
        const val CARD_WIDTH = 900
        const val CARD_HEIGHT = 600
        const val CACHE_FILE = "share_card.png"
        const val FILE_AUTHORITY_SUFFIX = ".fileprovider"
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
        val bitmap = generateBitmap(result, theme)
        val uri = saveBitmapToCache(bitmap)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share your age"))
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

        // Primary age line
        paint.alpha = 255; paint.textSize = 90f; paint.typeface = Typeface.DEFAULT_BOLD; paint.color = textColor
        canvas.drawText("${result.years} yrs  ${result.months} mo  ${result.days} days", 60f, 175f, paint)

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

    private fun drawWatermark(canvas: Canvas, paint: Paint) {
        paint.color = Color.WHITE; paint.alpha = 60; paint.textSize = 22f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("Made with AgeReveal", CARD_WIDTH - 280f, CARD_HEIGHT - 25f, paint)
        paint.alpha = 255
    }

    // ---------------------------------------------------------------------------
    // File cache
    // ---------------------------------------------------------------------------

    private fun saveBitmapToCache(bitmap: Bitmap) = run {
        val cacheDir = File(context.cacheDir, "shared_images").also { it.mkdirs() }
        val file = File(cacheDir, CACHE_FILE)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        FileProvider.getUriForFile(context, "${context.packageName}$FILE_AUTHORITY_SUFFIX", file)
    }
}
