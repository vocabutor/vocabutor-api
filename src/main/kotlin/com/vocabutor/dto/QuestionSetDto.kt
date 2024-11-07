package com.vocabutor.dto

import com.vocabutor.entity.QuestionSetStatus
import com.vocabutor.util.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class QuestionSetDto(
    val id: String,
    val userId: Long,
    val languageId: Long,
    val deckId: String? = null,
    val count: Long,
    val status: QuestionSetStatus,
    val progressIndex: Long,
    @Serializable(with = InstantSerializer::class) val startedAt: Instant? = null,
    @Serializable(with = InstantSerializer::class) val finishedAt: Instant? = null,
    val auditDto: AuditDto
)
