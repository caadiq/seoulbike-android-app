package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beemer.seoulbike.model.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(private val repository: DataStoreRepository) : ViewModel() {
    val accessToken = repository.getAccessToken()
    val refreshToken = repository.getRefreshToken()

    fun saveAccessToken(accessToken: String) {
        viewModelScope.launch {
            repository.saveAccessToken(accessToken)
        }
    }

    fun saveRefreshToken(refreshToken: String) {
        viewModelScope.launch {
            repository.saveRefreshToken(refreshToken)
        }
    }
}