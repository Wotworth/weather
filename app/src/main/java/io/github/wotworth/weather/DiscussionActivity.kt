package io.github.wotworth.weather

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.ScrollView
import io.github.wotworth.weather.Ui.Font
import io.github.wotworth.weather.WeatherTokens.Colors
import io.github.wotworth.weather.WeatherTokens.Layout
import io.github.wotworth.weather.WeatherTokens.Type
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * WX-10 (Chris, 9 Nov): the National Weather Service's forecast discussion for the town, in the
 * meteorologists' own words, laid out in sections. Fetched from weather.gov on opening if the kept
 * copy is over 30 minutes old; the kept copy is shown meanwhile and if the fetch fails.
 */
class DiscussionActivity : Activity() {
    private val app get() = application as App
    private lateinit var content: LinearLayout
    private val main = Handler(Looper.getMainLooper())
    private var busy = false

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
        render()
        val s = app.store
        if (!busy && app.secret.get() != null && System.currentTimeMillis() - s.discussionAt >= WeatherTokens.Data.DISCUSSION_FRESH_MS) {
            busy = true
            render()
            app.worker.execute {
                app.updater().discussion(System.currentTimeMillis(), LocalDate.now())
                main.post { busy = false; render() }
            }
        }
    }

    fun render() {
        val s = app.store
        content.removeAllViews()
        content.addView(Ui.appTitle(this))
        content.addView(Blocks.town(this, s.place), Ui.fill())
        content.addView(Blocks.header(this, "Forecast discussion"), Ui.fill())
        val text = s.discussion.takeIf { s.discussionOffice != null && s.discussionOffice == s.place.office }
        val issued = s.discussionIssued?.let { runCatching { OffsetDateTime.parse(it) }.getOrNull() }
        val by = text?.let { Nws.office(it) }
        val line = listOfNotNull(by?.let { "National Weather Service $it" },
            issued?.let { "issued " + DateTimeFormatter.ofPattern("EEE MMM d, h:mm a", Locale.US).format(it.atZoneSameInstant(Blocks.zone(s.place))) }).joinToString(" · ")
        if (line.isNotEmpty()) content.addView(Ui.text(this, line, Type.FOOTER, color = Colors.SECONDARY).apply { setPadding(0, Ui.dp(this@DiscussionActivity, 4f), 0, 0) })
        when {
            busy && text == null -> body("Getting the discussion from weather.gov…", Colors.SECONDARY)
            text == null -> body(s.discussionError?.let { "No discussion yet: $it" } ?: "No discussion yet.", Colors.SECONDARY)
        }
        if (busy && text != null) body("Checking weather.gov for a newer one…", Colors.SECONDARY)
        if (!busy && text != null) s.discussionError?.let { body("Couldn't update: $it", Colors.SECONDARY) }
        text?.let { t ->
            for (sec in Nws.sections(t)) {
                content.addView(Ui.text(this, sec.title, if (sec.sub) Type.DISCUSSION_SUBHEAD else Type.DISCUSSION_HEAD,
                    if (sec.sub) Font.Semibold else Font.Semilight, if (sec.sub) Colors.PRIMARY else Colors.SECONDARY).apply {
                    setPadding(0, Ui.dp(this@DiscussionActivity, if (sec.sub) 14f else Layout.SECTION_TOP), 0, Ui.dp(this@DiscussionActivity, 4f))
                })
                sec.note?.let { body(it, Colors.SECONDARY) }
                sec.paragraphs.forEach { body(it, Colors.PRIMARY, Type.DISCUSSION_BODY) }
            }
        }
    }

    private fun body(s: String, color: Int, size: Float = Type.BODY) = content.addView(Ui.text(this, s, size, color = color).apply {
        setPadding(0, Ui.dp(this@DiscussionActivity, 6f), 0, 0)
        setLineSpacing(0f, 1.15f)
        setTextIsSelectable(true)
    }, Ui.fill())
}
