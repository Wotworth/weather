package io.github.wotworth.weather

import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * WX-2.3, WX-10.1 (Chris, 9 Nov: "allow it to access weather.gov for that information but no other
 * network access"): the app's second and last network access, for the forecast discussion only.
 *
 * - HTTPS GET only, to [Nws.HOST] only, on three path forms with no parameters: anything else is
 *   refused before a connection is made. Redirects aren't followed.
 * - No key, no cookies, no device details: a fixed User-Agent (weather.gov asks for one) naming the
 *   app, never the person. The AccuWeather key is never sent here.
 * - The network security config trusts certificates for this host and AccuWeather's only.
 */
object NwsNet {
    val ALLOWED: List<Regex> = listOf(
        Regex("/points/-?\\d{1,2}\\.\\d{1,4},-?\\d{1,3}\\.\\d{1,4}"),
        Regex("/products/types/AFD/locations/[A-Z]{3}"),
        Regex("/products/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
    )

    class HttpError(val code: Int) : java.io.IOException("weather.gov HTTP $code")

    const val USER_AGENT = "WotworthWeather (personal use)"

    /** Every request, after the checks (tests replace it; CI never calls weather.gov). */
    @Volatile var transport: (URL) -> String = ::httpsGet

    fun check(url: String): URL {
        val u = try { URI(url) } catch (e: Exception) { throw SecurityException("not a URL") }
        if (u.scheme != "https") throw SecurityException("only https is allowed")
        if (u.host != Nws.HOST) throw SecurityException("host ${u.host} is not allowed")
        if (u.rawUserInfo != null || (u.port != -1 && u.port != 443) || u.rawFragment != null || u.rawQuery != null) throw SecurityException("URL form not allowed")
        if (ALLOWED.none { it.matches(u.rawPath.orEmpty()) }) throw SecurityException("path ${u.rawPath} is not allowed")
        return u.toURL()
    }

    fun get(url: String): String = transport(check(url))

    private fun httpsGet(u: URL): String {
        val c = u.openConnection() as HttpsURLConnection
        try {
            c.requestMethod = "GET"
            c.instanceFollowRedirects = false
            c.useCaches = false
            c.connectTimeout = WeatherTokens.Data.TIMEOUT_MS
            c.readTimeout = WeatherTokens.Data.TIMEOUT_MS
            c.setRequestProperty("User-Agent", USER_AGENT)
            c.setRequestProperty("Accept", "application/geo+json, application/ld+json")
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
