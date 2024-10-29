package com.beemer.seoulbike.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.beemer.seoulbike.model.dao.FavoriteStationDao
import com.beemer.seoulbike.model.entity.FavoriteStationEntity

@Database(entities = [FavoriteStationEntity::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun favoriteStationDao(): FavoriteStationDao
}