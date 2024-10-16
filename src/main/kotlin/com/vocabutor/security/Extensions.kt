package com.vocabutor.security

import io.ktor.server.auth.jwt.*

fun JWTPrincipal.username(): String? = getClaim("username", String::class)