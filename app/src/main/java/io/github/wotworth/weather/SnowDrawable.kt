package io.github.wotworth.weather

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import io.github.wotworth.weather.WeatherTokens.Festive
import kotlin.random.Random

/**
 * WX-6.11 (Chris, 9 Nov): the snow block, "a little Christmas festive" but sophisticated: an
 * evergreen-to-midnight gradient, a faint warm glow from one corner like candlelight through a
 * window, and a light, still snowfall of soft dots. Text contrast is checked in PaletteTest.
 */
class SnowDrawable(private val density: Float) : Drawable() {
    private val base = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG)
    private val flake = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }

    /** Flakes as (x, y) fractions of the block, radius in dp and opacity: the same every time. */
    private val flakes = Random(Festive.FLAKE_SEED).let { r ->
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
        base.shader = LinearGradient(b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.bottom.toFloat(),
            Palette.FESTIVE_SNOW, null, Shader.TileMode.CLAMP)
        canvas.drawRect(b, base)
        val glowColor = (Math.round(Festive.GLOW_ALPHA * 255) shl 24) or (Festive.GLOW and 0xFFFFFF)
        glow.shader = RadialGradient(b.right.toFloat(), b.bottom.toFloat(), maxOf(w, h) * Festive.GLOW_RADIUS,
            intArrayOf(glowColor, glowColor and 0xFFFFFF), null, Shader.TileMode.CLAMP)
        canvas.drawRect(b, glow)
        for (f in flakes) {
            flake.alpha = Math.round(f[3] * 255)
            canvas.drawCircle(b.left + f[0] * w, b.top + f[1] * h, f[2] * density, flake)
        }
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.OPAQUE
}
