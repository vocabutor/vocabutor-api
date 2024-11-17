package com.vocabutor.routes

import com.vocabutor.dto.request.GoogleLoginRequestDto
import com.vocabutor.service.GoogleAuthService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.openAuthRoutes(googleAuthService: GoogleAuthService) {
    route("/v1/auth") {
        route("/login") {
            post("/google") {
                val requestBody = call.receive<GoogleLoginRequestDto>()
                call.respond(googleAuthService.validateGoogleTokenAndIssueJwt(requestBody))
            }
        }
    }
}