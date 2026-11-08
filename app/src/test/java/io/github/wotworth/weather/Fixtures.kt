package io.github.wotworth.weather

/** WX-2.9: recorded-shape AccuWeather replies (src/test/resources/fixtures), no key in them. */
object Fixtures {
    fun read(name: String): String = Fixtures::class.java.getResource("/fixtures/$name")!!.readText()
}
