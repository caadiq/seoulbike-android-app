package com.beemer.seoulbike.model.api

import com.beemer.seoulbike.model.dto.NearbyStationListDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BikeApi {

    @GET("/api/seoulbike/stations/nearby")
    fun getNearByStations(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("distance") radius: Int
    ): Call<List<NearbyStationListDto>>
}