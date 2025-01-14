package com.beemer.seoulbike.model.dto

data class TokenDto(
    val accessToken: String?,
    val refreshToken: String?
)
