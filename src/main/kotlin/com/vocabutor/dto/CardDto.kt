package com.vocabutor.dto

import com.vocabutor.entity.CardStatus
import kotlinx.serialization.Serializable

@Serializable
data class CardDto(
    val id: String,
    val userId: Long,
    val languageId: Long,
    val phrase: String,
    val answer: String,
    val status: CardStatus,
    val audit: AuditDto
)
