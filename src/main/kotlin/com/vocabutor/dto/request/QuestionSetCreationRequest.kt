package com.vocabutor.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class QuestionSetCreationRequest(
    val languageId: Long,
    val deckId: String? = null,
    val count: Long,
)
