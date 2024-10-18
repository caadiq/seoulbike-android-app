package com.beemer.seoulbike.model.dto

data class StationStatusDto(
    val rackCnt: Int?,
    val parkingCnt: Int?,
    val updateTime: String?
)