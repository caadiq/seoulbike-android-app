package com.beemer.seoulbike.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beemer.seoulbike.model.utils.RetrofitUtil
import kotlinx.coroutines.launch

data class Request<T>(
    val response: MutableLiveData<T> = MutableLiveData(),
    val errorCode: MutableLiveData<Int?> = MutableLiveData(),
    val errorMessage: MutableLiveData<String?> = MutableLiveData()
)

open class BaseViewModel : ViewModel() {
    protected fun <T> execute(
        call: suspend () -> RetrofitUtil.Results<T>,
        onSuccess: (T) -> Unit,
        errorCode: MutableLiveData<Int?>? = null,
        errorMessage: MutableLiveData<String?>? = null,
        onFinally: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                when (val result = call()) {
                    is RetrofitUtil.Results.Success -> {
                        onSuccess(result.data)
                        errorCode?.postValue(null)
                    }
                    is RetrofitUtil.Results.Error -> {
                        errorCode?.postValue(result.statusCode)
                        errorMessage?.postValue(result.message)
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