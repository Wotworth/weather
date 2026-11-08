package io.github.wotworth.weather

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** The forecast page's top block: white and 75% white text read at 4.5:1 everywhere on each gradient. */
class PaletteTest {
    private fun lin(c: Int): Double { val v = c / 255.0; return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4) }
    private fun lum(c: Int) = 0.2126 * lin((c shr 16) and 0xFF) + 0.7152 * lin((c shr 8) and 0xFF) + 0.0722 * lin(c and 0xFF)
    private fun contrast(a: Int, b: Int) = (max(lum(a), lum(b)) + 0.05) / (min(lum(a), lum(b)) + 0.05)
    private fun mix(a: Int, b: Int, t: Double): Int {
        fun ch(s: Int) = Math.round(((a shr s) and 0xFF) + (((b shr s) and 0xFF) - ((a shr s) and 0xFF)) * t).toInt()
        return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    @Test fun textReadsOnEveryGradient() {
        for ((k, s) in Palette.STOPS) for (i in 0..200) {
            val t = i / 200.0 * 2
            val bg = if (t <= 1) mix(s[0], s[1], t) else mix(s[1], s[2], t - 1)
            assertTrue("$k white", contrast(WeatherTokens.Colors.PRIMARY, bg) >= 4.5)
            assertTrue("$k low", contrast(mix(bg, WeatherTokens.Colors.PRIMARY, 0.75), bg) >= 4.5)
        }
    }

    @Test fun accentTextReadsOnBlack() {
        assertTrue(contrast(WeatherTokens.Colors.ACCENT_TEXT, WeatherTokens.Colors.BACKGROUND) >= 4.5)
        assertTrue(contrast(WeatherTokens.Colors.SECONDARY.let { mix(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0.6) }, WeatherTokens.Colors.BACKGROUND) >= 4.5)
    }
}
