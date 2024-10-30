package com.vocabutor.routes

import com.vocabutor.dto.request.AddCardRequest
import com.vocabutor.dto.request.UpdateCardRequest
import com.vocabutor.entity.CardStatus
import com.vocabutor.security.userId
import com.vocabutor.security.username
import com.vocabutor.service.CardService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cardRoutes(cardService: CardService) {
    route("/v1/cards") {
        get {
            val page = call.request.queryParameters["page"]?.toInt() ?: 0
            val size = call.request.queryParameters["size"]?.toInt() ?: 10
            val query = call.request.queryParameters["q"] ?: ""

            val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@get
            }
            call.respond(cardService.pageAll(userId, query, page, size))
        }

        get("/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@get
            }
            call.respond(cardService.getByIdOrNotFound(id, userId))
        }

        post {
            val username = call.principal<JWTPrincipal>()?.username() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@post
            }
            val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@post
            }
            val req = call.receive<AddCardRequest>()
            call.respond(cardService.create(req, userId, username))
        }

        put("/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val username = call.principal<JWTPrincipal>()?.username() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@put
            }
            val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@put
            }
            val reqBody = call.receive<UpdateCardRequest>()
            call.respond(cardService.update(id, reqBody, username, userId))
        }

        delete("/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val username = call.principal<JWTPrincipal>()?.username() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@delete
            }
            val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@delete
            }
            cardService.updateStatus(id, CardStatus.DELETED, username, userId)
            call.respond("OK")
        }

    }
}