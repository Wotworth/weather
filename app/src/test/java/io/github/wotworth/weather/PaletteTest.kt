package io.github.wotworth.weather

import org.junit.Assert.assertEquals
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
    /** The top block's secondary text opacity (WeatherTokens.Colors.LOW). */
    private val LOW = ((WeatherTokens.Colors.LOW ushr 24) and 0xFF) / 255.0

    private fun mix(a: Int, b: Int, t: Double): Int {
        fun ch(s: Int) = Math.round(((a shr s) and 0xFF) + (((b shr s) and 0xFF) - ((a shr s) and 0xFF)) * t).toInt()
        return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    @Test fun textReadsOnEveryGradient() {
        for ((k, s) in Palette.STOPS) for (i in 0..200) {
            val t = i / 200.0 * 2
            val bg = if (t <= 1) mix(s[0], s[1], t) else mix(s[1], s[2], t - 1)
            assertTrue("$k white", contrast(WeatherTokens.Colors.PRIMARY, bg) >= 4.5)
            assertTrue("$k low", contrast(mix(bg, WeatherTokens.Colors.PRIMARY, LOW), bg) >= 4.5)
        }
    }

    /** WX-6.11: the festive snow, at full glow everywhere (worse than it ever is). */
    @Test fun textReadsOnFestiveSnow() {
        val s = Palette.FESTIVE_SNOW
        val glow = WeatherTokens.Festive.GLOW
        for (i in 0..200) {
            val t = i / 200.0 * 2
            val bg = mix(if (t <= 1) mix(s[0], s[1], t) else mix(s[1], s[2], t - 1), glow, WeatherTokens.Festive.GLOW_ALPHA.toDouble())
            assertTrue("festive white", contrast(WeatherTokens.Colors.PRIMARY, bg) >= 4.5)
            assertTrue("festive low", contrast(mix(bg, WeatherTokens.Colors.PRIMARY, LOW), bg) >= 4.5)
            // White text over a snowflake in the glow.
            val flakeOnGlow = mix(bg, 0xFFFFFFFF.toInt(), WeatherTokens.Festive.SNOWFLAKE_MAX_ALPHA.toDouble())
            assertTrue("festive white on a snowflake", contrast(WeatherTokens.Colors.PRIMARY, flakeOnGlow) >= 4.5)
            // 75% white text over a snowflake outside the glow.
            val plain = if (t <= 1) mix(s[0], s[1], t) else mix(s[1], s[2], t - 1)
            val flake = mix(plain, 0xFFFFFFFF.toInt(), WeatherTokens.Festive.SNOWFLAKE_MAX_ALPHA.toDouble())
            assertTrue("festive low on a snowflake", contrast(mix(flake, WeatherTokens.Colors.PRIMARY, LOW), flake) >= 4.5)
        }
        assertTrue(WeatherTokens.Festive.SNOWFLAKES.all { it[4] <= WeatherTokens.Festive.SNOWFLAKE_MAX_ALPHA })
    }

    /** WX-6.12: every season's card, with its glow at full strength everywhere and under each motif. */
    @Test fun textReadsOnEverySeasonsCard() {
        val white = WeatherTokens.Colors.PRIMARY
        val bad = LinkedHashSet<String>()
        fun check(ok: Boolean, what: String) { if (!ok) bad += what }
        for (night in listOf(false, true)) for (season in Season.values()) for (kind in Forecast.Kind.values()) {
            val look = CardLook.of(kind, season, night)
            val s = look.stops
            // Behind text, a motif is at most FAINT_ALPHA (the card fades it; HeroTest checks that).
            val tints = look.motifs.filter { it.tint != 0 }.map { it.tint to (if (it.avoidText) minOf(it.alpha, WeatherTokens.Seasons.FAINT_ALPHA) else it.alpha) }.distinct()
            for (i in 0..100) {
                val t = i / 100.0 * (s.size - 1)
                val seg = minOf(t.toInt(), s.size - 2)
                val plain = mix(s[seg], s[seg + 1], t - seg)
                val glowed = mix(plain, look.glow, look.glowAlpha.toDouble())
                val name = "$season $kind${if (night) " night" else ""}"
                check(contrast(white, glowed) >= 4.5, "$name white")
                check(contrast(mix(glowed, white, LOW), glowed) >= 4.5, "$name low")
                for ((tint, a) in tints) {
                    val onGlow = mix(glowed, tint, a.toDouble())
                    check(contrast(white, onGlow) >= 4.5, "$name white on a motif")
                    val onPlain = mix(plain, tint, a.toDouble())
                    check(contrast(mix(onPlain, white, LOW), onPlain) >= 4.5, "$name low on a motif")
                }
            }
        }
        assertTrue("contrast below 4.5:1: $bad", bad.isEmpty())
    }

    @Test fun seasonsByMonth() {
        assertEquals(Season.WINTER, Season.of(java.time.LocalDate.of(2026, 12, 1)))
        assertEquals(Season.WINTER, Season.of(java.time.LocalDate.of(2027, 2, 28)))
        assertEquals(Season.SPRING, Season.of(java.time.LocalDate.of(2027, 3, 1)))
        assertEquals(Season.SUMMER, Season.of(java.time.LocalDate.of(2027, 8, 31)))
        assertEquals(Season.FALL, Season.of(java.time.LocalDate.of(2026, 11, 9)))
    }

    @Test fun accentTextReadsOnBlack() {
        assertTrue(contrast(WeatherTokens.Colors.ACCENT_TEXT, WeatherTokens.Colors.BACKGROUND) >= 4.5)
        assertTrue(contrast(WeatherTokens.Colors.SECONDARY.let { mix(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0.6) }, WeatherTokens.Colors.BACKGROUND) >= 4.5)
    }
}
