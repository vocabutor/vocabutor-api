package com.vocabutor.entity

import com.vocabutor.dto.AuditDto
import com.vocabutor.util.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Audit(
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant,
    val createdBy: String,
    val updatedBy: String,
)

fun Audit.toDto(): AuditDto {
    return AuditDto(createdAt, updatedAt, createdBy, updatedBy)
}