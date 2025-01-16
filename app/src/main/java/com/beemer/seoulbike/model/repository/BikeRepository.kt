package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.api.BikeApi
import com.beemer.seoulbike.model.di.BasicRetrofit
import com.beemer.seoulbike.model.dto.StationDto
import com.beemer.seoulbike.model.dto.StationSearchDto
import com.beemer.seoulbike.model.utils.RetrofitUtil
import retrofit2.Retrofit
import javax.inject.Inject

class BikeRepository @Inject constructor(@BasicRetrofit retrofit: Retrofit) {
    private val bikeApi: BikeApi = retrofit.create(BikeApi::class.java)

    private fun formatToken(token: String?): String? = token?.let { "Bearer $it" }

    suspend fun getNearByStations(myLat: Double, myLon: Double, mapLat: Double, mapLon: Double, distance: Double, accessToken: String?): RetrofitUtil.Results<List<StationDto>> {
        return RetrofitUtil.call(bikeApi.getNearByStations(myLat, myLon, mapLat, mapLon, distance, formatToken(accessToken)))
    }

    suspend fun getStationDetails(myLat: Double, myLon: Double, stationId: String, accessToken: String?): RetrofitUtil.Results<StationDto> {
        return RetrofitUtil.call(bikeApi.getStationDetails(myLat, myLon, stationId, formatToken(accessToken)))
    }

    suspend fun getStations(myLat: Double, myLon: Double, page: Int?, limit: Int?, query: String, accessToken: String?): RetrofitUtil.Results<StationSearchDto> {
        return RetrofitUtil.call(bikeApi.getStations(myLat, myLon, page, limit, query, formatToken(accessToken)))
    }
}