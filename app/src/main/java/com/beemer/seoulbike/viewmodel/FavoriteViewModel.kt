package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.beemer.seoulbike.model.dto.PageDto
import com.beemer.seoulbike.model.dto.StationDto
import com.beemer.seoulbike.model.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(private val repository: FavoriteRepository) : BaseViewModel() {
    private val _addFavoriteStation = MutableLiveData<Unit>()
    val addFavoriteStation: LiveData<Unit> = _addFavoriteStation

    private val _favoriteStations = MutableLiveData<List<StationDto>>()
    val favoriteStations: LiveData<List<StationDto>> = _favoriteStations

    private val _page = MutableLiveData<PageDto>()
    val page: LiveData<PageDto> = _page

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRefreshed = MutableLiveData<Boolean>()
    val isRefreshed: LiveData<Boolean> = _isRefreshed

    fun addFavoriteStation(stationId: String) {
        execute(
            call = { repository.addFavoriteStation(stationId) },
            onSuccess = { _addFavoriteStation.postValue(Unit) }
        )
    }

    fun getFavoriteStations(page: Int?, limit: Int?, myLat: Double, myLon: Double, refresh: Boolean) {
        execute(
            call = { repository.getFavoriteStations(page, limit, myLat, myLon) },
            onSuccess = {
                _favoriteStations.postValue(
                    if (refresh) {
                        it.stations
                    } else {
                        _favoriteStations.value?.plus(it.stations) ?: it.stations
                    }
                )
                _page.postValue(it.page)
            },
            onFinally = { setLoading(false) }
        )
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }
}