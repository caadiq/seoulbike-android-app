package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beemer.seoulbike.model.dto.PageDto
import com.beemer.seoulbike.model.dto.StationListDto
import com.beemer.seoulbike.model.repository.BikeRepository
import com.beemer.seoulbike.model.utils.ApiUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BikeViewModel @Inject constructor(private val repository: BikeRepository) : ViewModel() {
    private val _nearbyStations = MutableLiveData<List<StationListDto>>()
    val nearbyStations: LiveData<List<StationListDto>> = _nearbyStations

    private val _stations = MutableLiveData<List<StationListDto>>()
    val stations: LiveData<List<StationListDto>> = _stations

    private val _page = MutableLiveData<PageDto>()
    val page: LiveData<PageDto> = _page

    private val _myLocation = MutableLiveData<Pair<Double, Double>>()
    val myLocation: LiveData<Pair<Double, Double>> = _myLocation

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRefreshed = MutableLiveData<Boolean>()
    val isRefreshed: LiveData<Boolean> = _isRefreshed

    fun getNearbyStations(myLat: Double, myLon: Double, mapLat: Double, mapLon: Double, distance: Double) {
        viewModelScope.launch {
            handleApiResponse(repository.getNearByStations(myLat, myLon, mapLat, mapLon, distance))
        }
    }

    fun getStations(myLat: Double, myLon: Double, page: Int?, limit: Int?, query: String, refresh: Boolean) {
        viewModelScope.launch {
            setLoading(true)
            _isRefreshed.value = refresh

            when (val response = repository.getStations(myLat, myLon, page, limit, query)) {
                is ApiUtils.Results.Success -> {
                    _stations.postValue(
                        if (refresh) {
                            response.data.stations
                        } else {
                            _stations.value?.plus(response.data.stations) ?: emptyList()
                        }
                    )
                    _page.postValue(response.data.page)
                    _errorMessage.postValue(null)
                }
                is ApiUtils.Results.Error -> {
                    _errorMessage.postValue(response.message)
                }
            }
            setLoading(false)
        }
    }

    private fun handleApiResponse(result: ApiUtils.Results<List<StationListDto>>) {
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