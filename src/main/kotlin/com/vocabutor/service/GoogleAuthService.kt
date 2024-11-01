package com.vocabutor.service

import com.vocabutor.entity.User
import com.vocabutor.entity.UserGoogleAuth
import com.vocabutor.repository.UserGoogleAuthRepository
import com.vocabutor.repository.UserRepository
import com.vocabutor.repository.dbTransaction
import com.vocabutor.security.JWTConfig
import com.vocabutor.security.createToken
import com.vocabutor.security.google.UserInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.auth.*
import java.time.Clock
import java.time.Instant

class GoogleAuthService(
    private val userRepository: UserRepository,
    private val userGoogleAuthRepository: UserGoogleAuthRepository,
    private val httpClient: HttpClient,
    private val jwtConfig: JWTConfig,
    private val clock: Clock
) {

    suspend fun handleGoogleAuthCallback(principal: OAuthAccessTokenResponse.OAuth2): String {
        val googleAccessToken = principal.accessToken
        val expirationInstant = Instant.now().plusSeconds(Math.abs(principal.expiresIn - 30))
        val userInfo = getUserInfo(googleAccessToken, httpClient)
        var user = createOrFetchUserWithGoogleAuthInfo(userInfo, googleAccessToken, expirationInstant)
        return jwtConfig.createToken(clock, googleAccessToken, 3600, user)
    }

    private suspend fun createOrFetchUserWithGoogleAuthInfo(
        userInfo: UserInfo,
        googleAccessToken: String,
        expirationInstant: Instant
    ): User {
        val existingUserGoogleAuth = userGoogleAuthRepository.findByGoogleId(userInfo.id)
        if (existingUserGoogleAuth != null) {
            return manageExistingUser(existingUserGoogleAuth, googleAccessToken, expirationInstant)
        }
        return createUser(userInfo, googleAccessToken, expirationInstant)
    }

    private suspend fun createUser(
        userInfo: UserInfo,
        googleAccessToken: String,
        expirationInstant: Instant
    ) = dbTransaction {
        val user = userRepository.findByEmail(userInfo.email) ?: userRepository.findById(
            userRepository.insert(
                User(name = userInfo.name, email = userInfo.email, username = extractUsername(userInfo.email)),
                "system:googleCallback"
            )
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
        user
    }

    private suspend fun manageExistingUser(
        existingUserGoogleAuth: UserGoogleAuth,
        googleAccessToken: String,
        expirationInstant: Instant
    ): User {
        val existingUser = userRepository.findById(existingUserGoogleAuth.userId)
            ?: throw IllegalStateException(
                "user not found for google auth record with id ${existingUserGoogleAuth.userId}"
            )
        userGoogleAuthRepository.updateAccessTokenForGoogleId(
            existingUserGoogleAuth.googleId, googleAccessToken, expirationInstant, existingUser.username
        )
        return existingUser
    }

    private suspend fun getUserInfo(
        accessToken: String,
        httpClient: HttpClient
    ):
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

}