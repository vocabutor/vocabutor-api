package com.vocabutor.plugins

import com.vocabutor.applicationHttpClient
import com.vocabutor.entity.User
import com.vocabutor.entity.UserGoogleAuth
import com.vocabutor.repository.Migrations
import com.vocabutor.repository.UserGoogleAuthRepository
import com.vocabutor.repository.UserRepository
import com.vocabutor.repository.dbTransaction
import com.vocabutor.security.JWTConfig
import com.vocabutor.security.createToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
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

    routing {
        get("/") {
            call.respondText("Vocabutor API running!")
        }

//        post("/signup") {
//            val dto = call.receive<UserDto>()
//            val entity = User(
//                name = dto.name,
//                email = dto.email,
//                username = dto.username,
//                dateOfBirth = dto.dateOfBirth)
//            val userId = userRepository.insert(entity, entity.username)
//            call.respond(HttpStatusCode.Created, userId)
//        }

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
                    val jwtToken = handleGoogleAuthCallback(principal, httpClient, userGoogleAuthRepository, userRepository, jwtConfig, clock)
                    call.respondText(jwtToken, contentType = ContentType.Text.Plain)
                }
            }
        }
    }
}

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

    val existingUserGoogleAuth = userGoogleAuthRepository.findByGoogleId(userInfo.id)
    if (existingUserGoogleAuth != null) {
        val existingUser = userRepository.findById(existingUserGoogleAuth.userId)
            ?: throw IllegalStateException(
                "user not found for google auth record with id ${existingUserGoogleAuth.userId}")
        userGoogleAuthRepository.updateAccessTokenForGoogleId(
            existingUserGoogleAuth.googleId, googleAccessToken, expirationInstant, existingUser.username)
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
        }
    }
    return jwtConfig.createToken(clock, googleAccessToken, 3600)
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
