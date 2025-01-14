package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.api.BikeApi
import com.beemer.seoulbike.model.di.AuthRetrofit
import com.beemer.seoulbike.model.di.BasicRetrofit
import com.beemer.seoulbike.model.dto.StationDto
import com.beemer.seoulbike.model.dto.StationSearchDto
import com.beemer.seoulbike.model.utils.RetrofitUtil
import retrofit2.Retrofit
import javax.inject.Inject

class BikeRepository @Inject constructor(@BasicRetrofit retrofit: Retrofit, @AuthRetrofit retrofitAuth: Retrofit) {
    private val bikeApi: BikeApi = retrofit.create(BikeApi::class.java)
    private val bikeApiAuth: BikeApi = retrofitAuth.create(BikeApi::class.java)

    suspend fun getNearByStations(myLat: Double, myLon: Double, maoLat: Double, mapLon: Double, distance: Double): RetrofitUtil.Results<List<StationDto>> {
        return RetrofitUtil.call(bikeApi.getNearByStations(myLat, myLon, maoLat, mapLon, distance))
    }

    suspend fun getNearByStationsAuth(myLat: Double, myLon: Double, maoLat: Double, mapLon: Double, distance: Double): RetrofitUtil.Results<List<StationDto>> {
        return RetrofitUtil.call(bikeApiAuth.getNearByStations(myLat, myLon, maoLat, mapLon, distance))
    }

    suspend fun getStationDetails(myLat: Double, myLon: Double, stationId: String): RetrofitUtil.Results<StationDto> {
        return RetrofitUtil.call(bikeApi.getStationDetails(myLat, myLon, stationId))
    }

    suspend fun getStationAuth(myLat: Double, myLon: Double, stationId: String): RetrofitUtil.Results<StationDto> {
        return RetrofitUtil.call(bikeApiAuth.getStationDetails(myLat, myLon, stationId))
    }

    suspend fun getStations(myLat: Double, myLon: Double, page: Int?, limit: Int?, query: String): RetrofitUtil.Results<StationSearchDto> {
        return RetrofitUtil.call(bikeApi.getStations(myLat, myLon, page, limit, query))
    }

    suspend fun getStationsAuth(myLat: Double, myLon: Double, page: Int?, limit: Int?, query: String): RetrofitUtil.Results<StationSearchDto> {
        return RetrofitUtil.call(bikeApiAuth.getStations(myLat, myLon, page, limit, query))
    }
}