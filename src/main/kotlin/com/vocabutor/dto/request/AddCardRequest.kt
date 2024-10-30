package com.vocabutor.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AddCardRequest(
    val phrase: String,
    val answer: String,
    val languageId: Long,
)
