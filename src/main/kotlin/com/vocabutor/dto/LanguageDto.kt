package com.vocabutor.dto

import com.vocabutor.entity.LanguageStatus
import kotlinx.serialization.Serializable

@Serializable
data class LanguageDto(
    val id: Long,
    val name: String,
    val shortName: String,
    val displayOrder: Float,
    val description: String?,
    val iconUrl: String,
    val languageStatus: LanguageStatus,
    val audit: AuditDto
)
