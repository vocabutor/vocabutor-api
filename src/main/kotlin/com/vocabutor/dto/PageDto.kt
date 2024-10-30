package com.vocabutor.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageDto<T>(
    val page: Int,
    val size: Int,
    val items: List<T>,
    val totalCount: Long,
    val hasNext: Boolean
)