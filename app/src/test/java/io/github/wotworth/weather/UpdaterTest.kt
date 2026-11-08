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
import java.time.ZoneId
import java.time.ZonedDateTime

/** WX-2.5, WX-2.7, WX-2.8: updates, call counting, and failures. */
@RunWith(AndroidJUnit4::class)
class UpdaterTest {
    private lateinit var store: Store
    private val calls = ArrayList<String>()
    private var reply: (String) -> String = { url ->
        when {
            "/cities/search" in url -> Fixtures.read("cities-hastings.json")
            "/daily/" in url -> Fixtures.read("daily-5day.json")
            else -> Fixtures.read("hourly-12hour.json")
        }
    }
    private val today = LocalDate.of(2026, 11, 8)

    @Before fun setUp() {
        val c = ApplicationProvider.getApplicationContext<Context>()
        store = Store(c.getSharedPreferences("t-${System.nanoTime()}", Context.MODE_PRIVATE))
    }

    private fun updater(key: String? = "abcdEFGH1234") = Updater(store, { key }) { url, k ->
        AccuNet.check(url); calls += url; assertEquals(key, k); reply(url)
    }

    @Test fun defaultTownLookedUpOnceThenCached() {
        assertTrue(updater().daily(1000L, today))
        assertEquals(2, calls.size)
        assertTrue("/cities/search" in calls[0])
        assertEquals("2100339", store.place.key)
        assertEquals(5, store.days.size)
        assertEquals(1000L, store.fetchedAt)
        assertTrue(updater().daily(2000L, today))
        assertTrue(updater().hourly(2000L, today))
        assertEquals(4, calls.size)
        assertEquals(1, calls.count { "/cities/search" in it })
        assertEquals(4, store.callsOn(today))
        assertEquals(0, store.callsOn(today.plusDays(1)))
    }

    @Test fun failureKeepsTheLastGoodForecast() {
        updater().daily(1000L, today)
        val before = store.days
        reply = { throw AccuNet.HttpError(401) }
        assertFalse(updater().daily(2000L, today))
        assertEquals(before, store.days)
        assertEquals(1000L, store.fetchedAt)
        assertEquals("AccuWeather didn't accept the key (401)", store.error)
        assertFalse(store.error!!.contains("abcd"))
        reply = { "<html>oops</html>" }
        assertFalse(updater().daily(3000L, today))
        assertEquals("reply unreadable", store.error)
        assertEquals(before, store.days)
        reply = { Fixtures.read("daily-5day.json") }
        assertTrue(updater().daily(4000L, today))
        assertNull(store.error)
    }

    @Test fun hourlyErrorsDontTouchTheTile() {
        updater().daily(1000L, today)
        reply = { throw java.net.UnknownHostException() }
        assertFalse(updater().hourly(2000L, today))
        assertNull(store.error)
        assertEquals("no connection", store.hourError)
    }

    @Test fun noKeyNoCalls() {
        assertFalse(updater(key = null).daily(1000L, today))
        assertEquals(0, calls.size)
        assertEquals(0, store.callsOn(today))
    }

    @Test fun dailyCapStopsCalls() {
        updater().daily(1000L, today)
        repeat(WeatherTokens.Data.DAILY_CALL_CAP) { updater().hourly(1000L, today) }
        assertEquals(WeatherTokens.Data.DAILY_CALL_CAP, calls.size)
        assertEquals(WeatherTokens.Data.DAILY_CALL_CAP, store.callsOn(today))
        assertTrue(store.hourError!!.contains("limit"))
        // A new day starts again.
        assertTrue(updater().daily(1000L, today.plusDays(1)))
    }

    @Test fun search() {
        reply = { Fixtures.read("cities-paris.json") }
        val (found, note) = updater().search("Paris", today)
        assertEquals(2, found.size)
        assertNull(note)
        assertEquals("use letters, spaces, . ' and - only", updater().search("10706", today).second)
        assertEquals(1, calls.size)
    }

    @Test fun slots() {
        val z = ZoneId.of("America/New_York")
        fun t(h: Int, m: Int) = ZonedDateTime.of(2026, 11, 8, h, m, 0, 0, z)
        assertEquals(t(0, 0), Updater.lastSlot(t(5, 59)))
        assertEquals(t(6, 0), Updater.lastSlot(t(6, 0)))
        assertEquals(t(18, 0), Updater.lastSlot(t(23, 59)))
        assertEquals(t(12, 0), Updater.nextSlot(t(6, 0)))
        assertEquals(ZonedDateTime.of(2026, 11, 9, 0, 0, 0, 0, z), Updater.nextSlot(t(18, 1)))
        assertTrue(Updater.due(t(11, 59).toInstant().toEpochMilli(), t(12, 1)))
        assertFalse(Updater.due(t(12, 0).toInstant().toEpochMilli(), t(17, 59)))
        assertTrue(Updater.hourlyDue(0L, WeatherTokens.Data.HOURLY_FRESH_MS))
        assertFalse(Updater.hourlyDue(1L, WeatherTokens.Data.HOURLY_FRESH_MS))
    }

    @Test fun storedFormsRoundTrip() {
        val days = Accu.parseDaily(Fixtures.read("daily-fog-missing.json"))!!
        store.days = days
        assertEquals(days.map { it.copy(highC = it.highC?.let { v -> Math.round(v * 100) / 100.0 }) }, store.days)
        val hours = Accu.parseHourly(Fixtures.read("hourly-12hour.json"))!!
        store.hours = hours
        assertEquals(hours, store.hours)
        val paris = Accu.parseCities(Fixtures.read("cities-paris.json"))[0]
        store.place = paris
        assertEquals(paris, store.place)
    }
}
