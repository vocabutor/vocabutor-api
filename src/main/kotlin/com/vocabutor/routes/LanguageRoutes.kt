package com.vocabutor.routes

import com.vocabutor.dto.request.AddLanguageRequest
import com.vocabutor.dto.request.UpdateLanguageRequest
import com.vocabutor.entity.LanguageStatus
import com.vocabutor.security.username
import com.vocabutor.service.LanguageService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.languageRoutes(languageService: LanguageService) {
    route("/v1/languages") {
        get {
            call.respond(languageService.getAll())
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toLong()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            call.respond(languageService.getById(id))
        }
        post {
            val username = call.principal<JWTPrincipal>()?.username() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@post
            }
            val req = call.receive<AddLanguageRequest>()
            call.respond(languageService.insert(req, username))
        }
        put("/{id}") {
            val id = call.parameters["id"]?.toLong()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val username = call.principal<JWTPrincipal>()?.username() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@put
            }
            val req = call.receive<UpdateLanguageRequest>()
            call.respond(languageService.update(id, req, username))
        }

        patch("/{id}/status/{status}") {
            val id = call.parameters["id"]?.toLong()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }
            val statusParam = call.parameters["status"]
            if (statusParam == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }
            val status = LanguageStatus.valueOf(statusParam)
            val username = call.principal<JWTPrincipal>()?.username() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@patch
            }
            call.respond(languageService.updateStatus(id, status, username))
        }

        patch("/{id}/order/{order}") {
            val id = call.parameters["id"]?.toLong()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }
            val order = call.parameters["order"]?.toFloat()
            if (order == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }
            val username = call.principal<JWTPrincipal>()?.username() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@patch
            }
            call.respond(languageService.updateOrder(id, order, username))
        }
    }
}