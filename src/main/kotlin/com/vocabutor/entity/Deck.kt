package com.vocabutor.entity

data class Deck(
    val id: String,
    val userId: Long,
    val title: String,
    val languageId: Long,
    val answerLanguageId: Long? = null,
    val audit: Audit
)
