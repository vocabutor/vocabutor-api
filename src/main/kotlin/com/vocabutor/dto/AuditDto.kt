package com.vocabutor.dto

import com.vocabutor.util.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class AuditDto(
    @Serializable(with = InstantSerializer::class) val createdAt: Instant,
    @Serializable(with = InstantSerializer::class) val updatedAt: Instant,
    val createdBy: String,
    val updatedBy: String,
)
