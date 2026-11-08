package io.github.wotworth.weather.fake

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import java.time.LocalDate

/**
 * WX-4.6, CI only: the Weather app's tile interface (docs/LAUNCHER-CHANGES.md) with fixed days,
 * one of each condition from today, so the launcher's tests can draw the tile without a key.
 * No caller check: it is signed with CI's debug key and never installed on a phone.
 */
class FakeWeather : ContentProvider() {
    override fun onCreate() = true

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val today = LocalDate.now()
        return when (uri.pathSegments) {
            listOf("state") -> MatrixCursor(arrayOf("version", "town", "units", "fetched_at", "source", "error"), 1).apply {
                addRow(arrayOf<Any?>(today.toEpochDay(), "Hastings-on-Hudson, NY", "F", System.currentTimeMillis(), "AccuWeather", null))
            }
            listOf("days") -> MatrixCursor(arrayOf("date", "kind", "high_c", "low_c")).apply {
                val kinds = listOf("SUNNY", "PARTLY_CLOUDY", "CLOUDY", "RAIN", "SNOW")
                val highs = listOf(22.2, 18.9, 12.5, 8.1, -0.6)
                val lows = listOf(10.5, 9.0, 4.4, null, -6.1)
                for (i in 0 until 5) addRow(arrayOf<Any?>(today.plusDays(i.toLong()).toString(), kinds[i], highs[i], lows[i]))
            }
            else -> null
        }
    }

    override fun getType(uri: Uri) = "vnd.android.cursor.dir/vnd.io.github.wotworth.weather.tile"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException("read-only")
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException("read-only")
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException("read-only")
}
