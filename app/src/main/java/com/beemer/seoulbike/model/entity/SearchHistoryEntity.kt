package com.beemer.seoulbike.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String
)