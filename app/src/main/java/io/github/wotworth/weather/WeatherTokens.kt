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
        const val HOUR_RAIN = 13f
        const val DAY_NAME = 22f
        const val DAY_TEMP = 24f
        const val DAY_RAIN = 15f
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
        const val HOUR_WIDTH = 60f
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
    }

    object Data {
        /** WX-3.1: days kept and served. */
        const val DAYS = 5
        /** Hours shown on the forecast page (the 12-hour reply). */
        const val HOURS = 12
        /** WX-2.4: recent towns kept. */
        const val RECENT_TOWNS = 5
        /** WX-2.5: daily forecast slots every 6 hours from midnight (the launcher's). */
        const val EVERY_HOURS = 6
        /** WX-2.6: hourly data is fetched on opening when the stored copy is older than this. */
        const val HOURLY_FRESH_MS = 30 * 60_000L
        /**
         * WX-2.7: the most calls the app makes in a day (the phone's date), whatever happens.
         * Q1 (Chris, 9 Nov): the plan allows 500 calls a day; 50 are left spare. Normal use is
         * about 4 + one per opening.
         */
        const val DAILY_CALL_CAP = 450
        const val TIMEOUT_MS = 15_000
        const val MAX_REPLY_BYTES = 512 * 1024
        /** Errors shown in settings are cut to this length. */
        const val ERROR_CHARS = 160
    }
}
