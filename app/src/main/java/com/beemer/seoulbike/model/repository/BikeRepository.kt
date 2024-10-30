package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.api.BikeApi
import com.beemer.seoulbike.model.dto.StationListDto
import com.beemer.seoulbike.model.dto.StationPopularDto
import com.beemer.seoulbike.model.dto.StationSearchDto
import com.beemer.seoulbike.model.utils.ApiUtils
import retrofit2.Retrofit
import javax.inject.Inject

class BikeRepository @Inject constructor(retrofit: Retrofit) {
    private val bikeApi: BikeApi = retrofit.create(BikeApi::class.java)

    suspend fun getNearByStations(myLat: Double, myLon: Double, maoLat: Double, mapLon: Double, distance: Double): ApiUtils.Results<List<StationListDto>> {
        return ApiUtils.safeApiCall(bikeApi.getNearByStations(myLat, myLon, maoLat, mapLon, distance))
    }

    suspend fun getStations(myLat: Double, myLon: Double, page: Int?, limit: Int?, query: String): ApiUtils.Results<StationSearchDto> {
        return ApiUtils.safeApiCall(bikeApi.getStations(myLat, myLon, page, limit, query))
    }

    suspend fun getFavoriteStations(myLat: Double, myLon: Double, page: Int?, limit: Int?, stationId: List<String>): ApiUtils.Results<StationSearchDto> {
        return ApiUtils.safeApiCall(bikeApi.getFavoriteStations(myLat, myLon, page, limit, stationId))
    }

    suspend fun getPopularStations(): ApiUtils.Results<List<StationPopularDto>> {
        return ApiUtils.safeApiCall(bikeApi.getPopularStations())
    }
}