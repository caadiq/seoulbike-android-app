package com.beemer.seoulbike.model.api

import com.beemer.seoulbike.model.dto.NearbyStationListDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BikeApi {

    @GET("/api/seoulbike/stations/nearby")
    fun getNearByStations(
        @Query("my_lat") myLat: Double,
        @Query("my_lon") myLon: Double,
        @Query("map_lat") mapLat: Double,
        @Query("map_lon") mapLon: Double,
        @Query("distance") distance: Double
    ): Call<List<NearbyStationListDto>>
}