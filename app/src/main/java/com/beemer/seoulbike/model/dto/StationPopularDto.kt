package com.beemer.seoulbike.model.dto

data class StationPopularDto(
    val stationNo: String,
    val stationNm: String,
    var rank: Int = 0
)
