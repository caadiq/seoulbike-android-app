package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.api.BikeApi
import retrofit2.Retrofit
import retrofit2.awaitResponse
import javax.inject.Inject

class BikeRepository @Inject constructor(retrofit: Retrofit) {
    private val bikeApi: BikeApi = retrofit.create(BikeApi::class.java)

    suspend fun getNearByStations(lat: Double, lon: Double, distance: Double) = bikeApi.getNearByStations(lat, lon, distance).awaitResponse().body() ?: emptyList()
}