package com.vocabutor.plugins

import com.vocabutor.applicationHttpClient
import com.vocabutor.repository.*
import com.vocabutor.routes.cardRoutes
import com.vocabutor.routes.deckRoutes
import com.vocabutor.routes.languageRoutes
import com.vocabutor.security.JWTConfig
import com.vocabutor.service.CardService
import com.vocabutor.service.DeckService
import com.vocabutor.service.GoogleAuthService
import com.vocabutor.service.LanguageService
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Clock

val logger: Logger = LoggerFactory.getLogger("Routing")

fun Application.configureRouting(
    jwtConfig: JWTConfig, clock: Clock,
    httpClient: HttpClient = applicationHttpClient
) {
    val userRepository = UserRepository()
    val userGoogleAuthRepository = UserGoogleAuthRepository()
    val deckRepository = DeckRepository()
    val cardRepository = CardRepository()
    val cardDeckRelRepository = CardDeckRelRepository()

    val googleAuthService = GoogleAuthService(userRepository, userGoogleAuthRepository, httpClient, jwtConfig, clock)
    val languageService = LanguageService(LanguageRepository())
    val cardService = CardService(cardRepository)
    val deckService = DeckService(deckRepository, cardRepository, cardDeckRelRepository)

    routing {
        get("/") {
            call.respondText("Vocabutor API running!")
        }
        authenticate(jwtConfig.name) {
            languageRoutes(languageService)
            cardRoutes(cardService)
            deckRoutes(deckService)
        }
        authenticate("auth-oauth-google") {
            get("/login") {
                // Redirects to 'authorizeUrl' automatically
            }
            get("/callback") {
                (call.principal() as OAuthAccessTokenResponse.OAuth2?)?.let { principal ->
                    val jwtToken = googleAuthService.handleGoogleAuthCallback(principal)
                    call.respondText(jwtToken, contentType = ContentType.Text.Plain)
                }
            }
        }
    }
}




