package io.github.wotworth.weather

import android.app.Activity
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import java.time.LocalDate

/** WX-6.12: on every card, a motif drawn bold never sits behind text or the condition icon. */
@RunWith(AndroidJUnit4::class)
class HeroTest {
    private val dates = mapOf(Season.WINTER to LocalDate.of(2026, 12, 20), Season.SPRING to LocalDate.of(2027, 4, 5),
        Season.SUMMER to LocalDate.of(2027, 7, 15), Season.FALL to LocalDate.of(2026, 10, 20))

    private fun check() {
        val a = Robolectric.buildActivity(Activity::class.java).setup().get()
        val bad = ArrayList<String>()
        for ((season, date) in dates) for (kind in Forecast.Kind.values()) for (title in listOf(null, "Tuesday")) {
            val phrase = if (title == null) "Sunny" else "Breezy at times with decreasing clouds and a shower late"
            val d = Forecast.Day(date, kind, 31.0, 22.0, 40, 60, 50, phrase)
            val box = Blocks.hero(a, d, true, title) as ViewGroup
            val w = (412 * a.resources.displayMetrics.density).toInt() - 2 * Ui.dp(a, 16f)
            box.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED)
            box.layout(0, 0, box.measuredWidth, box.measuredHeight)
            val card = box.background as CardDrawable
            val text = Blocks.textRects(box)
            if (text.size < 8 || card.avoid.size != text.size) { bad += "$season $kind: text not passed to the card"; continue }
            card.sceneRect()?.let { r -> if (text.any { RectF.intersects(it, r) }) bad += "$season $kind $title: scene behind text" }
            val full = CardLook.of(kind, date).sceneDp * a.resources.displayMetrics.density
            val sr = card.sceneRect()
            if (sr == null || sr.width() < full * 0.99f) bad += "$season $kind $title: corner scene not at full size"
            for (m in CardLook.of(kind, date).motifs) {
                if (!m.avoidText || card.alphaOf(m) <= WeatherTokens.Seasons.FAINT_ALPHA) continue
                val r = card.motifRect(m)
                if (text.any { RectF.intersects(it, r) }) bad += "$season $kind $title: motif at ${m.x},${m.y} behind text"
            }
        }
        assertTrue(bad.joinToString("\n"), bad.isEmpty())
        // Each season still shows plenty: at least half its motifs on a short-phrase sunny card.
        for ((season, date) in dates) {
            val box = Blocks.hero(a, Forecast.Day(date, Forecast.Kind.SUNNY, 31.0, 22.0, 40, 60, 50, "Sunny"), true) as ViewGroup
            val w = (412 * a.resources.displayMetrics.density).toInt() - 2 * Ui.dp(a, 16f)
            box.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED)
            box.layout(0, 0, box.measuredWidth, box.measuredHeight)
            val card = box.background as CardDrawable
            val ms = CardLook.of(Forecast.Kind.SUNNY, date).motifs
            val shown = ms.count { card.alphaOf(it) > 0f }
            assertTrue("$season shows only $shown of ${ms.size}", shown * 2 >= ms.size)
        }
    }

    @Test @Config(qualifiers = "w412dp-h915dp-xxhdpi") fun boldMotifsClearOfText() = check()

    @Test @Config(qualifiers = "w412dp-h915dp-xxhdpi", fontScale = 1.3f) fun boldMotifsClearOfTextLargeText() = check()
}
