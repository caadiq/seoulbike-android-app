package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beemer.seoulbike.model.utils.RetrofitUtil
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    private val _errorCode = MutableLiveData<Int?>(null)
    val errorCode: MutableLiveData<Int?> = _errorCode

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
                    }
                }
            } finally {
                onFinally()
            }
        }
    }
}