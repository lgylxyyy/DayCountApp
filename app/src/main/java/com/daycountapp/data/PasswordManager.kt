package com.daycountapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.security.MessageDigest

val Context.passwordDataStore by preferencesDataStore(name = "password_prefs")

object PasswordManager {
    private const val SALT = "DayCountApp2026"

    private val PASSWORD_TYPE_KEY = stringPreferencesKey("password_type")
    private val PASSWORD_HASH_KEY = stringPreferencesKey("password_hash")

    private var cachedType: PasswordType = PasswordType.NONE
    private var cachedHash: String = ""
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        runBlocking {
            val prefs = context.passwordDataStore.data.first()
            val typeStr = prefs[PASSWORD_TYPE_KEY] ?: "NONE"
            cachedType = try { PasswordType.valueOf(typeStr) } catch (_: Exception) { PasswordType.NONE }
            cachedHash = prefs[PASSWORD_HASH_KEY] ?: ""
        }
        initialized = true
    }

    fun getType(): PasswordType = cachedType

    fun isEnabled(): Boolean = cachedType != PasswordType.NONE

    fun isFirstTime(): Boolean = cachedType == PasswordType.NONE

    fun isFingerprint(): Boolean = cachedType == PasswordType.FINGERPRINT

    fun verify(input: String): Boolean {
        if (cachedType == PasswordType.NONE) return true
        if (cachedType == PasswordType.FINGERPRINT) return true
        return hash(input) == cachedHash
    }

    fun setPassword(type: PasswordType, password: String) {
        cachedType = type
        cachedHash = if (type == PasswordType.NONE || type == PasswordType.FINGERPRINT) "" else hash(password)
    }

    fun setFingerprint() {
        cachedType = PasswordType.FINGERPRINT
        cachedHash = ""
    }

    fun clearPassword() {
        cachedType = PasswordType.NONE
        cachedHash = ""
    }

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest((input + SALT).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun save(context: Context) {
        runBlocking {
            context.passwordDataStore.edit { prefs ->
                prefs[PASSWORD_TYPE_KEY] = cachedType.name
                if (cachedType == PasswordType.NONE || cachedType == PasswordType.FINGERPRINT) {
                    prefs.remove(PASSWORD_HASH_KEY)
                } else {
                    prefs[PASSWORD_HASH_KEY] = cachedHash
                }
            }
        }
    }
}
