package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _myLocation = MutableLiveData<Pair<Double, Double>>()
    val myLocation: LiveData<Pair<Double, Double>> = _myLocation

    fun setMyLocation(lat: Double, lon: Double) {
        _myLocation.postValue(Pair(lat, lon))
    }
}