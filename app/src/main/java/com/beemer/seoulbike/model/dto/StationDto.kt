package com.beemer.seoulbike.model.dto

data class StationDto(
    val stationNo: String,
    val stationId: String,
    val stationNm: String,
    val distance: Double?,
    val stationDetails: StationDetailsDto,
    val stationStatus: StationStatusDto,
    var isFavorite: Boolean = false,
    var rank: Int = 0
)