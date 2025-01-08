package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.api.BikeApi
import com.beemer.seoulbike.model.dto.StationDto
import com.beemer.seoulbike.model.dto.StationPopularDto
import com.beemer.seoulbike.model.dto.StationSearchDto
import com.beemer.seoulbike.model.utils.RetrofitUtil
import retrofit2.Retrofit
import javax.inject.Inject

class BikeRepository @Inject constructor(retrofit: Retrofit) {
    private val bikeApi: BikeApi = retrofit.create(BikeApi::class.java)

    suspend fun getNearByStations(myLat: Double, myLon: Double, maoLat: Double, mapLon: Double, distance: Double): RetrofitUtil.Results<List<StationDto>> {
        return RetrofitUtil.call(bikeApi.getNearByStations(myLat, myLon, maoLat, mapLon, distance))
    }

    suspend fun getStation(myLat: Double, myLon: Double, stationId: String): RetrofitUtil.Results<StationDto> {
        return RetrofitUtil.call(bikeApi.getStation(myLat, myLon, stationId))
    }

    suspend fun getStations(myLat: Double, myLon: Double, page: Int?, limit: Int?, query: String): RetrofitUtil.Results<StationSearchDto> {
        return RetrofitUtil.call(bikeApi.getStations(myLat, myLon, page, limit, query))
    }

    suspend fun getFavoriteStations(myLat: Double, myLon: Double, page: Int?, limit: Int?, stationId: List<String>): RetrofitUtil.Results<StationSearchDto> {
        return RetrofitUtil.call(bikeApi.getFavoriteStations(myLat, myLon, page, limit, stationId))
    }

    suspend fun getPopularStations(): RetrofitUtil.Results<List<StationPopularDto>> {
        return RetrofitUtil.call(bikeApi.getPopularStations())
    }

    suspend fun addPopularStation(stationId: String): RetrofitUtil.Results<Unit> {
        return RetrofitUtil.call(bikeApi.addPopularStation(stationId))
    }
}