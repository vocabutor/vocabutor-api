package com.vocabutor.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLanguageRequest(
    val name: String,
    val shortName: String,
    val description: String? = null,
    val iconUrl: String,
)
