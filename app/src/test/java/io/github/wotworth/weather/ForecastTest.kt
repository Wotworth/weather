package io.github.wotworth.weather

import io.github.wotworth.weather.Forecast.Kind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate

/** WX-3.2, WX-3.4, WX-3.6: the launcher's rules, and AccuWeather days classified by them. */
@RunWith(AndroidJUnit4::class)
class ForecastTest {
    /** The launcher's own cases (WeatherTest.openMeteoIconRules at a444997), unchanged. */
    @Test fun launcherRulesUnchanged() {
        val k = Forecast::classify
        val T = Forecast.Thresholds
        assertEquals(Kind.RAIN, k(T.PRECIP_CHANCE.toDouble(), T.PRECIP_MM, 0.0, 0.0, 30000.0))
        assertEquals(Kind.SUNNY, k(49.0, 20.0, 0.0, 30000.0, 36000.0))
        assertEquals(Kind.PARTLY_CLOUDY, k(90.0, 0.9, 0.0, 18000.0, 36000.0))
        assertEquals(Kind.SNOW, k(80.0, 4.0, 2.0, 0.0, 30000.0))
        assertEquals(Kind.RAIN, k(80.0, 4.0, 1.0, 0.0, 30000.0))
        assertEquals(Kind.SUNNY, k(0.0, 0.0, 0.0, 0.75 * 36000, 36000.0))
        assertEquals(Kind.PARTLY_CLOUDY, k(0.0, 0.0, 0.0, 0.35 * 36000, 36000.0))
        assertEquals(Kind.CLOUDY, k(0.0, 0.0, 0.0, 0.34 * 36000, 36000.0))
        assertEquals(Kind.CLOUDY, k(null, null, null, null, null))
    }

    @Test fun fiveKindsFromTheDailyReply() {
        val days = Accu.parseDaily(Fixtures.read("daily-5day.json"))!!
        assertEquals(listOf(Kind.SUNNY, Kind.PARTLY_CLOUDY, Kind.CLOUDY, Kind.RAIN, Kind.SNOW), days.map { it.kind })
        assertEquals(LocalDate.of(2026, 11, 8), days[0].date)
        // WX-3.1: exact °C, not rounded.
        assertEquals(15.6, days[0].highC!!, 1e-9)
        assertEquals(-6.1, days[4].lowC!!, 1e-9)
        // Whole day: the larger chance, the average humidity and cloud cover.
        assertEquals(80, days[3].chance)
        assertEquals(93, days[3].humidity)
        assertEquals(100, days[3].cloud)
    }

    @Test fun fogThatClearsAndMissingValues() {
        val days = Accu.parseDaily(Fixtures.read("daily-fog-missing.json"))!!
        // 8 of 10 hours of sun: the morning fog doesn't decide the day.
        assertEquals(Kind.SUNNY, days[0].kind)
        // No sunshine figure, no chance, no high: cloudy, and the high stays missing.
        assertEquals(Kind.CLOUDY, days[1].kind)
        assertNull(days[1].highC)
        assertEquals(3.3, days[1].lowC!!, 1e-9)
        assertNull(days[1].chance)
    }

    @Test fun unreadableReplies() {
        assertNull(Accu.parseDaily("not json"))
        assertNull(Accu.parseDaily("""{"DailyForecasts":[]}"""))
        assertNull(Accu.parseHourly("{}"))
        assertEquals(emptyList<Forecast.Place>(), Accu.parseCities("""{"Code":"Unauthorized"}"""))
    }

    @Test fun hours() {
        val h = Accu.parseHourly(Fixtures.read("hourly-12hour.json"))!!
        assertEquals(12, h.size)
        assertEquals(19, h[0].localHour)
        assertEquals(Kind.SUNNY, h[0].kind)        // 15% cloud
        assertEquals(Kind.CLOUDY, h[4].kind)        // 70% cloud
        assertEquals(Kind.RAIN, h[8].kind)
        assertEquals(0, h[5].localHour)
    }

    /** Replies recorded from AccuWeather on 9 Nov 2026 (Hastings-on-Hudson, key 344606). */
    @Test fun realReplies() {
        val town = Accu.parseCities(Fixtures.read("real-cities-hastings.json"))
        assertEquals("344606", Accu.pickDefault(town)!!.key)
        assertEquals("Hastings-on-Hudson, NY", town[0].label)
        val days = Accu.parseDaily(Fixtures.read("real-daily-5day.json"))!!
        assertEquals(5, days.size)
        // 6.1 of 10.2 h of sun, 6.1/10.1, 9.2/10.1, 5.7/10.05, 2.4/10.0 ("Intermittent clouds", "Partly sunny", "Sunny", "Times of sun and clouds", "Cloudy").
        assertEquals(listOf(Kind.PARTLY_CLOUDY, Kind.PARTLY_CLOUDY, Kind.SUNNY, Kind.PARTLY_CLOUDY, Kind.CLOUDY), days.map { it.kind })
        assertEquals(15.5, days[0].highC!!, 1e-9)
        assertEquals(7.9, days[0].lowC!!, 1e-9)
        assertEquals(59, days[0].humidity)   // day 57, night 61
        val hours = Accu.parseHourly(Fixtures.read("real-hourly-12hour.json"))!!
        assertEquals(12, hours.size)
        assertEquals(20, hours[0].localHour)
        assertFalse(hours[0].daylight)
        assertEquals(R.drawable.wx_moon, hours[0].icon)                  // 16% cloud at 8 PM
        assertEquals(R.drawable.wx_partly_cloudy_night, hours[1].icon)   // 26%
        assertTrue(hours[11].daylight)                                   // 7 AM
        assertEquals(R.drawable.wx_partly_cloudy, hours[11].icon)
    }

    @Test fun nightIcons() {
        assertEquals(R.drawable.wx_moon, Kind.SUNNY.nightIcon)
        assertEquals(R.drawable.wx_partly_cloudy_night, Kind.PARTLY_CLOUDY.nightIcon)
        for (k in listOf(Kind.CLOUDY, Kind.RAIN, Kind.SNOW)) assertEquals(k.icon, k.nightIcon)
    }

    @Test fun hourIcons() {
        val k = Forecast::classifyHour
        assertEquals(Kind.SUNNY, k(0, false, null, 25))
        assertEquals(Kind.PARTLY_CLOUDY, k(0, false, null, 26))
        assertEquals(Kind.PARTLY_CLOUDY, k(0, false, null, 65))
        assertEquals(Kind.CLOUDY, k(0, false, null, 66))
        assertEquals(Kind.CLOUDY, k(60, false, null, 66))   // a chance but no precipitation expected
        assertEquals(Kind.RAIN, k(50, true, "Rain", 100))
        assertEquals(Kind.SNOW, k(50, true, "Snow", 100))
        assertEquals(Kind.SNOW, k(50, true, "Mixed", 100))
        assertEquals(Kind.CLOUDY, k(null, false, null, null))
    }

    @Test fun townLabels() {
        val hastings = Accu.parseCities(Fixtures.read("cities-hastings.json")).single()
        assertEquals("Hastings-on-Hudson, NY", hastings.label)
        assertEquals("2100339", hastings.key)
        assertEquals("America/New_York", hastings.zone)
        val paris = Accu.parseCities(Fixtures.read("cities-paris.json"))
        assertEquals(listOf("Paris, France", "Paris, TX"), paris.map { it.label })
        // A US town whose state ID isn't a 2-letter code: from the state's name.
        assertEquals("Albany, NY", Forecast.Place("Albany", "New York", "", "United States", "US", "1").label)
        assertEquals("Hastings-on-Hudson, NY", Forecast.DEFAULT_PLACE.label)
        assertEquals("NY", Forecast.stateCode("new york"))
    }

    @Test fun defaultTownMatch() {
        assertEquals("2100339", Accu.pickDefault(Accu.parseCities(Fixtures.read("cities-hastings.json")))!!.key)
        assertNull(Accu.pickDefault(Accu.parseCities(Fixtures.read("cities-paris.json"))))
    }

    @Test fun recentTowns() {
        val towns = (1..7).map { Forecast.Place("T$it", "", "", "France", "FR", "$it") }
        var recent = emptyList<Forecast.Place>()
        towns.forEach { recent = Forecast.withRecent(recent, it) }
        assertEquals(listOf("T7", "T6", "T5", "T4", "T3"), recent.map { it.name })
        assertEquals(listOf("T5", "T7", "T6", "T4", "T3"), Forecast.withRecent(recent, towns[4]).map { it.name })
    }
}
