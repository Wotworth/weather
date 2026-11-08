package io.github.wotworth.weather

import android.content.ContentValues
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ContentProviderController

/** WX-4: the tile interface. */
@RunWith(AndroidJUnit4::class)
class TileTest {
    private lateinit var provider: TileProvider
    private lateinit var app: App

    @Before fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        provider = Robolectric.buildContentProvider(TileProvider::class.java).create(Tile.AUTHORITY).get()
    }

    @After fun tearDown() { TileProvider.signatureCheck = null }

    @Test fun onlyTheLauncherSignedWithItsKey() {
        assertTrue(Tile.allowed("io.github.wotworth.wplauncher") { true })
        assertTrue(Tile.allowed("io.github.wotworth.wplauncher.preview") { true })
        assertFalse(Tile.allowed("io.github.wotworth.wplauncher") { false })   // a look-alike, other key
        assertFalse(Tile.allowed("com.example.spy") { true })
        assertFalse(Tile.allowed("io.github.wotworth.weather") { true })
        assertFalse(Tile.allowed(null) { true })
        assertEquals(32, Tile.LAUNCHER_CERT.size)
        assertEquals(0xa7.toByte(), Tile.LAUNCHER_CERT[0])
        assertEquals(0x1d.toByte(), Tile.LAUNCHER_CERT[31])
    }

    @Test fun otherCallersGetASecurityException() {
        TileProvider.signatureCheck = { false }
        try { provider.query(Tile.STATE, null, null, null, null); fail() } catch (e: SecurityException) { }
    }

    @Test fun readOnly() {
        try { provider.insert(Tile.DAYS, ContentValues()); fail() } catch (e: UnsupportedOperationException) { }
        try { provider.update(Tile.DAYS, ContentValues(), null, null); fail() } catch (e: UnsupportedOperationException) { }
        try { provider.delete(Tile.DAYS, null, null); fail() } catch (e: UnsupportedOperationException) { }
    }

    @Test fun stateAndDays() {
        val s = app.store
        s.place = Accu.parseCities(Fixtures.read("cities-hastings.json"))[0]
        s.days = Accu.parseDaily(Fixtures.read("daily-fog-missing.json"))!! + Accu.parseDaily(Fixtures.read("daily-5day.json"))!!
        s.fetchedAt = 1234L
        s.fahrenheit = true
        val v1 = s.version()
        // The provider reads its caller from Binder; here the check is replaced, as the launcher would pass it.
        TileProvider.signatureCheck = { true }
        val rows = Tile.dayRows(s)
        assertEquals(5, rows.size)
        assertEquals("2026-11-08", rows[0][0])   // oldest first
        assertEquals("SUNNY", rows[0][1])
        assertEquals(15.6, rows[0][2])
        assertEquals(listOf("version", "town", "units", "fetched_at", "source", "error"), Tile.STATE_COLUMNS.toList())
        assertEquals(listOf("date", "kind", "high_c", "low_c"), Tile.DAY_COLUMNS.toList())
        val st = Tile.stateRow(s)
        assertEquals("Hastings-on-Hudson, NY", st[1]); assertEquals("F", st[2]); assertEquals(1234L, st[3]); assertEquals("AccuWeather", st[4]); assertEquals(null, st[5])
        s.fahrenheit = false
        val v2 = s.version()
        assertTrue(v1 != v2)
        assertEquals("C", Tile.stateRow(s)[2])
        s.error = "no connection"
        assertTrue(s.version() != v2)
        // A missing high is served as null.
        s.days = Accu.parseDaily(Fixtures.read("daily-fog-missing.json"))!!
        assertEquals(null, Tile.dayRows(s)[1][2])
    }
}
