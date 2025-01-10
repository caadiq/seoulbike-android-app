package com.beemer.seoulbike.model.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.awaitResponse

object RetrofitUtil {
    sealed class Results<out T> {
        data class Success<out T>(val data: T) : Results<T>()
        data class Error(val statusCode: Int, val message: String?) : Results<Nothing>()
    }

    suspend fun <T> call(call: Call<T>): Results<T> {
        return try {
            val response = call.awaitResponse()
            if (response.isSuccessful) {
                Results.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody?.let {
                    try {
                        val json = Gson().fromJson(it, JsonObject::class.java)
                        json.get("message")?.asString
                    } catch (e: Exception) {
                        null
                    }
                }
                Results.Error(response.code(), errorMessage)
            }
        } catch (e: Exception) {
            Results.Error(-1, e.message)
        }
    }
}