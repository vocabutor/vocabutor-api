package com.vocabutor.repository

import com.vocabutor.entity.Audit
import com.vocabutor.entity.UserGoogleAuth
import com.vocabutor.repository.UserRepository.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Clock
import java.time.Instant

class UserGoogleAuthRepository {

    object UserGoogleAuthTable : Table("app_user_google_auth") {
        val id = long("id")
            .references(Users.id)
        val googleId = varchar("google_id", length = 255)
            .uniqueIndex("google_id_unique_index")
        val name = varchar("name", length = 255)
        val givenName = varchar("given_name", length = 255)
        val familyName = varchar("family_name", length = 255)
        val accessToken = varchar("access_token", length = 1024)
        val accessTokenExpiresAt = timestamp("access_token_expires_at")
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")
        val createdBy = varchar("created_by", length = 50)
        val updatedBy = varchar("updated_by", length = 50)

        override val primaryKey = PrimaryKey(Users.id)
    }

    suspend fun insert(userGoogleAuth: UserGoogleAuth, currentUsername: String): Long = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        UserGoogleAuthTable.insert {
            it[id] = userGoogleAuth.userId
            it[googleId] = userGoogleAuth.googleId
            it[name] = userGoogleAuth.name
            it[givenName] = userGoogleAuth.givenName
            it[familyName] = userGoogleAuth.familyName
            it[accessToken] = userGoogleAuth.accessToken
            it[accessTokenExpiresAt] = userGoogleAuth.accessTokenExpiresAt
            it[createdAt] = now
            it[updatedAt] = now
            it[createdBy] = currentUsername
            it[updatedBy] = currentUsername
        }[UserGoogleAuthTable.id]
    }

    suspend fun findById(id: Long): UserGoogleAuth? = dbTransaction {
        UserGoogleAuthTable.selectAll().where( UserGoogleAuthTable.id eq id )
            .map {
                UserGoogleAuth(
                    it[UserGoogleAuthTable.id],
                    it[UserGoogleAuthTable.googleId],
                    it[UserGoogleAuthTable.name],
                    it[UserGoogleAuthTable.givenName],
                    it[UserGoogleAuthTable.familyName],
                    it[UserGoogleAuthTable.accessToken],
                    it[UserGoogleAuthTable.accessTokenExpiresAt],
                    Audit(
                        it[UserGoogleAuthTable.createdAt],
                        it[UserGoogleAuthTable.updatedAt],
                        it[UserGoogleAuthTable.createdBy],
                        it[UserGoogleAuthTable.updatedBy]
                    )
                )
            }
            .singleOrNull()
    }

    suspend fun updateAccessTokenForGoogleId(googleId: String,
                                             accessToken: String,
                                             expiresAt: Instant,
                                             currentUsername: String) {
        dbTransaction {
            UserGoogleAuthTable.update({ UserGoogleAuthTable.googleId eq googleId })
            {
                it[UserGoogleAuthTable.accessToken] = accessToken
                it[accessTokenExpiresAt] = expiresAt
                it[updatedBy] = currentUsername
                it[updatedAt] = Instant.now(Clock.systemUTC())
            }
        }
    }

    suspend fun findByGoogleId(googleId: String): UserGoogleAuth? = dbTransaction {
        UserGoogleAuthTable.selectAll().where( UserGoogleAuthTable.googleId eq googleId )
            .map {
                UserGoogleAuth(
                    it[UserGoogleAuthTable.id],
                    it[UserGoogleAuthTable.googleId],
                    it[UserGoogleAuthTable.name],
                    it[UserGoogleAuthTable.givenName],
                    it[UserGoogleAuthTable.familyName],
                    it[UserGoogleAuthTable.accessToken],
                    it[UserGoogleAuthTable.accessTokenExpiresAt],
                    Audit(
                        it[UserGoogleAuthTable.createdAt],
                        it[UserGoogleAuthTable.updatedAt],
                        it[UserGoogleAuthTable.createdBy],
                        it[UserGoogleAuthTable.updatedBy]
                    )
                )
            }
            .singleOrNull()
    }

}