package com.beemer.seoulbike.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.beemer.seoulbike.model.entity.SearchHistoryEntity

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search ORDER BY id DESC LIMIT 5")
    fun getTop5History(): LiveData<List<SearchHistoryEntity>>

    @Query("SELECT * FROM search WHERE title = :title LIMIT 1")
    suspend fun getHistoryByTitle(title: String): SearchHistoryEntity?

    @Insert
    suspend fun insertHistory(dto: SearchHistoryEntity)

    @Query("DELETE FROM search")
    suspend fun deleteAllHistory()

    @Query("DELETE FROM search WHERE title = :title")
    suspend fun deleteHistoryByTitle(title: String)
}