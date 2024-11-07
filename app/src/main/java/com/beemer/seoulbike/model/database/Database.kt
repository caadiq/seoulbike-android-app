package com.beemer.seoulbike.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.beemer.seoulbike.model.dao.FavoriteStationDao
import com.beemer.seoulbike.model.dao.SearchHistoryDao
import com.beemer.seoulbike.model.entity.FavoriteStationEntity
import com.beemer.seoulbike.model.entity.SearchHistoryEntity

@Database(entities = [FavoriteStationEntity::class, SearchHistoryEntity::class], version = 2)
abstract class Database : RoomDatabase() {
    abstract fun favoriteStationDao(): FavoriteStationDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}