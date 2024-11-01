package com.vocabutor.dto

import com.vocabutor.entity.DeckStatus
import kotlinx.serialization.Serializable

@Serializable
data class DeckDto(
    val id: String,
    val userId: Long,
    val title: String,
    val status: DeckStatus,
    val languageId: Long,
    val answerLanguageId: Long? = null,
    val audit: AuditDto
)
