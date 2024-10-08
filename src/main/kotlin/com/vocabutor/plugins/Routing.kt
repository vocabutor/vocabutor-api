package com.vocabutor.plugins

import com.vocabutor.applicationHttpClient
import com.vocabutor.dto.UserDto
import com.vocabutor.entity.User
import com.vocabutor.repository.UserRepository
import com.vocabutor.security.JWTConfig
import com.vocabutor.security.createToken
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

val logger: Logger = LoggerFactory.getLogger("Routing")

fun Application.configureRouting(jwtConfig: JWTConfig, clock: Clock,
                                 httpClient: HttpClient = applicationHttpClient) {
    val database = Database.connect(
        url = environment.config.property("postgres.url").getString(),
        user = environment.config.property("postgres.user").getString(),
        driver = "org.postgresql.Driver",
        password = environment.config.property("postgres.pass").getString(),
    )
    val userRepository = UserRepository(database)

    routing {
        get("/") {
            call.respondText("Vocabutor API running!")
        }

        post("/signup") {
            val dto = call.receive<UserDto>()
            val entity = User(
                name = dto.name,
                email = dto.email,
                username = dto.username,
                dateOfBirth = dto.dateOfBirth)
            val userId = userRepository.insert(entity, entity.username)
            call.respond(HttpStatusCode.Created, userId)
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
        }

        authenticate("auth-oauth-google") {
            get("/login") {
                // Redirects to 'authorizeUrl' automatically
            }
            get("/callback") {
                (call.principal() as OAuthAccessTokenResponse.OAuth2?)?.let {
                        principal ->
                    val accessToken = principal.accessToken
                    val jwtToken = jwtConfig.createToken(clock, accessToken, 3600)
                    call.respondText(jwtToken, contentType = ContentType.Text.Plain)
                }
            }
        }
    }
}

private suspend fun getUserInfo(
    accessToken: String,
    httpClient: HttpClient):
        UserInfo =
    httpClient.get("https://www.googleapis.com/oauth2/v1/userinfo") {
        headers {
            append("Authorization", "Bearer $accessToken")
        }
    }.body()


@Serializable
data class UserInfo(
    val id: String,
    val name: String,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val picture: String,
//    val locale: String
)
