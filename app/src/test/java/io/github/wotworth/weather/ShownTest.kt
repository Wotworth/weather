package io.github.wotworth.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** WX-6.2: every number on the app's screens. */
class ShownTest {
    @Test fun nearestFive() {
        assertEquals(50, Shown.round(51.0))
        assertEquals(50, Shown.round(48.0))
        assertEquals(70, Shown.round(72.0))
        assertEquals(75, Shown.round(73.0))
        assertEquals(75, Shown.round(72.5))   // halves up
        assertEquals(10, Shown.round(10.0))
        assertEquals(10, Shown.round(12.4))
        assertEquals(15, Shown.round(12.5))
        assertEquals(100, Shown.round(98.0))
        assertEquals(-10, Shown.round(-10.0))
        assertEquals(-10, Shown.round(-12.4))
        assertEquals(-15, Shown.round(-13.0))
    }

    @Test fun oddOrZeroBetweenMinusTenAndTen() {
        assertEquals(9, Shown.round(9.9))
        assertEquals(9, Shown.round(8.5))
        assertEquals(9, Shown.round(8.0))     // between 7 and 9: up
        assertEquals(7, Shown.round(7.8))
        assertEquals(5, Shown.round(4.2))
        assertEquals(5, Shown.round(4.0))
        assertEquals(3, Shown.round(3.0))
        assertEquals(0, Shown.round(0.0))     // Chris, 9 Nov: 0 stays 0
        assertEquals(0, Shown.round(0.4))
        assertEquals(1, Shown.round(0.5))     // between 0 and 1: up
        assertEquals(0, Shown.round(-0.5))    // between −1 and 0: up
        assertEquals(-1, Shown.round(-0.6))
        assertEquals(1, Shown.round(1.9))
        assertEquals(3, Shown.round(2.0))     // between 1 and 3: up
        assertEquals(-1, Shown.round(-2.0))
        assertEquals(-3, Shown.round(-2.6))
        assertEquals(-9, Shown.round(-9.9))
        // Every value in (−10, 10) comes out odd, or 0.
        var v = -9.99
        while (v < 10.0) { val r = Shown.round(v); assertTrue("at $v: $r", r == 0 || Math.floorMod(r, 2) == 1); v += 0.01 }
    }

    @Test fun temperaturesInTheShownUnit() {
        assertEquals("60°", Shown.temp(15.6, true))   // 60.1 °F
        assertEquals("15°", Shown.temp(15.6, false))
        assertEquals("30°", Shown.temp(-0.6, true))   // 30.9 °F
        assertEquals("-1°", Shown.temp(-0.6, false))
        assertEquals("0°", Shown.temp(0.2, false))
        assertEquals("0°", Shown.temp(-17.8, true))   // 0.0 °F
        assertEquals("7°", Shown.temp(-13.9, true))   // 7.0 °F
        assertEquals(null, Shown.temp(null, true))
        assertEquals("0%", Shown.pct(0))
        assertEquals("1%", Shown.pct(1))
        assertEquals("3%", Shown.pct(2))
        assertEquals("25%", Shown.pct(24))
        assertEquals("5%", Shown.pct(5))
        assertEquals("100%", Shown.pct(100))
    }
}
