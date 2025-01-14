package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.data.DataStoreModule
import javax.inject.Inject

class DataStoreRepository @Inject constructor(private val dataStore: DataStoreModule) {
    suspend fun saveAccessToken(token: String) {
        dataStore.saveAccessToken(token)
    }

    fun getAccessToken() = dataStore.getAccessToken()

    suspend fun clearAccessToken() {
        dataStore.clearAccessToken()
    }

    suspend fun saveRefreshToken(token: String) {
        dataStore.saveRefreshToken(token)
    }

    fun getRefreshToken() = dataStore.getRefreshToken()

    suspend fun clearRefreshToken() {
        dataStore.clearRefreshToken()
    }
}