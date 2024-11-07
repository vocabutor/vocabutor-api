package com.vocabutor.routes

import com.vocabutor.dto.request.QuestionFeedbackDto
import com.vocabutor.dto.request.QuestionSetCreationRequest
import com.vocabutor.security.userId
import com.vocabutor.security.username
import com.vocabutor.service.QuestionSetService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.questionSetRoutes(questionSetService: QuestionSetService) {
   route("/v1/question-sets") {
       post {
           val username = call.principal<JWTPrincipal>()?.username() ?: run {
               call.respond(HttpStatusCode.Unauthorized, "No access token")
               return@post
           }
           val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
               call.respond(HttpStatusCode.Unauthorized, "No access token")
               return@post
           }
           val body = call.receive<QuestionSetCreationRequest>()
           call.respond(questionSetService.createSet(userId, username, body))
       }

       get("/{id}/current-question") {
           val id = call.parameters["id"] ?: run {
               call.respond(HttpStatusCode.BadRequest)
               return@get
           }
           val index = call.request.queryParameters["index"]?.toLong() ?: run {
               call.respond(HttpStatusCode.BadRequest, "no index parameter set")
               return@get
           }
           val username = call.principal<JWTPrincipal>()?.username() ?: run {
               call.respond(HttpStatusCode.Unauthorized, "No access token")
               return@get
           }
           val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
               call.respond(HttpStatusCode.Unauthorized, "No access token")
               return@get
           }
           call.respond(questionSetService.viewCurrentQuestion(id, userId, username, index))
       }

       post("/{id}/feedback") {
           val id = call.parameters["id"] ?: run {
               call.respond(HttpStatusCode.BadRequest)
               return@post
           }
           val index = call.request.queryParameters["index"]?.toLong() ?: run {
               call.respond(HttpStatusCode.BadRequest, "no index parameter set")
               return@post
           }
           val username = call.principal<JWTPrincipal>()?.username() ?: run {
               call.respond(HttpStatusCode.Unauthorized, "No access token")
               return@post
           }
           val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
               call.respond(HttpStatusCode.Unauthorized, "No access token")
               return@post
           }
           val body = call.receive<QuestionFeedbackDto>()
           call.respond(questionSetService.saveFeedbackAndProceedToNext(id, userId, username, index, body))
       }

       patch("/{id}/status/cancel") {
           val id = call.parameters["id"] ?: run {
               call.respond(HttpStatusCode.BadRequest)
               return@patch
           }
           val username = call.principal<JWTPrincipal>()?.username() ?: run {
               call.respond(HttpStatusCode.Unauthorized, "No access token")
               return@patch
           }
           val userId = call.principal<JWTPrincipal>()?.userId() ?: run {
               call.respond(HttpStatusCode.Unauthorized, "No access token")
               return@patch
           }
           call.respond(questionSetService.cancel(id, userId, username))
       }

   }
}