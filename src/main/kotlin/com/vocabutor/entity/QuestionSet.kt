package com.vocabutor.entity

import com.vocabutor.dto.QuestionSetDto
import java.time.Instant

data class QuestionSet(
    val id: String,
    val userId: Long,
    val languageId: Long,
    val deckId: String? = null,
    val count: Long,
    val status: QuestionSetStatus,
    val progressIndex: Long,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
    val audit: Audit
)

enum class QuestionSetStatus {
    NEW, IN_PROGRESS, FINISHED, CANCELLED,
}

fun QuestionSet.toDto() = QuestionSetDto(
    id, userId, languageId, deckId, count, status, progressIndex, startedAt, finishedAt, audit.toDto())