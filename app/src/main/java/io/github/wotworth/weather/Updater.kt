package io.github.wotworth.weather

import java.time.LocalDate

/**
 * One update, as pure as it can be: every request goes through [get] (AccuNet in the app, recorded
 * replies in tests). WX-2.8: a failure never clears the stored forecast; it records the error.
 */
class Updater(private val store: Store, private val apiKey: () -> String?, private val get: (String, String) -> String = AccuNet::get) {

    class LimitReached : Exception("daily call limit reached")

    /** WX-2.7: counts the call, and refuses past the day's cap. */
    private fun call(url: String, today: LocalDate): String {
        val key = apiKey() ?: throw IllegalStateException("no key")
        if (store.callsOn(today) >= WeatherTokens.Data.DAILY_CALL_CAP) throw LimitReached()
        store.countCall(today)
        return get(url, key)
    }

    /** The town's location key: cached, or (default town only, once) looked up by name. */
    private fun located(today: LocalDate): Forecast.Place {
        val p = store.place
        if (p.key != null) return p
        val found = Accu.pickDefault(Accu.parseCities(call(Accu.searchUrl(p.name), today))) ?: throw IllegalStateException("town not found")
        store.place = found
        store.recent = Forecast.withRecent(store.recent.filter { it.key != null }, found)
        return found
    }

    /** WX-2.5: the 5-day forecast. True if it was stored. */
    fun daily(now: Long, today: LocalDate): Boolean = attempt(hourly = false) {
        store.attemptAt = now
        val p = located(today)
        val days = Accu.parseDaily(call(Accu.dailyUrl(p.key!!), today)) ?: throw IllegalStateException("reply unreadable")
        store.days = days
        store.fetchedAt = now
    }

    /** WX-2.5 (Chris, 9 Nov): one background update, the 5 days then the next 12 hours. */
    fun update(now: Long, today: LocalDate) {
        daily(now, today)
        hourly(now, today)
    }

    /** WX-2.6: the next 12 hours. True if they were stored. */
    fun hourly(now: Long, today: LocalDate): Boolean = attempt(hourly = true) {
        val p = located(today)
        val hours = Accu.parseHourly(call(Accu.hourlyUrl(p.key!!), today)) ?: throw IllegalStateException("reply unreadable")
        store.hours = hours
        store.hoursAt = now
    }

    /** City search from settings: the matches, or an error message. */
    fun search(text: String, today: LocalDate): Pair<List<Forecast.Place>, String?> = try {
        if (!Accu.validSearch(text)) emptyList<Forecast.Place>() to "use letters, spaces, . ' and - only"
        else Accu.parseCities(call(Accu.searchUrl(text.trim()), today)).let { it to if (it.isEmpty()) "no towns found" else null }
    } catch (e: Exception) { emptyList<Forecast.Place>() to describe(e) }

    /** The daily forecast's error is the tile's (WX-4.2); the hourly one is shown in settings only. */
    private inline fun attempt(hourly: Boolean, block: () -> Unit): Boolean = try {
        block(); if (hourly) store.hourError = null else store.error = null; true
    } catch (e: Exception) {
        if (hourly) store.hourError = describe(e) else store.error = describe(e); false
    }

    companion object {
        /** WX-2.8: what settings shows. Never contains the key (it is only ever in a header). */
        fun describe(e: Exception): String = when (e) {
            is AccuNet.HttpError -> when (e.code) {
                401 -> "AccuWeather didn't accept the key (401)"
                403 -> "AccuWeather refused: over the plan's limit, or not in the plan (403)"
                429 -> "AccuWeather: too many calls (429)"
                in 500..599 -> "AccuWeather server error (${e.code})"
                else -> "AccuWeather error (${e.code})"
            }
            is LimitReached -> "the app's limit of ${WeatherTokens.Data.DAILY_CALL_CAP} calls today was reached"
            is java.net.UnknownHostException, is java.net.ConnectException, is java.net.SocketTimeoutException -> "no connection"
            is java.io.IOException -> "network: ${e.javaClass.simpleName}"
            is SecurityException -> "refused: ${e.message}"
            else -> e.message ?: e.javaClass.simpleName
        }

        /**
         * An update is due if none was tried in the last [WeatherTokens.Data.UPDATE_SKIP_MIN]
         * minutes (a failure waits too, so a bad key can't spend calls).
         */
        fun due(attemptAt: Long, now: Long) = now - attemptAt >= WeatherTokens.Data.UPDATE_SKIP_MIN * 60_000L
    }
}
