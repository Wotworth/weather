package io.github.wotworth.weather

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import io.github.wotworth.weather.Ui.Font
import io.github.wotworth.weather.WeatherTokens.Colors
import io.github.wotworth.weather.WeatherTokens.Layout
import io.github.wotworth.weather.WeatherTokens.Type
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** WX-6: the pieces the forecast page and the day page share. */
object Blocks {
    private val DAY_NAME = DateTimeFormatter.ofPattern("EEEE", Locale.US)
    private val DAY_SHORT = DateTimeFormatter.ofPattern("EEE", Locale.US)
    private val MONTH_DAY = DateTimeFormatter.ofPattern("MMMM d", Locale.US)
    private val MONTH_DAY_SHORT = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    /** WX-6.6 (Chris, 9 Nov): day and month names capitalised. */
    fun dayName(d: LocalDate, today: LocalDate) = if (d == today) "Today" else DAY_NAME.format(d)
    fun dayShort(d: LocalDate, today: LocalDate) = if (d == today) "Today" else DAY_SHORT.format(d)
    fun monthDay(d: LocalDate): String = MONTH_DAY.format(d)
    fun monthDayShort(d: LocalDate): String = MONTH_DAY_SHORT.format(d)

    fun zone(p: Forecast.Place): ZoneId = p.zone?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: ZoneId.systemDefault()

    fun hourLabel(h: Int) = when (val x = h % 12) { 0 -> if (h == 0) "12 AM" else "12 PM"; else -> "$x ${if (h < 12) "AM" else "PM"}" }

    /** WX-6.6 (Chris, 9 Nov): the town's own name, capitalised as AccuWeather writes it, no state; shrinks to fit. */
    fun town(c: Context, p: Forecast.Place) = FitText(c, Type.TOWN).apply {
        text = p.name
        typeface = Ui.font(c, Font.Light)
        setTextColor(Colors.PRIMARY)
    }

    /** Big one-line header (the day page). */
    fun header(c: Context, s: String) = FitText(c, Type.PAGE_HEADER).apply {
        text = s
        typeface = Ui.font(c, Font.Light)
        setTextColor(Colors.PRIMARY)
    }

    /** WX-6.8 (Chris, 9 Nov): every percentage says what it is. */
    fun precip(v: Int?) = Shown.pct(v)?.let { "precip $it" }
    fun cloud(v: Int?) = Shown.pct(v)?.let { "cloud $it" }

    /**
     * A day's block: the tile's gradient, its icon, high and low on one line that shrinks to fit,
     * the phrase, and precipitation, humidity and cloud cover.
     */
    fun hero(c: Context, d: Forecast.Day, f: Boolean, title: String? = null): View {
        val box = Ui.column(c).apply {
            background = Palette.background(c, d.kind, d.date)
            val pad = Ui.dp(c, Layout.HERO_PAD)
            setPadding(pad, pad, pad, pad)
        }
        title?.let { box.addView(Ui.text(c, it, Type.HERO_PHRASE, Font.Semilight)) }
        val top = Ui.row(c).apply { gravity = Gravity.CENTER_VERTICAL }
        top.addView(Ui.icon(c, d.kind.icon, Layout.HERO_ICON).apply { contentDescription = d.kind.words })
        top.addView(Ui.gap(c, 1f).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(c, Layout.HERO_ICON_GAP), 1) })
        top.addView(temps(c, d, f), Ui.weight())
        box.addView(top)
        box.addView(Ui.text(c, d.phrase ?: d.kind.words, Type.HERO_PHRASE, Font.Semilight).apply { setPadding(0, Ui.dp(c, 2f), 0, 0) })
        val stats = Ui.row(c).apply { setPadding(0, Ui.dp(c, Layout.STAT_GAP), 0, 0); gravity = Gravity.TOP }
        stats.addView(stat(c, "PRECIPITATION", Shown.pct(d.chance)), Ui.weight())
        stats.addView(stat(c, "HUMIDITY", Shown.pct(d.humidity)), Ui.weight())
        stats.addView(stat(c, "CLOUD COVER", Shown.pct(d.cloud)), Ui.weight())
        box.addView(stats)
        return box
    }

    /** High and low as one line: the low smaller and dimmer, on the same baseline. */
    private fun temps(c: Context, d: Forecast.Day, f: Boolean): View {
        val s = SpannableStringBuilder(Shown.temp(d.highC, f) ?: "–")
        Shown.temp(d.lowC, f)?.let {
            val start = s.length
            s.append("  ").append(it)
            s.setSpan(RelativeSizeSpan(Type.HERO_LOW / Type.HERO_TEMP), start, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            s.setSpan(ForegroundColorSpan(Colors.LOW), start, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return FitText(c, Type.HERO_TEMP).apply { text = s; typeface = Ui.font(c, Font.Light); setTextColor(Colors.PRIMARY) }
    }

    fun stat(c: Context, label: String, v: String?) = Ui.column(c).apply {
        addView(FitText(c, Type.STAT_LABEL).apply {
            text = label; typeface = Ui.font(c, Font.Semibold); setTextColor(Colors.LOW); letterSpacing = Type.APP_TITLE_SPACING
        }, Ui.fill())
        addView(Ui.text(c, v ?: "–", Type.STAT_VALUE, Font.Light))
    }

    /** One hour in the strip: time, icon, temperature, precipitation, cloud cover. */
    fun hourCell(c: Context, h: Forecast.Hour, f: Boolean): View = Ui.column(c).apply {
        gravity = Gravity.START
        minimumWidth = Ui.dp(c, Layout.HOUR_WIDTH)
        setPadding(0, 0, Ui.dp(c, Layout.HOUR_GAP), 0)
        addView(one(c, hourLabel(h.localHour), Type.HOUR_TIME, color = Colors.SECONDARY), Ui.wrap())
        addView(Ui.icon(c, h.icon, Layout.HOUR_ICON).apply { contentDescription = h.words; (layoutParams as LinearLayout.LayoutParams).setMargins(0, Ui.dp(c, 6f), 0, Ui.dp(c, 4f)) })
        addView(one(c, Shown.temp(h.tempC, f) ?: "–", Type.HOUR_TEMP, Font.Light), Ui.wrap())
        addView(one(c, precip(h.chance) ?: "", Type.HOUR_RAIN, color = Colors.SECONDARY).apply { setPadding(0, Ui.dp(c, 2f), 0, 0) }, Ui.wrap())
        addView(one(c, cloud(h.cloud) ?: "", Type.HOUR_RAIN, color = Colors.SECONDARY), Ui.wrap())
    }

    /** Text that never wraps (it sizes its parent instead). */
    private fun one(c: Context, s: String, size: Float, f: Font = Font.Regular, color: Int = Colors.PRIMARY) =
        Ui.text(c, s, size, f, color).apply { isSingleLine = true }

    /** One hour as a row (the day page): time, icon, temperature, precipitation, cloud cover. */
    fun hourRow(c: Context, h: Forecast.Hour, f: Boolean): View = Ui.row(c).apply {
        minimumHeight = Ui.dp(c, Layout.HOUR_ROW_HEIGHT)
        addView(one(c, hourLabel(h.localHour), Type.DAY_RAIN, color = Colors.SECONDARY).apply { minWidth = Ui.dp(c, Layout.HOUR_ROW_TIME) })
        addView(Ui.icon(c, h.icon, Layout.HOUR_ICON).apply { contentDescription = h.words })
        addView(Ui.gap(c, 1f).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(c, 16f), 1) })
        addView(one(c, Shown.temp(h.tempC, f) ?: "–", Type.HOUR_TEMP, Font.Light), LinearLayout.LayoutParams(0, -2, 1f))
        addView(figures(c, h.chance, h.cloud))
    }

    /** "precip 25%" over "cloud 30%", right-aligned. */
    fun figures(c: Context, chance: Int?, cloudPct: Int?): View = Ui.column(c).apply {
        gravity = Gravity.END
        addView(one(c, precip(chance) ?: "", Type.DAY_RAIN, color = Colors.SECONDARY), Ui.wrap())
        addView(one(c, cloud(cloudPct) ?: "", Type.DAY_RAIN, color = Colors.SECONDARY), Ui.wrap())
    }

    /** One of the next days: name over date, icon, high, low, figures. Tapping opens the day. */
    fun dayRow(c: Context, d: Forecast.Day, f: Boolean, today: LocalDate, onClick: () -> Unit): View = Ui.row(c).apply {
        minimumHeight = Ui.dp(c, Layout.DAY_ROW_HEIGHT)
        contentDescription = dayName(d.date, today)
        val name = Ui.column(c).apply {
            addView(one(c, dayShort(d.date, today), Type.DAY_NAME, Font.Semilight).apply { ellipsize = TextUtils.TruncateAt.END })
            addView(one(c, monthDayShort(d.date), Type.DAY_DATE, color = Colors.SECONDARY))
        }
        addView(name, LinearLayout.LayoutParams(Ui.dp(c, Layout.DAY_NAME_WIDTH), -2))
        addView(Ui.icon(c, d.kind.icon, Layout.DAY_ICON).apply { contentDescription = d.kind.words })
        addView(Ui.gap(c, 1f).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(c, 16f), 1) })
        addView(one(c, Shown.temp(d.highC, f) ?: "–", Type.DAY_TEMP, Font.Light).apply { minWidth = Ui.dp(c, Layout.DAY_TEMP_WIDTH) })
        addView(one(c, Shown.temp(d.lowC, f) ?: "–", Type.DAY_TEMP, Font.Light, Colors.SECONDARY))
        addView(View(c), LinearLayout.LayoutParams(0, 1, 1f))
        addView(figures(c, d.chance, d.cloud))
        Ui.pressable(this, onClick)
    }

    /** WX-6.7: the daytime or overnight half of a day. */
    fun partRow(c: Context, label: String, p: Forecast.Part, night: Boolean): View = Ui.row(c).apply {
        gravity = Gravity.TOP
        setPadding(0, Ui.dp(c, 6f), 0, Ui.dp(c, 6f))
        addView(Ui.icon(c, if (night) p.kind.nightIcon else p.kind.icon, Layout.DAY_ICON).apply { contentDescription = if (night) p.kind.nightWords else p.kind.words })
        addView(Ui.gap(c, 1f).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(c, 16f), 1) })
        val col = Ui.column(c)
        col.addView(Ui.text(c, label, Type.DAY_NAME, Font.Semilight))
        p.phrase?.let { col.addView(Ui.text(c, it, Type.BODY, color = Colors.PRIMARY).apply { setPadding(0, Ui.dp(c, 2f), 0, 0) }) }
        val figs = listOfNotNull(precip(p.chance), cloud(p.cloud), Shown.pct(p.humidity)?.let { "humidity $it" })
        if (figs.isNotEmpty()) col.addView(Ui.text(c, figs.joinToString("   "), Type.DAY_RAIN, color = Colors.SECONDARY).apply { setPadding(0, Ui.dp(c, 4f), 0, 0) })
        addView(col, LinearLayout.LayoutParams(0, -2, 1f))
    }

    /** The footer: source and time of the last forecast, and the last failure. */
    fun footer(c: Context, s: Store): View = Ui.column(c).apply {
        setPadding(0, Ui.dp(c, Layout.FOOTER_TOP), 0, 0)
        val at = DateTimeFormatter.ofPattern("EEE h:mm a", Locale.US).format(Instant.ofEpochMilli(s.fetchedAt).atZone(ZoneId.systemDefault()))
        addView(Ui.text(c, "AccuWeather · updated $at", Type.FOOTER, color = Colors.SECONDARY))
        s.error?.let { addView(Ui.text(c, "last update failed: $it", Type.FOOTER, color = Colors.SECONDARY).apply { setPadding(0, Ui.dp(c, 4f), 0, 0) }) }
    }

    /** Hours that fall on [date] where the town is. */
    fun hoursOn(hours: List<Forecast.Hour>, date: LocalDate, zone: ZoneId) =
        hours.filter { Instant.ofEpochMilli(it.epochMs).atZone(zone).toLocalDate() == date }
}
