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
import com.willowvibe.agereveal.data.model.BadgeDefinition
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
 *
 * Error handling: If sharing fails, the [onShareError] callback is invoked on the main thread.
 */
@Singleton
class ShareCardGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    enum class CardTheme { DARK_COSMOS, MINIMAL_LIGHT, FESTIVE_INDIA }

    private val sharingCard = AtomicBoolean(false)
    private val sharingMilestone = AtomicBoolean(false)
    private val sharingBadge = AtomicBoolean(false)
    private val sharingLifeStat = AtomicBoolean(false)
    private val sharingStory = AtomicBoolean(false)
    private val sharingTransparent = AtomicBoolean(false)

    // Error callback — invoked on main thread when sharing fails
    private var onShareError: ((Throwable) -> Unit)? = null
    private var onMilestoneShareError: ((Throwable) -> Unit)? = null
    private var onCompatibilityShareError: ((Throwable) -> Unit)? = null
    private var onBadgeShareError: ((Throwable) -> Unit)? = null
    private var onLifeStatShareError: ((Throwable) -> Unit)? = null
    private var onStoryShareError: ((Throwable) -> Unit)? = null
    private var onTransparentShareError: ((Throwable) -> Unit)? = null

    /** Register an error callback for share failures. */
    fun setShareErrorHandler(handler: ((Throwable) -> Unit)?) {
        onShareError = handler
    }

    /** Register an error callback for milestone share failures. */
    fun setMilestoneShareErrorHandler(handler: ((Throwable) -> Unit)?) {
        onMilestoneShareError = handler
    }

    /** Register an error callback for compatibility share failures. */
    fun setCompatibilityShareErrorHandler(handler: ((Throwable) -> Unit)?) {
        onCompatibilityShareError = handler
    }

    /** Register an error callback for badge share failures. */
    fun setBadgeShareErrorHandler(handler: ((Throwable) -> Unit)?) {
        onBadgeShareError = handler
    }

    /** Register an error callback for life-stat share failures. */
    fun setLifeStatShareErrorHandler(handler: ((Throwable) -> Unit)?) {
        onLifeStatShareError = handler
    }

    /** Register an error callback for story share failures. */
    fun setStoryShareErrorHandler(handler: ((Throwable) -> Unit)?) {
        onStoryShareError = handler
    }

    /** Register an error callback for transparent overlay share failures. */
    fun setTransparentShareErrorHandler(handler: ((Throwable) -> Unit)?) {
        onTransparentShareError = handler
    }

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
        const val BADGE_CACHE_FILE = "badge_card.png"
        const val LIFE_STAT_CACHE_FILE = "life_stat_card.png"
        const val STORY_CACHE_FILE = "story_card.png"
        const val TRANSPARENT_CACHE_FILE = "transparent_overlay.png"
        const val FORTUNE_CACHE_FILE = "fortune_card.png"
        const val FILE_AUTHORITY_SUFFIX = ".fileprovider"

        /** Story dimensions (9:16 portrait, 1080×1920). */
        const val STORY_WIDTH = 1080
        const val STORY_HEIGHT = 1920
        private val MILESTONE_DATE_FMT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

        /** Read the user-chosen accent color from mirrored SharedPreferences. */
        fun readAccentColor(context: Context): Int {
            val prefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
            return prefs.getInt("accent_color", 0xFF86EFAC.toInt())
        }
    }

    private val sharingCompatibility = AtomicBoolean(false)

    // ---------------------------------------------------------------------------
    // Public API — generate
    // ---------------------------------------------------------------------------

    /** Generate a square share card from [result] using the given [theme]. */
    fun generateBitmap(result: AgeResult, theme: CardTheme = CardTheme.DARK_COSMOS): Bitmap {
        val accent = readAccentColor(context)
        // 1. Draw content at native 900×600
        val contentBmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (theme) {
            CardTheme.DARK_COSMOS -> drawDarkCosmos(Canvas(contentBmp), paint, result, accent)
            CardTheme.MINIMAL_LIGHT -> drawMinimalLight(Canvas(contentBmp), paint, result, accent)
            CardTheme.FESTIVE_INDIA -> drawFestiveIndia(Canvas(contentBmp), paint, result)
        }
        // 2. Embed in square with matching background
        return embedInSquare(contentBmp, theme, paint)
    }

    /** Generate a dedicated milestone share card (e.g. "You'll turn 10,000 days old on…"). */
    fun generateMilestoneBitmap(milestone: Milestone, theme: CardTheme = CardTheme.FESTIVE_INDIA): Bitmap {
        val accent = readAccentColor(context)
        val contentBmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(contentBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (theme) {
            CardTheme.DARK_COSMOS -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawMilestoneContent(canvas, paint, milestone, Color.WHITE, accent)
            }
            CardTheme.MINIMAL_LIGHT -> {
                paint.color = Color.WHITE
                canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
                drawMilestoneContent(canvas, paint, milestone,
                    Color.parseColor("#1c1917"), accent)
            }
            CardTheme.FESTIVE_INDIA -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawMilestoneContent(canvas, paint, milestone, Color.WHITE, Color.parseColor("#FFD700"))
            }
        }
        return embedInSquare(contentBmp, theme, paint)
    }

    fun generateCompatibilityBitmap(result: CompatibilityResult, theme: CardTheme = CardTheme.DARK_COSMOS): Bitmap {
        val accent = readAccentColor(context)
        val contentBmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(contentBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (theme) {
            CardTheme.DARK_COSMOS -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawCompatibilityContent(canvas, paint, result, Color.WHITE, accent)
            }
            CardTheme.MINIMAL_LIGHT -> {
                paint.color = Color.WHITE
                canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
                drawCompatibilityContent(canvas, paint, result,
                    Color.parseColor("#1c1917"), accent)
            }
            CardTheme.FESTIVE_INDIA -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawCompatibilityContent(canvas, paint, result, Color.WHITE, Color.parseColor("#FFD700"))
            }
        }
        return embedInSquare(contentBmp, theme, paint)
    }

    /** Generate a shareable life-stat card. */
    fun generateLifeStatBitmap(
        label: String,
        value: String,
        emoji: String,
        theme: CardTheme = CardTheme.DARK_COSMOS,
    ): Bitmap {
        val accent = readAccentColor(context)
        val contentBmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(contentBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (theme) {
            CardTheme.DARK_COSMOS -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawLifeStatContent(canvas, paint, label, value, emoji, Color.WHITE, accent)
            }
            CardTheme.MINIMAL_LIGHT -> {
                paint.color = Color.WHITE
                canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
                drawLifeStatContent(canvas, paint, label, value, emoji, Color.parseColor("#1c1917"), accent)
            }
            CardTheme.FESTIVE_INDIA -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawLifeStatContent(canvas, paint, label, value, emoji, Color.WHITE, Color.parseColor("#FFD700"))
            }
        }
        return embedInSquare(contentBmp, theme, paint)
    }

    /** Generate a 9:16 portrait story card (1080×1920). */
    fun generateStoryBitmap(
        result: AgeResult,
        theme: CardTheme = CardTheme.DARK_COSMOS,
    ): Bitmap {
        val accent = readAccentColor(context)
        val bmp = Bitmap.createBitmap(STORY_WIDTH, STORY_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (theme) {
            CardTheme.DARK_COSMOS -> drawStoryDarkCosmos(canvas, paint, result, accent)
            CardTheme.MINIMAL_LIGHT -> drawStoryMinimalLight(canvas, paint, result, accent)
            CardTheme.FESTIVE_INDIA -> drawStoryFestiveIndia(canvas, paint, result)
        }
        drawStoryWatermark(canvas, paint)
        return bmp
    }

    /** Generate a shareable badge unlock card. */
    fun generateBadgeBitmap(
        badge: BadgeDefinition,
        unlockedAt: Long? = null,
        theme: CardTheme = CardTheme.DARK_COSMOS,
    ): Bitmap {
        val accent = readAccentColor(context)
        val contentBmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(contentBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (theme) {
            CardTheme.DARK_COSMOS -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawBadgeContent(canvas, paint, badge, unlockedAt, Color.WHITE, accent)
            }
            CardTheme.MINIMAL_LIGHT -> {
                paint.color = Color.WHITE
                canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
                drawBadgeContent(canvas, paint, badge, unlockedAt,
                    Color.parseColor("#1c1917"), accent)
            }
            CardTheme.FESTIVE_INDIA -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawBadgeContent(canvas, paint, badge, unlockedAt, Color.WHITE, Color.parseColor("#FFD700"))
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
                } catch (e: Exception) {
                    onShareError?.invoke(e)
                } finally {
                    sharingCard.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingCard.set(false)
            onShareError?.invoke(e)
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
            }
            Handler(Looper.getMainLooper()).post {
                try {
                    val chooser = Intent.createChooser(intent, "Share milestone")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (context is android.app.Activity) {
                        context.startActivity(chooser)
                    } else {
                        context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                } catch (e: Exception) {
                    onMilestoneShareError?.invoke(e)
                } finally {
                    sharingMilestone.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingMilestone.set(false)
            onMilestoneShareError?.invoke(e)
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
            }
            Handler(Looper.getMainLooper()).post {
                try {
                    val chooser = Intent.createChooser(intent, "Share compatibility")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (context is android.app.Activity) {
                        context.startActivity(chooser)
                    } else {
                        context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                } catch (e: Exception) {
                    onCompatibilityShareError?.invoke(e)
                } finally {
                    sharingCompatibility.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingCompatibility.set(false)
            onCompatibilityShareError?.invoke(e)
        }
    }

    /** Share a life-stat card via Android share sheet. */
    fun shareLifeStat(label: String, value: String, emoji: String, theme: CardTheme = CardTheme.DARK_COSMOS) {
        if (!sharingLifeStat.compareAndSet(false, true)) return
        var bitmap: Bitmap? = null
        try {
            bitmap = generateLifeStatBitmap(label, value, emoji, theme)
            val uri = saveBitmapToCache(bitmap, LIFE_STAT_CACHE_FILE)
            bitmap.recycle()
            bitmap = null
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Handler(Looper.getMainLooper()).post {
                try {
                    val chooser = Intent.createChooser(intent, "Share life stat")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (context is android.app.Activity) {
                        context.startActivity(chooser)
                    } else {
                        context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                } catch (e: Exception) {
                    onLifeStatShareError?.invoke(e)
                } finally {
                    sharingLifeStat.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingLifeStat.set(false)
            onLifeStatShareError?.invoke(e)
        }
    }

    /** Share a 9:16 portrait story card via Android share sheet. */
    fun shareStory(result: AgeResult, theme: CardTheme = CardTheme.DARK_COSMOS) {
        if (!sharingStory.compareAndSet(false, true)) return
        var bitmap: Bitmap? = null
        try {
            bitmap = generateStoryBitmap(result, theme)
            val uri = saveBitmapToCache(bitmap, STORY_CACHE_FILE)
            bitmap.recycle()
            bitmap = null
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Handler(Looper.getMainLooper()).post {
                try {
                    val chooser = Intent.createChooser(intent, "Share story")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (context is android.app.Activity) {
                        context.startActivity(chooser)
                    } else {
                        context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                } catch (e: Exception) {
                    onStoryShareError?.invoke(e)
                } finally {
                    sharingStory.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingStory.set(false)
            onStoryShareError?.invoke(e)
        }
    }

    /** Generate a 9:16 transparent overlay for green-screen use (TikTok/Reels). */
    fun generateTransparentOverlayBitmap(result: AgeResult): Bitmap {
        val bmp = Bitmap.createBitmap(STORY_WIDTH, STORY_HEIGHT, Bitmap.Config.ARGB_8888)
        // Start fully transparent
        bmp.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        drawTransparentOverlayContent(canvas, paint, result)
        return bmp
    }

    /** Share a transparent green-screen overlay via Android share sheet. */
    fun shareTransparentOverlay(result: AgeResult) {
        if (!sharingTransparent.compareAndSet(false, true)) return
        var bitmap: Bitmap? = null
        try {
            bitmap = generateTransparentOverlayBitmap(result)
            val uri = saveBitmapToCache(bitmap, TRANSPARENT_CACHE_FILE)
            bitmap.recycle()
            bitmap = null
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Handler(Looper.getMainLooper()).post {
                try {
                    val chooser = Intent.createChooser(intent, "Share green-screen overlay")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (context is android.app.Activity) {
                        context.startActivity(chooser)
                    } else {
                        context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                } catch (e: Exception) {
                    onTransparentShareError?.invoke(e)
                } finally {
                    sharingTransparent.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingTransparent.set(false)
            onTransparentShareError?.invoke(e)
        }
    }

    /** Generate a fortune card bitmap. */
    fun generateFortuneBitmap(
        headline: String,
        body: String,
        emoji: String,
        moonPhase: String,
        sunSign: String,
        stemBranch: String,
        luckyNumber: Int,
        luckyColor: String,
        theme: CardTheme = CardTheme.DARK_COSMOS,
    ): Bitmap {
        val contentBmp = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(contentBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val accent = readAccentColor(context)
        when (theme) {
            CardTheme.DARK_COSMOS -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawFortuneContent(canvas, paint, headline, body, emoji, moonPhase, sunSign, stemBranch, luckyNumber, luckyColor, Color.WHITE, accent)
            }
            CardTheme.MINIMAL_LIGHT -> {
                paint.color = Color.WHITE
                canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)
                drawFortuneContent(canvas, paint, headline, body, emoji, moonPhase, sunSign, stemBranch, luckyNumber, luckyColor, Color.parseColor("#1c1917"), accent)
            }
            CardTheme.FESTIVE_INDIA -> {
                drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), theme)
                drawFortuneContent(canvas, paint, headline, body, emoji, moonPhase, sunSign, stemBranch, luckyNumber, luckyColor, Color.WHITE, Color.parseColor("#FFD700"))
            }
        }
        return embedInSquare(contentBmp, theme, paint)
    }

    /** Share a cosmic fortune card via Android share sheet. */
    fun shareFortune(
        headline: String,
        body: String,
        emoji: String,
        moonPhase: String,
        sunSign: String,
        stemBranch: String,
        luckyNumber: Int,
        luckyColor: String,
        theme: CardTheme = CardTheme.DARK_COSMOS,
    ) {
        if (!sharingCard.compareAndSet(false, true)) return
        var bitmap: Bitmap? = null
        try {
            bitmap = generateFortuneBitmap(headline, body, emoji, moonPhase, sunSign, stemBranch, luckyNumber, luckyColor, theme)
            val uri = saveBitmapToCache(bitmap, FORTUNE_CACHE_FILE)
            bitmap.recycle()
            bitmap = null
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Handler(Looper.getMainLooper()).post {
                try {
                    val chooser = Intent.createChooser(intent, "Share your fortune")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (context is android.app.Activity) {
                        context.startActivity(chooser)
                    } else {
                        context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                } catch (e: Exception) {
                    onShareError?.invoke(e)
                } finally {
                    sharingCard.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingCard.set(false)
            onShareError?.invoke(e)
        }
    }

    /** Share a badge unlock card via Android share sheet. */
    fun shareBadge(badge: BadgeDefinition, unlockedAt: Long? = null, theme: CardTheme = CardTheme.DARK_COSMOS) {
        if (!sharingBadge.compareAndSet(false, true)) return
        var bitmap: Bitmap? = null
        try {
            bitmap = generateBadgeBitmap(badge, unlockedAt, theme)
            val uri = saveBitmapToCache(bitmap, BADGE_CACHE_FILE)
            bitmap.recycle()
            bitmap = null
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Handler(Looper.getMainLooper()).post {
                try {
                    val chooser = Intent.createChooser(intent, "Share badge")
                    chooser.clipData = ClipData.newRawUri("", uri)
                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (context is android.app.Activity) {
                        context.startActivity(chooser)
                    } else {
                        context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                } catch (e: Exception) {
                    onBadgeShareError?.invoke(e)
                } finally {
                    sharingBadge.set(false)
                }
            }
        } catch (e: Exception) {
            bitmap?.recycle()
            sharingBadge.set(false)
            onBadgeShareError?.invoke(e)
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

    private fun drawDarkCosmos(canvas: Canvas, paint: Paint, result: AgeResult, accent: Int) {
        drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), CardTheme.DARK_COSMOS)
        drawContent(canvas, paint, result, textColor = Color.WHITE, accentColor = accent)
    }

    private fun drawMinimalLight(canvas: Canvas, paint: Paint, result: AgeResult, accent: Int) {
        drawThemeBackground(canvas, paint, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), CardTheme.MINIMAL_LIGHT)
        drawContent(canvas, paint, result,
            textColor = Color.parseColor("#1c1917"),
            accentColor = accent)
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
    // Fortune card layout
    // ---------------------------------------------------------------------------

    private fun drawFortuneContent(
        canvas: Canvas, paint: Paint,
        headline: String, body: String, emoji: String,
        moonPhase: String, sunSign: String, stemBranch: String,
        luckyNumber: Int, luckyColor: String,
        textColor: Int, accentColor: Int,
    ) {
        paint.color = textColor; paint.alpha = 120; paint.textSize = 28f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("DAILY COSMIC FORTUNE", 60f, 75f, paint)

        paint.alpha = 255; paint.textSize = 80f; paint.typeface = Typeface.DEFAULT_BOLD
        val emojiWidth = paint.measureText(emoji)
        canvas.drawText(emoji, (CARD_WIDTH - emojiWidth) / 2f, 190f, paint)

        paint.color = accentColor; paint.textSize = 40f
        val headWidth = paint.measureText(headline)
        val headSize = if (headWidth > CARD_WIDTH - 120f) 40f * (CARD_WIDTH - 120f) / headWidth else 40f
        paint.textSize = headSize
        val headW = paint.measureText(headline)
        canvas.drawText(headline, (CARD_WIDTH - headW) / 2f, 260f, paint)

        paint.color = textColor; paint.alpha = 200; paint.textSize = 24f; paint.typeface = Typeface.DEFAULT
        val words = body.split(" ")
        val lines = mutableListOf<String>()
        var current = ""
        words.forEach { word ->
            if ((current + " " + word).length < 48) {
                current = if (current.isEmpty()) word else "$current $word"
            } else {
                lines.add(current)
                current = word
            }
        }
        if (current.isNotEmpty()) lines.add(current)
        var y = 310f
        lines.take(5).forEach { line ->
            canvas.drawText(line, 60f, y, paint)
            y += 36f
        }

        paint.alpha = 255
        drawStatCard(canvas, paint, 60f, 490f, "Moon", moonPhase, textColor, accentColor)
        drawStatCard(canvas, paint, 310f, 490f, "Sun Sign", sunSign, textColor, accentColor)
        drawStatCard(canvas, paint, 560f, 490f, "Stem-Branch", stemBranch, textColor, accentColor)
        drawStatCard(canvas, paint, 60f, 440f, "Lucky #", luckyNumber.toString(), textColor, accentColor)
        drawStatCard(canvas, paint, 310f, 440f, "Lucky Color", luckyColor, textColor, accentColor)
    }

    // ---------------------------------------------------------------------------
    // Badge card layout
    // ---------------------------------------------------------------------------

    private fun drawBadgeContent(
        canvas: Canvas, paint: Paint,
        badge: BadgeDefinition, unlockedAt: Long?,
        textColor: Int, accentColor: Int,
    ) {
        paint.color = textColor; paint.alpha = 120; paint.textSize = 28f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("BADGE UNLOCKED", 60f, 75f, paint)

        paint.alpha = 255; paint.textSize = 120f; paint.typeface = Typeface.DEFAULT_BOLD
        val emojiWidth = paint.measureText(badge.iconEmoji)
        canvas.drawText(badge.iconEmoji, (CARD_WIDTH - emojiWidth) / 2f, 220f, paint)

        paint.color = accentColor; paint.textSize = 52f
        val titleWidth = paint.measureText(badge.title)
        canvas.drawText(badge.title, (CARD_WIDTH - titleWidth) / 2f, 300f, paint)

        paint.color = textColor; paint.alpha = 200; paint.textSize = 28f; paint.typeface = Typeface.DEFAULT
        val descLines = badge.description.chunked(40)
        var descY = 350f
        descLines.take(2).forEach { line ->
            canvas.drawText(line, 60f, descY, paint)
            descY += 40f
        }

        paint.alpha = 255
        drawStatCard(canvas, paint, 60f, 430f, "rarity", badge.rarity.name, textColor, accentColor)
        if (unlockedAt != null) {
            val dateStr = java.time.Instant.ofEpochMilli(unlockedAt)
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"))
            drawStatCard(canvas, paint, 310f, 430f, "unlocked", dateStr, textColor, accentColor)
        }
    }

    // ---------------------------------------------------------------------------
    // Life-stat card layout
    // ---------------------------------------------------------------------------

    private fun drawLifeStatContent(
        canvas: Canvas, paint: Paint,
        label: String, value: String, emoji: String,
        textColor: Int, accentColor: Int,
    ) {
        paint.color = textColor; paint.alpha = 120; paint.textSize = 28f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("LIFE STAT", 60f, 75f, paint)

        paint.alpha = 255; paint.textSize = 120f; paint.typeface = Typeface.DEFAULT_BOLD
        val emojiWidth = paint.measureText(emoji)
        canvas.drawText(emoji, (CARD_WIDTH - emojiWidth) / 2f, 220f, paint)

        paint.color = accentColor; paint.textSize = 72f
        val valueWidth = paint.measureText(value)
        canvas.drawText(value, (CARD_WIDTH - valueWidth) / 2f, 340f, paint)

        paint.color = textColor; paint.alpha = 200; paint.textSize = 32f; paint.typeface = Typeface.DEFAULT
        val labelWidth = paint.measureText(label)
        canvas.drawText(label, (CARD_WIDTH - labelWidth) / 2f, 400f, paint)
        paint.alpha = 255
    }

    // ---------------------------------------------------------------------------
    // Story card renderers (9:16 portrait)
    // ---------------------------------------------------------------------------

    private fun drawStoryDarkCosmos(canvas: Canvas, paint: Paint, result: AgeResult, accent: Int) {
        drawThemeBackground(canvas, paint, STORY_WIDTH.toFloat(), STORY_HEIGHT.toFloat(), CardTheme.DARK_COSMOS)
        drawStoryContent(canvas, paint, result, Color.WHITE, accent)
    }

    private fun drawStoryMinimalLight(canvas: Canvas, paint: Paint, result: AgeResult, accent: Int) {
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, STORY_WIDTH.toFloat(), STORY_HEIGHT.toFloat(), paint)
        drawStoryContent(canvas, paint, result, Color.parseColor("#1c1917"), accent)
    }

    private fun drawStoryFestiveIndia(canvas: Canvas, paint: Paint, result: AgeResult) {
        drawThemeBackground(canvas, paint, STORY_WIDTH.toFloat(), STORY_HEIGHT.toFloat(), CardTheme.FESTIVE_INDIA)
        drawStoryContent(canvas, paint, result, Color.WHITE, Color.parseColor("#FFD700"))
    }

    private fun drawStoryContent(
        canvas: Canvas, paint: Paint, result: AgeResult,
        textColor: Int, accentColor: Int,
    ) {
        // Label — placed below the 250px Instagram top safe zone
        paint.color = textColor; paint.alpha = 120; paint.textSize = 36f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("MY AGE TODAY", 80f, 280f, paint)

        // Primary age line — large and centered
        val ageText = "${result.years} yrs  ${result.months} mo  ${result.days} days"
        paint.alpha = 255; paint.textSize = 110f; paint.typeface = Typeface.DEFAULT_BOLD; paint.color = textColor
        val maxWidth = STORY_WIDTH - 160f
        val measured = paint.measureText(ageText)
        if (measured > maxWidth) paint.textSize = 110f * maxWidth / measured
        canvas.drawText(ageText, 80f, 420f, paint)

        // Born on
        paint.textSize = 40f; paint.typeface = Typeface.DEFAULT; paint.alpha = 160
        canvas.drawText(
            "Born ${result.dayOfWeekBorn.lowercase().replaceFirstChar { it.uppercase() }}, ${result.birthDate}",
            80f, 490f, paint,
        )

        paint.alpha = 255
        // Stat cards — 2×2 grid, scaled for story
        drawStoryStatCard(canvas, paint, 80f, 560f, "Total days", "${"%,d".format(result.totalDays)}", textColor, accentColor)
        drawStoryStatCard(canvas, paint, 570f, 560f, "To birthday", "${result.daysToNextBirthday}d", textColor, accentColor)
        drawStoryStatCard(canvas, paint, 80f, 800f, "Zodiac", result.westernZodiac.ifEmpty { "—" }, textColor, accentColor)
        drawStoryStatCard(canvas, paint, 570f, 800f, "Rashi", result.rashi.ifEmpty { "—" }, textColor, accentColor)

        // Heartbeats (if unlocked)
        if (result.estimatedHeartbeats > 0) {
            paint.color = accentColor; paint.textSize = 72f; paint.typeface = Typeface.DEFAULT_BOLD; paint.alpha = 255
            val hbText = formatShareHeartbeats(result.estimatedHeartbeats)
            val hbWidth = paint.measureText(hbText)
            canvas.drawText(hbText, (STORY_WIDTH - hbWidth) / 2f, 1120f, paint)
            paint.color = textColor; paint.alpha = 160; paint.textSize = 36f; paint.typeface = Typeface.DEFAULT
            val sub = "heartbeats and counting"
            canvas.drawText(sub, (STORY_WIDTH - paint.measureText(sub)) / 2f, 1180f, paint)
        }
    }

    private fun drawStoryStatCard(
        canvas: Canvas, paint: Paint,
        x: Float, y: Float,
        label: String, value: String,
        textColor: Int, accentColor: Int,
    ) {
        paint.color = Color.argb(30, 255, 255, 255)
        canvas.drawRoundRect(RectF(x, y, x + 430f, y + 180f), 20f, 20f, paint)
        paint.color = accentColor; paint.textSize = 56f; paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(value, x + 20f, y + 75f, paint)
        paint.color = textColor; paint.alpha = 140; paint.textSize = 28f; paint.typeface = Typeface.DEFAULT
        canvas.drawText(label, x + 20f, y + 130f, paint)
        paint.alpha = 255
    }

    private fun formatShareHeartbeats(n: Long): String = when {
        n >= 1_000_000_000 -> "%,.2f B".format(n / 1_000_000_000.0)
        n >= 1_000_000 -> "%,.1f M".format(n / 1_000_000.0)
        else -> "%,d".format(n)
    }

    private fun drawStoryWatermark(canvas: Canvas, paint: Paint) {
        paint.color = Color.WHITE; paint.alpha = 60; paint.textSize = 28f; paint.typeface = Typeface.DEFAULT
        canvas.drawText("Made with AgeReveal", STORY_WIDTH - 340f, STORY_HEIGHT - 60f, paint)
        paint.alpha = 255
    }

    // ---------------------------------------------------------------------------
    // Transparent overlay (green-screen) content
    // ---------------------------------------------------------------------------

    private fun drawTransparentOverlayContent(canvas: Canvas, paint: Paint, result: AgeResult) {
        val textColor = Color.WHITE
        val outlineColor = Color.BLACK

        // Helper to draw text with dark outline for visibility on any background
        fun drawOutlinedText(text: String, x: Float, y: Float, size: Float, bold: Boolean = false) {
            paint.textSize = size
            paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            // Outline
            paint.color = outlineColor; paint.alpha = 200
            for (dx in listOf(-3f, 3f, 0f, 0f)) {
                for (dy in listOf(0f, 0f, -3f, 3f)) {
                    canvas.drawText(text, x + dx, y + dy, paint)
                }
            }
            // Fill
            paint.color = textColor; paint.alpha = 255
            canvas.drawText(text, x, y, paint)
        }

        // Label
        drawOutlinedText("MY AGE TODAY", 80f, 280f, 36f)

        // Primary age line
        val ageText = "${result.years} yrs  ${result.months} mo  ${result.days} days"
        paint.textSize = 110f; paint.typeface = Typeface.DEFAULT_BOLD
        val maxWidth = STORY_WIDTH - 160f
        val measured = paint.measureText(ageText)
        val size = if (measured > maxWidth) 110f * maxWidth / measured else 110f
        drawOutlinedText(ageText, 80f, 420f, size, bold = true)

        // Born on
        drawOutlinedText(
            "Born ${result.dayOfWeekBorn.lowercase().replaceFirstChar { it.uppercase() }}, ${result.birthDate}",
            80f, 490f, 40f,
        )

        // Stats
        drawOutlinedText("Total days", 80f, 600f, 28f)
        drawOutlinedText("%,d".format(result.totalDays), 80f, 660f, 72f, bold = true)

        drawOutlinedText("Zodiac", 570f, 600f, 28f)
        drawOutlinedText(result.westernZodiac.ifEmpty { "—" }, 570f, 660f, 56f, bold = true)

        drawOutlinedText("Rashi", 80f, 780f, 28f)
        drawOutlinedText(result.rashi.ifEmpty { "—" }, 80f, 840f, 56f, bold = true)

        drawOutlinedText("To birthday", 570f, 780f, 28f)
        drawOutlinedText("${result.daysToNextBirthday}d", 570f, 840f, 56f, bold = true)

        // Heartbeats
        if (result.estimatedHeartbeats > 0) {
            val hbText = formatShareHeartbeats(result.estimatedHeartbeats)
            drawOutlinedText(hbText, 80f, 980f, 80f, bold = true)
            drawOutlinedText("heartbeats and counting", 80f, 1040f, 36f)
        }

        // Watermark
        drawOutlinedText("Made with AgeReveal", STORY_WIDTH - 340f, STORY_HEIGHT - 60f, 28f)
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
