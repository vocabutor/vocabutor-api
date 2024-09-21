package com.vocabutor.plugins

import com.vocabutor.security.JWTConfig
import io.ktor.server.config.*

fun ApplicationConfig.jwtConfig(): JWTConfig =
    JWTConfig(
        name = property("name").getString(),
        realm = property("realm").getString(),
        secret = property("secret").getString(),
        audience = property("audience").getString(),
        issuer = property("issuer").getString(),
        expirationSeconds = property("expirationSeconds").getString().toLong()
    )