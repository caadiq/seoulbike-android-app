package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class SearchFragmentType(val tag: String) {
    SEARCH1("search1"),
    SEARCH2("search2"),
}

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
    private val _currentFragmentType = MutableLiveData(SearchFragmentType.SEARCH1)
    val currentFragmentType: LiveData<SearchFragmentType> = _currentFragmentType

    private val _query = MutableLiveData<String?>(null)
    val query: LiveData<String?> = _query

    fun setCurrentFragment(item: Int): Boolean {
        val pageType = getPageType(item)
        changeCurrentFragmentType(pageType)

        return true
    }

    private fun getPageType(item: Int): SearchFragmentType {
        return when (item) {
            0 -> SearchFragmentType.SEARCH1
            1 -> SearchFragmentType.SEARCH2
            else -> SearchFragmentType.SEARCH1
        }
    }

    private fun changeCurrentFragmentType(fragmentType: SearchFragmentType) {
        if (currentFragmentType.value == fragmentType)
            return

        _currentFragmentType.value = fragmentType
    }

    fun updateQuery(newQuery: String?) {
        _query.value = newQuery
    }
}