package com.vocabutor.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.vocabutor.entity.User
import java.time.Clock

data class JWTConfig(
    val name: String,
    val realm: String,
    val secret: String,
    val audience: String,
    val issuer: String,
    val expirationSeconds: Long
)

fun JWTConfig.createToken(clock: Clock,
                          accessToken: String,
                          expirationSeconds: Long,
                          user: User,
                          roles :List<String>? = listOf()): String =
    JWT.create()
        .withAudience(this.audience)
        .withIssuer(this.issuer)
        .withClaim("google_access_token", accessToken)
        .withClaim("username", user.username)
        .withClaim("roles", roles)
        .withClaim("userId", user.id!!)
        .withExpiresAt(clock.instant().plusSeconds(expirationSeconds))
        .sign(Algorithm.HMAC256(this.secret))