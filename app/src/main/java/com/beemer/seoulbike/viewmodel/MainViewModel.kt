package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class MainFragmentType(val tag: String) {
    MAP("map"),
    STATION("station"),
}

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _currentFragmentType = MutableLiveData(MainFragmentType.MAP)
    val currentFragmentType: LiveData<MainFragmentType> = _currentFragmentType

    private val _distance = MutableLiveData(1000.0)
    val distance: LiveData<Double> = _distance

    fun setCurrentFragment(item: Int): Boolean {
        val pageType = getPageType(item)
        changeCurrentFragmentType(pageType)

        return true
    }

    private fun getPageType(item: Int): MainFragmentType {
        return when (item) {
            0 -> MainFragmentType.MAP
            1 -> MainFragmentType.STATION
            else -> MainFragmentType.MAP
        }
    }

    private fun changeCurrentFragmentType(fragmentType: MainFragmentType) {
        if (currentFragmentType.value == fragmentType)
            return

        _currentFragmentType.value = fragmentType
    }

    fun setDistance(distance: Double) {
        _distance.value = distance
    }
}