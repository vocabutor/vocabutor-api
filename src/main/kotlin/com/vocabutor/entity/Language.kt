package com.vocabutor.entity

import com.vocabutor.dto.LanguageDto

data class Language(
    val id: Long,
    val name: String,
    val shortName: String,
    val displayOrder: Float,
    val description: String? = null,
    val iconUrl: String,
    val languageStatus: LanguageStatus,
    val audit: Audit,
)

enum class LanguageStatus {
    ACTIVE,
    INACTIVE,
}

fun Language.toDto(): LanguageDto {
    return LanguageDto(
        id,
        name,
        shortName,
        displayOrder,
        description,
        iconUrl,
        languageStatus,
        audit.toDto()
    )
}