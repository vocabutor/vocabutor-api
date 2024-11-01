package com.vocabutor.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateDeckRequest(
    val title: String,
)
