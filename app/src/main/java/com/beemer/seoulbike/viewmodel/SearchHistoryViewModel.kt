package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beemer.seoulbike.model.repository.SearchHistoryRepository
import com.beemer.seoulbike.model.entity.SearchHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchHistoryViewModel @Inject constructor(private val repository: SearchHistoryRepository) : ViewModel() {
    val searchHistory: LiveData<List<SearchHistoryEntity>> = repository.getTop5History()

    private val _isTitleExists = MutableLiveData<Boolean>()
    val isTitleExists: LiveData<Boolean> get() = _isTitleExists

    fun checkTitleExists(title: String) {
        viewModelScope.launch {
            _isTitleExists.value = repository.getHistoryByTitle(title) != null
        }
    }

    fun insertHistory(history: SearchHistoryEntity) {
        viewModelScope.launch {
            repository.insertHistory(history)
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch {
            repository.deleteAllHistory()
        }
    }

    fun deleteHistoryByTitle(title: String) {
        viewModelScope.launch {
            repository.deleteHistoryByTitle(title)
        }
    }
}