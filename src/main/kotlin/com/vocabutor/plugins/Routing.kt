package com.vocabutor.plugins

import com.vocabutor.applicationHttpClient
import com.vocabutor.repository.*
import com.vocabutor.routes.*
import com.vocabutor.security.JWTConfig
import com.vocabutor.service.*
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
    jwtConfig: JWTConfig, clock: Clock, googleClientId: String,
    httpClient: HttpClient = applicationHttpClient
) {
    val userRepository = UserRepository()
    val userGoogleAuthRepository = UserGoogleAuthRepository()
    val deckRepository = DeckRepository()
    val cardRepository = CardRepository()
    val cardDeckRelRepository = CardDeckRelRepository()
    val questionSetRepository = QuestionSetRepository()
    val questionRepository = QuestionRepository()

    val googleAuthService = GoogleAuthService(googleClientId, userRepository, userGoogleAuthRepository, httpClient, jwtConfig, clock)
    val languageService = LanguageService(LanguageRepository())
    val cardService = CardService(cardRepository)
    val deckService = DeckService(deckRepository, cardRepository, cardDeckRelRepository)
    val questionSetService = QuestionSetService(questionSetRepository, questionRepository, cardRepository)

    routing {
        get("/") {
            call.respondText("Vocabutor API running!")
        }
        openAuthRoutes(googleAuthService)
        authenticate(jwtConfig.name) {
            languageRoutes(languageService)
            cardRoutes(cardService)
            deckRoutes(deckService)
            questionSetRoutes(questionSetService)
            scraperRoutes()
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




