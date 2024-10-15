package com.vocabutor.dto.request

import com.vocabutor.entity.LanguageStatus
import kotlinx.serialization.Serializable

@Serializable
data class AddLanguageRequest(
    val name: String,
    val shortName: String,
    val displayOrder: Float,
    val description: String? = null,
    val iconUrl: String,
    val languageStatus: LanguageStatus,
)
