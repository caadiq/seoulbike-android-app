package com.beemer.seoulbike.model.dto

data class StationStatusDto(
    val rackCnt: Int?,
    val qrBikeCnt: Int?,
    val elecBikeCnt: Int?,
    val updateTime: String?
)