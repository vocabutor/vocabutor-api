package com.vocabutor.entity

import java.time.Instant

data class Question(
    val id: Long,
    val questionSetId: Long,
    val order: Long,
    val cardId: String,
    val cardRevisionId: Long,
    val status: QuestionStatus,
    val viewedAt: Instant? = null,
    val answeredAt: Instant? = null,
    val difficulty: Int? = null,
    val phrase: String,
    val answer: String,
    val audit: Audit
)

enum class QuestionStatus {
    NEW,
    ANSWERED,
    DELETED,
    INACTIVE
}