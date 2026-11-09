package io.github.wotworth.weather

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.database.ContentObserver
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import io.github.wotworth.weather.Ui.Font
import io.github.wotworth.weather.WeatherTokens.Colors
import io.github.wotworth.weather.WeatherTokens.Layout
import io.github.wotworth.weather.WeatherTokens.Type
import java.time.LocalDate

/**
 * WX-6: the forecast page. The forecast is the page: today in a block coloured like the live
 * tile, then the next 12 hours, then 5 days. Tapping today or a day opens the day page (WX-6.7).
 * Settings sit behind the app bar's "…".
 */
class MainActivity : Activity() {
    private val app get() = application as App
    private lateinit var content: LinearLayout
    private val main = Handler(Looper.getMainLooper())
    private lateinit var hint: android.widget.TextView
    private lateinit var spinner: View
    private var spin: ObjectAnimator? = null
    private val stopSpin = Runnable { stopSpinning() }
    private val observer = object : ContentObserver(main) { override fun onChange(selfChange: Boolean) { stopSpinning(); render() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this).apply { setBackgroundColor(Colors.BACKGROUND) }
        content = Ui.column(this)
        // WX-6.10: the hint behind the page, shown as it is pulled down.
        hint = Ui.text(this, "PULL TO REFRESH", Type.FOOTER, Font.Semibold, Colors.SECONDARY, Type.APP_TITLE_SPACING).apply { alpha = 0f; gravity = Gravity.CENTER_HORIZONTAL }
        root.addView(hint, FrameLayout.LayoutParams(-1, -2, Gravity.TOP))
        val scroll = PullScrollView(this).apply {
            isVerticalScrollBarEnabled = false; overScrollMode = View.OVER_SCROLL_NEVER; addView(content)
            onPull = { refresh() }
            onProgress = { p ->
                hint.alpha = p
                hint.text = if (p >= 1f) "RELEASE TO REFRESH" else "PULL TO REFRESH"
            }
        }
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        val bar = appBar()
        root.addView(bar, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
        root.setOnApplyWindowInsetsListener { _, insets ->
            val sys = insets.getInsets(WindowInsets.Type.systemBars())
            content.setPadding(Ui.dp(this, Layout.SIDE), sys.top + Ui.dp(this, Layout.TOP), Ui.dp(this, Layout.SIDE), sys.bottom + Ui.dp(this, Layout.BOTTOM))
            bar.setPadding(0, 0, 0, sys.bottom)
            hint.setPadding(0, sys.top + Ui.dp(this, Layout.PULL_HINT_TOP), 0, 0)
            insets
        }
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        contentResolver.registerContentObserver(Tile.BASE, true, observer)
        render()
        // WX-2.5: updates run every 30 minutes; on opening, one runs now if the last is older.
        if (app.secret.get() != null) App.schedule(this, now = false)
    }

    override fun onPause() {
        contentResolver.unregisterContentObserver(observer)
        super.onPause()
    }

    override fun onDestroy() {
        main.removeCallbacks(stopSpin)
        spin?.cancel()
        super.onDestroy()
    }

    /** Refresh (the button or a pull): the icon turns until the update comes back (WX-6.9). */
    fun refresh() {
        if (app.secret.get() == null) { openSettings(); return }
        startSpinning()
        App.schedule(this, now = true)
        render()
    }

    /** WX-6.9 (Chris, 9 Nov): the refresh icon turns so the press visibly registered. */
    private fun startSpinning() {
        main.removeCallbacks(stopSpin)
        if (spin?.isRunning != true) {
            spin = ObjectAnimator.ofFloat(spinner, View.ROTATION, 0f, 360f).apply {
                duration = WeatherTokens.Motion.SPIN_MS
                interpolator = LinearInterpolator()
                repeatCount = ValueAnimator.INFINITE
                start()
            }
        }
        main.postDelayed(stopSpin, WeatherTokens.Motion.SPIN_TIMEOUT_MS)
    }

    /** Stops at the end of the current turn, so the icon never stops crooked. */
    private fun stopSpinning() {
        main.removeCallbacks(stopSpin)
        spin?.let { if (it.isRunning) it.repeatCount = 0 }
    }

    /** Tests: whether the refresh icon is turning. */
    val spinning get() = spin?.isRunning == true

    private fun openDay(d: LocalDate) = startActivity(Intent(this, DayActivity::class.java).putExtra(DayActivity.DATE, d.toString()))

    private fun openSettings() = startActivity(Intent(this, SettingsActivity::class.java))

    private fun appBar(): View {
        val bar = Ui.row(this).apply { setBackgroundColor(Colors.APP_BAR); gravity = Gravity.CENTER }
        fun item(icon: Int, desc: String, onClick: () -> Unit) = FrameLayout(this).apply {
            contentDescription = desc
            val img = Ui.icon(this@MainActivity, icon, Layout.APP_BAR_ICON)
            if (icon == R.drawable.fluent_arrow_sync) spinner = img
            addView(img, FrameLayout.LayoutParams(Ui.dp(this@MainActivity, Layout.APP_BAR_ICON), Ui.dp(this@MainActivity, Layout.APP_BAR_ICON), Gravity.CENTER))
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
        val today = LocalDate.now(Blocks.zone(place))
        val days = s.days.filter { !it.date.isBefore(today) }.take(WeatherTokens.Data.DAYS)
        val now = System.currentTimeMillis()
        val hours = s.hours.filter { it.epochMs >= now - 3_600_000L }.take(WeatherTokens.Data.HOURS)
        val hasKey = app.secret.get() != null

        content.removeAllViews()
        content.addView(Ui.appTitle(this))
        content.addView(Blocks.town(this, place), Ui.fill())
        content.addView(Ui.gap(this, Layout.HERO_TOP_GAP))

        val first = days.firstOrNull()
        when {
            first != null -> content.addView(Ui.pressable(Blocks.hero(this, first, f, if (first.date == today) null else Blocks.dayName(first.date, today),
                night = first.date == today && Blocks.isNight(s.hours, now)), { openDay(first.date) }), Ui.fill())
            !hasKey -> content.addView(empty("Add your AccuWeather key in settings to see the forecast.", "settings") { openSettings() })
            else -> content.addView(empty(if (s.error == null) "Getting the forecast…" else "No forecast yet: ${s.error}", "refresh") { refresh() })
        }

        if (hours.isNotEmpty()) {
            content.addView(Ui.section(this, "next ${hours.size} hours"))
            val strip = Ui.row(this).apply { gravity = Gravity.TOP }
            hours.forEach { strip.addView(Blocks.hourCell(this, it, f), Ui.wrap()) }
            content.addView(HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false; overScrollMode = View.OVER_SCROLL_NEVER; addView(strip) })
        }

        if (days.isNotEmpty()) {
            content.addView(Ui.section(this, "next ${days.size} days"))
            days.forEach { d -> content.addView(Blocks.dayRow(this, d, f, today) { openDay(d.date) }, Ui.fill()) }
        }

        // WX-10: the National Weather Service's forecast discussion (US towns).
        if (hasKey && place.isUs) {
            content.addView(Ui.gap(this, Layout.FOOTER_TOP))
            content.addView(Ui.button(this, "forecast discussion") { startActivity(Intent(this, DiscussionActivity::class.java)) }, Ui.wrap())
        }

        if (hasKey && s.fetchedAt > 0) content.addView(Blocks.footer(this, s))
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
