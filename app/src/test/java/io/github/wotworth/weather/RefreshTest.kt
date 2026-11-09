package io.github.wotworth.weather

import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/** WX-6.9, WX-6.10 (Chris, 9 Nov): the refresh icon turns; pulling down at the top refreshes. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w412dp-h915dp-xxhdpi")
class RefreshTest {
    private lateinit var app: App

    @Before fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        app.secret = MemorySecret("abcdEFGH1234wxyz")
        WorkManager.getInstance(app).cancelAllWork()
        WorkManager.getInstance(app).pruneWork()
    }

    private fun refreshQueued() = WorkManager.getInstance(app).getWorkInfosForUniqueWork(App.WORK_NOW).get()
        .any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.SUCCEEDED }

    private fun pull(a: MainActivity, dyPx: Float) {
        val root = (a.findViewById<ViewGroup>(android.R.id.content)).getChildAt(0) as FrameLayout
        val scroll = (0 until root.childCount).map { root.getChildAt(it) }.filterIsInstance<PullScrollView>().single()
        val t = SystemClock.uptimeMillis()
        fun ev(action: Int, y: Float) = MotionEvent.obtain(t, t, action, 200f, y, 0)
        scroll.dispatchTouchEvent(ev(MotionEvent.ACTION_DOWN, 300f))
        for (k in 1..10) scroll.dispatchTouchEvent(ev(MotionEvent.ACTION_MOVE, 300f + dyPx * k / 10))
        scroll.dispatchTouchEvent(ev(MotionEvent.ACTION_UP, 300f + dyPx))
    }

    @Test fun buttonSpinsUntilTheUpdateComesBack() {
        val a = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        assertFalse(a.spinning)
        a.refresh()
        assertTrue(a.spinning)
        assertTrue(refreshQueued())
        // The update comes back: the icon finishes its turn and stops.
        app.notifyTile()
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(WeatherTokens.Motion.SPIN_MS * 2))
        assertFalse(a.spinning)
    }

    @Test fun spinStopsAfterTimeoutWithoutNetwork() {
        val a = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        a.refresh()
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(WeatherTokens.Motion.SPIN_TIMEOUT_MS + WeatherTokens.Motion.SPIN_MS * 2))
        assertFalse(a.spinning)
    }

    @Test fun longPullRefreshes() {
        val a = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        WorkManager.getInstance(app).cancelAllWork(); WorkManager.getInstance(app).pruneWork()
        // xxhdpi: 3 px per dp. 600 px of finger = 300 px of page = 100 dp, past the 72 dp trigger.
        pull(a, 600f)
        assertTrue(a.spinning)
        assertTrue(refreshQueued())
    }

    @Test fun shortPullDoesNot() {
        val a = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        WorkManager.getInstance(app).cancelAllWork(); WorkManager.getInstance(app).pruneWork()
        pull(a, 300f) // 50 dp of page
        assertFalse(a.spinning)
        assertFalse(refreshQueued())
    }

    @Test fun pullFollowsTheFingerAtHalfUpToItsLimit() {
        assertEquals(50f, PullScrollView.pullOffset(100f, 420f))
        assertEquals(420f, PullScrollView.pullOffset(2000f, 420f))
        assertEquals(0f, PullScrollView.pullOffset(-50f, 420f))
    }
}
