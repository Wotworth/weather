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
    /** Night (Chris, 9 Nov): a starfield of soft dots, and the scene dimmed to moonlight. */
    val stars: Boolean = false, val night: Boolean = false,
) {
    /** [avoidText]: left out where it would sit behind text (the festive snow's faint flakes are not). */
    data class Motif(val res: Int, val tint: Int, val alpha: Float, val x: Float, val y: Float, val sizeDp: Float, val turn: Float, val avoidText: Boolean = true)

    companion object {
        private fun mix(a: Int, b: Int, t: Float): Int {
            fun ch(s: Int) = Math.round(((a shr s) and 0xFF) + (((b shr s) and 0xFF) - ((a shr s) and 0xFF)) * t)
            return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
        }

        /** The look of a [kind] card on [date]. Snow is festive in every season (Chris, 9 Nov, Q6). */
        fun of(kind: Forecast.Kind, date: LocalDate, night: Boolean = false): CardLook = of(kind, Season.of(date), night)

        fun of(kind: Forecast.Kind, season: Season, night: Boolean = false): CardLook {
            val S = WeatherTokens.Seasons
            val st = S.STYLE.getValue(season)
            if (kind == Forecast.Kind.SNOW && season == Season.WINTER) {
                // Winter snow: the festive Christmas card, snowflakes, snowy pines (WX-6.11).
                val F = WeatherTokens.Festive
                return CardLook(Palette.FESTIVE_SNOW, F.GLOW, F.GLOW_ALPHA, S.CORNER_BR,
                    F.SNOWFLAKES.map { Motif(R.drawable.festive_snowflake, 0xFFFFFFFF.toInt(), it[4], it[0], it[1], it[2], it[3], avoidText = false) },
                    dots = true, scene = st.snowScene, sceneDp = st.sceneDp, night = night)
            }
            val k = S.TONE.getValue(kind)
            val base = if (night) S.NIGHT.getValue(season) else st.stops
            // At night the condition greys the sky by half as much (it is already dark).
            val share = if (night) k.share * 0.5f else k.share
            val stops = base.map { mix(mix(it, k.toward, share), 0xFF000000.toInt(), k.darken) }.toIntArray()
            if (kind == Forecast.Kind.SNOW) {
                // Snow in spring, summer or fall (Chris, 9 Nov): the season's own sky, snowing, with
                // its scene under a coat of snow.
                val F = WeatherTokens.Festive
                return CardLook(stops, if (night) S.MOONLIGHT else st.glow, (if (night) S.MOONLIGHT_ALPHA else st.glowAlpha) * k.glowFactor,
                    if (night) S.CORNER_TR else st.corner,
                    F.SNOWFLAKES.map { Motif(R.drawable.festive_snowflake, 0xFFFFFFFF.toInt(), it[4], it[0], it[1], it[2], it[3], avoidText = false) },
                    dots = true, scene = st.snowScene, sceneDp = st.sceneDp, night = night)
            }
            val w = (if (night) S.NIGHT_WEATHER else S.WEATHER).getValue(kind)
            val motifs = (0 until minOf(w.count, S.PLACES.size)).map { i ->
                val p = S.PLACES[i]
                Motif(w.icons[i % w.icons.size], w.tints[i % w.tints.size], w.alpha,
                    p[0], p[1], minOf(p[2] * w.sizeScale, w.maxSizeDp), p[3] * w.turnScale)
            }
            return if (night) CardLook(stops, S.MOONLIGHT, S.MOONLIGHT_ALPHA * k.glowFactor, S.CORNER_TR, motifs, dots = false,
                scene = st.scene, sceneDp = st.sceneDp, stars = kind in S.STARRY, night = true)
            else CardLook(stops, st.glow, st.glowAlpha * k.glowFactor, st.corner, motifs, dots = false, scene = st.scene, sceneDp = st.sceneDp)
        }
    }
}
