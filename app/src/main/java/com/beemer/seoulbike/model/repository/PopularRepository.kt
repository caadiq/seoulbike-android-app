package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.api.PopularApi
import com.beemer.seoulbike.model.di.BasicRetrofit
import com.beemer.seoulbike.model.dto.StationPopularDto
import com.beemer.seoulbike.model.utils.RetrofitUtil
import retrofit2.Retrofit
import javax.inject.Inject

class PopularRepository @Inject constructor(@BasicRetrofit retrofit: Retrofit) {
    private val popularApi: PopularApi = retrofit.create(PopularApi::class.java)

    suspend fun addPopularStation(stationId: String): RetrofitUtil.Results<Unit> {
        return RetrofitUtil.call(popularApi.addPopularStation(stationId))
    }

    suspend fun getPopularStations(): RetrofitUtil.Results<List<StationPopularDto>> {
        return RetrofitUtil.call(popularApi.getPopularStations())
    }
}