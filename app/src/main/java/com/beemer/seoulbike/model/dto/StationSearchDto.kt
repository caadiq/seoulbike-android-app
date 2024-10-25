package com.beemer.seoulbike.model.dto

data class StationSearchDto(
    val page: PageDto,
    val count: CountDto,
    val stations: List<StationListDto>
)