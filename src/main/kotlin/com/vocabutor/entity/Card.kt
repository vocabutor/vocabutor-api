package com.vocabutor.entity

import com.vocabutor.dto.CardDto

data class Card(
    val id: String,
    val userId: Long,
    val languageId: Long,
    val phrase: String,
    val answer: String,
    val status: CardStatus,
    val audit: Audit
)

enum class CardStatus {
    ACTIVE,
    DELETED
}

fun Card.toDto(): CardDto = CardDto(id, userId, languageId, phrase, answer, status, audit.toDto())