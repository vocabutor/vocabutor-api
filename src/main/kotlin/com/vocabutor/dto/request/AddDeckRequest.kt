package com.vocabutor.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AddDeckRequest(
    val languageId: Long,
    val answerLanguageId: Long? = null,
    val title: String,
)
