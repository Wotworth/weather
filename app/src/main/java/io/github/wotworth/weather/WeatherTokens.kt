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
        /** On the top block: the tile's lows, 75% white (WeatherDesign.LOW_ALPHA). */
        const val LOW = 0xBFFFFFFF.toInt()
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

    /** WX-6.11 (Chris, 9 Nov): the festive snow block (SnowDrawable). */
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
