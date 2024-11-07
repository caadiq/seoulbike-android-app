package com.beemer.seoulbike.model.utils

import retrofit2.Call
import retrofit2.awaitResponse

object ApiUtils {
    sealed class Results<out T> {
        data class Success<out T>(val data: T) : Results<T>()
        data class Error(val message: String) : Results<Nothing>()
    }

    suspend fun <T> safeApiCall(call: Call<T>): Results<T> {
        return try {
            val response = call.awaitResponse()
            if (response.isSuccessful) {
                Results.Success(response.body()!!)
            } else {
                Results.Error("${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Results.Error(e.message ?: e.toString())
        }
    }
}