package com.vocabutor.plugins

import com.vocabutor.applicationHttpClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("Routing")

fun Application.configureRouting(redirects: MutableMap<String, String>,
                                 httpClient: HttpClient = applicationHttpClient) {

    routing {
        get("/") {
            call.respondText("Vocabulator API running!")
        }
        authenticate("auth-oauth-google") {
            get("/login") {
                // Redirects to 'authorizeUrl' automatically
            }
            get("/callback") {
                val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                // redirects home if the url is not found before authorization
                currentPrincipal?.let { principal ->
                    principal.state?.let { state ->
                        call.sessions.set(UserSession(state, principal.accessToken))
                        redirects[state]?.let { redirect ->
                            call.respondRedirect(redirect)
                            return@get
                        }
                    }
                }
                call.respondRedirect("/home")
            }
        }

        get("/{path}") {
            val userSession: UserSession? = getSession(call)
            if (userSession != null) {
                val userInfo: UserInfo? = getPersonalGreeting(httpClient, userSession)
                if (userInfo == null) {
                    logger.debug("could not fetch user info. redirecting to login")
                    call.respondRedirect("/login")
                } else {
                    call.respondText("Hello, ${userInfo.name}!")
                }
            }
        }
    }
}

private suspend fun getSession(
    call: ApplicationCall
): UserSession? {
    val userSession: UserSession? = call.sessions.get()
    //if there is no session, redirect to login
    if (userSession == null) {
        val redirectUrl = URLBuilder("http://0.0.0.0:8080/login").run {
            parameters.append("redirectUrl", call.request.uri)
            build()
        }
        call.respondRedirect(redirectUrl)
        return null
    }
    return userSession
}

data class UserSession(val state: String, val token: String)

@Serializable
data class UserInfo(
    val id: String,
    val name: String,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val picture: String,
//    val locale: String
)

private suspend fun getPersonalGreeting(httpClient: HttpClient, userSession: UserSession): UserInfo? {
    if (userSession.token.isBlank()) return null
    val httpResponse = httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${userSession.token}")
        }
    }
    when (httpResponse.status) {
        HttpStatusCode.OK -> return httpResponse.body()
        HttpStatusCode.Unauthorized -> {
            logger.debug("got unauthorized response")
            return null
        }
        else -> {
            logger.error("failed to get user info from Google Oauth endpoint: ${httpResponse}")
            return null
        }
    }
}