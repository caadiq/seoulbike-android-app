package com.beemer.seoulbike.model.repository

import com.beemer.seoulbike.model.dao.SearchHistoryDao
import com.beemer.seoulbike.model.entity.SearchHistoryEntity

class SearchHistoryRepository(private val dao: SearchHistoryDao) {
    fun getTop5History() = dao.getTop5History()

    suspend fun getHistoryByTitle(title: String): SearchHistoryEntity? = dao.getHistoryByTitle(title)

    suspend fun insertHistory(dto: SearchHistoryEntity) = dao.insertHistory(dto)

    suspend fun deleteAllHistory() = dao.deleteAllHistory()

    suspend fun deleteHistoryByTitle(title: String) = dao.deleteHistoryByTitle(title)
}