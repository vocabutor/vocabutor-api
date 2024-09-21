package com.vocabutor

import com.vocabutor.plugins.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.kotlinx.json.*
import java.time.Clock

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val applicationHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.module(httpClient: HttpClient = applicationHttpClient) {
    val redirects = mutableMapOf<String, String>()
    configureSerialization()
    val jwtConfig = environment.config.config("ktor.auth.jwt").jwtConfig()
    configureAuth(redirects, httpClient, jwtConfig)
    configureRouting(redirects, jwtConfig, Clock.systemUTC(), httpClient)
}
