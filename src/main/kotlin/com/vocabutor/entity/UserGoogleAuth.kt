package com.vocabutor.entity

import java.time.Instant

data class UserGoogleAuth(
    val userId: Long,
    val googleId: String,
    val name: String,
    val givenName: String,
    val familyName: String,
    var accessToken: String,
    var accessTokenExpiresAt: Instant,
    val audit: Audit? = null,
)
