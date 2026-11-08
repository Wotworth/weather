package io.github.wotworth.weather

import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * WX-2.3: the app's only network access (after the launcher's WeatherNet.kt). Nothing else in
 * the app may use the network; CI fails the build if any other host appears.
 *
 * - HTTPS GET only, to [Accu.HOST] only, on three paths with an allowlist of parameters and
 *   their values: anything else is refused before a connection is made. Redirects aren't followed.
 * - WX-2.2: the key goes only in the Authorization header ("Bearer", per AccuWeather's
 *   Authentication page), never in a URL, a log or an error message.
 * - Sends no cookies or device details: a fixed User-Agent, Accept and "Connection: close".
 * - The network security config trusts certificates for this one host only.
 */
object AccuNet {
    class Rule(val path: Regex, val params: Map<String, Regex>)

    private val LANG = Regex(Regex.escape(Accu.LANGUAGE))
    private val TRUE = Regex("true")

    val ALLOWED: List<Rule> = listOf(
        Rule(Regex("/locations/v1/cities/search"), mapOf("q" to Regex("(?=.*\\p{L})[\\p{L} .'-]{2,60}"), "language" to LANG)),
        Rule(Regex("/forecasts/v1/daily/5day/\\d{1,12}"), mapOf("details" to TRUE, "metric" to TRUE, "language" to LANG)),
        Rule(Regex("/forecasts/v1/hourly/12hour/\\d{1,12}"), mapOf("details" to TRUE, "metric" to TRUE, "language" to LANG)),
    )

    const val USER_AGENT = "WotworthWeather (personal use)"

    /** An API key as pasted: letters, digits, - and _ only (so it can't break the header). */
    fun validApiKey(k: String) = k.length in 8..128 && k.all { it.isLetterOrDigit() && it.code < 128 || it == '-' || it == '_' }

    class HttpError(val code: Int) : java.io.IOException("HTTP $code")

    /** Every request, after the checks (tests replace it; CI never calls AccuWeather, WX-2.9). */
    @Volatile var transport: (URL, String) -> String = ::httpsGet

    fun encode(v: String): String = java.net.URLEncoder.encode(v, "UTF-8")

    /** The request if allowed: https, the one host, an allowed path, only its parameters in their forms. */
    fun check(url: String): URL {
        val u = try { URI(url) } catch (e: Exception) { throw SecurityException("not a URL") }
        if (u.scheme != "https") throw SecurityException("only https is allowed")
        if (u.host != Accu.HOST) throw SecurityException("host ${u.host} is not allowed")
        if (u.rawUserInfo != null || (u.port != -1 && u.port != 443) || u.rawFragment != null) throw SecurityException("URL form not allowed")
        val rule = ALLOWED.firstOrNull { it.path.matches(u.rawPath.orEmpty()) } ?: throw SecurityException("path ${u.rawPath} is not allowed")
        val pairs = u.rawQuery.orEmpty().split('&').filter { it.isNotEmpty() }.map { it.substringBefore('=') to it.substringAfter('=', "") }
        if (pairs.map { it.first }.toSet().size != pairs.size) throw SecurityException("repeated parameter")
        for ((k, v) in pairs) {
            val form = rule.params[k] ?: throw SecurityException("parameter $k is not allowed")
            val value = try { java.net.URLDecoder.decode(v, "UTF-8") } catch (e: Exception) { throw SecurityException("bad value of $k") }
            if (!form.matches(value)) throw SecurityException("value of $k is not allowed")
        }
        return u.toURL()
    }

    fun get(url: String, apiKey: String): String {
        if (!validApiKey(apiKey)) throw SecurityException("the key has characters an AccuWeather key doesn't")
        return transport(check(url), apiKey)
    }

    private fun httpsGet(u: URL, apiKey: String): String {
        val c = u.openConnection() as HttpsURLConnection
        try {
            c.requestMethod = "GET"
            c.instanceFollowRedirects = false
            c.useCaches = false
            c.connectTimeout = WeatherTokens.Data.TIMEOUT_MS
            c.readTimeout = WeatherTokens.Data.TIMEOUT_MS
            c.setRequestProperty("Authorization", "Bearer $apiKey")
            c.setRequestProperty("User-Agent", USER_AGENT)
            c.setRequestProperty("Accept", "application/json")
            c.setRequestProperty("Connection", "close")
            val code = c.responseCode
            if (code != 200) throw HttpError(code)
            val out = ByteArrayOutputStream()
            c.inputStream.use { ins ->
                val buf = ByteArray(8192)
                while (true) {
                    val n = ins.read(buf)
                    if (n < 0) break
                    out.write(buf, 0, n)
                    if (out.size() > WeatherTokens.Data.MAX_REPLY_BYTES) throw java.io.IOException("reply too large")
                }
            }
            return out.toString("UTF-8")
        } finally {
            c.disconnect()
        }
    }
}
