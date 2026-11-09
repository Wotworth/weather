package io.github.wotworth.weather

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.time.LocalDate
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

        /** v0.1.4's four-a-day work; cancelled so it can't run beside the new schedule. */
        private const val OLD_WORK = "daily-forecast"
        const val WORK = "update-every-30-min"
        const val WORK_NOW = "update-now"

        private fun net() = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        /**
         * WX-2.5 (Chris, 9 Nov): the forecast and the next 12 hours every
         * [WeatherTokens.Data.UPDATE_EVERY_MIN] minutes, in the background, when there is a network.
         * [now]: also right away (refresh, town changed). Otherwise right away only if the last try
         * is older than the period (the app was opened after a missed update).
         */
        fun schedule(context: Context, now: Boolean) {
            val app = context.applicationContext as App
            val wm = WorkManager.getInstance(context)
            wm.cancelUniqueWork(OLD_WORK)
            val every = WeatherTokens.Data.UPDATE_EVERY_MIN
            wm.enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<UpdateWorker>(every, TimeUnit.MINUTES).setConstraints(net()).build())
            if (now || Updater.due(app.store.attemptAt, System.currentTimeMillis())) {
                val req = OneTimeWorkRequestBuilder<UpdateWorker>().setConstraints(net())
                    .setInputData(workDataOf(UpdateWorker.FORCE to now)).build()
                wm.enqueueUniqueWork(WORK_NOW, ExistingWorkPolicy.REPLACE, req)
            }
        }
    }
}

/**
 * WX-2.5: one update (the 5-day forecast, then the next 12 hours; 2 calls). Skipped if another
 * update was tried in the last [WeatherTokens.Data.UPDATE_SKIP_MIN] minutes, unless forced, so an
 * opening or a failed key can't spend extra calls.
 */
class UpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val app = applicationContext as App
        val nowMs = System.currentTimeMillis()
        if (app.secret.get() != null && (inputData.getBoolean(FORCE, false) || Updater.due(app.store.attemptAt, nowMs))) {
            app.updater().update(nowMs, LocalDate.now())
            app.notifyTile()
        }
        return Result.success()
    }

    companion object { const val FORCE = "force" }
}
