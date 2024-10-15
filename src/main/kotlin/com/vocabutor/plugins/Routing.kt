package com.vocabutor.plugins

import com.vocabutor.applicationHttpClient
import com.vocabutor.dto.request.AddLanguageRequest
import com.vocabutor.dto.request.UpdateLanguageRequest
import com.vocabutor.entity.LanguageStatus
import com.vocabutor.entity.User
import com.vocabutor.entity.UserGoogleAuth
import com.vocabutor.repository.*
import com.vocabutor.security.JWTConfig
import com.vocabutor.security.createToken
import com.vocabutor.service.LanguageService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Instant

val logger: Logger = LoggerFactory.getLogger("Routing")

fun Application.configureRouting(jwtConfig: JWTConfig, clock: Clock,
                                 httpClient: HttpClient = applicationHttpClient) {
    val database = Database.connect(
        url = environment.config.property("postgres.url").getString(),
        user = environment.config.property("postgres.user").getString(),
        driver = "org.postgresql.Driver",
        password = environment.config.property("postgres.pass").getString(),
    )
    Migrations(database);
    val userRepository = UserRepository()
    val userGoogleAuthRepository = UserGoogleAuthRepository()

    val languageService = LanguageService(LanguageRepository())

    routing {
        get("/") {
            call.respondText("Vocabutor API running!")
        }

        authenticate(jwtConfig.name) {

            get("/me") {
                val principal = call.principal<JWTPrincipal>() ?: run {
                    call.respond(HttpStatusCode.Forbidden, "Not logged in")
                    return@get
                }
                val accessToken = principal.getClaim(
                    "google_access_token",
                    String::class) ?: run {
                    call.respond(HttpStatusCode.Forbidden, "No access token")
                    return@get
                }
                val userInfo = getUserInfo(accessToken, httpClient)
                call.respondText("Hi, ${userInfo.name}!")
            }

            languageRoutes(languageService)
        }

        authenticate("auth-oauth-google") {
            get("/login") {
                // Redirects to 'authorizeUrl' automatically
            }
            get("/callback") {
                (call.principal() as OAuthAccessTokenResponse.OAuth2?)?.let {
                        principal ->
                    val jwtToken = handleGoogleAuthCallback(principal, httpClient, userGoogleAuthRepository, userRepository, jwtConfig, clock)
                    call.respondText(jwtToken, contentType = ContentType.Text.Plain)
                }
            }
        }
    }
}

private fun Route.languageRoutes(languageService: LanguageService) {
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

private fun JWTPrincipal.username(): String? = getClaim("username", String::class)


private suspend fun handleGoogleAuthCallback(
    principal: OAuthAccessTokenResponse.OAuth2,
    httpClient: HttpClient,
    userGoogleAuthRepository: UserGoogleAuthRepository,
    userRepository: UserRepository,
    jwtConfig: JWTConfig,
    clock: Clock
): String {
    val googleAccessToken = principal.accessToken
    val expirationInstant = Instant.now().plusSeconds(Math.abs(principal.expiresIn - 30))
    val userInfo = getUserInfo(googleAccessToken, httpClient)

    var username = ""

    val existingUserGoogleAuth = userGoogleAuthRepository.findByGoogleId(userInfo.id)
    if (existingUserGoogleAuth != null) {
        val existingUser = userRepository.findById(existingUserGoogleAuth.userId)
            ?: throw IllegalStateException(
                "user not found for google auth record with id ${existingUserGoogleAuth.userId}")
        userGoogleAuthRepository.updateAccessTokenForGoogleId(
            existingUserGoogleAuth.googleId, googleAccessToken, expirationInstant, existingUser.username)
        username = existingUser.username
    } else {
        dbTransaction {
            val user = userRepository.findByEmail(userInfo.email) ?:
                userRepository.findById(
                    userRepository.insert(
                    User(name = userInfo.name, email = userInfo.email, username = extractUsername(userInfo.email)),
                        "system:googleCallback")
                ) ?: throw IllegalStateException("failed to fetch or create user for google user id ${userInfo.id}")

            val userGoogleAuth = UserGoogleAuth(
                userId = user.id!!, // fetch from the database - should be non-null
                googleId = userInfo.id,
                name = userInfo.name,
                givenName = userInfo.givenName,
                familyName = userInfo.familyName,
                accessToken = googleAccessToken,
                accessTokenExpiresAt = expirationInstant
            )
            userGoogleAuthRepository.insert(userGoogleAuth, user.username)
            username = user.username
        }
    }
    return jwtConfig.createToken(clock, googleAccessToken, 3600, username)
}

private suspend fun getUserInfo(
    accessToken: String,
    httpClient: HttpClient):
        UserInfo {
    val response = httpResponse(httpClient, accessToken)
    return response.body()
}

private suspend fun httpResponse(
    httpClient: HttpClient,
    accessToken: String
) = httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
    headers {
        append("Authorization", "Bearer $accessToken")
    }
}

private fun extractUsername(email: String): String {
    return email.substringBefore("@")
}

@Serializable
data class UserInfo(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("verified_email") val verifiedEmail: Boolean,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val picture: String
)
