package io.github.wotworth.weather.screenshots

import android.app.Activity
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.wotworth.weather.Accu
import io.github.wotworth.weather.App
import io.github.wotworth.weather.Blocks
import io.github.wotworth.weather.DayActivity
import io.github.wotworth.weather.Forecast
import io.github.wotworth.weather.Fixtures
import io.github.wotworth.weather.MainActivity
import io.github.wotworth.weather.MemorySecret
import io.github.wotworth.weather.Season
import io.github.wotworth.weather.SettingsActivity
import io.github.wotworth.weather.Ui
import io.github.wotworth.weather.WeatherTokens
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** The pages as CI draws them (the S25 is about 412 dp wide). Images: app/build/outputs/roborazzi. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h915dp-xxhdpi")
class ScreenshotTest {
    private lateinit var app: App

    @Before fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        App.testSecret = null
        app.secret = MemorySecret("abcdEFGH1234wxyz")
        app.store.fahrenheit = true
    }

    /** The fixtures' days and hours, moved to today and now so the page shows them. */
    private fun withForecast() {
        val s = app.store
        s.place = Accu.parseCities(Fixtures.read("cities-hastings.json"))[0]
        val today = LocalDate.now(ZoneId.of("America/New_York"))
        val days = Accu.parseDaily(Fixtures.read("daily-5day.json"))!!
        s.days = days.mapIndexed { i, d -> d.copy(date = today.plusDays(i.toLong())) }
        val hours = Accu.parseHourly(Fixtures.read("hourly-12hour.json"))!!
        val start = Instant.now().toEpochMilli() - hours[0].epochMs + 3_600_000L
        s.hours = hours.map { it.copy(epochMs = it.epochMs + start) }
        s.hoursAt = System.currentTimeMillis()
        s.fetchedAt = System.currentTimeMillis()
        s.error = null
    }

    private fun shoot(name: String, cls: Class<out android.app.Activity>) {
        val a = Robolectric.buildActivity(cls, Intent(app, cls)).setup().get()
        a.window.decorView.captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    @Test fun forecastF() { withForecast(); shoot("forecast-f", MainActivity::class.java) }

    /** The real reply from 9 Nov: evening hours show the moon. */
    @Test fun forecastRealNight() {
        withForecast()
        val s = app.store
        val today = LocalDate.now(ZoneId.of("America/New_York"))
        s.days = Accu.parseDaily(Fixtures.read("real-daily-5day.json"))!!.mapIndexed { i, d -> d.copy(date = today.plusDays(i.toLong())) }
        val hours = Accu.parseHourly(Fixtures.read("real-hourly-12hour.json"))!!
        val start = Instant.now().toEpochMilli() - hours[0].epochMs + 3_600_000L
        s.hours = hours.map { it.copy(epochMs = it.epochMs + start) }
        s.fahrenheit = false
        shoot("forecast-real-night-c", MainActivity::class.java)
    }

    @Test fun forecastC() { withForecast(); app.store.fahrenheit = false; shoot("forecast-c", MainActivity::class.java) }

    @Test fun forecastRainyToday() {
        withForecast()
        val s = app.store
        s.days = s.days.drop(3).mapIndexed { i, d -> d.copy(date = LocalDate.now(ZoneId.of("America/New_York")).plusDays(i.toLong())) }
        s.error = "no connection"
        shoot("forecast-rain-error", MainActivity::class.java)
    }

    @Test fun noKey() {
        app.secret = MemorySecret(null)
        app.store.days = emptyList(); app.store.hours = emptyList(); app.store.fetchedAt = 0
        shoot("no-key", MainActivity::class.java)
    }

    /** Chris, 9 Nov: the town and the top temperature were cut at the phone's text size. Chris's phone is 412 dp wide; text 30% larger. */
    @Test @Config(qualifiers = "w412dp-h915dp-xxhdpi", fontScale = 1.3f)
    fun forecastLargeText() { withForecast(); shoot("forecast-large-text", MainActivity::class.java) }

    /** WX-6.7: tapping a day. Today has its remaining hours; later days their day and night. */
    @Test fun dayToday() { withRealDays(); shootDay("day-today", 0) }

    @Test fun dayLater() { withRealDays(); shootDay("day-later", 2) }

    private fun withRealDays() {
        withForecast()
        val today = LocalDate.now(ZoneId.of("America/New_York"))
        app.store.days = Accu.parseDaily(Fixtures.read("real-daily-5day.json"))!!.mapIndexed { i, d -> d.copy(date = today.plusDays(i.toLong())) }
    }

    private fun shootDay(name: String, offset: Long) {
        val date = LocalDate.now(ZoneId.of("America/New_York")).plusDays(offset)
        val i = Intent(app, DayActivity::class.java).putExtra(DayActivity.DATE, date.toString())
        val a = Robolectric.buildActivity(DayActivity::class.java, i).setup().get()
        a.window.decorView.captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    /** WX-6.12 (Chris, 9 Nov): each condition's card in each season, in °F. */
    private fun season(season: Season, date: LocalDate) {
        val a = Robolectric.buildActivity(Activity::class.java).setup().get()
        val col = Ui.column(a).apply { setBackgroundColor(WeatherTokens.Colors.BACKGROUND); setPadding(Ui.dp(a, 16f), Ui.dp(a, 16f), Ui.dp(a, 16f), 0) }
        val names = mapOf(Forecast.Kind.SUNNY to "Sunny", Forecast.Kind.PARTLY_CLOUDY to "Partly cloudy", Forecast.Kind.CLOUDY to "Cloudy", Forecast.Kind.RAIN to "Rain", Forecast.Kind.SNOW to "Snow")
        val temps = mapOf(Season.WINTER to (2.0 to -6.0), Season.SPRING to (16.0 to 6.0), Season.SUMMER to (30.0 to 21.0), Season.FALL to (14.0 to 5.0))
        val (hi, lo) = temps.getValue(season)
        names.forEach { (k, n) ->
            val d = Forecast.Day(date, k, if (k == Forecast.Kind.SNOW) -1.0 else hi, if (k == Forecast.Kind.SNOW) -7.0 else lo, chance = 40, humidity = 60, cloud = 50, phrase = "$n · ${season.name.lowercase().replaceFirstChar { it.uppercase() }}")
            col.addView(Blocks.hero(a, d, true), Ui.fill())
            col.addView(Ui.gap(a, 12f))
        }
        a.setContentView(col)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        a.window.decorView.captureRoboImage("build/outputs/roborazzi/season-${season.name.lowercase()}.png")
    }

    @Test @Config(qualifiers = "w412dp-h1500dp-xxhdpi") fun seasonWinter() = season(Season.WINTER, LocalDate.of(2026, 12, 20))
    @Test @Config(qualifiers = "w412dp-h1500dp-xxhdpi") fun seasonSpring() = season(Season.SPRING, LocalDate.of(2027, 4, 5))
    @Test @Config(qualifiers = "w412dp-h1500dp-xxhdpi") fun seasonSummer() = season(Season.SUMMER, LocalDate.of(2027, 7, 15))
    @Test @Config(qualifiers = "w412dp-h1500dp-xxhdpi") fun seasonFall() = season(Season.FALL, LocalDate.of(2026, 10, 20))

    @Test fun settings() { withForecast(); shoot("settings", SettingsActivity::class.java) }
}
