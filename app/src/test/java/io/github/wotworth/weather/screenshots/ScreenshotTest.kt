package io.github.wotworth.weather.screenshots

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.wotworth.weather.Accu
import io.github.wotworth.weather.App
import io.github.wotworth.weather.Fixtures
import io.github.wotworth.weather.MainActivity
import io.github.wotworth.weather.MemorySecret
import io.github.wotworth.weather.SettingsActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
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

    @Test fun settings() { withForecast(); shoot("settings", SettingsActivity::class.java) }
}
