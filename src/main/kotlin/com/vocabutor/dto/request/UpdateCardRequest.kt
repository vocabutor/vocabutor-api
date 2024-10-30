package com.vocabutor.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCardRequest(
    val phrase: String,
    val answer: String
)
