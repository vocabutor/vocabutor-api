package com.vocabutor.repository

import com.vocabutor.entity.Audit
import com.vocabutor.entity.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Clock
import java.time.Instant

class UserRepository {
    object Users : Table("app_user") {
        val id = long("id").autoIncrement()
        val name = varchar("name", length = 50)
        val email = varchar("email", length = 50)
            .uniqueIndex("email_unique_index")
        val username = varchar("username", length = 50)
            .uniqueIndex("username_unique_index")
        val dateOfBirth = timestamp("date_of_birth")
            .nullable()
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")
        val createdBy = varchar("created_by", length = 50)
        val updatedBy = varchar("updated_by", length = 50)

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun insert(user: User, currentUsername: String): Long = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        Users.insert {
            it[name] = user.name
            it[dateOfBirth] = user.dateOfBirth
            it[email] = user.email
            it[username] = user.username
            it[createdAt] = now
            it[updatedAt] = now
            it[createdBy] = currentUsername
            it[updatedBy] = currentUsername
        }[Users.id]
    }

    suspend fun findById(id: Long): User? {
        return dbTransaction {
            Users.selectAll().where { Users.id eq id }
                .map {
                    User(
                        it[Users.id],
                        it[Users.name],
                        it[Users.email],
                        it[Users.username],
                        it[Users.dateOfBirth],
                        Audit(
                            it[Users.createdAt],
                            it[Users.updatedAt],
                            it[Users.createdBy],
                            it[Users.updatedBy]
                        )
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun findByEmail(email: String): User? {
        return dbTransaction {
            Users.selectAll().where { Users.email eq email }
                .map {
                    User(
                        it[Users.id],
                        it[Users.name],
                        it[Users.email],
                        it[Users.username],
                        it[Users.dateOfBirth],
                        Audit(
                            it[Users.createdAt],
                            it[Users.updatedAt],
                            it[Users.createdBy],
                            it[Users.updatedBy]
                        )
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(id: Long, user: User, currentUsername: String) {
        dbTransaction {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[dateOfBirth] = user.dateOfBirth
                it[updatedBy] = currentUsername
                it[updatedAt] = Instant.now(Clock.systemUTC())
            }
        }
    }

    suspend fun delete(id: Long) {
        dbTransaction {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }


}