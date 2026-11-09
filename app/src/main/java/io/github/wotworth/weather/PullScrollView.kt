package io.github.wotworth.weather

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.ScrollView
import io.github.wotworth.weather.WeatherTokens.Motion

/**
 * WX-6.10 (Chris, 9 Nov): pull down at the top of the page and let go to refresh. The page
 * follows the finger at [Motion.PULL_RESISTANCE] of its travel, up to [Motion.PULL_MAX_DP];
 * letting go past [Motion.PULL_TRIGGER_DP] calls [onPull]. No Material spinner: [onProgress]
 * gets 0–1 so the page can show its own hint.
 */
class PullScrollView(c: Context) : ScrollView(c) {
    var onPull: () -> Unit = {}
    var onProgress: (Float) -> Unit = {}
    private val slop = ViewConfiguration.get(c).scaledTouchSlop
    private var startY = 0f
    private var pulling = false
    private var offset = 0f

    private fun px(dp: Float) = dp * resources.displayMetrics.density

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { startY = e.y; pulling = false }
            MotionEvent.ACTION_MOVE -> if (scrollY == 0 && e.y - startY > slop) { pulling = true; startY = e.y; return true }
        }
        return super.onInterceptTouchEvent(e)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!pulling) {
            if (e.actionMasked == MotionEvent.ACTION_MOVE && scrollY == 0 && e.y - startY > slop) { pulling = true; startY = e.y }
            else return super.onTouchEvent(e)
        }
        when (e.actionMasked) {
            MotionEvent.ACTION_MOVE -> show(pullOffset(e.y - startY, px(Motion.PULL_MAX_DP)))
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val fire = e.actionMasked == MotionEvent.ACTION_UP && offset >= px(Motion.PULL_TRIGGER_DP)
                pulling = false
                getChildAt(0)?.animate()?.translationY(0f)?.setDuration(Motion.PULL_BACK_MS)?.start()
                offset = 0f
                onProgress(0f)
                if (fire) onPull()
            }
        }
        return true
    }

    private fun show(y: Float) {
        offset = y
        getChildAt(0)?.translationY = y
        onProgress((y / px(Motion.PULL_TRIGGER_DP)).coerceAtMost(1f))
    }

    companion object {
        /** How far the page moves for a finger travel of [dy] px. */
        fun pullOffset(dy: Float, maxPx: Float) = (dy * Motion.PULL_RESISTANCE).coerceIn(0f, maxPx)
    }
}
