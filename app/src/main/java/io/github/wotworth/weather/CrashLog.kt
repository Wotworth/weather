package io.github.wotworth.weather

import android.content.Context
import java.io.File

/**
 * The last crash, for bug reports (settings → crash log → copy). Holds only the stack trace and
 * the version: the API key is never in an exception message (it is only ever in a header).
 */
object CrashLog {
    fun file(c: Context) = File(c.filesDir, "crash.txt")

    fun install(c: Context) {
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try { file(c).writeText("Weather ${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_DATE})\n${java.util.Date()}\n${e.stackTraceToString().take(8000)}") } catch (_: Exception) {}
            prev?.uncaughtException(t, e)
        }
    }

    fun read(c: Context): String? = file(c).takeIf { it.exists() }?.readText()
    fun clear(c: Context) { file(c).delete() }
}
