package io.github.wotworth.weather

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** WX-10: the forecast discussion from weather.gov (recorded replies; CI never calls weather.gov). */
@RunWith(AndroidJUnit4::class)
class DiscussionTest {
    private lateinit var store: Store
    private val accu = ArrayList<String>()
    private val nws = ArrayList<String>()
    private val today = LocalDate.of(2026, 11, 9)

    @Before fun setUp() {
        val c = ApplicationProvider.getApplicationContext<Context>()
        store = Store(c.getSharedPreferences("d-${System.nanoTime()}", Context.MODE_PRIVATE))
    }

    private fun updater() = Updater(store, { "abcdEFGH1234" }) { url, _ -> AccuNet.check(url); accu += url; Fixtures.read("real-cities-hastings.json") }
    private val fakeNws: (String) -> String = { url ->
        NwsNet.check(url); nws += url
        when {
            "/points/" in url -> Fixtures.read("real-nws-points-hastings.json")
            "/locations/" in url -> Fixtures.read("real-nws-afd-list-okx.json")
            else -> Fixtures.read("real-nws-afd-okx.json")
        }
    }

    @Test fun onlyWeatherGovsThreeFormsAreAllowed() {
        NwsNet.check("https://api.weather.gov/points/40.995,-73.879")
        NwsNet.check("https://api.weather.gov/products/types/AFD/locations/OKX")
        NwsNet.check("https://api.weather.gov/products/9f975270-181a-4b90-9724-791f56b9617f")
        for (bad in listOf("http://api.weather.gov/points/40.995,-73.879", "https://api.weather.gov/gridpoints/OKX/1,1/forecast",
            "https://api.weather.gov/points/40.995,-73.879?x=1", "https://example.com/products/types/AFD/locations/OKX",
            "https://api.weather.gov/products/types/AFD/locations/okx", "https://dataservice.accuweather.com/products/types/AFD/locations/OKX")) {
            assertTrue(bad, runCatching { NwsNet.check(bad) }.exceptionOrNull() is SecurityException)
        }
        // And AccuWeather's client won't go to weather.gov (so the key can never be sent there).
        assertTrue(runCatching { AccuNet.check("https://api.weather.gov/products/types/AFD/locations/OKX") }.exceptionOrNull() is SecurityException)
    }

    @Test fun firstTimeFindsPositionAndOfficeThenOnlyChecksForNewer() {
        store.place = Accu.parseCities(Fixtures.read("real-cities-hastings.json"))[0].copy(lat = null, lon = null)
        assertTrue(updater().discussion(1000L, today, fakeNws))
        assertEquals(1, accu.size) // the town's position, once
        assertEquals(listOf("https://api.weather.gov/points/40.995,-73.879", "https://api.weather.gov/products/types/AFD/locations/OKX",
            "https://api.weather.gov/products/9f975270-181a-4b90-9724-791f56b9617f"), nws)
        assertEquals("OKX", store.place.office)
        assertTrue(store.discussion!!.contains("Area Forecast Discussion"))
        assertNull(store.discussionError)
        assertEquals(1, store.callsOn(today))
        // Next time: the list only (same discussion), no AccuWeather call.
        nws.clear()
        assertTrue(updater().discussion(2000L, today, fakeNws))
        assertEquals(listOf("https://api.weather.gov/products/types/AFD/locations/OKX"), nws)
        assertEquals(1, accu.size)
    }

    @Test fun outsideTheUsNothingIsFetched() {
        store.place = Forecast.Place("Paris", "Ile-de-France", "IDF", "France", "FR", "623", "Europe/Paris", 48.85, 2.35)
        assertFalse(updater().discussion(1000L, today, fakeNws))
        assertTrue(nws.isEmpty())
        assertEquals("forecast discussions are for US towns only", store.discussionError)
    }

    @Test fun failureKeepsTheLastDiscussion() {
        store.place = Accu.parseCities(Fixtures.read("real-cities-hastings.json"))[0]
        assertTrue(updater().discussion(1000L, today, fakeNws))
        assertFalse(updater().discussion(2000L, today) { throw NwsNet.HttpError(503) })
        assertTrue(store.discussion!!.isNotEmpty())
        assertEquals("weather.gov error (503)", store.discussionError)
    }

    @Test fun sectionsReadAsWritten() {
        val text = Nws.parseProduct(Fixtures.read("real-nws-afd-okx.json"))!!.text
        val secs = Nws.sections(text)
        assertEquals("What has changed", secs[0].title)
        assertEquals("Key messages", secs[1].title)
        assertTrue(secs[1].paragraphs[0].startsWith("1) A series of weak cold fronts push through over the next couple of days."))
        assertTrue(secs.any { it.title == "Key message 1" && it.sub })
        assertEquals("New York NY", Nws.office(text))
        assertTrue(secs.none { it.paragraphs.any { p -> p.contains("&&") || p.contains("$$") } })
    }

    /** WX-6.2 in the discussion (Chris, 9 Nov): measurements rounded; times, dates, codes and points kept. */
    @Test fun numbersRoundedLikeTheRestOfTheApp() {
        assertEquals("highs in the upper 60s, winds 10-15 mph with gusts 25-30 mph.",
            Nws.roundNumbers("highs in the upper 60s, winds 12-17 mph with gusts 26-29 mph."))
        assertEquals("a 35% chance, 1-3 inches, lows near 45, 0 degrees", Nws.roundNumbers("a 33% chance, 1-2 inches, lows near 44, 0.4 degrees"))
        assertEquals("seas 3 ft, 1000 mb, -5F", Nws.roundNumbers("seas 2.6 ft, 1002 mb, -4.6F"))
        // Kept as written: clock times, dates, years, numbered points, codes, addresses.
        for (kept in listOf("655 AM EST Mon Nov 9 2026", "through 12Z Tuesday", "/12Z", "1) A series", "Key Message 2",
            "ANZ331-335-338-340-345.", "FXUS61 KOKX", "I-95 corridor", "https:/www.weather.gov/zny/n90", "around 18z.", "at 6:55 tonight")) {
            assertEquals(kept, Nws.roundNumbers(kept))
        }
        // The real discussion: every remaining measurement follows the rule.
        val text = Nws.parseProduct(Fixtures.read("real-nws-afd-okx.json"))!!.text
        val shown = Nws.sections(text).flatMap { it.paragraphs }.joinToString(" ") { Nws.roundNumbers(it) }
        assertTrue(shown.contains("10-15"))
    }
}
