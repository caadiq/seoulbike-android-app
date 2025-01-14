package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.beemer.seoulbike.model.dto.PageDto
import com.beemer.seoulbike.model.dto.StationDto
import com.beemer.seoulbike.model.repository.BikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BikeViewModel @Inject constructor(private val repository: BikeRepository) : BaseViewModel() {
    private val _nearbyStations = MutableLiveData<List<StationDto>>()
    val nearbyStations: LiveData<List<StationDto>> = _nearbyStations

    private val _stationDetails = MutableLiveData<StationDto>()
    val stationDetails: LiveData<StationDto> = _stationDetails

    private val _stations = MutableLiveData<List<StationDto>>()
    val stations: LiveData<List<StationDto>> = _stations

    private val _page = MutableLiveData<PageDto>()
    val page: LiveData<PageDto> = _page

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRefreshed = MutableLiveData<Boolean>()
    val isRefreshed: LiveData<Boolean> = _isRefreshed

    fun getNearbyStations(myLat: Double, myLon: Double, mapLat: Double, mapLon: Double, distance: Double, accessToken: String?) {
        execute(
            call = { repository.getNearByStations(myLat, myLon, mapLat, mapLon, distance, accessToken) },
            onSuccess = { data -> _nearbyStations.postValue(data) },
            onFinally = { setLoading(false) }
        )
    }

    fun getStationDetails(myLat: Double, myLon: Double, stationId: String, accessToken: String?) {
        execute(
            call = { repository.getStationDetails(myLat, myLon, stationId, accessToken) },
            onSuccess = { data -> _stationDetails.postValue(data) }
        )
    }

    fun getStations(myLat: Double, myLon: Double, page: Int?, limit: Int?, query: String, refresh: Boolean, accessToken: String?) {
        setLoading(true)
        _isRefreshed.value = refresh

        execute(
            call = { repository.getStations(myLat, myLon, page, limit, query, accessToken) },
            onSuccess = { data ->
                _stations.postValue(
                    if (refresh) {
                        data.stations
                    } else {
                        _stations.value?.plus(data.stations) ?: emptyList()
                    }
                )
                _page.postValue(data.page)
            },
            onFinally = { setLoading(false) }
        )
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }
}