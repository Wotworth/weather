package io.github.wotworth.weather

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
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

    /** Dots as (x, y) shares of the block, radius in dp and opacity: the same every time. */
    private val dots = Random(Festive.FLAKE_SEED).let { r ->
        List(Festive.FLAKES) {
            floatArrayOf(r.nextFloat(), r.nextFloat(),
                Festive.FLAKE_MIN_DP + r.nextFloat() * (Festive.FLAKE_MAX_DP - Festive.FLAKE_MIN_DP),
                Festive.FLAKE_MIN_ALPHA + r.nextFloat() * (Festive.FLAKE_MAX_ALPHA - Festive.FLAKE_MIN_ALPHA))
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
        if (look.dots) for (f in dots) {
            dot.alpha = Math.round(f[3] * 255)
            canvas.drawCircle(b.left + f[0] * w, b.top + f[1] * h, f[2] * density, dot)
        }
        canvas.save()
        canvas.clipRect(b)
        for (m in look.motifs) {
            val d = icons.getOrPut(m.res) { c.getDrawable(m.res)?.mutate() } ?: continue
            val half = m.sizeDp * density / 2
            val cx = b.left + m.x * w; val cy = b.top + m.y * h
            canvas.save()
            canvas.rotate(m.turn, cx, cy)
            d.colorFilter = PorterDuffColorFilter(m.tint or 0xFF000000.toInt(), PorterDuff.Mode.SRC_IN)
            d.alpha = Math.round(m.alpha * 255)
            d.setBounds((cx - half).toInt(), (cy - half).toInt(), (cx + half).toInt(), (cy + half).toInt())
            d.draw(canvas)
            canvas.restore()
        }
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.OPAQUE
}
