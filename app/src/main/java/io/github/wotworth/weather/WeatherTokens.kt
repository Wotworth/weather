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
        /** WX-10: the forecast discussion's headings and text. */
        const val DISCUSSION_HEAD = 22f
        const val DISCUSSION_SUBHEAD = 17f
        const val DISCUSSION_BODY = 16f
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
        /** Space between the top block's three figures. */
        const val STAT_COLUMN_GAP = 10f
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
     * WX-6.12, WX-6.13 (Chris, 9 Nov, third pass): the season sets the card's colours, its light and
     * the scene in the bottom-right corner; the decorations across the card show only the weather
     * ([WEATHER]). [Style.stops] is the sunny card: a blue sky with the season's horizon (Chris, 9 Nov); [TONE] turns it into the other conditions. A
     * snowy day uses the festive snow card (WX-6.11) with the season's scene under snow.
     */
    object Seasons {
        const val CORNER_TL = 0
        const val CORNER_TR = 1
        const val CORNER_BR = 2
        const val CORNER_BL = 3

        class Style(
            val stops: IntArray,
            val glow: Int, val glowAlpha: Float, val corner: Int,
            /** The corner scene, its snow-coated version for snowy days, and its width in dp. */
            val scene: Int, val snowScene: Int, val sceneDp: Float,
        )

        /** How a condition changes the sunny card: mix toward [toward] by [share], darken by [darken]. */
        class Tone(val toward: Int, val share: Float, val darken: Float, val glowFactor: Float)

        val STYLE: Map<Season, Style> = mapOf(
            // Winter: a crisp, pale winter sky (steel to ice blue), cool light from the top-left; snowy pines.
            Season.WINTER to Style(intArrayOf(0xFF224873.toInt(), 0xFF2C5273.toInt(), 0xFF395B73.toInt()),
                0xFFE6F2FF.toInt(), 0.10f, CORNER_TL, R.drawable.scene_winter, R.drawable.scene_winter, 100f),
            // Spring, cheery: blue sky to a pink-and-raspberry horizon, sunny yellow light; Easter eggs in grass.
            Season.SPRING to Style(intArrayOf(0xFF27599C.toInt(), 0xFF4E3E9C.toInt(), 0xFF9C2F65.toInt()),
                0xFFFFE680.toInt(), 0.12f, CORNER_TR, R.drawable.scene_spring, R.drawable.scene_spring_snow, 104f),
            // Summer: a bright summer-blue sky (Chris, 9 Nov), golden sun from the top-right; a maple and a cedar.
            Season.SUMMER to Style(intArrayOf(0xFF1247B2.toInt(), 0xFF1554B2.toInt(), 0xFF1961B2.toInt()),
                0xFFFFD54F.toInt(), 0.08f, CORNER_TR, R.drawable.scene_summer, R.drawable.scene_summer_snow, 104f),
            // Fall: a clear autumn-blue sky to an amber-rust horizon, amber light; a pile of maple leaves.
            Season.FALL to Style(intArrayOf(0xFF1D5391.toInt(), 0xFF3A5E91.toInt(), 0xFF914316.toInt()),
                0xFFFFB347.toInt(), 0.12f, CORNER_BR, R.drawable.scene_fall, R.drawable.scene_fall_snow, 104f),
        )

        val TONE: Map<Forecast.Kind, Tone> = mapOf(
            Forecast.Kind.SUNNY to Tone(0xFF000000.toInt(), 0f, 0f, 1f),
            // Partly cloudy keeps the sky and the sun's full light (Chris, 9 Nov: "more sunshine").
            Forecast.Kind.PARTLY_CLOUDY to Tone(0xFF3A4A5A.toInt(), 0.05f, 0f, 1f),
            Forecast.Kind.CLOUDY to Tone(0xFF4A5260.toInt(), 0.45f, 0.08f, 0.3f),
            Forecast.Kind.RAIN to Tone(0xFF22405E.toInt(), 0.40f, 0.10f, 0.3f),
            Forecast.Kind.SNOW to Tone(0xFF000000.toInt(), 0f, 0f, 1f),
        )

        /**
         * The weather's decorations across the card (Chris, 9 Nov: "only represent the weather"):
         * drawables and tints (cycled), opacity, how many of [PLACES], size share and cap, and how
         * much of each place's turn to keep (0: upright, as rain falls).
         */
        class Weather(val icons: IntArray, val tints: IntArray, val alpha: Float, val count: Int,
                      val sizeScale: Float, val maxSizeDp: Float, val turnScale: Float)

        val WEATHER: Map<Forecast.Kind, Weather> = mapOf(
            // Sun: gold sparkles.
            Forecast.Kind.SUNNY to Weather(intArrayOf(R.drawable.season_sparkle), intArrayOf(0xFFFFE07A.toInt()), 0.55f, 8, 0.8f, 40f, 0.3f),
            // Sun and cloud: more gold sparkles than soft white clouds (Chris, 9 Nov: "more sunshine").
            Forecast.Kind.PARTLY_CLOUDY to Weather(intArrayOf(R.drawable.season_sparkle, R.drawable.season_cloud, R.drawable.season_sparkle),
                intArrayOf(0xFFFFE07A.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFE07A.toInt()), 0.5f, 11, 0.9f, 48f, 0.3f),
            // Cloud: soft white clouds.
            Forecast.Kind.CLOUDY to Weather(intArrayOf(R.drawable.season_cloud), intArrayOf(0xFFFFFFFF.toInt()), 0.3f, 10, 1.2f, 64f, 0f),
            // Rain: falling drops, upright.
            Forecast.Kind.RAIN to Weather(intArrayOf(R.drawable.season_drop), intArrayOf(0xFFBFE0FF.toInt()), 0.55f, 18, 0.6f, 22f, 0f),
        )

        /**
         * Night versions (Chris, 9 Nov): today's card after dark. Each season's night sky (the clear
         * card); the condition's [TONE] applies on top. Moonlight from the top-right.
         */
        val NIGHT: Map<Season, IntArray> = mapOf(
            Season.WINTER to intArrayOf(0xFF0A1830.toInt(), 0xFF0E2142.toInt(), 0xFF142B50.toInt()),
            Season.SPRING to intArrayOf(0xFF1A1845.toInt(), 0xFF2A1A52.toInt(), 0xFF3D1A48.toInt()),
            Season.SUMMER to intArrayOf(0xFF0A1C47.toInt(), 0xFF0C2660.toInt(), 0xFF0E2F6A.toInt()),
            Season.FALL to intArrayOf(0xFF0E1A36.toInt(), 0xFF1A1F3C.toInt(), 0xFF33201A.toInt()),
        )
        const val MOONLIGHT = 0xFFDDE8FF.toInt()
        const val MOONLIGHT_ALPHA = 0.12f

        /** At night: clear skies show stars (soft dots and white twinkles); clouds are dimmer. */
        val NIGHT_WEATHER: Map<Forecast.Kind, Weather> = mapOf(
            Forecast.Kind.SUNNY to Weather(intArrayOf(R.drawable.season_sparkle), intArrayOf(0xFFFFFFFF.toInt()), 0.7f, 7, 0.55f, 26f, 0.3f),
            Forecast.Kind.PARTLY_CLOUDY to Weather(intArrayOf(R.drawable.season_sparkle, R.drawable.season_cloud, R.drawable.season_sparkle),
                intArrayOf(0xFFFFFFFF.toInt(), 0xFF9FB3CC.toInt(), 0xFFFFFFFF.toInt()), 0.6f, 10, 0.7f, 44f, 0.2f),
            Forecast.Kind.CLOUDY to Weather(intArrayOf(R.drawable.season_cloud), intArrayOf(0xFF9FB3CC.toInt()), 0.3f, 10, 1.2f, 64f, 0f),
            Forecast.Kind.RAIN to Weather(intArrayOf(R.drawable.season_drop), intArrayOf(0xFF9FC4E8.toInt()), 0.5f, 18, 0.6f, 22f, 0f),
        )
        /** Which night skies show the starfield of soft dots. */
        val STARRY = setOf(Forecast.Kind.SUNNY, Forecast.Kind.PARTLY_CLOUDY)
        const val STARS = 46
        const val STAR_SEED = 1977
        const val STAR_MIN_DP = 0.5f
        const val STAR_MAX_DP = 1.4f
        const val STAR_MIN_ALPHA = 0.3f
        const val STAR_MAX_ALPHA = 0.95f
        /** The corner scene at night: its colours multiplied by this (dimmer, moonlit blue). */
        const val NIGHT_SCENE_TINT = 0xFF8090B0.toInt()

        /** Places across the card: x and y as shares of the card, size in dp, turn in degrees. */
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

        /** A decoration that would sit behind text is left out. */
        const val FAINT_ALPHA = 0f
        /** A Fluent icon's shape fills about this share of its square (the rest is padding). */
        const val ICON_FILL = 0.85f
        /** Text is kept this clear of decorations. */
        const val TEXT_MARGIN_DP = 3f
        /** On cards with a scene, the phrase and the three figures leave this much room on the right. */
        const val SCENE_ROOM_DP = 84f
        /** A corner scene steps down to these sizes if text would reach it, else is left out. */
        val SCENE_SCALES = floatArrayOf(1f, 0.85f, 0.7f)
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
        /** WX-10: the forecast discussion is fetched again on opening its page if older than this. */
        const val DISCUSSION_FRESH_MS = 30 * 60_000L
        const val TIMEOUT_MS = 15_000
        const val MAX_REPLY_BYTES = 512 * 1024
        /** Errors shown in settings are cut to this length. */
        const val ERROR_CHARS = 160
    }
}
