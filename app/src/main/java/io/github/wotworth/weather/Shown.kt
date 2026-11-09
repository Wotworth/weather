package io.github.wotworth.weather

import kotlin.math.abs
import kotlin.math.floor

/**
 * WX-6.2 (Chris, 8 and 9 Nov): every number the app's screens show is rounded. To the nearest 5
 * (51 → 50, 48 → 50), except values between −10 and 10 (exclusive), which go to the nearest odd
 * number or 0 (4.2 → 5, 7.8 → 7, −2.6 → −3, 0 → 0, 0.4 → 0). Halves go up, as the launcher's
 * roundTo5 does (72.5 → 75, 4 → 5, 2 → 3, −0.5 → 0, 0.5 → 1). The rule applies to the value in
 * the unit shown. The launcher's tile uses the same rule (wp-launcher Weather.roundShown, 9 Nov).
 */
object Shown {
    fun round(v: Double): Int {
        if (v <= -10.0 || v >= 10.0) return (floor(v / 5.0 + 0.5) * 5).toInt()
        val odd = (2 * floor((v - 1.0) / 2.0 + 0.5) + 1).toInt()
        val toZero = abs(v)
        val toOdd = abs(v - odd)
        return if (toZero < toOdd || (toZero == toOdd && odd < 0)) 0 else odd
    }

    /** A temperature in °F or °C, rounded, with a degree sign; null stays null. */
    fun temp(c: Double?, fahrenheit: Boolean): String? = c?.let { "${round(if (fahrenheit) Forecast.cToF(it) else it)}°" }

    /** A percentage, rounded, with a % sign. */
    fun pct(p: Int?): String? = p?.let { "${round(it.toDouble())}%" }
}
