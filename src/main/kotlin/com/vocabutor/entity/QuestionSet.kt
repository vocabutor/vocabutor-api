package com.vocabutor.entity

import java.time.Instant

data class QuestionSet(
    val id: String,
    val userId: Long,
    val deckId: Long? = null,
    val count: Long,
    val status: QuestionSetStatus,
    val progressIndex: Long,
    val startedAt: Instant,
    val finishedAt: Instant,
    val audit: Audit
)

enum class QuestionSetStatus {
    NEW, IN_PROGRESS, FINISHED
}
