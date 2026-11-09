package io.github.wotworth.weather

/**
 * Every tunable value: colours, sizes, type, timings and limits, in one place (as TennisTokens.kt
 * in Wotworth/Tennis). Sizes in dp, text in sp.
 */
object WeatherTokens {

    object Colors {
        const val BACKGROUND = 0xFF000000.toInt()
        const val PRIMARY = 0xFFFFFFFF.toInt()
        const val SECONDARY = 0x99FFFFFF.toInt()
        /** On the top block: lows and labels, 85% white (Chris, 9 Nov: brighter seasonal cards; was the tile's 75%). */
        const val LOW = 0xD9FFFFFF.toInt()
        const val DISABLED = 0x66FFFFFF.toInt()
        /** The application bar and prompts (Windows Phone dark chrome). */
        const val APP_BAR = 0xFF1F1F1F.toInt()
        /** On-states only (the chosen unit and town): Windows Phone cobalt. */
        const val ACCENT = 0xFF0050EF.toInt()
        /** The accent on text (chosen town and unit): cobalt lightened to read on black at 4.5:1. */
        const val ACCENT_TEXT = 0xFF4D8BFF.toInt()
        /** Text boxes (Windows Phone dark theme): unfocused fill, focused fill, text. */
        const val TEXT_BOX = 0xFFBFBFBF.toInt()
        const val TEXT_BOX_FOCUSED = 0xFFFFFFFF.toInt()
        const val TEXT_BOX_TEXT = 0xFF000000.toInt()
        /** Pressed: the item dims to this opacity (no ripples). */
        const val PRESSED_ALPHA = 0.55f
        /** Errors (settings only). */
        const val ERROR = 0xFFFF6B5E.toInt()
    }

    object Type {
        const val APP_TITLE = 13f
        const val APP_TITLE_SPACING = 0.02f
        const val TOWN = 34f
        const val HERO_TEMP = 96f
        const val HERO_LOW = 34f
        const val HERO_PHRASE = 18f
        const val STAT_LABEL = 11f
        const val STAT_VALUE = 26f
        const val SECTION = 20f
        const val HOUR_TIME = 13f
        const val HOUR_TEMP = 22f
        const val HOUR_RAIN = 12f
        const val DAY_NAME = 22f
        const val DAY_TEMP = 24f
        const val DAY_RAIN = 14f
        /** Under the day name: "Nov 10". */
        const val DAY_DATE = 13f
        const val FOOTER = 13f
        const val PAGE_HEADER = 42f
        const val BODY = 15f
        const val BUTTON = 15f
        const val INPUT = 16f
        const val CHOICE = 22f
    }

    object Layout {
        const val SIDE = 16f
        const val TOP = 20f
        const val TITLE_TOWN_GAP = 0f
        const val HERO_TOP_GAP = 12f
        const val HERO_PAD = 16f
        const val HERO_ICON = 84f
        const val HERO_ICON_GAP = 12f
        const val STAT_GAP = 14f
        const val SECTION_TOP = 28f
        const val SECTION_BOTTOM = 8f
        /** Narrowest hour in the strip, and the space after each. */
        const val HOUR_WIDTH = 60f
        const val HOUR_GAP = 14f
        /** The day page's hour rows. */
        const val HOUR_ROW_HEIGHT = 56f
        const val HOUR_ROW_TIME = 64f
        const val HOUR_ICON = 30f
        const val DAY_ROW_HEIGHT = 60f
        const val DAY_NAME_WIDTH = 92f
        const val DAY_ICON = 34f
        const val DAY_TEMP_WIDTH = 64f
        const val FOOTER_TOP = 24f
        const val BOTTOM = 96f
        const val APP_BAR_HEIGHT = 56f
        const val APP_BAR_ICON = 26f
        const val APP_BAR_BUTTON = 64f
        const val BUTTON_BORDER = 2f
        const val BUTTON_HEIGHT = 44f
        const val BUTTON_PAD = 18f
        const val INPUT_HEIGHT = 44f
        const val ROW_GAP = 10f
        /** The pull-to-refresh hint, below the status bar. */
        const val PULL_HINT_TOP = 16f
    }

    /** WX-6.9, WX-6.10 (Chris, 9 Nov): refresh feedback and pull to refresh. */
    object Motion {
        /** One turn of the refresh icon while an update runs. */
        const val SPIN_MS = 900L
        /** The icon stops after this long if no update has come back (no network: it waits). */
        const val SPIN_TIMEOUT_MS = 30_000L
        /** Pull: the page moves this share of the finger's travel, at most [PULL_MAX_DP]. */
        const val PULL_RESISTANCE = 0.5f
        const val PULL_MAX_DP = 140f
        /** Letting go past this refreshes. */
        const val PULL_TRIGGER_DP = 72f
        const val PULL_BACK_MS = 200L
    }

    /** WX-6.11 (Chris, 9 Nov): the festive snow block (CardDrawable), in every season. */
    object Festive {
        /** Warm candlelight from the bottom-right corner, at this opacity at its centre. */
        const val GLOW = 0xFFF2A23C.toInt()
        const val GLOW_ALPHA = 0.28f
        /** The glow fades out over this share of the block's longer side. */
        const val GLOW_RADIUS = 0.75f
        /** Still snowfall: this many soft dots, sizes and opacities in these ranges. */
        const val FLAKES = 26
        const val FLAKE_SEED = 1225
        const val FLAKE_MIN_DP = 0.8f
        const val FLAKE_MAX_DP = 2.2f
        const val FLAKE_MIN_ALPHA = 0.12f
        const val FLAKE_MAX_ALPHA = 0.45f
        /**
         * Fluent snowflakes (Chris, 9 Nov: "use snowflakes"), placed by hand: x and y as shares of
         * the block, size in dp, turn in degrees, opacity. The first is large and runs off the
         * top-right edge.
         */
        val SNOWFLAKES = arrayOf(
            floatArrayOf(0.94f, 0.10f, 64f, 12f, 0.13f),
            floatArrayOf(0.50f, 0.62f, 34f, -14f, 0.11f),
            floatArrayOf(0.62f, 0.07f, 18f, 28f, 0.14f),
            floatArrayOf(0.82f, 0.58f, 26f, 6f, 0.12f),
            floatArrayOf(0.285f, 0.86f, 16f, 38f, 0.11f),
            floatArrayOf(0.97f, 0.88f, 14f, -22f, 0.14f),
            // Chris, 9 Nov: "add a few more".
            floatArrayOf(0.17f, 0.08f, 22f, -8f, 0.12f),
            floatArrayOf(0.42f, 0.05f, 14f, 18f, 0.13f),
            floatArrayOf(0.33f, 0.60f, 18f, 24f, 0.12f),
            floatArrayOf(0.88f, 0.36f, 16f, -30f, 0.12f),
            floatArrayOf(0.555f, 0.88f, 14f, 10f, 0.11f),
        )
        /** The most opaque snowflake (for the contrast test). */
        const val SNOWFLAKE_MAX_ALPHA = 0.14f
    }

    /**
     * WX-6.12 (Chris, 9 Nov, second pass: "lean more into the differences"): each season's cards.
     * [Style.stops] is the sunny card; [TONE] turns it into the other conditions. Motifs are Fluent
     * icons (and spring's eggs) at [PLACES], bold, and left out wherever they would sit behind text
     * ([FAINT_ALPHA]). Snow keeps the festive look (WX-6.11, Q6).
     */
    object Seasons {
        const val CORNER_TL = 0
        const val CORNER_TR = 1
        const val CORNER_BR = 2
        const val CORNER_BL = 3

        class Style(
            val stops: IntArray,
            val glow: Int, val glowAlpha: Float, val corner: Int,
            val icons: IntArray, val tints: IntArray, val alpha: Float, val count: Int,
            val sizeScale: Float, val maxSizeDp: Float, val turnScale: Float,
            val dots: Boolean,
        )

        /** How a condition changes the sunny card: mix toward [toward] by [share], darken by [darken]. */
        class Tone(val toward: Int, val share: Float, val darken: Float, val glowFactor: Float, val motifFactor: Float)

        val STYLE: Map<Season, Style> = mapOf(
            // Winter: crisp ice blue, cool pale light from the top-left, white snowflakes and snowfall.
            Season.WINTER to Style(intArrayOf(0xFF15304F.toInt(), 0xFF1B4066.toInt(), 0xFF24507A.toInt()),
                0xFFE6F2FF.toInt(), 0.10f, CORNER_TL,
                intArrayOf(R.drawable.festive_snowflake), intArrayOf(0xFFFFFFFF.toInt()), 0.35f, 14, 1f, 64f, 1f, dots = true),
            // Spring, Easter: violet to rose, butter-yellow light, pastel eggs in five colours, clover.
            Season.SPRING to Style(intArrayOf(0xFF5C3590.toInt(), 0xFF7A3584.toInt(), 0xFF8E3A6C.toInt()),
                0xFFFFE08A.toInt(), 0.12f, CORNER_TR,
                intArrayOf(R.drawable.season_egg, R.drawable.season_egg, R.drawable.season_clover, R.drawable.season_egg),
                intArrayOf(0xFFFFB3CF.toInt(), 0xFFB5F0CF.toInt(), 0xFFFFE699.toInt(), 0xFFB9D9FF.toInt(), 0xFFE0C2FF.toInt()),
                0.75f, 16, 0.9f, 48f, 0.35f, dots = false),
            // Summer: deep summer green, a strong golden sun from the top-right, gold sparkles.
            Season.SUMMER to Style(intArrayOf(0xFF0F4D2A.toInt(), 0xFF165C33.toInt(), 0xFF186445.toInt()),
                0xFFFFC94A.toInt(), 0.15f, CORNER_TR,
                intArrayOf(R.drawable.season_sparkle, R.drawable.season_leaf_two), intArrayOf(0xFFFFD978.toInt(), 0xFF7FD48A.toInt()),
                0.5f, 12, 0.85f, 48f, 0.5f, dots = false),
            // Fall: burnt orange to russet, amber light, a fall of leaves in orange, red, gold and brown.
            Season.FALL to Style(intArrayOf(0xFF7C330C.toInt(), 0xFF8C3610.toInt(), 0xFF6E2513.toInt()),
                0xFFFFB347.toInt(), 0.13f, CORNER_BR,
                intArrayOf(R.drawable.season_leaf_one, R.drawable.season_leaf_two, R.drawable.season_leaf_three),
                intArrayOf(0xFFFFC24D.toInt(), 0xFFC0392B.toInt(), 0xFF5A2A0E.toInt(), 0xFFFFE2A8.toInt(), 0xFFFF8A3D.toInt()),
                0.7f, 18, 1f, 64f, 1f, dots = false),
        )

        val TONE: Map<Forecast.Kind, Tone> = mapOf(
            Forecast.Kind.SUNNY to Tone(0xFF000000.toInt(), 0f, 0f, 1f, 1f),
            Forecast.Kind.PARTLY_CLOUDY to Tone(0xFF3A4A5A.toInt(), 0.15f, 0f, 0.7f, 1f),
            Forecast.Kind.CLOUDY to Tone(0xFF4A5260.toInt(), 0.45f, 0.08f, 0.3f, 0.75f),
            Forecast.Kind.RAIN to Tone(0xFF22405E.toInt(), 0.40f, 0.10f, 0.3f, 0.75f),
            Forecast.Kind.SNOW to Tone(0xFF000000.toInt(), 0f, 0f, 1f, 1f),
        )

        /** Motif places: x and y as shares of the card, size in dp, turn in degrees (a cascade from the top-right). */
        val PLACES = arrayOf(
            floatArrayOf(0.93f, 0.14f, 60f, 12f), floatArrayOf(0.78f, 0.05f, 26f, -20f),
            floatArrayOf(0.58f, 0.03f, 18f, 35f), floatArrayOf(0.97f, 0.42f, 30f, -35f),
            floatArrayOf(0.88f, 0.62f, 36f, 20f), floatArrayOf(0.97f, 0.93f, 26f, -15f),
            floatArrayOf(0.40f, 0.03f, 16f, -10f), floatArrayOf(0.62f, 0.64f, 26f, 40f),
            floatArrayOf(0.22f, 0.04f, 20f, 25f), floatArrayOf(0.80f, 0.97f, 18f, 30f),
            floatArrayOf(0.45f, 0.66f, 20f, -25f), floatArrayOf(0.04f, 0.06f, 22f, -40f),
            floatArrayOf(0.62f, 0.98f, 16f, -30f), floatArrayOf(0.30f, 0.98f, 18f, 55f),
            floatArrayOf(0.75f, 0.62f, 16f, 10f), floatArrayOf(0.12f, 0.98f, 16f, 10f),
            floatArrayOf(0.02f, 0.40f, 20f, -20f), floatArrayOf(0.50f, 0.50f, 14f, 15f),
        )

        /** A motif that would sit behind text is left out (Chris, 9 Nov: faint ones looked messy). */
        const val FAINT_ALPHA = 0f
        /** A Fluent icon's shape fills about this share of its square (the rest is padding). */
        const val ICON_FILL = 0.85f
        /** Text is kept this clear of bold motifs. */
        const val TEXT_MARGIN_DP = 3f

        /** Rain in any season adds pale raindrops. */
        const val DROPS = 6
        const val DROP_TINT = 0xFFBFD8F0.toInt()
        const val DROP_ALPHA = 0.4f
        const val DROP_DP = 16f
    }

    object Data {
        /** WX-3.1: days kept and served. */
        const val DAYS = 5
        /** Hours shown on the forecast page (the 12-hour reply). */
        const val HOURS = 12
        /** WX-2.4: recent towns kept. */
        const val RECENT_TOWNS = 5
        /**
         * WX-2.5 (Chris, 9 Nov): an update (5 days + 12 hours, 2 calls) every this many minutes:
         * 96 calls a day. Android may delay it while the phone sleeps (Doze).
         */
        const val UPDATE_EVERY_MIN = 30L
        /** No update (background or on opening) within this many minutes of the last try; refresh ignores it. */
        const val UPDATE_SKIP_MIN = 20L
        /**
         * WX-2.7: the most calls the app makes in a day (the phone's date), whatever happens.
         * Q1 (Chris, 9 Nov): the plan allows 500 calls a day; 50 are left spare. Normal use is
         * 96 (every 30 minutes) plus refreshes.
         */
        const val DAILY_CALL_CAP = 450
        const val TIMEOUT_MS = 15_000
        const val MAX_REPLY_BYTES = 512 * 1024
        /** Errors shown in settings are cut to this length. */
        const val ERROR_CHARS = 160
    }
}
