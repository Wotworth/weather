package io.github.wotworth.weather

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/** WX-4: the tile interface. Names are fixed: the launcher codes against them. */
object Tile {
    const val AUTHORITY = "io.github.wotworth.weather.tile"
    val BASE: Uri by lazy { Uri.parse("content://$AUTHORITY") }
    val STATE: Uri by lazy { Uri.parse("content://$AUTHORITY/state") }
    val DAYS: Uri by lazy { Uri.parse("content://$AUTHORITY/days") }

    val STATE_COLUMNS = arrayOf("version", "town", "units", "fetched_at", "source", "error")
    val DAY_COLUMNS = arrayOf("date", "kind", "high_c", "low_c")

    /** WX-4.1: the only callers. */
    val READERS = setOf("io.github.wotworth.wplauncher", "io.github.wotworth.wplauncher.preview")

    /** WX-4.1: the launcher's release key (SHA-256). */
    val LAUNCHER_CERT: ByteArray = hex("a76a8518adf98b85d4972f180338ca8c3582c1470d7305800206899195a6741d")

    fun hex(s: String) = ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    /** WX-4.1: a reader must be one of [READERS] and signed with the launcher's key. */
    fun allowed(caller: String?, signedWithLauncherKey: (String) -> Boolean): Boolean =
        caller != null && caller in READERS && signedWithLauncherKey(caller)

    fun stateRow(s: Store): Array<Any?> = arrayOf(s.version(), s.place.label, if (s.fahrenheit) "F" else "C", s.fetchedAt, "AccuWeather", s.error)

    /** WX-4.3: up to 5 days, oldest first; exact °C (WX-3.1), null when missing. */
    fun dayRows(s: Store): List<Array<Any?>> = s.days.sortedBy { it.date }.take(WeatherTokens.Data.DAYS)
        .map { arrayOf(it.date.toString(), it.kind.name, it.highC, it.lowC) }
}

/** WX-4.1: read-only, for the launcher only. */
class TileProvider : ContentProvider() {
    override fun onCreate() = true

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val pm = context!!.packageManager
        val ok = Tile.allowed(callingPackage) { pkg -> signatureCheck?.invoke(pkg) ?: pm.hasSigningCertificate(pkg, Tile.LAUNCHER_CERT, PackageManager.CERT_INPUT_SHA256) }
        if (!ok) throw SecurityException("The weather tile is for the WP launcher only")
        val store = (context!!.applicationContext as App).store
        return when (uri.pathSegments) {
            listOf("state") -> MatrixCursor(Tile.STATE_COLUMNS, 1).apply { addRow(Tile.stateRow(store)) }
            listOf("days") -> MatrixCursor(Tile.DAY_COLUMNS).apply { Tile.dayRows(store).forEach { addRow(it) } }
            else -> null
        }
    }

    override fun getType(uri: Uri) = "vnd.android.cursor.dir/vnd.io.github.wotworth.weather.tile"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException("read-only")
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException("read-only")
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException("read-only")

    companion object {
        /** Tests only: stands in for the signature check (Robolectric can't sign packages). */
        @Volatile var signatureCheck: ((String) -> Boolean)? = null
    }
}
