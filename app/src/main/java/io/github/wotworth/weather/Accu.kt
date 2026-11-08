package io.github.wotworth.weather

import org.json.JSONArray
import org.json.JSONObject
import java.time.Duration
import java.time.OffsetDateTime
import java.util.Locale

/**
 * AccuWeather's Core Weather API: the requests this app makes, and how replies are read.
 * Paths, parameters and field names checked against developer.accuweather.com (OpenAPI pages
 * for "Daily Forecasts", "Hourly Forecasts" and "City search", read 8 Nov 2026).
 *
 * WX-2.4 privacy: the only things sent are the text typed in settings (city search, on "search"
 * only) and the chosen town's location key. No location, device or account details.
 */
object Accu {
    const val HOST = "dataservice.accuweather.com"
    const val LANGUAGE = "en-us"

    /** What may be searched for: letters, spaces, . ' - (2–60 characters). Nothing else is sent. */
    fun validSearch(text: String) = text.length in 2..60 && text.any { it.isLetter() } && text.all { it.isLetter() || it in " .'-" }

    /** A location key as AccuWeather gives them for cities: digits only. */
    fun validKey(k: String) = k.length in 1..12 && k.all { it in '0'..'9' }

    fun searchUrl(text: String): String {
        require(validSearch(text))
        return "https://$HOST/locations/v1/cities/search?q=${AccuNet.encode(text)}&language=$LANGUAGE"
    }

    /** WX-2.5: the 5-day daily forecast, details, metric. */
    fun dailyUrl(key: String): String {
        require(validKey(key))
        return "https://$HOST/forecasts/v1/daily/5day/$key?details=true&metric=true&language=$LANGUAGE"
    }

    /** WX-2.6: the next 12 hours, details, metric. */
    fun hourlyUrl(key: String): String {
        require(validKey(key))
        return "https://$HOST/forecasts/v1/hourly/12hour/$key?details=true&metric=true&language=$LANGUAGE"
    }

    // ------------------------------------------------------------------ replies

    /** City search matches: Key, LocalizedName, AdministrativeArea, Country, TimeZone. */
    fun parseCities(json: String): List<Forecast.Place> {
        val a = try { JSONArray(json) } catch (e: Exception) { return emptyList() }
        val out = ArrayList<Forecast.Place>()
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            val key = o.optString("Key").trim()
            val name = o.optString("LocalizedName").trim()
            if (!validKey(key) || name.isEmpty()) continue
            val admin = o.optJSONObject("AdministrativeArea")
            val country = o.optJSONObject("Country")
            val p = Forecast.Place(
                name, admin?.optString("LocalizedName")?.trim().orEmpty(), admin?.optString("ID")?.trim().orEmpty(),
                country?.optString("LocalizedName")?.trim().orEmpty(), country?.optString("ID")?.trim()?.uppercase(Locale.ROOT).orEmpty(),
                key, o.optJSONObject("TimeZone")?.optString("Name")?.takeIf { it.isNotBlank() },
            )
            if (out.none { it.key == p.key }) out += p
        }
        return out
    }

    /** The default town's match: Hastings-on-Hudson in New York State. */
    fun pickDefault(matches: List<Forecast.Place>): Forecast.Place? = matches.firstOrNull {
        it.isUs && it.name.equals(Forecast.DEFAULT_PLACE.name, ignoreCase = true) &&
            (it.adminId.equals("NY", ignoreCase = true) || it.admin.equals("New York", ignoreCase = true))
    }

    private fun JSONObject.num(vararg path: String): Double? {
        var o: JSONObject = this
        for (k in path.dropLast(1)) o = o.optJSONObject(k) ?: return null
        val last = path.last()
        if (!o.has(last) || o.isNull(last)) return null
        return o.optDouble(last).takeIf { !it.isNaN() }
    }

    private fun JSONObject.int(vararg path: String): Int? = num(*path)?.let { Math.round(it).toInt() }

    private fun sum(a: Double?, b: Double?): Double? = if (a == null && b == null) null else (a ?: 0.0) + (b ?: 0.0)
    private fun maxOf2(a: Int?, b: Int?): Int? = listOfNotNull(a, b).maxOrNull()
    private fun avg(a: Int?, b: Int?): Int? = listOfNotNull(a, b).takeIf { it.isNotEmpty() }?.let { Math.round(it.average()).toInt() }

    /**
     * WX-3.1, WX-3.2: days from the 5-day reply. The day's icon from the whole day (Day + Night):
     * chance = the larger PrecipitationProbability, precipitation = TotalLiquid sum (mm), snow =
     * Snow sum (cm), sunshine = HoursOfSun × 3600 s, daylight = Sun.Set − Sun.Rise. A missing
     * value stays missing.
     */
    fun parseDaily(json: String): List<Forecast.Day>? = try {
        val list = JSONObject(json).getJSONArray("DailyForecasts")
        (0 until minOf(list.length(), WeatherTokens.Data.DAYS)).map { i ->
            val d = list.getJSONObject(i)
            val day = d.optJSONObject("Day") ?: JSONObject()
            val night = d.optJSONObject("Night") ?: JSONObject()
            val chance = maxOf2(day.int("PrecipitationProbability"), night.int("PrecipitationProbability"))
            val liquid = sum(day.num("TotalLiquid", "Value"), night.num("TotalLiquid", "Value"))
            val snow = sum(day.num("Snow", "Value"), night.num("Snow", "Value"))
            val sunshine = d.num("HoursOfSun")?.let { it * 3600.0 }
            val sun = d.optJSONObject("Sun")
            val daylight = try {
                Duration.between(OffsetDateTime.parse(sun!!.getString("Rise")), OffsetDateTime.parse(sun.getString("Set"))).seconds.toDouble()
            } catch (e: Exception) { null }
            Forecast.Day(
                date = OffsetDateTime.parse(d.getString("Date")).toLocalDate(),
                kind = Forecast.classify(chance?.toDouble(), liquid, snow, sunshine, daylight),
                highC = d.num("Temperature", "Maximum", "Value"),
                lowC = d.num("Temperature", "Minimum", "Value"),
                chance = chance,
                humidity = avg(day.int("RelativeHumidity", "Average"), night.int("RelativeHumidity", "Average")),
                cloud = avg(day.int("CloudCover"), night.int("CloudCover")),
                phrase = day.optString("LongPhrase").ifBlank { day.optString("IconPhrase") }.trim().take(120).ifBlank { null },
            )
        }.takeIf { it.isNotEmpty() }
    } catch (e: Exception) { null }

    /** WX-2.6: hours from the 12-hour reply. */
    fun parseHourly(json: String): List<Forecast.Hour>? = try {
        val a = JSONArray(json)
        (0 until a.length()).map { i ->
            val h = a.getJSONObject(i)
            val t = OffsetDateTime.parse(h.getString("DateTime"))
            val chance = h.int("PrecipitationProbability")
            val cloud = h.int("CloudCover")
            Forecast.Hour(
                epochMs = t.toInstant().toEpochMilli(), localHour = t.hour,
                kind = Forecast.classifyHour(chance, h.optBoolean("HasPrecipitation"), h.optString("PrecipitationType").takeIf { h.has("PrecipitationType") }, cloud),
                tempC = h.num("Temperature", "Value"), chance = chance, humidity = h.int("RelativeHumidity"), cloud = cloud,
            )
        }.takeIf { it.isNotEmpty() }
    } catch (e: Exception) { null }
}
