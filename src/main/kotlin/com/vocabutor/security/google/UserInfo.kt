package com.vocabutor.security.google

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("verified_email") val verifiedEmail: Boolean,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val picture: String
)