package com.playoutedge.player.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

/**
 * Secure storage for device authentication tokens.
 */
class TokenStorage(private val context: Context) {

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val TENANT_ID = stringPreferencesKey("tenant_id")
    }

    val accessToken: Flow<String?> = context.tokenDataStore.data.map { it[Keys.ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.tokenDataStore.data.map { it[Keys.REFRESH_TOKEN] }
    val deviceId: Flow<String?> = context.tokenDataStore.data.map { it[Keys.DEVICE_ID] }
    val tenantId: Flow<String?> = context.tokenDataStore.data.map { it[Keys.TENANT_ID] }

    suspend fun getAccessToken(): String? = accessToken.first()
    suspend fun getRefreshToken(): String? = refreshToken.first()
    suspend fun getDeviceId(): String? = deviceId.first()
    suspend fun getTenantId(): String? = tenantId.first()

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.tokenDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveDeviceInfo(deviceId: String, tenantId: String) {
        context.tokenDataStore.edit { prefs ->
            prefs[Keys.DEVICE_ID] = deviceId
            prefs[Keys.TENANT_ID] = tenantId
        }
    }

    suspend fun clearAll() {
        context.tokenDataStore.edit { it.clear() }
    }

    suspend fun isEnrolled(): Boolean = getAccessToken() != null
}
