package io.github.wotworth.weather

import android.app.Activity
import android.content.Intent
import android.database.ContentObserver
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import io.github.wotworth.weather.Ui.Font
import io.github.wotworth.weather.WeatherTokens.Colors
import io.github.wotworth.weather.WeatherTokens.Layout
import io.github.wotworth.weather.WeatherTokens.Type
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * WX-6: the forecast page. The forecast is the page: today in a block coloured like the live
 * tile, then the next 12 hours, then 5 days. Settings sit behind the app bar's "…".
 */
class MainActivity : Activity() {
    private val app get() = application as App
    private lateinit var content: LinearLayout
    private var busy = false
    private val main = Handler(Looper.getMainLooper())
    private val observer = object : ContentObserver(main) { override fun onChange(selfChange: Boolean) { render() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this).apply { setBackgroundColor(Colors.BACKGROUND) }
        content = Ui.column(this)
        val scroll = ScrollView(this).apply { isVerticalScrollBarEnabled = false; overScrollMode = View.OVER_SCROLL_NEVER; addView(content) }
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        val bar = appBar()
        root.addView(bar, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
        root.setOnApplyWindowInsetsListener { _, insets ->
            val sys = insets.getInsets(WindowInsets.Type.systemBars())
            content.setPadding(Ui.dp(this, Layout.SIDE), sys.top + Ui.dp(this, Layout.TOP), Ui.dp(this, Layout.SIDE), sys.bottom + Ui.dp(this, Layout.BOTTOM))
            bar.setPadding(0, 0, 0, sys.bottom)
            insets
        }
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        contentResolver.registerContentObserver(Tile.BASE, true, observer)
        render()
        if (app.secret.get() != null) {
            App.schedule(this, now = false)
            if (Updater.hourlyDue(app.store.hoursAt, System.currentTimeMillis())) fetchHours()
        }
    }

    override fun onPause() {
        contentResolver.unregisterContentObserver(observer)
        super.onPause()
    }

    private fun fetchHours() {
        if (busy) return
        busy = true
        app.worker.execute {
            app.updater().hourly(System.currentTimeMillis(), LocalDate.now())
            main.post { busy = false; render() }
        }
    }

    private fun refresh() {
        if (app.secret.get() == null) { openSettings(); return }
        App.schedule(this, now = true)
        fetchHours()
        render()
    }

    private fun openSettings() = startActivity(Intent(this, SettingsActivity::class.java))

    private fun appBar(): View {
        val bar = Ui.row(this).apply { setBackgroundColor(Colors.APP_BAR); gravity = Gravity.CENTER }
        fun item(icon: Int, desc: String, onClick: () -> Unit) = FrameLayout(this).apply {
            contentDescription = desc
            addView(Ui.icon(this@MainActivity, icon, Layout.APP_BAR_ICON), FrameLayout.LayoutParams(Ui.dp(this@MainActivity, Layout.APP_BAR_ICON), Ui.dp(this@MainActivity, Layout.APP_BAR_ICON), Gravity.CENTER))
            Ui.pressable(this, onClick)
        }
        val size = LinearLayout.LayoutParams(Ui.dp(this, Layout.APP_BAR_BUTTON), Ui.dp(this, Layout.APP_BAR_HEIGHT))
        bar.addView(item(R.drawable.fluent_arrow_sync, "refresh") { refresh() }, size)
        bar.addView(item(R.drawable.fluent_more_horizontal, "settings") { openSettings() }, LinearLayout.LayoutParams(size))
        return bar
    }

    fun render() {
        val s = app.store
        val f = s.fahrenheit
        val place = s.place
        val zone = place.zone?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val days = s.days.filter { !it.date.isBefore(today) }.take(WeatherTokens.Data.DAYS)
        val now = System.currentTimeMillis()
        val hours = s.hours.filter { it.epochMs >= now - 3_600_000L }.take(WeatherTokens.Data.HOURS)
        val hasKey = app.secret.get() != null

        content.removeAllViews()
        content.addView(Ui.appTitle(this))
        content.addView(Ui.text(this, place.label.lowercase(Locale.ROOT), Type.TOWN, Font.Light).apply {
            isSingleLine = true; ellipsize = TextUtils.TruncateAt.END
            setPadding(0, Ui.dp(this@MainActivity, Layout.TITLE_TOWN_GAP), 0, 0)
        })
        content.addView(Ui.gap(this, Layout.HERO_TOP_GAP))

        val first = days.firstOrNull()
        when {
            first != null -> content.addView(hero(first, f, first.date == today))
            !hasKey -> content.addView(empty("Add your AccuWeather key in settings to see the forecast.", "settings") { openSettings() })
            else -> content.addView(empty(if (busy || s.error == null) "Getting the forecast…" else "No forecast yet: ${s.error}", "refresh") { refresh() })
        }

        if (hours.isNotEmpty()) {
            content.addView(Ui.section(this, "next ${hours.size} hours"))
            val strip = Ui.row(this).apply { gravity = Gravity.TOP }
            hours.forEach { strip.addView(hourCell(it, f), LinearLayout.LayoutParams(Ui.dp(this, Layout.HOUR_WIDTH), -2)) }
            content.addView(HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false; overScrollMode = View.OVER_SCROLL_NEVER; addView(strip) })
        }

        if (days.isNotEmpty()) {
            content.addView(Ui.section(this, "next ${days.size} days"))
            days.forEach { content.addView(dayRow(it, f, it.date == today), Ui.fill()) }
        }

        if (hasKey && s.fetchedAt > 0) {
            val at = DateTimeFormatter.ofPattern("EEE h:mm a", Locale.US).format(Instant.ofEpochMilli(s.fetchedAt).atZone(ZoneId.systemDefault()))
            content.addView(Ui.text(this, "AccuWeather · updated ${at.lowercase(Locale.ROOT)}", Type.FOOTER, color = Colors.SECONDARY).apply {
                setPadding(0, Ui.dp(this@MainActivity, Layout.FOOTER_TOP), 0, 0)
            })
            s.error?.let { content.addView(Ui.text(this, "last update failed: $it", Type.FOOTER, color = Colors.SECONDARY).apply { setPadding(0, Ui.dp(this@MainActivity, 4f), 0, 0) }) }
        }
    }

    /** Today (or the first day): the tile's gradient, its icon, high and low, the phrase, and three figures. */
    private fun hero(d: Forecast.Day, f: Boolean, isToday: Boolean): View {
        val c = this
        val box = Ui.column(c).apply {
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, Palette.STOPS[d.kind] ?: Palette.NONE)
            val pad = Ui.dp(c, Layout.HERO_PAD)
            setPadding(pad, pad, pad, pad)
        }
        if (!isToday) box.addView(Ui.text(c, DateTimeFormatter.ofPattern("EEEE", Locale.US).format(d.date).lowercase(Locale.ROOT), Type.HERO_PHRASE, Font.Semilight))
        val top = Ui.row(c).apply { gravity = Gravity.BOTTOM }
        top.addView(Ui.icon(c, d.kind.icon, Layout.HERO_ICON).apply { contentDescription = d.kind.words })
        top.addView(Ui.gap(c, 1f).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(c, Layout.HERO_ICON_GAP), 1) })
        top.addView(Ui.text(c, Shown.temp(d.highC, f) ?: "–", Type.HERO_TEMP, Font.Light))
        top.addView(Ui.text(c, Shown.temp(d.lowC, f)?.let { "  $it" } ?: "", Type.HERO_LOW, Font.Light, Colors.LOW).apply { setPadding(0, 0, 0, Ui.dp(c, 12f)) })
        box.addView(top)
        box.addView(Ui.text(c, d.phrase ?: d.kind.words, Type.HERO_PHRASE, Font.Semilight).apply { setPadding(0, Ui.dp(c, 2f), 0, 0) })
        val stats = Ui.row(c).apply { setPadding(0, Ui.dp(c, Layout.STAT_GAP), 0, 0); gravity = Gravity.TOP }
        fun stat(label: String, v: String?) = Ui.column(c).apply {
            addView(Ui.text(c, label, Type.STAT_LABEL, Font.Semibold, Colors.LOW, WeatherTokens.Type.APP_TITLE_SPACING))
            addView(Ui.text(c, v ?: "–", Type.STAT_VALUE, Font.Light))
        }
        stats.addView(stat("RAIN", Shown.pct(d.chance)), Ui.weight())
        stats.addView(stat("HUMIDITY", Shown.pct(d.humidity)), Ui.weight())
        stats.addView(stat("CLOUD", Shown.pct(d.cloud)), Ui.weight())
        box.addView(stats)
        return box
    }

    private fun hourCell(h: Forecast.Hour, f: Boolean): View = Ui.column(this).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        val label = when (val x = h.localHour % 12) { 0 -> if (h.localHour == 0) "12 AM" else "12 PM"; else -> "$x ${if (h.localHour < 12) "AM" else "PM"}" }
        addView(Ui.text(context, label, Type.HOUR_TIME, color = Colors.SECONDARY))
        addView(Ui.icon(context, h.icon, Layout.HOUR_ICON).apply { contentDescription = h.words; (layoutParams as LinearLayout.LayoutParams).setMargins(0, Ui.dp(context, 6f), 0, Ui.dp(context, 4f)) })
        addView(Ui.text(context, Shown.temp(h.tempC, f) ?: "–", Type.HOUR_TEMP, Font.Light))
        addView(Ui.text(context, Shown.pct(h.chance) ?: "", Type.HOUR_RAIN, color = Colors.SECONDARY).apply { setPadding(0, Ui.dp(context, 2f), 0, 0) })
    }

    private fun dayRow(d: Forecast.Day, f: Boolean, isToday: Boolean): View = Ui.row(this).apply {
        minimumHeight = Ui.dp(context, Layout.DAY_ROW_HEIGHT)
        val name = if (isToday) "today" else DateTimeFormatter.ofPattern("EEE", Locale.US).format(d.date).lowercase(Locale.ROOT)
        addView(Ui.text(context, name, Type.DAY_NAME, Font.Semilight), LinearLayout.LayoutParams(Ui.dp(context, Layout.DAY_NAME_WIDTH), -2))
        addView(Ui.icon(context, d.kind.icon, Layout.DAY_ICON).apply { contentDescription = d.kind.words })
        addView(Ui.gap(context, 1f).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(context, 16f), 1) })
        addView(Ui.text(context, Shown.temp(d.highC, f) ?: "–", Type.DAY_TEMP, Font.Light), LinearLayout.LayoutParams(Ui.dp(context, Layout.DAY_TEMP_WIDTH), -2))
        addView(Ui.text(context, Shown.temp(d.lowC, f) ?: "–", Type.DAY_TEMP, Font.Light, Colors.SECONDARY), LinearLayout.LayoutParams(0, -2, 1f))
        addView(Ui.text(context, Shown.pct(d.chance)?.let { "rain $it" } ?: "", Type.DAY_RAIN, color = Colors.SECONDARY))
    }

    private fun empty(message: String, action: String, onClick: () -> Unit): View = Ui.column(this).apply {
        background = GradientDrawable(GradientDrawable.Orientation.TL_BR, Palette.NONE)
        val pad = Ui.dp(context, Layout.HERO_PAD)
        setPadding(pad, pad, pad, pad)
        addView(Ui.text(context, message, Type.HERO_PHRASE, Font.Semilight))
        addView(Ui.gap(context, 14f))
        addView(Ui.button(context, action, onClick), Ui.wrap())
    }
}
