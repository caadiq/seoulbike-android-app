package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.beemer.seoulbike.model.dto.StationPopularDto
import com.beemer.seoulbike.model.repository.PopularRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PopularViewModel @Inject constructor(private val repository: PopularRepository) : BaseViewModel() {
    private val _addPopularStation = MutableLiveData<Unit>()
    val addPopularStation: LiveData<Unit> = _addPopularStation

    private val _popularStations = MutableLiveData<List<StationPopularDto>>()
    val popularStations: LiveData<List<StationPopularDto>> = _popularStations

    fun addPopularStation(stationId: String) {
        execute(
            call = { repository.addPopularStation(stationId) },
            onSuccess = { _addPopularStation.postValue(Unit) }
        )
    }

    fun getPopularStations() {
        execute(
            call = { repository.getPopularStations() },
            onSuccess = { _popularStations.postValue(it) }
        )
    }
}