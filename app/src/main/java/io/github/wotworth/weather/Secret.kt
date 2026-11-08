package io.github.wotworth.weather

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** WX-2.1: where the AccuWeather key is kept. */
interface Secret {
    fun get(): String?
    fun set(value: String?)
}

/**
 * WX-2.1: the key, encrypted with an AES-GCM key that never leaves the Android Keystore. Only the
 * ciphertext is stored, in app-private preferences that are excluded from backups.
 */
class KeystoreSecret(private val prefs: SharedPreferences) : Secret {
    private val alias = "accuweather_key"

    private fun aes(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).build())
        return gen.generateKey()
    }

    override fun get(): String? = try {
        val iv = prefs.getString("key_iv", null) ?: return null
        val ct = prefs.getString("key_ct", null) ?: return null
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, aes(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
        String(c.doFinal(Base64.decode(ct, Base64.NO_WRAP)), Charsets.UTF_8)
    } catch (e: Exception) { null }

    override fun set(value: String?) {
        if (value == null) { prefs.edit().remove("key_iv").remove("key_ct").apply(); return }
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, aes())
        prefs.edit().putString("key_iv", Base64.encodeToString(c.iv, Base64.NO_WRAP))
            .putString("key_ct", Base64.encodeToString(c.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)).apply()
    }
}

/** Tests only (Robolectric has no Android Keystore). */
class MemorySecret(private var v: String? = null) : Secret {
    override fun get() = v
    override fun set(value: String?) { v = value }
}
