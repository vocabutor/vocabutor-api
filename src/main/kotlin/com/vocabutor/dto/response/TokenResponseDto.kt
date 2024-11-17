package com.vocabutor.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponseDto(
    val token: String
)
