package com.vocabutor.entity

import com.vocabutor.dto.QuestionDto
import java.time.Instant

data class Question(
    val id: String,
    val userId: Long,
    val languageId: Long,
    val questionSetId: String,
    val orderInSet: Long,
    val cardId: String,
//    val cardRevisionId: Long,
    val status: QuestionStatus,
    val viewedAt: Instant? = null,
    val answeredAt: Instant? = null,
    val difficulty: Int? = null,
    val phrase: String,
    val expectedAnswer: String,
    val audit: Audit
)

enum class QuestionStatus {
    NEW,
    ANSWERED,
    DELETED,
    INACTIVE
}

fun Question.toDto() = QuestionDto(
    id,
    userId,
    languageId,
    questionSetId,
    orderInSet,
    cardId,
    status,
    viewedAt,
    answeredAt,
    difficulty,
    phrase,
    expectedAnswer,
    audit.toDto()
)