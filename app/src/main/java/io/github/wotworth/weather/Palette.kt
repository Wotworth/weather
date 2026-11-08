package io.github.wotworth.weather

/**
 * The tile's gradient per condition, so the forecast page's top block matches the live tile.
 * Copied from Wotworth/wp-launcher app/src/main/java/io/github/wotworth/wplauncher/WeatherDesign.kt
 * (STOPS and POSITIONS) at commit a444997452257f0bfc9b9e339f77a2243a3ac81e: contrast-checked there
 * for white and 75% white text at 4.5:1 (PaletteTest checks it again here).
 */
object Palette {
    val STOPS: Map<Forecast.Kind, IntArray> = mapOf(
        Forecast.Kind.SUNNY to intArrayOf(0xFF003078.toInt(), 0xFF014D7E.toInt(), 0xFF046182.toInt()),
        Forecast.Kind.PARTLY_CLOUDY to intArrayOf(0xFF033383.toInt(), 0xFF034B8E.toInt(), 0xFF315D8A.toInt()),
        Forecast.Kind.CLOUDY to intArrayOf(0xFF303C4E.toInt(), 0xFF3E4B60.toInt(), 0xFF515D6F.toInt()),
        Forecast.Kind.RAIN to intArrayOf(0xFF1D3150.toInt(), 0xFF294D70.toInt(), 0xFF325F7F.toInt()),
        Forecast.Kind.SNOW to intArrayOf(0xFF213D5C.toInt(), 0xFF2D4D6D.toInt(), 0xFF415E78.toInt()),
    )
    /** No forecast yet: the cloudy stops. */
    val NONE = STOPS.getValue(Forecast.Kind.CLOUDY)
}
