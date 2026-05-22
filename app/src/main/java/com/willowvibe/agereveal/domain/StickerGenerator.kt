package com.willowvibe.agereveal.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Generates 512×512 PNG cosmic-themed stickers for WhatsApp sticker pack export.
 * Each sticker is drawn procedurally via Android Canvas — no external assets needed.
 */
@Singleton
class StickerGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val size = 512
    private val stickerDir = File(context.filesDir, "whatsapp_stickers")

    data class StickerDef(
        val id: String,
        val name: String,
        val emojis: List<String>,
    )

    val stickers = listOf(
        StickerDef("starry_night", "Starry Night", listOf("✨", "🌟", "🌙")),
        StickerDef("crescent_moon", "Crescent Moon", listOf("🌙", "✨")),
        StickerDef("sun_burst", "Sun Burst", listOf("☀️", "💫")),
        StickerDef("ringed_planet", "Ringed Planet", listOf("🪐", "💫")),
        StickerDef("galaxy_swirl", "Galaxy Swirl", listOf("🌌", "✨")),
        StickerDef("zodiac_wheel", "Zodiac Wheel", listOf("♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐", "♑", "♒", "♓")),
        StickerDef("comet", "Comet", listOf("☄️", "💫")),
        StickerDef("constellation", "Constellation", listOf("⭐", "🔭")),
        StickerDef("eclipse", "Eclipse", listOf("🌑", "☀️")),
        StickerDef("shooting_star", "Shooting Star", listOf("🌠", "✨")),
        StickerDef("nebula", "Nebula", listOf("🌌", "💜")),
        StickerDef("cosmic_heart", "Cosmic Heart", listOf("💜", "✨", "🪐")),
    )

    /** Ensures all stickers are generated on disk. Returns the list of generated files. */
    fun ensureStickersGenerated(): List<File> {
        if (!stickerDir.exists()) stickerDir.mkdirs()
        return stickers.map { def ->
            val file = File(stickerDir, "${def.id}.png")
            if (!file.exists()) {
                val bitmap = drawSticker(def.id)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                bitmap.recycle()
            }
            file
        }
    }

    /** Generate the tray icon (preview image for the sticker pack picker). */
    fun generateTrayIcon(): File {
        val file = File(stickerDir, "tray_icon.png")
        if (file.exists()) return file
        val bitmap = drawSticker("galaxy_swirl")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file
    }

    fun getStickerFile(id: String): File? {
        val file = File(stickerDir, "$id.png")
        return if (file.exists()) file else null
    }

    fun getTrayIconFile(): File? {
        val file = File(stickerDir, "tray_icon.png")
        return if (file.exists()) file else null
    }

    // ── Drawing routines ───────────────────────────────────────────────────

    private fun drawSticker(id: String): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        when (id) {
            "starry_night" -> drawStarryNight(canvas)
            "crescent_moon" -> drawCrescentMoon(canvas)
            "sun_burst" -> drawSunBurst(canvas)
            "ringed_planet" -> drawRingedPlanet(canvas)
            "galaxy_swirl" -> drawGalaxySwirl(canvas)
            "zodiac_wheel" -> drawZodiacWheel(canvas)
            "comet" -> drawComet(canvas)
            "constellation" -> drawConstellation(canvas)
            "eclipse" -> drawEclipse(canvas)
            "shooting_star" -> drawShootingStar(canvas)
            "nebula" -> drawNebula(canvas)
            "cosmic_heart" -> drawCosmicHeart(canvas)
        }
        return bmp
    }

    private fun drawStarryNight(canvas: Canvas) {
        val bg = Paint().apply { shader = RadialGradient(256f, 256f, 400f, intArrayOf(0xFF1a1a3e.toInt(), 0xFF0a0a1a.toInt(), 0xFF000011.toInt()), null, Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, 512f, 512f, bg)
        val star = Paint().apply { color = 0xFFFFFFFF.toInt(); isAntiAlias = true }
        val rng = Random(42)
        repeat(60) {
            val x = rng.nextFloat() * 512f; val y = rng.nextFloat() * 512f
            val r = 1f + rng.nextFloat() * 3f
            canvas.drawCircle(x, y, r, star)
        }
        // Moon
        val moon = Paint().apply { color = 0xFFFFF3C4.toInt(); isAntiAlias = true }
        canvas.drawCircle(380f, 120f, 50f, moon)
        val mask = Paint().apply { color = 0xFF1a1a3e.toInt(); isAntiAlias = true }
        canvas.drawCircle(400f, 110f, 42f, mask)
    }

    private fun drawCrescentMoon(canvas: Canvas) {
        canvas.drawColor(0xFF0d0d2b.toInt())
        val glow = Paint().apply { shader = RadialGradient(256f, 256f, 300f, intArrayOf(0x44FFF3C4, 0x00000000), null, Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, 512f, 512f, glow)
        val moon = Paint().apply { color = 0xFFFFF3C4.toInt(); isAntiAlias = true }
        canvas.drawCircle(220f, 256f, 120f, moon)
        val mask = Paint().apply { color = 0xFF0d0d2b.toInt(); isAntiAlias = true }
        canvas.drawCircle(280f, 220f, 105f, mask)
    }

    private fun drawSunBurst(canvas: Canvas) {
        canvas.drawColor(0xFF1a0a00.toInt())
        val glow = Paint().apply { shader = RadialGradient(256f, 256f, 250f, intArrayOf(0xFFFF8C00.toInt(), 0x66FF4500, 0x001a0a00), null, Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, 512f, 512f, glow)
        val sun = Paint().apply { color = 0xFFFFD700.toInt(); isAntiAlias = true }
        canvas.drawCircle(256f, 256f, 80f, sun)
        val ray = Paint().apply { color = 0x88FFD700.toInt(); isAntiAlias = true; strokeWidth = 4f; style = Paint.Style.STROKE }
        for (i in 0..11) {
            val angle = Math.toRadians(i * 30.0)
            canvas.drawLine(256f, 256f, 256f + 220f * cos(angle).toFloat(), 256f + 220f * sin(angle).toFloat(), ray)
        }
    }

    private fun drawRingedPlanet(canvas: Canvas) {
        canvas.drawColor(0xFF050515.toInt())
        val planet = Paint().apply { shader = RadialGradient(200f, 256f, 90f, intArrayOf(0xFFE8A87C.toInt(), 0xFFC4714B.toInt(), 0xFF8B4513.toInt()), null, Shader.TileMode.CLAMP); isAntiAlias = true }
        canvas.drawCircle(200f, 240f, 80f, planet)
        val ring = Paint().apply { color = 0x88C8A87C.toInt(); style = Paint.Style.STROKE; strokeWidth = 14f; isAntiAlias = true }
        canvas.save()
        canvas.scale(1f, 0.3f, 200f, 240f)
        canvas.drawCircle(200f, 240f, 130f, ring)
        canvas.restore()
        // Stars
        val star = Paint().apply { color = 0xFFFFFFFF.toInt(); isAntiAlias = true }
        val rng = Random(99)
        repeat(30) { canvas.drawCircle(rng.nextFloat() * 512f, rng.nextFloat() * 512f, 1f + rng.nextFloat() * 2f, star) }
    }

    private fun drawGalaxySwirl(canvas: Canvas) {
        canvas.drawColor(0xFF05051a.toInt())
        val rng = Random(77)
        val paint = Paint().apply { isAntiAlias = true; alpha = 180 }
        val colors = intArrayOf(0xFF7B2FBE.toInt(), 0xFF4A90D9.toInt(), 0xFFE94E77.toInt(), 0xFF2ECC71.toInt())
        for (i in 0..199) {
            val t = i / 200f; val angle = t * 8 * Math.PI
            val r = t * 280f
            paint.color = colors[i % 4]
            paint.alpha = (120 + rng.nextInt(80)).coerceAtMost(255)
            canvas.drawCircle(256f + r * cos(angle).toFloat(), 256f + r * sin(angle).toFloat(), 4f + rng.nextFloat() * 6f, paint)
        }
    }

    private fun drawZodiacWheel(canvas: Canvas) {
        canvas.drawColor(0xFF0a0a1a.toInt())
        val inner = Paint().apply { shader = RadialGradient(256f, 256f, 200f, intArrayOf(0xFF2d1b4e.toInt(), 0xFF0a0a1a.toInt()), null, Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, 512f, 512f, inner)
        val rim = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 2f; color = 0x88FFFFFF.toInt(); isAntiAlias = true }
        canvas.drawCircle(256f, 256f, 200f, rim)
        canvas.drawCircle(256f, 256f, 160f, rim)
        // 12 segments
        val line = Paint().apply { color = 0x44FFFFFF.toInt(); strokeWidth = 1f; isAntiAlias = true }
        for (i in 0..11) {
            val a = Math.toRadians(i * 30.0)
            canvas.drawLine(256f, 256f, 256f + 200f * cos(a).toFloat(), 256f + 200f * sin(a).toFloat(), line)
        }
        // Center dot
        val dot = Paint().apply { color = 0xFFFFD700.toInt(); isAntiAlias = true }
        canvas.drawCircle(256f, 256f, 8f, dot)
    }

    private fun drawComet(canvas: Canvas) {
        canvas.drawColor(0xFF05051a.toInt())
        val tail = Paint().apply { shader = RadialGradient(80f, 100f, 350f, intArrayOf(0xFFFFFFFF.toInt(), 0x44AACCFF.toInt(), 0x0005051a.toInt()), null, Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, 512f, 512f, tail)
        val head = Paint().apply { color = 0xFFFFFFFF.toInt(); isAntiAlias = true }
        canvas.drawCircle(100f, 110f, 24f, head)
        // Trail
        val trail = Paint().apply { isAntiAlias = true; alpha = 150 }
        val trailColors = intArrayOf(0xFFFFFFFF.toInt(), 0x88AACCFF.toInt(), 0x004488FF.toInt())
        for (i in 0..60) {
            val t = i / 60f; trail.alpha = (200 * (1 - t)).toInt()
            trail.color = trailColors[(i * trailColors.size / 60).coerceAtMost(trailColors.size - 1)]
            canvas.drawCircle(100f + t * 300f, 110f - t * 60f, (20 * (1 - t)).toFloat() + 2f, trail)
        }
    }

    private fun drawConstellation(canvas: Canvas) {
        canvas.drawColor(0xFF05051a.toInt())
        val pts = listOf(100f to 120f, 180f to 80f, 260f to 140f, 340f to 100f, 400f to 200f, 300f to 280f, 200f to 320f, 140f to 240f)
        // Lines
        val line = Paint().apply { color = 0x4488AADD.toInt(); strokeWidth = 1.5f; isAntiAlias = true }
        for (i in 0 until pts.size - 1) {
            canvas.drawLine(pts[i].first, pts[i].second, pts[i + 1].first, pts[i + 1].second, line)
        }
        // Stars
        val star = Paint().apply { color = 0xFFFFFFFF.toInt(); isAntiAlias = true }
        pts.forEach { (x, y) ->
            canvas.drawCircle(x, y, 5f, star)
            val glow = Paint().apply { shader = RadialGradient(x, y, 12f, intArrayOf(0x44FFFFFF, 0x00000000), null, Shader.TileMode.CLAMP) }
            canvas.drawCircle(x, y, 12f, glow)
        }
    }

    private fun drawEclipse(canvas: Canvas) {
        canvas.drawColor(0xFF050510.toInt())
        val corona = Paint().apply { shader = RadialGradient(256f, 256f, 140f, intArrayOf(0xFFFFD700.toInt(), 0x44FF8C00, 0x00050510.toInt()), null, Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, 512f, 512f, corona)
        val dark = Paint().apply { color = 0xFF050510.toInt(); isAntiAlias = true }
        canvas.drawCircle(256f, 256f, 85f, dark)
        val rim = Paint().apply { color = 0x88FFD700.toInt(); style = Paint.Style.STROKE; strokeWidth = 3f; isAntiAlias = true }
        canvas.drawCircle(256f, 256f, 90f, rim)
    }

    private fun drawShootingStar(canvas: Canvas) {
        canvas.drawColor(0xFF0a0a2a.toInt())
        val rng = Random(13)
        val star = Paint().apply { color = 0xFFFFFFFF.toInt(); isAntiAlias = true }
        repeat(40) { canvas.drawCircle(rng.nextFloat() * 512f, rng.nextFloat() * 512f, 1f + rng.nextFloat() * 2f, star) }
        // Shooting star
        val head = Paint().apply { color = 0xFFFFFFFF.toInt(); isAntiAlias = true }
        canvas.drawCircle(350f, 180f, 6f, head)
        val trail = Paint().apply { isAntiAlias = true; alpha = 180 }
        for (i in 0..30) {
            val t = i / 30f; trail.alpha = (180 * (1 - t)).toInt()
            trail.color = 0xFFFFFFFF.toInt()
            canvas.drawCircle(350f - t * 170f, 180f - t * 80f, (5 * (1 - t)).toFloat() + 1f, trail)
        }
    }

    private fun drawNebula(canvas: Canvas) {
        canvas.drawColor(0xFF05051a.toInt())
        val colors = intArrayOf(0x667B2FBE.toInt(), 0x664A90D9.toInt(), 0x66E94E77.toInt())
        val rng = Random(55)
        val paint = Paint().apply { isAntiAlias = true; alpha = 80 }
        repeat(150) {
            paint.color = colors[rng.nextInt(3)]
            paint.alpha = 40 + rng.nextInt(80)
            paint.shader = RadialGradient(rng.nextFloat() * 512f, rng.nextFloat() * 512f, 30f + rng.nextFloat() * 80f,
                intArrayOf(paint.color, 0x00000000), null, Shader.TileMode.CLAMP)
            canvas.drawCircle(rng.nextFloat() * 512f, rng.nextFloat() * 512f, 30f + rng.nextFloat() * 60f, paint)
        }
        val star = Paint().apply { color = 0xFFFFFFFF.toInt(); isAntiAlias = true }
        repeat(20) { canvas.drawCircle(rng.nextFloat() * 512f, rng.nextFloat() * 512f, 1f + rng.nextFloat() * 2f, star) }
    }

    private fun drawCosmicHeart(canvas: Canvas) {
        val bg = Paint().apply { shader = RadialGradient(256f, 256f, 400f, intArrayOf(0xFF2d1b4e.toInt(), 0xFF0a0a1a.toInt(), 0xFF000011.toInt()), null, Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, 512f, 512f, bg)
        val heart = Paint().apply {
            shader = SweepGradient(256f, 270f, intArrayOf(0xFFE94E77.toInt(), 0xFF7B2FBE.toInt(), 0xFF4A90D9.toInt(), 0xFFE94E77.toInt()), null)
            isAntiAlias = true
        }
        val path = Path().apply {
            moveTo(256f, 420f)
            cubicTo(60f, 300f, 60f, 100f, 256f, 180f)
            cubicTo(452f, 100f, 452f, 300f, 256f, 420f)
            close()
        }
        canvas.drawPath(path, heart)
        // Stars
        val star = Paint().apply { color = 0xFFFFFFFF.toInt(); isAntiAlias = true }
        val rng = Random(88)
        repeat(25) { canvas.drawCircle(rng.nextFloat() * 512f, rng.nextFloat() * 512f, 1f + rng.nextFloat() * 2f, star) }
    }
}
