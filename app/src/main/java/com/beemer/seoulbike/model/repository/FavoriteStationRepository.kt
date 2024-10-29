package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.dao.FavoriteStationDao
import com.beemer.seoulbike.model.entity.FavoriteStationEntity

class FavoriteStationRepository(private val dao: FavoriteStationDao) {
    fun getAllFavoriteStation() = dao.getAllFavoriteStation()

    fun getTop5FavoriteStation() = dao.getTop5FavoriteStation()

    suspend fun insertFavoriteStation(favoriteStation: FavoriteStationEntity) = dao.insertFavoriteStation(favoriteStation)

    suspend fun deleteFavoriteStationByStationId(stationId: String) = dao.deleteFavoriteStationByStationId(stationId)
}