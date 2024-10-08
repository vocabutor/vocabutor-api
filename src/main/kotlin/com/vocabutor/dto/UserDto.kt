package com.vocabutor.dto

import com.vocabutor.util.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class UserDto(
    val id: Long? = null,
    val name: String,
    val email: String,
    val username: String,
    @Serializable(with = InstantSerializer::class) val dateOfBirth: Instant,
    val audit: AuditDto? = null,
)
