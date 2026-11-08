package io.github.wotworth.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** WX-2.2, WX-2.3, WX-2.4: what may be sent, and where. */
class NetTest {
    private fun refused(url: String) = try { AccuNet.check(url); fail("allowed: $url") } catch (e: SecurityException) { }

    @Test fun theThreeRequestsAreAllowed() {
        AccuNet.check(Accu.dailyUrl("2100339"))
        AccuNet.check(Accu.hourlyUrl("2100339"))
        AccuNet.check(Accu.searchUrl("Hastings-on-Hudson"))
        AccuNet.check(Accu.searchUrl("Saint-Étienne"))
        assertEquals("https://dataservice.accuweather.com/forecasts/v1/daily/5day/2100339?details=true&metric=true&language=en-us", Accu.dailyUrl("2100339"))
    }

    @Test fun everythingElseIsRefused() {
        refused("http://dataservice.accuweather.com/forecasts/v1/daily/5day/1?details=true&metric=true")
        refused("https://example.com/forecasts/v1/daily/5day/1")
        refused("https://dataservice.accuweather.com.evil.com/forecasts/v1/daily/5day/1")
        refused("https://dataservice.accuweather.com:8443/forecasts/v1/daily/5day/1")
        refused("https://dataservice.accuweather.com/forecasts/v1/daily/10day/1")
        refused("https://dataservice.accuweather.com/currentconditions/v1/1")
        refused("https://dataservice.accuweather.com/locations/v1/cities/geoposition/search?q=40.99,-73.88")
        refused("https://dataservice.accuweather.com/forecasts/v1/daily/5day/1?details=true&metric=false")
        refused("https://dataservice.accuweather.com/forecasts/v1/daily/5day/1?apikey=abc")
        refused("https://dataservice.accuweather.com/forecasts/v1/daily/5day/1?details=true&details=true")
        refused("https://dataservice.accuweather.com/locations/v1/cities/search?q=40.99%2C-73.88")
        refused("https://user@dataservice.accuweather.com/forecasts/v1/daily/5day/1")
    }

    @Test fun searchTextRules() {
        assertTrue(Accu.validSearch("Hastings-on-Hudson"))
        assertTrue(Accu.validSearch("St. John's"))
        assertFalse(Accu.validSearch("10706"))
        assertFalse(Accu.validSearch("x"))
        assertFalse(Accu.validSearch("40.99,-73.88"))
    }

    /** The key travels only in the Authorization header: never in the URL. */
    @Test fun keyOnlyInTheHeader() {
        var seenUrl = ""; var seenKey = ""
        val old = AccuNet.transport
        AccuNet.transport = { u, k -> seenUrl = u.toString(); seenKey = k; "[]" }
        try {
            AccuNet.get(Accu.searchUrl("Paris"), "abcdEFGH1234")
        } finally { AccuNet.transport = old }
        assertFalse(seenUrl.contains("abcd"))
        assertEquals("abcdEFGH1234", seenKey)
        assertFalse(AccuNet.validApiKey("abc def ghi"))
        assertFalse(AccuNet.validApiKey("abcdefgh\r\nX: y"))
        try { AccuNet.get(Accu.searchUrl("Paris"), "bad key\n"); fail() } catch (e: SecurityException) { }
    }
}
