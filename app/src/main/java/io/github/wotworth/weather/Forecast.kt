package io.github.wotworth.weather

import java.time.LocalDate
import java.util.Locale

/**
 * The forecast model, the five conditions and how a day picks one.
 *
 * Kind, Thresholds, classify (from classifyOpenMeteo), stateCode, Place.label and roundTo5 are
 * copied from Wotworth/wp-launcher app/src/main/java/io/github/wotworth/wplauncher/Weather.kt at
 * commit a444997452257f0bfc9b9e339f77a2243a3ac81e, unchanged in their rules (WX-3.2, WX-3.4).
 */
object Forecast {
    /**
     * WX-3.2: the five conditions (names fixed: the tile interface sends them), with the launcher's
     * icons. WX-6.3 (Chris, 9 Nov): at night the app's screens show the Fluent moon for a clear
     * hour and the partly cloudy night icon; cloudy, rain and snow look the same day and night.
     */
    enum class Kind(val icon: Int, val nightIcon: Int, val words: String, val nightWords: String) {
        SUNNY(R.drawable.wx_sunny, R.drawable.wx_moon, "sunny", "clear"),
        PARTLY_CLOUDY(R.drawable.wx_partly_cloudy, R.drawable.wx_partly_cloudy_night, "partly cloudy", "partly cloudy"),
        CLOUDY(R.drawable.wx_cloudy, R.drawable.wx_cloudy, "cloudy", "cloudy"),
        RAIN(R.drawable.wx_rain, R.drawable.wx_rain, "rain", "rain"),
        SNOW(R.drawable.wx_snow, R.drawable.wx_snow, "snow", "snow"),
    }

    /** WX-3.2: the launcher's thresholds, unchanged. Every threshold for picking a day's icon, in one place. */
    object Thresholds {
        /** A precipitation icon only from this chance of precipitation (%). */
        const val PRECIP_CHANCE = 50
        /** And at least this much precipitation in the day (mm). */
        const val PRECIP_MM = 1.0
        /** Sunshine ÷ daylight at least this → sunny; at least [PARTLY] → partly cloudy; else cloudy. */
        const val SUNNY = 0.75
        const val PARTLY = 0.35
        /** Snow (as water) at least this share of the precipitation → snow, else rain. */
        const val SNOW_SHARE = 0.5
    }

    /**
     * WX-3.2: a day's icon from the whole day's figures (copied from the launcher's
     * classifyOpenMeteo): rain from a [Thresholds.PRECIP_CHANCE]% chance and [Thresholds.PRECIP_MM]
     * mm (snow if snowfall, as water — 7 cm of snow is 10 mm — is most of it); otherwise
     * sunshine ÷ daylight.
     */
    fun classify(chance: Double?, precipMm: Double?, snowCm: Double?, sunshineS: Double?, daylightS: Double?): Kind {
        val p = precipMm ?: 0.0
        if ((chance ?: 0.0) >= Thresholds.PRECIP_CHANCE && p >= Thresholds.PRECIP_MM) {
            val snowWater = (snowCm ?: 0.0) * 10.0 / 7.0
            return if (snowWater >= p * Thresholds.SNOW_SHARE) Kind.SNOW else Kind.RAIN
        }
        val sun = sunshineS ?: return Kind.CLOUDY
        val day = daylightS?.takeIf { it > 0 } ?: return Kind.CLOUDY
        val r = sun / day
        return when {
            r >= Thresholds.SUNNY -> Kind.SUNNY
            r >= Thresholds.PARTLY -> Kind.PARTLY_CLOUDY
            else -> Kind.CLOUDY
        }
    }

    /**
     * WX-6.3 (app screens only, never the tile): an hour's icon, by the same thresholds. Rain or
     * snow from a [Thresholds.PRECIP_CHANCE]% chance when AccuWeather expects precipitation that
     * hour; otherwise the cloud cover stands in for "sunshine ÷ daylight" (sunshine share = 1 −
     * cloud cover).
     */
    fun classifyHour(chance: Int?, hasPrecip: Boolean, precipType: String?, cloudPct: Int?): Kind {
        if ((chance ?: 0) >= Thresholds.PRECIP_CHANCE && hasPrecip) {
            val t = precipType.orEmpty().lowercase(Locale.ROOT)
            return if (t == "snow" || t == "ice" || t == "mixed") Kind.SNOW else Kind.RAIN
        }
        val cloud = cloudPct ?: return Kind.CLOUDY
        val sun = 1.0 - cloud / 100.0
        return when {
            sun >= Thresholds.SUNNY -> Kind.SUNNY
            sun >= Thresholds.PARTLY -> Kind.PARTLY_CLOUDY
            else -> Kind.CLOUDY
        }
    }

    /**
     * One day. [date] is the town's local date; [highC]/[lowC] exact °C (WX-3.1). The rest is for
     * the app's own screens: chance of precipitation, humidity and cloud cover (whole day: the
     * larger of day and night for the chance, the averages for the others), and AccuWeather's
     * phrase for the day.
     */
    data class Day(
        val date: LocalDate, val kind: Kind, val highC: Double?, val lowC: Double?,
        val chance: Int? = null, val humidity: Int? = null, val cloud: Int? = null, val phrase: String? = null,
    )

    /** One hour: [epochMs], [localHour] (0–23 where the town is), exact °C and the rest as in [Day]; [daylight] is AccuWeather's IsDaylight. */
    data class Hour(
        val epochMs: Long, val localHour: Int, val kind: Kind, val tempC: Double?,
        val chance: Int?, val humidity: Int?, val cloud: Int?, val daylight: Boolean = true,
    ) {
        val icon get() = if (daylight) kind.icon else kind.nightIcon
        val words get() = if (daylight) kind.words else kind.nightWords
    }

    /**
     * A town from AccuWeather's city search. [key] is AccuWeather's location key (null only for
     * the default town before its first lookup; WX-2.7 keeps it after that). [adminId] is
     * AdministrativeArea.ID (the state's 2-letter code in the US).
     */
    data class Place(
        val name: String, val admin: String, val adminId: String, val country: String, val countryCode: String,
        val key: String?, val zone: String? = null,
    ) {
        val isUs get() = countryCode.equals("US", ignoreCase = true)

        /** WX-3.4: "Hastings-on-Hudson, NY"; outside the US "Paris, France". */
        val label: String get() = if (isUs) {
            val code = adminId.takeIf { it.length == 2 && it.all(Char::isLetter) }?.uppercase(Locale.ROOT) ?: stateCode(admin)
            name + (code?.let { ", $it" } ?: if (admin.isNotEmpty()) ", $admin" else "")
        } else name + if (country.isNotEmpty()) ", $country" else ""

        /** A match as listed in settings: town, state or region, country. */
        val full get() = listOf(name, admin, country).filter { it.isNotEmpty() }.joinToString(", ")
    }

    /** WX-2.4: the default town. Its location key is looked up once, on the first update. */
    val DEFAULT_PLACE = Place("Hastings-on-Hudson", "New York", "NY", "United States", "US", null, "America/New_York")

    /** [place] first among the recent towns (no duplicates), at most [WeatherTokens.Data.RECENT_TOWNS]. */
    fun withRecent(recent: List<Place>, place: Place): List<Place> =
        (listOf(place) + recent.filter { !same(it, place) }).take(WeatherTokens.Data.RECENT_TOWNS)

    fun same(a: Place, b: Place) = if (a.key != null && b.key != null) a.key == b.key else a.full == b.full

    private val STATES = mapOf(
        "AL" to "Alabama", "AK" to "Alaska", "AZ" to "Arizona", "AR" to "Arkansas", "CA" to "California",
        "CO" to "Colorado", "CT" to "Connecticut", "DE" to "Delaware", "DC" to "District of Columbia",
        "FL" to "Florida", "GA" to "Georgia", "HI" to "Hawaii", "ID" to "Idaho", "IL" to "Illinois",
        "IN" to "Indiana", "IA" to "Iowa", "KS" to "Kansas", "KY" to "Kentucky", "LA" to "Louisiana",
        "ME" to "Maine", "MD" to "Maryland", "MA" to "Massachusetts", "MI" to "Michigan", "MN" to "Minnesota",
        "MS" to "Mississippi", "MO" to "Missouri", "MT" to "Montana", "NE" to "Nebraska", "NV" to "Nevada",
        "NH" to "New Hampshire", "NJ" to "New Jersey", "NM" to "New Mexico", "NY" to "New York",
        "NC" to "North Carolina", "ND" to "North Dakota", "OH" to "Ohio", "OK" to "Oklahoma", "OR" to "Oregon",
        "PA" to "Pennsylvania", "RI" to "Rhode Island", "SC" to "South Carolina", "SD" to "South Dakota",
        "TN" to "Tennessee", "TX" to "Texas", "UT" to "Utah", "VT" to "Vermont", "VA" to "Virginia",
        "WA" to "Washington", "WV" to "West Virginia", "WI" to "Wisconsin", "WY" to "Wyoming", "PR" to "Puerto Rico",
    )

    /** A US state's 2-letter code from its name ("New York" → "NY"); null if it isn't one. */
    fun stateCode(name: String): String? = STATES.entries.firstOrNull { it.value.equals(name.trim(), ignoreCase = true) }?.key

    fun cToF(c: Double) = c * 9.0 / 5.0 + 32.0
}
