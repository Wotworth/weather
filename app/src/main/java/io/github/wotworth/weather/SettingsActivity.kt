package io.github.wotworth.weather

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import io.github.wotworth.weather.Ui.Font
import io.github.wotworth.weather.WeatherTokens.Colors
import io.github.wotworth.weather.WeatherTokens.Layout
import io.github.wotworth.weather.WeatherTokens.Type
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Settings: the key (WX-2.1), town (WX-2.4), units (WX-3.5), calls and errors (WX-2.7, WX-2.8), crash log. */
class SettingsActivity : Activity() {
    private val app get() = application as App
    private lateinit var content: LinearLayout
    private val main = Handler(Looper.getMainLooper())
    private var matches: List<Forecast.Place> = emptyList()
    private var searchNote: String? = null
    private var searching = false
    private var keyNote: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = Ui.column(this)
        val scroll = ScrollView(this).apply { setBackgroundColor(Colors.BACKGROUND); isVerticalScrollBarEnabled = false; overScrollMode = View.OVER_SCROLL_NEVER; addView(content) }
        scroll.setOnApplyWindowInsetsListener { _, insets ->
            val sys = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
            content.setPadding(Ui.dp(this, Layout.SIDE), sys.top + Ui.dp(this, Layout.TOP), Ui.dp(this, Layout.SIDE), sys.bottom + Ui.dp(this, 32f))
            insets
        }
        setContentView(scroll)
        render()
    }

    private fun gap(h: Float = Layout.ROW_GAP) = content.addView(Ui.gap(this, h))
    private fun body(s: String, color: Int = Colors.PRIMARY) = content.addView(Ui.text(this, s, Type.BODY, color = color), Ui.fill())

    private fun changed() {
        app.notifyTile()
        render()
    }

    fun render() {
        val s = app.store
        content.removeAllViews()
        content.addView(Ui.appTitle(this))
        content.addView(Ui.text(this, "settings", Type.PAGE_HEADER, Font.Light))

        // WX-2.1: the key, shown only by its last 4 characters.
        content.addView(Ui.section(this, "accuweather key"))
        val key = app.secret.get()
        body(if (key == null) "Not set. Paste the key from your AccuWeather developer account." else "Saved: •••• ${key.takeLast(4)}")
        keyNote?.let { body(it, Colors.ERROR) }
        gap()
        val keyBox = Ui.input(this, if (key == null) "API key" else "new key", secret = true)
        content.addView(keyBox, Ui.fill())
        gap()
        val keyButtons = Ui.row(this)
        keyButtons.addView(Ui.button(this, "save key") {
            val v = keyBox.text.toString().trim()
            if (!AccuNet.validApiKey(v)) { keyNote = "That doesn't look like an AccuWeather key (letters and digits)."; render(); return@button }
            app.secret.set(v); keyNote = null; keyBox.setText("")
            App.schedule(this, now = true)
            changed()
        }, Ui.wrap())
        if (key != null) {
            keyButtons.addView(Ui.gap(this, 1f).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(context, 12f), 1) })
            keyButtons.addView(Ui.button(this, "remove") { app.secret.set(null); changed() }, Ui.wrap())
        }
        content.addView(keyButtons)

        // WX-2.4: town search, and the recent towns.
        content.addView(Ui.section(this, "town"))
        val recent = s.recent
        recent.forEach { p -> content.addView(choice(p.label, Forecast.same(p, s.place)) { choose(p) }, Ui.fill()) }
        gap()
        val searchBox = Ui.input(this, "search for a town", secret = false)
        searchBox.imeOptions = EditorInfo.IME_ACTION_SEARCH
        val doSearch = { search(searchBox.text.toString()) }
        searchBox.setOnEditorActionListener { _, id, _ -> if (id == EditorInfo.IME_ACTION_SEARCH) { doSearch(); true } else false }
        content.addView(searchBox, Ui.fill())
        gap()
        content.addView(Ui.button(this, if (searching) "searching…" else "search") { doSearch() }, Ui.wrap())
        searchNote?.let { gap(); body(it, Colors.SECONDARY) }
        matches.forEach { p -> content.addView(choice(p.full, false) { choose(p) }, Ui.fill()) }

        // WX-3.5
        content.addView(Ui.section(this, "units"))
        content.addView(choice("°F  fahrenheit", s.fahrenheit) { s.fahrenheit = true; changed() }, Ui.fill())
        content.addView(choice("°C  celsius", !s.fahrenheit) { s.fahrenheit = false; changed() }, Ui.fill())

        // WX-2.7, WX-2.8
        content.addView(Ui.section(this, "updates"))
        val fmt = DateTimeFormatter.ofPattern("EEE d MMM, h:mm a", Locale.US)
        fun at(ms: Long) = fmt.format(Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()))
        body("Calls today: ${s.callsOn(LocalDate.now())} (the app stops at ${WeatherTokens.Data.DAILY_CALL_CAP})")
        body(if (s.fetchedAt > 0) "Last forecast: ${at(s.fetchedAt)}" else "No forecast yet")
        body("The forecast updates at midnight, 6 AM, noon and 6 PM, and the next 12 hours when you open the app.", Colors.SECONDARY)
        s.error?.let { body("Last error: $it" + if (s.errorAt > 0) " (${at(s.errorAt)})" else "", Colors.ERROR) }
        s.hourError?.let { body("Hourly: $it", Colors.ERROR) }
        gap()
        content.addView(Ui.button(this, "update now") { App.schedule(this, now = true); body("Updating…", Colors.SECONDARY) }, Ui.wrap())

        // Bug reports.
        content.addView(Ui.section(this, "crash log"))
        val log = CrashLog.read(this)
        if (log == null) body("No crashes.", Colors.SECONDARY) else {
            content.addView(Ui.text(this, log.take(1200), 11f, color = Colors.SECONDARY), Ui.fill())
            gap()
            val r = Ui.row(this)
            r.addView(Ui.button(this, "copy") {
                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Weather crash log", log))
            }, Ui.wrap())
            r.addView(Ui.gap(this, 1f).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(context, 12f), 1) })
            r.addView(Ui.button(this, "clear") { CrashLog.clear(this); render() }, Ui.wrap())
            content.addView(r)
        }

        content.addView(Ui.section(this, "about"))
        body("Weather ${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_DATE}). Forecasts by AccuWeather.", Colors.SECONDARY)
        body("Privacy: no location. Only the town you search for and its AccuWeather location key are sent, to AccuWeather only. The key stays on this phone, encrypted.", Colors.SECONDARY)
    }

    /** A Windows Phone list choice: Light 22 sp; the chosen one in the accent colour. */
    private fun choice(label: String, on: Boolean, onClick: () -> Unit): View =
        Ui.pressable(Ui.text(this, label, Type.CHOICE, Font.Light, if (on) Colors.ACCENT_TEXT else Colors.PRIMARY).apply {
            setPadding(0, Ui.dp(context, 6f), 0, Ui.dp(context, 6f))
        }, onClick)

    private fun search(text: String) {
        if (searching) return
        if (app.secret.get() == null) { searchNote = "Save the key first: town search uses AccuWeather."; render(); return }
        searching = true; render()
        app.worker.execute {
            val (found, note) = app.updater().search(text.trim(), LocalDate.now())
            main.post { searching = false; matches = found; searchNote = note; render() }
        }
    }

    /** WX-2.5: a new town is fetched at once. */
    private fun choose(p: Forecast.Place) {
        val s = app.store
        if (!Forecast.same(p, s.place)) {
            s.place = p
            s.recent = Forecast.withRecent(s.recent, p)
            s.days = emptyList(); s.hours = emptyList(); s.fetchedAt = 0; s.hoursAt = 0; s.error = null
            App.schedule(this, now = true)
        }
        matches = emptyList(); searchNote = null
        changed()
    }
}
