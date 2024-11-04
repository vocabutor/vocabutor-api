package com.vocabutor.routes

import com.vocabutor.dto.request.AddDeckRequest
import com.vocabutor.dto.request.UpdateDeckRequest
import com.vocabutor.entity.DeckStatus
import com.vocabutor.security.userId
import com.vocabutor.security.username
import com.vocabutor.service.DeckService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.deckRoutes(deckService: DeckService) {
    route("/v1/decks") {
        get {
            val page = call.request.queryParameters["page"]?.toInt() ?: 0
            val size = call.request.queryParameters["size"]?.toInt() ?: 10
            val query = call.request.queryParameters["q"] ?: ""

            val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@get
            }
            call.respond(deckService.pageAll(userId, query, page, size))
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
            call.respond(deckService.getByIdOrNotFound(id, userId))
        }

        get("/{id}/with-cards") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@get
            }
            call.respond(deckService.getByIdWithCardsOrNotFound(id, userId))
        }

        put("/{deckId}/cards/{cardId}") {
            val deckId = call.parameters["deckId"]
            if (deckId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }

            val cardId = call.parameters["cardId"]
            if (cardId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@put
            }
            val username = call.principal<JWTPrincipal>()?.username() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@put
            }
            deckService.addCardToDeck(userId, username, cardId, deckId)
            call.respond("ok")
        }

        delete("/{deckId}/cards/{cardId}") {
            val deckId = call.parameters["deckId"]
            if (deckId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            val cardId = call.parameters["cardId"]
            if (cardId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
                call.respond(HttpStatusCode.Unauthorized, "No access token")
                return@delete
            }

            deckService.removeCardFromDeck(userId, cardId, deckId)
            call.respond("ok")
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
            val req = call.receive<AddDeckRequest>()
            call.respond(deckService.create(req, userId, username))
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
            val reqBody = call.receive<UpdateDeckRequest>()
            call.respond(deckService.update(id, reqBody, username, userId))
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
            deckService.updateStatus(id, DeckStatus.DELETED, username, userId)
            call.respond("OK")
        }

    }
}