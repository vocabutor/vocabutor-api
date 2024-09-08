package com.vocabutor

import com.vocabutor.plugins.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.kotlinx.json.*

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
    configureAuth(redirects, httpClient)
    configureRouting(redirects, httpClient)
}
