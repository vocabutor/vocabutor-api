package com.vocabutor.entity

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long? = null,
    val name: String,
    val age: Int
)
