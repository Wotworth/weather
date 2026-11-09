package io.github.wotworth.weather

import android.app.Activity
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.widget.ScrollView
import io.github.wotworth.weather.WeatherTokens.Colors
import io.github.wotworth.weather.WeatherTokens.Layout
import java.time.LocalDate

/**
 * WX-6.7 (Chris, 9 Nov): one day, opened by tapping it. Its block, then its daytime and overnight
 * halves, then whichever of the next 12 hours fall on it (AccuWeather's hourly forecast beyond 12
 * hours isn't in Chris's plan).
 */
class DayActivity : Activity() {
    private val app get() = application as App
    private lateinit var content: android.widget.LinearLayout
    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) { override fun onChange(selfChange: Boolean) { render() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = Ui.column(this)
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Colors.BACKGROUND); isVerticalScrollBarEnabled = false; overScrollMode = View.OVER_SCROLL_NEVER; addView(content)
        }
        scroll.setOnApplyWindowInsetsListener { _, insets ->
            val sys = insets.getInsets(WindowInsets.Type.systemBars())
            content.setPadding(Ui.dp(this, Layout.SIDE), sys.top + Ui.dp(this, Layout.TOP), Ui.dp(this, Layout.SIDE), sys.bottom + Ui.dp(this, Layout.TOP))
            insets
        }
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        contentResolver.registerContentObserver(Tile.BASE, true, observer)
        render()
    }

    override fun onPause() {
        contentResolver.unregisterContentObserver(observer)
        super.onPause()
    }

    fun render() {
        val s = app.store
        val f = s.fahrenheit
        val zone = Blocks.zone(s.place)
        val today = LocalDate.now(zone)
        val date = intent.getStringExtra(DATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today
        val d = s.days.firstOrNull { it.date == date }
        content.removeAllViews()
        content.addView(Ui.appTitle(this))
        content.addView(Blocks.town(this, s.place), Ui.fill())
        content.addView(Blocks.header(this, Blocks.dayName(date, today)), Ui.fill())
        content.addView(Ui.text(this, Blocks.monthDay(date), WeatherTokens.Type.SECTION, Ui.Font.Semilight, Colors.SECONDARY))
        content.addView(Ui.gap(this, Layout.HERO_TOP_GAP))
        if (d == null) {
            content.addView(Ui.text(this, "This day isn't in the forecast any more.", WeatherTokens.Type.BODY, color = Colors.SECONDARY))
            return
        }
        content.addView(Blocks.hero(this, d, f, night = date == today && Blocks.isNight(s.hours, System.currentTimeMillis())), Ui.fill())
        if (d.day != null || d.night != null) {
            content.addView(Ui.section(this, "day and night"))
            d.day?.let { content.addView(Blocks.partRow(this, "Daytime", it, night = false), Ui.fill()) }
            d.night?.let { content.addView(Blocks.partRow(this, "Overnight", it, night = true), Ui.fill()) }
        }
        val now = System.currentTimeMillis()
        val hours = Blocks.hoursOn(s.hours.filter { it.epochMs >= now - 3_600_000L }, date, zone)
        if (hours.isNotEmpty()) {
            content.addView(Ui.section(this, "hourly"))
            hours.forEach { content.addView(Blocks.hourRow(this, it, f), Ui.fill()) }
        }
        if (s.fetchedAt > 0) content.addView(Blocks.footer(this, s))
    }

    companion object { const val DATE = "date" }
}
