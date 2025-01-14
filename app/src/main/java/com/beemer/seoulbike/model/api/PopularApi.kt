package com.beemer.seoulbike.model.api

import com.beemer.seoulbike.model.dto.StationPopularDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PopularApi {

    @POST("/api/seoulbike/stations/popular")
    fun addPopularStation(
        @Query("station_id") stationId: String
    ): Call<Unit>

    @GET("/api/seoulbike/stations/popular")
    fun getPopularStations(): Call<List<StationPopularDto>>
}