package com.beemer.seoulbike.model.dto

data class StationListDto(
    val stationNo: String,
    val stationId: String,
    val stationNm: String,
    val distance: Double?,
    val stationDetails: StationDetailsDto,
    val stationStatus: StationStatusDto,
)