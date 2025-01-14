package com.beemer.seoulbike.model.dto

data class VerifyCodeDto(
    val email: String,
    val code: String
)