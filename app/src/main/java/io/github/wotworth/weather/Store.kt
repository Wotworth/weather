package io.github.wotworth.weather

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.Locale
import java.util.zip.CRC32

/**
 * Everything the app keeps (app-private preferences, excluded from backups): the town and recent
 * towns, units, the last good forecast (WX-2.8), the last error, and today's call count (WX-2.7).
 */
class Store(private val p: SharedPreferences) {

    var place: Forecast.Place
        get() = decodePlace(p.getString("place", null)) ?: Forecast.DEFAULT_PLACE
        set(v) { p.edit().putString("place", encodePlace(v).toString()).apply() }

    var recent: List<Forecast.Place>
        get() = try {
            val a = JSONArray(p.getString("recent", null) ?: return listOf(place))
            (0 until a.length()).mapNotNull { decodePlace(a.optJSONObject(it)?.toString()) }.ifEmpty { listOf(place) }
        } catch (e: Exception) { listOf(place) }
        set(v) { p.edit().putString("recent", JSONArray(v.map { encodePlace(it) }).toString()).apply() }

    /** WX-3.5: °F (default) or °C. */
    var fahrenheit: Boolean
        get() = p.getBoolean("fahrenheit", true)
        set(v) { p.edit().putBoolean("fahrenheit", v).apply() }

    var days: List<Forecast.Day>
        get() = decodeDays(p.getString("days", null))
        set(v) { p.edit().putString("days", encodeDays(v)).apply() }

    var fetchedAt: Long
        get() = p.getLong("fetched_at", 0L)
        set(v) { p.edit().putLong("fetched_at", v).apply() }

    /** When the daily forecast was last tried (good or not). */
    var attemptAt: Long
        get() = p.getLong("attempt_at", 0L)
        set(v) { p.edit().putLong("attempt_at", v).apply() }

    var hours: List<Forecast.Hour>
        get() = decodeHours(p.getString("hours", null))
        set(v) { p.edit().putString("hours", encodeHours(v)).apply() }

    var hoursAt: Long
        get() = p.getLong("hours_at", 0L)
        set(v) { p.edit().putLong("hours_at", v).apply() }

    /** The last update's error (null after a good one), and when. */
    var error: String?
        get() = p.getString("error", null)
        set(v) { p.edit().putString("error", v?.take(WeatherTokens.Data.ERROR_CHARS)).putLong("error_at", if (v == null) 0L else System.currentTimeMillis()).apply() }
    val errorAt: Long get() = p.getLong("error_at", 0L)

    var hourError: String?
        get() = p.getString("hour_error", null)
        set(v) { p.edit().putString("hour_error", v?.take(WeatherTokens.Data.ERROR_CHARS)).apply() }

    /** WX-10: the last forecast discussion (its text, id, issue time as ISO text), when fetched, and its error. */
    var discussion: String?
        get() = p.getString("afd_text", null)
        set(v) { p.edit().putString("afd_text", v).apply() }
    var discussionId: String?
        get() = p.getString("afd_id", null)
        set(v) { p.edit().putString("afd_id", v).apply() }
    var discussionIssued: String?
        get() = p.getString("afd_issued", null)
        set(v) { p.edit().putString("afd_issued", v).apply() }
    var discussionOffice: String?
        get() = p.getString("afd_office", null)
        set(v) { p.edit().putString("afd_office", v).apply() }
    var discussionAt: Long
        get() = p.getLong("afd_at", 0L)
        set(v) { p.edit().putLong("afd_at", v).apply() }
    var discussionError: String?
        get() = p.getString("afd_error", null)
        set(v) { p.edit().putString("afd_error", v?.take(WeatherTokens.Data.ERROR_CHARS)).apply() }

    /** WX-2.7: calls on the phone's date [today]. */
    fun callsOn(today: LocalDate): Int = if (p.getString("calls_date", null) == today.toString()) p.getInt("calls", 0) else 0

    fun countCall(today: LocalDate) {
        val n = callsOn(today) + 1
        p.edit().putString("calls_date", today.toString()).putInt("calls", n).apply()
    }

    /** WX-4.2: changes whenever anything the tile shows changes. */
    fun version(): Long {
        val crc = CRC32()
        crc.update("${place.label}|${if (fahrenheit) "F" else "C"}|$fetchedAt|${error.orEmpty()}|${encodeDays(days)}".toByteArray())
        return crc.value
    }

    companion object {
        fun encodePlace(pl: Forecast.Place): JSONObject = JSONObject().put("name", pl.name).put("admin", pl.admin).put("adminId", pl.adminId)
            .put("country", pl.country).put("cc", pl.countryCode).put("key", pl.key ?: JSONObject.NULL).put("zone", pl.zone ?: JSONObject.NULL)
            .put("lat", pl.lat ?: JSONObject.NULL).put("lon", pl.lon ?: JSONObject.NULL).put("office", pl.office ?: JSONObject.NULL)

        fun decodePlace(s: String?): Forecast.Place? = try {
            val o = JSONObject(s ?: return null)
            Forecast.Place(o.getString("name"), o.optString("admin"), o.optString("adminId"), o.optString("country"), o.optString("cc"),
                if (o.isNull("key")) null else o.getString("key").takeIf { Accu.validKey(it) },
                if (o.isNull("zone")) null else o.optString("zone").ifBlank { null },
                if (o.isNull("lat")) null else o.optDouble("lat").takeIf { !it.isNaN() },
                if (o.isNull("lon")) null else o.optDouble("lon").takeIf { !it.isNaN() },
                if (o.isNull("office")) null else o.optString("office").takeIf { Nws.validOffice(it) })
                .takeIf { it.name.isNotBlank() }
        } catch (e: Exception) { null }

        private fun d(v: Double?) = v?.let { String.format(Locale.US, "%.2f", it) }.orEmpty()
        private fun i(v: Int?) = v?.toString().orEmpty()

        fun encodeDays(days: List<Forecast.Day>): String = JSONArray(days.map {
            JSONObject().put("date", it.date.toString()).put("kind", it.kind.name).put("high", d(it.highC)).put("low", d(it.lowC))
                .put("chance", i(it.chance)).put("humidity", i(it.humidity)).put("cloud", i(it.cloud)).put("phrase", it.phrase.orEmpty())
                .put("dayPart", encodePart(it.day)).put("nightPart", encodePart(it.night))
        }).toString()

        private fun encodePart(x: Forecast.Part?): Any = x?.let {
            JSONObject().put("kind", it.kind.name).put("phrase", it.phrase.orEmpty()).put("chance", i(it.chance)).put("cloud", i(it.cloud)).put("humidity", i(it.humidity))
        } ?: JSONObject.NULL

        private fun decodePart(o: JSONObject?): Forecast.Part? = try {
            o?.let { Forecast.Part(Forecast.Kind.valueOf(it.getString("kind")), it.optString("phrase").ifBlank { null }, it.optString("chance").toIntOrNull(), it.optString("cloud").toIntOrNull(), it.optString("humidity").toIntOrNull()) }
        } catch (e: Exception) { null }

        fun decodeDays(s: String?): List<Forecast.Day> = try {
            val a = JSONArray(s ?: return emptyList())
            (0 until a.length()).mapNotNull { k ->
                val o = a.getJSONObject(k)
                try {
                    Forecast.Day(LocalDate.parse(o.getString("date")), Forecast.Kind.valueOf(o.getString("kind")),
                        o.optString("high").toDoubleOrNull(), o.optString("low").toDoubleOrNull(), o.optString("chance").toIntOrNull(),
                        o.optString("humidity").toIntOrNull(), o.optString("cloud").toIntOrNull(), o.optString("phrase").ifBlank { null },
                        decodePart(o.optJSONObject("dayPart")), decodePart(o.optJSONObject("nightPart")))
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }

        fun encodeHours(hours: List<Forecast.Hour>): String = JSONArray(hours.map {
            JSONObject().put("t", it.epochMs).put("h", it.localHour).put("kind", it.kind.name).put("temp", d(it.tempC))
                .put("chance", i(it.chance)).put("humidity", i(it.humidity)).put("cloud", i(it.cloud)).put("day", it.daylight)
        }).toString()

        fun decodeHours(s: String?): List<Forecast.Hour> = try {
            val a = JSONArray(s ?: return emptyList())
            (0 until a.length()).mapNotNull { k ->
                val o = a.getJSONObject(k)
                try {
                    Forecast.Hour(o.getLong("t"), o.getInt("h"), Forecast.Kind.valueOf(o.getString("kind")), o.optString("temp").toDoubleOrNull(),
                        o.optString("chance").toIntOrNull(), o.optString("humidity").toIntOrNull(), o.optString("cloud").toIntOrNull(), o.optBoolean("day", true))
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }
    }
}
