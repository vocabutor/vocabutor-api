package com.vocabutor.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class QuestionFeedbackDto(
    val difficulty: Int
)
