package io.github.wotworth.weather

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import io.github.wotworth.weather.WeatherTokens.Festive
import io.github.wotworth.weather.WeatherTokens.Seasons
import kotlin.random.Random

/**
 * WX-6.11, WX-6.12: a day's top block. The condition's gradient tinted for the season, a soft
 * glow from one corner (the season's light), and a few faint Fluent motifs: snowflakes, eggs and
 * clover, sparkles, leaves, raindrops. Winter and snow add a light, still snowfall of soft dots.
 * Text contrast is checked in PaletteTest.
 */
class CardDrawable(private val c: Context, private val look: CardLook) : Drawable() {
    private val density = c.resources.displayMetrics.density
    private val base = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    private val icons = HashMap<Int, Drawable?>()
    private val flats = HashMap<Long, Bitmap>()
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    /** A multi-colour drawable drawn once, as it is (a fresh copy, never tinted), at [w] × [h] px. */
    private fun flat(res: Int, w: Int, h: Int): Bitmap = flats.getOrPut((res.toLong() shl 32) or (w.toLong() shl 16) or h.toLong()) {
        Bitmap.createBitmap(w.coerceAtLeast(1), h.coerceAtLeast(1), Bitmap.Config.ARGB_8888).also { b ->
            c.getDrawable(res)?.let { d -> d.setBounds(0, 0, b.width, b.height); d.draw(Canvas(b)) }
        }
    }

    /** Where the card's text and icon are (in the card's own coordinates): motifs there are faded. */
    var avoid: List<RectF> = emptyList()
        set(v) { field = v; invalidateSelf() }

    val hasScene get() = look.scene != 0

    /** Where the corner scene is drawn, at its largest size clear of text; null if none fits. */
    fun sceneRect(b: Rect = bounds): RectF? {
        if (look.scene == 0) return null
        val d = icons.getOrPut(look.scene) { c.getDrawable(look.scene)?.mutate() } ?: return null
        val margin = Seasons.TEXT_MARGIN_DP * density
        for (s in Seasons.SCENE_SCALES) {
            val w = look.sceneDp * density * s
            val h = w * d.intrinsicHeight / d.intrinsicWidth.coerceAtLeast(1)
            val r = RectF(b.right - w, b.bottom - h, b.right.toFloat(), b.bottom.toFloat())
            if (avoid.none { RectF.intersects(RectF(it.left - margin, it.top - margin, it.right + margin, it.bottom + margin), r) }) return r
        }
        return null
    }

    /** Where a motif is drawn (its square, turned or not, inside [b]). */
    fun motifRect(m: CardLook.Motif, b: Rect = bounds): RectF {
        val half = m.sizeDp * density / 2 * Seasons.ICON_FILL
        val cx = b.left + m.x * b.width(); val cy = b.top + m.y * b.height()
        // The turned square's own bounding box.
        val t = Math.toRadians(m.turn.toDouble())
        val r = (half * (Math.abs(Math.cos(t)) + Math.abs(Math.sin(t)))).toFloat()
        return RectF(cx - r, cy - r, cx + r, cy + r)
    }

    /** The opacity a motif is drawn at: [WeatherTokens.Seasons.FAINT_ALPHA] at most behind text. */
    fun alphaOf(m: CardLook.Motif, b: Rect = bounds): Float {
        if (!m.avoidText) return m.alpha
        val r = motifRect(m, b)
        val margin = Seasons.TEXT_MARGIN_DP * density
        val behind = avoid.any { RectF.intersects(RectF(it.left - margin, it.top - margin, it.right + margin, it.bottom + margin), r) }
        return if (behind) minOf(m.alpha, Seasons.FAINT_ALPHA) else m.alpha
    }

    /** Dots as (x, y) shares of the block, radius in dp and opacity: the same every time. */
    private val dots = Random(Festive.FLAKE_SEED).let { r ->
        List(Festive.FLAKES) {
            floatArrayOf(r.nextFloat(), r.nextFloat(),
                Festive.FLAKE_MIN_DP + r.nextFloat() * (Festive.FLAKE_MAX_DP - Festive.FLAKE_MIN_DP),
                Festive.FLAKE_MIN_ALPHA + r.nextFloat() * (Festive.FLAKE_MAX_ALPHA - Festive.FLAKE_MIN_ALPHA))
        }
    }

    /** Stars: (x, y) shares, radius in dp, opacity; the same every night. */
    private val stars = Random(Seasons.STAR_SEED).let { r ->
        List(Seasons.STARS) {
            floatArrayOf(r.nextFloat(), r.nextFloat() * 0.85f,
                Seasons.STAR_MIN_DP + r.nextFloat() * (Seasons.STAR_MAX_DP - Seasons.STAR_MIN_DP),
                Seasons.STAR_MIN_ALPHA + r.nextFloat() * (Seasons.STAR_MAX_ALPHA - Seasons.STAR_MIN_ALPHA))
        }
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        val w = b.width().toFloat(); val h = b.height().toFloat()
        if (w <= 0f || h <= 0f) return
        base.shader = LinearGradient(b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.bottom.toFloat(), look.stops, null, Shader.TileMode.CLAMP)
        canvas.drawRect(b, base)
        if (look.glowAlpha > 0f) {
            val gc = (Math.round(look.glowAlpha * 255) shl 24) or (look.glow and 0xFFFFFF)
            val gx = if (look.glowCorner == Seasons.CORNER_TR || look.glowCorner == Seasons.CORNER_BR) b.right.toFloat() else b.left.toFloat()
            val gy = if (look.glowCorner == Seasons.CORNER_BL || look.glowCorner == Seasons.CORNER_BR) b.bottom.toFloat() else b.top.toFloat()
            glow.shader = RadialGradient(gx, gy, maxOf(w, h) * Festive.GLOW_RADIUS, intArrayOf(gc, gc and 0xFFFFFF), null, Shader.TileMode.CLAMP)
            canvas.drawRect(b, glow)
        }
        // Snowfall and stars: soft dots, left out behind text (they'd read as stray punctuation).
        val margin = Seasons.TEXT_MARGIN_DP * density
        fun dots(list: List<FloatArray>) {
            for (f in list) {
                val x = b.left + f[0] * w; val y = b.top + f[1] * h; val r = f[2] * density
                if (avoid.any { x + r > it.left - margin && x - r < it.right + margin && y + r > it.top - margin && y - r < it.bottom + margin }) continue
                dot.alpha = Math.round(f[3] * 255)
                canvas.drawCircle(x, y, r, dot)
            }
        }
        if (look.dots) dots(dots)
        if (look.stars) dots(stars)
        canvas.save()
        canvas.clipRect(b)
        for (m in look.motifs) {
            val d = icons.getOrPut(m.res) { c.getDrawable(m.res)?.mutate() } ?: continue
            val half = m.sizeDp * density / 2
            val cx = b.left + m.x * w; val cy = b.top + m.y * h
            canvas.save()
            canvas.rotate(m.turn, cx, cy)
            val a = alphaOf(m, b)
            if (a <= 0f) { canvas.restore(); continue }
            val aspect = d.intrinsicWidth.toFloat() / d.intrinsicHeight.coerceAtLeast(1)
            val hw = if (aspect < 1f) half * aspect else half
            val hh = if (aspect > 1f) half / aspect else half
            val box = Rect((cx - hw).toInt(), (cy - hh).toInt(), (cx + hw).toInt(), (cy + hh).toInt())
            if (m.tint == 0) {
                // Tint 0: the drawable keeps its own colours (the decorated eggs). Drawn flat once,
                // then turned as a picture, so its clipped bands draw correctly at any angle.
                bitmapPaint.alpha = Math.round(a * 255)
                canvas.drawBitmap(flat(m.res, box.width(), box.height()), null, box, bitmapPaint)
            } else {
                d.colorFilter = PorterDuffColorFilter(m.tint or 0xFF000000.toInt(), PorterDuff.Mode.SRC_IN)
                d.alpha = Math.round(a * 255)
                d.bounds = box
                d.draw(canvas)
            }
            canvas.restore()
        }
        // The corner scene (Chris, 9 Nov), over the motifs, never behind text.
        sceneRect(b)?.let { r ->
            icons[look.scene]?.let { d ->
                d.alpha = 255
                // At night the scene is dimmed to moonlight.
                d.colorFilter = if (look.night) android.graphics.LightingColorFilter(Seasons.NIGHT_SCENE_TINT, 0) else null
                d.setBounds(r.left.toInt(), r.top.toInt(), r.right.toInt(), r.bottom.toInt())
                d.draw(canvas)
            }
        }
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.OPAQUE
}
