package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.beemer.seoulbike.model.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(private val repository: DataStoreRepository) : BaseViewModel() {
    val tokens: LiveData<Pair<String?, String?>> = combine(
        repository.getAccessToken().asLiveData(),
        repository.getRefreshToken().asLiveData()
    )

    val accessTokenFlow = repository.getAccessToken()
    val refreshTokenFlow = repository.getRefreshToken()

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