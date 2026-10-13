package com.example.proyectopenka.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Guarda los tokens en almacenamiento cifrado del dispositivo. */
class Sesion(context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "abstergo_sesion",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    val access: String?
        get() = prefs.getString(CLAVE_ACCESS, null)

    val refresh: String?
        get() = prefs.getString(CLAVE_REFRESH, null)

    fun haySesion(): Boolean = !access.isNullOrBlank()

    fun guardar(access: String, refresh: String?) {
        prefs.edit()
            .putString(CLAVE_ACCESS, access)
            .putString(CLAVE_REFRESH, refresh)
            .apply()
    }

    fun limpiar() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val CLAVE_ACCESS = "access"
        const val CLAVE_REFRESH = "refresh"
    }
}
