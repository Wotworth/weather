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

    /** WX-2.5 (Chris, 9 Nov): every 30 minutes; nothing within 20 minutes of the last try. */
    @Test fun dueEvery30Minutes() {
        val min = 60_000L
        assertTrue(Updater.due(0L, 1_000_000_000L))
        assertFalse(Updater.due(100 * min, 119 * min))
        assertTrue(Updater.due(100 * min, 120 * min))
        assertEquals(30L, WeatherTokens.Data.UPDATE_EVERY_MIN)
        // 2 calls every 30 minutes stays well under the app's cap and the plan's 500.
        assertTrue(2 * 24 * 60 / WeatherTokens.Data.UPDATE_EVERY_MIN < WeatherTokens.Data.DAILY_CALL_CAP)
    }

    @Test fun oneUpdateIsTheDaysAndTheHours() {
        updater().update(1000L, today)
        assertEquals(3, calls.size) // the default town's one-off lookup, 5 days, 12 hours
        assertEquals(5, store.days.size)
        assertEquals(12, store.hours.size)
        assertEquals(1000L, store.attemptAt)
        updater().update(2000L, today)
        assertEquals(5, calls.size)
    }

    @Test fun dayAndNightPartsStored() {
        store.days = Accu.parseDaily(Fixtures.read("real-daily-5day.json"))!!
        val d = store.days[0]
        assertEquals(Forecast.Part(Forecast.Kind.PARTLY_CLOUDY, "Breezy at times with decreasing clouds", 0, 34, 57), d.day)
        assertEquals(Forecast.Part(Forecast.Kind.SUNNY, "Partly cloudy and breezy", 2, 25, 60), d.night)
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
