package com.vocabutor.security.google

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenInfoResponse(
    val iss: String,
    val azp: String,
    val aud: String,
    val sub: String,
    val email: String,
    @SerialName("email_verified") val emailVerified: String,
    val name: String,
    val picture: String,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val iat: String,
    val exp: String,
    val jti: String,
    val alg: String,
    val kid: String,
    val typ: String,
)
