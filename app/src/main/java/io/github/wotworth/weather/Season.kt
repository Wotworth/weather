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
) {
    data class Motif(val res: Int, val tint: Int, val alpha: Float, val x: Float, val y: Float, val sizeDp: Float, val turn: Float) {
        /** At the right edge, clear of text (WeatherTokens.Seasons.EDGE_X). */
        val feature get() = x >= WeatherTokens.Seasons.EDGE_X
    }

    companion object {
        private fun mix(a: Int, b: Int, t: Float): Int {
            fun ch(s: Int) = Math.round(((a shr s) and 0xFF) + (((b shr s) and 0xFF) - ((a shr s) and 0xFF)) * t)
            return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
        }

        /** The look of a [kind] card on [date]. Snow is festive in every season (Chris, 9 Nov, Q6). */
        fun of(kind: Forecast.Kind, date: LocalDate): CardLook = of(kind, Season.of(date))

        fun of(kind: Forecast.Kind, season: Season): CardLook {
            val S = WeatherTokens.Seasons
            val places = WeatherTokens.Festive.SNOWFLAKES
            if (kind == Forecast.Kind.SNOW) {
                val F = WeatherTokens.Festive
                return CardLook(Palette.FESTIVE_SNOW, F.GLOW, F.GLOW_ALPHA, S.CORNER_BR,
                    places.map { Motif(R.drawable.festive_snowflake, 0xFFFFFFFF.toInt(), it[4], it[0], it[1], it[2], it[3]) }, dots = true)
            }
            val st = S.STYLE.getValue(season)
            val k = S.KIND.getValue(kind)
            val stops = Palette.STOPS.getValue(kind).map { mix(mix(it, st.tint, st.tintShare * k.tintFactor), 0xFF000000.toInt(), st.deepen) }.toIntArray()
            val n = Math.round(st.count * k.motifFactor)
            val motifs = ArrayList<Motif>()
            for (i in 0 until minOf(n, places.size)) {
                val p = places[i]
                val edge = p[0] >= S.EDGE_X
                motifs += Motif(st.icons[i % st.icons.size], st.tints[i % st.tints.size], if (edge) st.alpha * S.FEATURE_BOOST else st.alpha,
                    p[0], p[1], minOf(if (edge) p[2] else p[2] * st.sizeScale, st.maxSizeDp), p[3] * st.turnScale)
            }
            if (kind == Forecast.Kind.RAIN) for (i in 0 until S.DROPS) {
                val p = places[places.size - 1 - i]
                // Drops fall straight: placed beside the season's motif, unturned.
                motifs += Motif(R.drawable.season_drop, S.DROP_TINT, S.DROP_ALPHA, (p[0] + 0.06f).coerceAtMost(0.98f), p[1], S.DROP_DP, 0f)
            }
            return CardLook(stops, st.glow, st.glowAlpha * k.glowFactor, st.corner, motifs, dots = season == Season.WINTER)
        }
    }
}
