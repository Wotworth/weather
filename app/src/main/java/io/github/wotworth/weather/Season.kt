package io.github.wotworth.weather

import java.time.LocalDate

/**
 * WX-6.12 (Chris, 9 Nov): each card has a version per season. Meteorological seasons in the
 * northern hemisphere (the default town is in New York), by the day's own date.
 */
enum class Season {
    WINTER, SPRING, SUMMER, FALL;

    companion object {
        fun of(d: LocalDate): Season = when (d.monthValue) {
            12, 1, 2 -> WINTER
            3, 4, 5 -> SPRING
            6, 7, 8 -> SUMMER
            else -> FALL
        }
    }
}

/** A card's look, worked out from WeatherTokens.Seasons: everything [CardDrawable] draws. */
data class CardLook(
    val stops: IntArray,
    val glow: Int, val glowAlpha: Float, val glowCorner: Int,
    /** Motifs: drawable, tint, opacity, and its place (x, y share, size dp, turn degrees). */
    val motifs: List<Motif>,
    val dots: Boolean,
    /** The bottom-right scene: drawable and full width in dp (0: none). */
    val scene: Int = 0, val sceneDp: Float = 0f,
) {
    /** [avoidText]: left out where it would sit behind text (the festive snow's faint flakes are not). */
    data class Motif(val res: Int, val tint: Int, val alpha: Float, val x: Float, val y: Float, val sizeDp: Float, val turn: Float, val avoidText: Boolean = true)

    companion object {
        private fun mix(a: Int, b: Int, t: Float): Int {
            fun ch(s: Int) = Math.round(((a shr s) and 0xFF) + (((b shr s) and 0xFF) - ((a shr s) and 0xFF)) * t)
            return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
        }

        /** The look of a [kind] card on [date]. Snow is festive in every season (Chris, 9 Nov, Q6). */
        fun of(kind: Forecast.Kind, date: LocalDate): CardLook = of(kind, Season.of(date))

        fun of(kind: Forecast.Kind, season: Season): CardLook {
            val S = WeatherTokens.Seasons
            if (kind == Forecast.Kind.SNOW) {
                val F = WeatherTokens.Festive
                return CardLook(Palette.FESTIVE_SNOW, F.GLOW, F.GLOW_ALPHA, S.CORNER_BR,
                    F.SNOWFLAKES.map { Motif(R.drawable.festive_snowflake, 0xFFFFFFFF.toInt(), it[4], it[0], it[1], it[2], it[3], avoidText = false) }, dots = true,
                    scene = R.drawable.scene_winter, sceneDp = S.SNOW_SCENE_DP)
            }
            val st = S.STYLE.getValue(season)
            val k = S.TONE.getValue(kind)
            val stops = st.stops.map { mix(mix(it, k.toward, k.share), 0xFF000000.toInt(), k.darken) }.toIntArray()
            val n = minOf(Math.round(st.count * k.motifFactor), S.PLACES.size)
            val motifs = ArrayList<Motif>()
            for (i in 0 until n) {
                val p = S.PLACES[i]
                motifs += Motif(st.icons[i % st.icons.size], st.tints[i % st.tints.size], st.alpha,
                    p[0], p[1], minOf(p[2] * st.sizeScale, st.maxSizeDp), p[3] * st.turnScale)
            }
            if (kind == Forecast.Kind.RAIN) for (i in 0 until S.DROPS) {
                val p = S.PLACES[S.PLACES.size - 1 - i]
                motifs += Motif(R.drawable.season_drop, S.DROP_TINT, S.DROP_ALPHA, (p[0] + 0.05f).coerceAtMost(0.98f), (p[1] + 0.08f).coerceAtMost(0.97f), S.DROP_DP, 0f)
            }
            return CardLook(stops, st.glow, st.glowAlpha * k.glowFactor, st.corner, motifs, st.dots, st.scene, st.sceneDp)
        }
    }
}
