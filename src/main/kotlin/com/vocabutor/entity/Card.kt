package com.vocabutor.entity

data class Card(
    val id: String,
    val userId: Long,
    val phrase: String,
    val answer: String,
    val status: CardStatus,
    val deckId: Long?,
    val audit: Audit
)

enum class CardStatus {
    ACTIVE,
    DELETED
}