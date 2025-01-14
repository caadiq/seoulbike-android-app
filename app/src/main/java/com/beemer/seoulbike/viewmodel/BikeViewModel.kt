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

    private val _station = MutableLiveData<StationDto>()
    val station: LiveData<StationDto> = _station

    private val _stations = MutableLiveData<List<StationDto>>()
    val stations: LiveData<List<StationDto>> = _stations

    private val _page = MutableLiveData<PageDto>()
    val page: LiveData<PageDto> = _page

    private val _myLocation = MutableLiveData<Pair<Double, Double>>()
    val myLocation: LiveData<Pair<Double, Double>> = _myLocation

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRefreshed = MutableLiveData<Boolean>()
    val isRefreshed: LiveData<Boolean> = _isRefreshed

    fun getNearbyStations(myLat: Double, myLon: Double, mapLat: Double, mapLon: Double, distance: Double) {
        execute(
            call = { repository.getNearByStations(myLat, myLon, mapLat, mapLon, distance) },
            onSuccess = { data -> _nearbyStations.postValue(data) },
            onFinally = { setLoading(false) }
        )
    }

    fun getStation(myLat: Double, myLon: Double, stationId: String) {
        execute(
            call = { repository.getStation(myLat, myLon, stationId) },
            onSuccess = { data -> _station.postValue(data) }
        )
    }

    fun getStations(myLat: Double, myLon: Double, page: Int?, limit: Int?, query: String, refresh: Boolean) {
        setLoading(true)
        _isRefreshed.value = refresh

        execute(
            call = { repository.getStations(myLat, myLon, page, limit, query) },
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

    fun setMyLocation(lat: Double, lon: Double) {
        _myLocation.postValue(Pair(lat, lon))
    }
}