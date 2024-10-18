package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beemer.seoulbike.model.dto.NearbyStationListDto
import com.beemer.seoulbike.model.repository.BikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BikeViewModel @Inject constructor(private val repository: BikeRepository) : ViewModel() {
    private val _nearbyStations = MutableLiveData<List<NearbyStationListDto>>()
    val nearbyStations: LiveData<List<NearbyStationListDto>> = _nearbyStations

    fun getNearbyStations(lat: Double, lon: Double, distance: Double) {
        viewModelScope.launch {
            _nearbyStations.postValue(repository.getNearByStations(lat, lon, distance))
        }
    }
}