package com.beemer.seoulbike.model.api

import com.beemer.seoulbike.model.dto.StationSearchDto
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface FavoriteApi {

    @POST("/api/seoulbike/stations/favorite")
    fun addFavoriteStation(
        @Query("station_id") stationId: String
    ): Call<Unit>

    @POST("/api/seoulbike/stations/favorite")
    fun getFavoriteStations(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("my_lat") myLat: Double,
        @Query("my_lon") myLon: Double
    ): Call<StationSearchDto>
}