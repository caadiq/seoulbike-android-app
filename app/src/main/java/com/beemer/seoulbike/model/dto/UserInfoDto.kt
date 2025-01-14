package com.beemer.seoulbike.model.dto

data class UserInfoDto(
    val email: String,
    val nickname: String,
    val socialType: String,
    val createdDate: String
)