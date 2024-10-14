package com.vocabutor.entity

import com.vocabutor.util.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class User(
    val id: Long? = null,
    val name: String,
    val email: String,
    val username: String,
    @Serializable(with = InstantSerializer::class) val dateOfBirth: Instant? = null,
    val audit: Audit? = null,
)
