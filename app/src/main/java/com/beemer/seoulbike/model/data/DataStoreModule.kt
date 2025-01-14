package com.beemer.seoulbike.model.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreModule(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "MyDataStore")

    private val accessToken = stringPreferencesKey("accessToken")
    private val refreshToken = stringPreferencesKey("refreshToken")

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[accessToken] = token
        }
    }

    fun getAccessToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[accessToken]
        }
    }

    suspend fun clearAccessToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(accessToken)
        }
    }

    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[refreshToken] = token
        }
    }

    fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[refreshToken]
        }
    }

    suspend fun clearRefreshToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(refreshToken)
        }
    }
}