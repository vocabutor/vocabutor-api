package com.vocabutor.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class GoogleLoginRequestDto(
    val clientId: String,
    val credential: String
)