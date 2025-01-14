package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.api.FavoriteApi
import com.beemer.seoulbike.model.di.AuthRetrofit
import com.beemer.seoulbike.model.dto.StationSearchDto
import com.beemer.seoulbike.model.utils.RetrofitUtil
import retrofit2.Retrofit
import javax.inject.Inject

class FavoriteRepository @Inject constructor(@AuthRetrofit retrofitAuth: Retrofit) {
    private val favoriteApiAuth: FavoriteApi = retrofitAuth.create(FavoriteApi::class.java)

    suspend fun addFavoriteStation(stationId: String): RetrofitUtil.Results<Unit> {
        return RetrofitUtil.call(favoriteApiAuth.addFavoriteStation(stationId))
    }

    suspend fun getFavoriteStations(page: Int?, limit: Int?, myLat: Double, myLon: Double): RetrofitUtil.Results<StationSearchDto> {
        return RetrofitUtil.call(favoriteApiAuth.getFavoriteStations(page, limit, myLat, myLon))
    }
}