package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beemer.seoulbike.model.dto.NearbyStationListDto
import com.beemer.seoulbike.model.repository.BikeRepository
import com.beemer.seoulbike.model.utils.ApiUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BikeViewModel @Inject constructor(private val repository: BikeRepository) : ViewModel() {
    private val _nearbyStations = MutableLiveData<List<NearbyStationListDto>>()
    val nearbyStations: LiveData<List<NearbyStationListDto>> = _nearbyStations

    private val _myLocation = MutableLiveData<Pair<Double, Double>>()
    val myLocation: LiveData<Pair<Double, Double>> = _myLocation

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun getNearbyStations(myLat: Double, myLon: Double, mapLat: Double, mapLon: Double, distance: Double) {
        viewModelScope.launch {
            handleApiResponse(repository.getNearByStations(myLat, myLon, mapLat, mapLon, distance))
        }
    }

    private fun handleApiResponse(result: ApiUtils.Results<List<NearbyStationListDto>>) {
        when (result) {
            is ApiUtils.Results.Success -> {
                _nearbyStations.postValue(result.data)
                _errorMessage.postValue(null)
            }
            is ApiUtils.Results.Error -> {
                _errorMessage.postValue(result.message)
            }
        }
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }

    fun setMyLocation(lat: Double, lon: Double) {
        _myLocation.postValue(Pair(lat, lon))
    }
}