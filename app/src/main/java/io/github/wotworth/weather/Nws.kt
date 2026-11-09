package io.github.wotworth.weather

import org.json.JSONObject
import java.time.OffsetDateTime
import java.util.Locale

/**
 * WX-10 (Chris, 9 Nov): the National Weather Service's Area Forecast Discussion for the town's
 * office, from api.weather.gov. Checked against real replies (9 Nov, Hastings-on-Hudson → OKX).
 * US towns only. Sent: the town's coordinates (once, to find its office), then the office code.
 */
object Nws {
    const val HOST = "api.weather.gov"

    /** Coordinates as weather.gov takes them: at most 4 decimals. */
    fun pointsUrl(lat: Double, lon: Double) = "https://$HOST/points/${String.format(Locale.US, "%.4f", lat).trimEnd('0').trimEnd('.')},${String.format(Locale.US, "%.4f", lon).trimEnd('0').trimEnd('.')}"
    fun listUrl(office: String): String { require(validOffice(office)); return "https://$HOST/products/types/AFD/locations/$office" }
    fun productUrl(id: String) = "https://$HOST/products/$id"

    fun validOffice(o: String) = o.length == 3 && o.all { it in 'A'..'Z' }

    /** The forecast office ("OKX") for a point. */
    fun parseOffice(json: String): String? = try {
        JSONObject(json).getJSONObject("properties").optString("cwa").takeIf { validOffice(it) }
    } catch (e: Exception) { null }

    /** The newest discussion's id. */
    fun parseLatestId(json: String): String? = try {
        val g = JSONObject(json).getJSONArray("@graph")
        (0 until g.length()).map { g.getJSONObject(it) }
            .maxByOrNull { OffsetDateTime.parse(it.getString("issuanceTime")) }?.optString("id")?.takeIf { it.isNotBlank() }
    } catch (e: Exception) { null }

    class Product(val id: String, val issued: OffsetDateTime, val text: String)

    fun parseProduct(json: String): Product? = try {
        val o = JSONObject(json)
        Product(o.getString("id"), OffsetDateTime.parse(o.getString("issuanceTime")), o.getString("productText"))
    } catch (e: Exception) { null }

    /** One part of the discussion: a heading ("Key messages"), the heading's own note ("/6 PM tonight/"), paragraphs. */
    data class Section(val title: String, val note: String?, val paragraphs: List<String>, val sub: Boolean)

    /** "National Weather Service New York NY" → "New York NY". */
    fun office(text: String): String? = text.lines().firstOrNull { it.trim().startsWith("National Weather Service ") }?.trim()?.removePrefix("National Weather Service ")

    /**
     * The discussion as sections, for reading on a phone. Lines that start with "." and contain "..."
     * open a section; "&&" ends one; "$$" ends the discussion. Lines wrapped at 66 characters are
     * joined back into paragraphs; blank lines and numbered points ("1) ") start new ones.
     * The words are the meteorologists' own, unchanged.
     */
    fun sections(text: String): List<Section> {
        val out = ArrayList<Section>()
        var title: String? = null; var note: String? = null; var sub = false
        val paras = ArrayList<String>(); val cur = StringBuilder()
        fun flushPara() { if (cur.isNotBlank()) paras += cur.toString().trim(); cur.setLength(0) }
        fun flush() { flushPara(); title?.let { if (paras.isNotEmpty() || it.isNotBlank()) out += Section(it, note, paras.toList(), sub) }; paras.clear(); title = null; note = null }
        for (raw in text.replace("\r", "").lines()) {
            val line = raw.trimEnd()
            val t = line.trim()
            when {
                t == "$$" -> { flush(); break }
                t == "&&" -> flush()
                t.startsWith(".") && t.contains("...") && !t.startsWith("...") -> {
                    flush()
                    val head = t.removePrefix(".").substringBefore("...")
                    val rest = t.substringAfter("...").trim()
                    val n = Regex("/([^/]*)/").find(head)
                    note = n?.groupValues?.get(1)?.trim()?.ifBlank { null }
                    val name = head.replace(Regex("/[^/]*/"), "").trim()
                    sub = Regex("KEY MESSAGE \\d").containsMatchIn(name)
                    title = sentence(name)
                    if (rest.isNotEmpty()) cur.append(rest)
                }
                title == null -> {}
                t.isEmpty() -> flushPara()
                Regex("^(\\d+\\)|-|\\*) ").containsMatchIn(t) -> { flushPara(); cur.append(t) }
                else -> { if (cur.isNotEmpty()) cur.append(' '); cur.append(t) }
            }
        }
        flush()
        return out
    }

    /** "KEY MESSAGES" → "Key messages"; "OKX WATCHES/WARNINGS/ADVISORIES" keeps the office code. */
    fun sentence(s: String): String {
        val words = s.lowercase(Locale.ROOT).split(' ').filter { it.isNotEmpty() }
        return words.mapIndexed { i, w -> if (w.length == 3 && validOffice(w.uppercase(Locale.ROOT)) && i == 0 && s.startsWith(w.uppercase(Locale.ROOT)) && w.uppercase(Locale.ROOT) != "KEY") w.uppercase(Locale.ROOT) else if (i == 0) w.replaceFirstChar { it.titlecase(Locale.ROOT) } else w }.joinToString(" ")
    }
}
