package com.beemer.seoulbike.model.dto

data class SignInResponseDto(
    val userInfo: UserInfoDto,
    val token: TokenDto
)
