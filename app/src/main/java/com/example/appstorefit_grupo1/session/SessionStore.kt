package com.example.appstorefit_grupo1.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session_store")

object SessionStore {
    private val KEY_EMAIL   = stringPreferencesKey("email")
    private val KEY_RUT     = stringPreferencesKey("rut")
    private val KEY_NAME    = stringPreferencesKey("name")
    private val KEY_ADDRESS = stringPreferencesKey("address")
    private val KEY_PHONE   = stringPreferencesKey("phone")
    private val KEY_BIRTH   = stringPreferencesKey("birthDate")
    private val KEY_ROLEID  = longPreferencesKey("roleId")

    suspend fun save(
        context: Context,
        email: String,
        rut: String,
        name: String,
        address: String,
        phone: String?,
        birthDate: String?,
        roleId: Long
    ) {
        context.dataStore.edit { p ->
            p[KEY_EMAIL] = email
            p[KEY_RUT] = rut
            p[KEY_NAME] = name
            p[KEY_ADDRESS] = address
            if (!phone.isNullOrBlank()) p[KEY_PHONE] = phone
            if (!birthDate.isNullOrBlank()) p[KEY_BIRTH] = birthDate
            p[KEY_ROLEID] = roleId
        }
    }

    suspend fun clear(context: Context) {
        context.dataStore.edit { it.clear() }
    }

    suspend fun load(context: Context): LoadedSession? {
        val prefs = context.dataStore.data.map { it }.first()
        val email = prefs[KEY_EMAIL] ?: return null
        val rut = prefs[KEY_RUT] ?: ""
        val name = prefs[KEY_NAME] ?: ""
        val address = prefs[KEY_ADDRESS] ?: ""
        val phone = prefs[KEY_PHONE]
        val birth = prefs[KEY_BIRTH]
        val roleId = prefs[KEY_ROLEID] ?: 0L
        return LoadedSession(email, rut, name, address, phone, birth, roleId)
    }

    data class LoadedSession(
        val email: String,
        val rut: String,
        val name: String,
        val address: String,
        val phone: String?,
        val birthDate: String?,
        val roleId: Long
    )
}