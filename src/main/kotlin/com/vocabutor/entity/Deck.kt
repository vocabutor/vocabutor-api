package com.vocabutor.entity

import com.vocabutor.dto.DeckDto

data class Deck(
    val id: String,
    val userId: Long,
    val title: String,
    val status: DeckStatus,
    val languageId: Long,
    val answerLanguageId: Long? = null,
    val audit: Audit,
    val cards: List<Card> = emptyList()
)

enum class DeckStatus {
    ACTIVE, DELETED
}

fun Deck.toDto(): DeckDto =
    DeckDto(id, userId, title, status, languageId, answerLanguageId, audit.toDto(), cards.map { it.toDto() })