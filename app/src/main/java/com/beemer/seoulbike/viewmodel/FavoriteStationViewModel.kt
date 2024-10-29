package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beemer.seoulbike.model.entity.FavoriteStationEntity
import com.beemer.seoulbike.model.repository.FavoriteStationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteStationViewModel @Inject constructor(private val repository: FavoriteStationRepository) : ViewModel() {
    val favoriteStation = repository.getAllFavoriteStation()

    val top5FavoriteStation = repository.getTop5FavoriteStation()

    private val _isFavoriteExists = MutableLiveData<Boolean>()
    val isFavoriteExists: LiveData<Boolean> get() = _isFavoriteExists

    fun insertFavoriteStation(favoriteStation: FavoriteStationEntity) {
        viewModelScope.launch {
            repository.insertFavoriteStation(favoriteStation)
        }
    }

    fun deleteFavoriteStation(stationId: String) {
        viewModelScope.launch {
            repository.deleteFavoriteStationByStationId(stationId)
        }
    }

    fun checkFavoriteExists(stationId: String) {
        viewModelScope.launch {
            _isFavoriteExists.value = repository.getFavoriteStationByStationId(stationId) != null
        }
    }
}