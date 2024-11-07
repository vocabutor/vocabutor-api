package com.vocabutor.dto

import com.vocabutor.entity.QuestionStatus
import com.vocabutor.util.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class QuestionDto(
    val id: String,
    val userId: Long,
    val languageId: Long,
    val questionSetId: String,
    val orderInSet: Long,
    val cardId: String,
    val status: QuestionStatus,
    @Serializable(with = InstantSerializer::class) val viewedAt: Instant? = null,
    @Serializable(with = InstantSerializer::class) val answeredAt: Instant? = null,
    val difficulty: Int? = null,
    val phrase: String,
    val expectedAnswer: String,
    val audit: AuditDto
)
