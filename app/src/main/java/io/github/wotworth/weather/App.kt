package io.github.wotworth.weather

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

open class App : Application() {
    lateinit var store: Store
    lateinit var secret: Secret
    /** One update at a time, off the main thread. */
    val worker = Executors.newSingleThreadExecutor()

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("weather", Context.MODE_PRIVATE)
        store = Store(prefs)
        secret = if (testSecret != null) testSecret!! else KeystoreSecret(prefs)
        CrashLog.install(this)
        schedule(this, now = false)
    }

    fun updater() = Updater(store, { secret.get() })

    /** WX-4.4: tell the launcher the tile changed. */
    fun notifyTile() {
        contentResolver.notifyChange(Tile.BASE, null)
    }

    companion object {
        /** Tests only: a plain in-memory key store (Robolectric has no Android Keystore). */
        @Volatile var testSecret: Secret? = null

        const val WORK = "daily-forecast"

        /**
         * WX-2.5: the next daily update, at the next slot (midnight, 6 AM, noon, 6 PM), or [now]
         * (town changed, refresh, or a slot was missed). WorkManager waits for a network.
         */
        fun schedule(context: Context, now: Boolean) {
            val app = context.applicationContext as App
            val zNow = ZonedDateTime.now()
            // Due only if nothing was tried since the last slot: a failed try waits for the next
            // slot (or a refresh), so a bad key can't spend calls each time the app starts.
            val runNow = now || Updater.due(app.store.attemptAt, zNow)
            val delay = if (runNow) 0L else Duration.between(zNow, Updater.nextSlot(zNow)).toMillis().coerceAtLeast(0L)
            val req = OneTimeWorkRequestBuilder<DailyWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK, if (runNow) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP, req)
        }
    }
}

/** WX-2.5: the daily forecast in the background, then the next slot. */
class DailyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val app = applicationContext as App
        if (app.secret.get() != null) {
            app.updater().daily(System.currentTimeMillis(), LocalDate.now())
            app.notifyTile()
        }
        // The next slot (never "now" again: a failure waits for the next slot or a refresh, so a
        // bad key can't spend calls).
        val zNow = ZonedDateTime.now()
        val req = OneTimeWorkRequestBuilder<DailyWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInitialDelay(Duration.between(zNow, Updater.nextSlot(zNow)).toMillis().coerceAtLeast(60_000L), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(App.WORK, ExistingWorkPolicy.APPEND_OR_REPLACE, req)
        return Result.success()
    }
}
