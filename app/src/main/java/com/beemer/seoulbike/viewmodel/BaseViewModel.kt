package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beemer.seoulbike.model.utils.RetrofitUtil
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    private val _errorCode = MutableLiveData<Int?>(null)
    val errorCode: MutableLiveData<Int?> = _errorCode

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: MutableLiveData<String?> = _errorMessage

    protected fun <T> execute(call: suspend () -> RetrofitUtil.Results<T>, onSuccess: (T) -> Unit, onFinally: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                when (val result = call()) {
                    is RetrofitUtil.Results.Success -> {
                        onSuccess(result.data)
                        _errorCode.postValue(null)
                    }
                    is RetrofitUtil.Results.Error -> {
                        _errorCode.postValue(result.statusCode)
                        _errorMessage.postValue(result.message)
                    }
                }
            } finally {
                onFinally()
            }
        }
    }

    fun <A, B> combine(liveData1: LiveData<A>, liveData2: LiveData<B>): LiveData<Pair<A?, B?>> {
        return MediatorLiveData<Pair<A?, B?>>().apply {
            addSource(liveData1) { value = it to liveData2.value }
            addSource(liveData2) { value = liveData1.value to it }
        }
    }
}