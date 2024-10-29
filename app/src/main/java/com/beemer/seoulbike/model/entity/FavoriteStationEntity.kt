package com.beemer.seoulbike.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite")
data class FavoriteStationEntity(
    @PrimaryKey val stationId: String
)