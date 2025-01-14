package com.beemer.seoulbike.model.api

import com.beemer.seoulbike.model.dto.StationDto
import com.beemer.seoulbike.model.dto.StationSearchDto
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
    ): Call<List<StationDto>>

    @GET("/api/seoulbike/station")
    fun getStationDetails(
        @Query("my_lat") myLat: Double,
        @Query("my_lon") myLon: Double,
        @Query("station_id") stationId: String,
    ): Call<StationDto>

    @GET("/api/seoulbike/stations")
    fun getStations(
        @Query("my_lat") myLat: Double,
        @Query("my_lon") myLon: Double,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("query") query: String
    ): Call<StationSearchDto>
}