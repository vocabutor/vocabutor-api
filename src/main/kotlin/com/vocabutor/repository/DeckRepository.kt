package com.vocabutor.repository

import com.vocabutor.dto.request.AddDeckRequest
import com.vocabutor.dto.request.UpdateDeckRequest
import com.vocabutor.entity.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Clock
import java.time.Instant
import java.util.*

class DeckRepository {

    object DeckTable : Table("app_deck") {
        val id = varchar("id", length = 100)
        val userId = long("user_id")
            .references(UserRepository.Users.id)
        val languageId = long("language_id")
            .references(LanguageRepository.LanguageTable.id)
        val answerLanguageId = long("answer_language_id")
            .references(LanguageRepository.LanguageTable.id)
            .nullable()
        val title = varchar("title", length = 500)
        val status = varchar("status", length = 50)
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")
        val createdBy = varchar("created_by", length = 50)
        val updatedBy = varchar("updated_by", length = 50)

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun insert(req: AddDeckRequest, userId: Long, status: DeckStatus, currentUsername: String): String =
        dbTransaction {
            val now = Instant.now(Clock.systemUTC())
            DeckTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[DeckTable.userId] = userId
                it[languageId] = req.languageId
                it[answerLanguageId] = req.answerLanguageId
                it[title] = req.title
                it[DeckTable.status] = status.name
                it[createdAt] = now
                it[updatedAt] = now
                it[createdBy] = currentUsername
                it[updatedBy] = currentUsername
            }[DeckTable.id]
        }

    suspend fun findById(id: String): Deck? = dbTransaction {
        DeckTable.selectAll().where{ DeckTable.id eq id and (DeckTable.status eq DeckStatus.ACTIVE.name) }
            .map { rowMapper(it) }
            .singleOrNull()
    }

    suspend fun update(id: String, req: UpdateDeckRequest, currentUsername: String) = dbTransaction {
        DeckTable.update({ DeckTable.id eq id }) {
            it[title] = req.title
            it[updatedAt] = Instant.now(Clock.systemUTC())
            it[updatedBy] = currentUsername
        }
    }

    suspend fun updateStatus(id: String, status: DeckStatus, currentUsername: String) = dbTransaction {
        DeckTable.update({ DeckTable.id eq id }) {
            it[DeckTable.status] = status.name
            it[updatedAt] = Instant.now(Clock.systemUTC())
            it[updatedBy] = currentUsername
        }
    }

    suspend fun pageByUserIdAndSearchQuery(userId: Long, offset: Long, limit: Int, search: String): List<Deck> =
        dbTransaction {
            DeckTable.selectAll().where {
                (DeckTable.userId eq userId) and ((DeckTable.title ilike '%' + search + '%')) and (DeckTable.status eq DeckStatus.ACTIVE.name)
            }
                .limit(limit, offset = offset)
                .map {
                    rowMapper(it)
                }
        }

    suspend fun countByUserIdAndSearchQuery(userId: Long, search: String): Long =
        dbTransaction {
            DeckTable.selectAll().where {
                (DeckTable.userId eq userId) and ((DeckTable.title ilike '%' + search + '%')) and (DeckTable.status eq DeckStatus.ACTIVE.name)
            }.count()
        }

    private fun rowMapper(it: ResultRow) = Deck(
        it[DeckTable.id],
        it[DeckTable.userId],
        it[DeckTable.title],
        DeckStatus.valueOf(it[DeckTable.status]),
        it[DeckTable.languageId],
        it[DeckTable.answerLanguageId],
        Audit(
            it[DeckTable.createdAt],
            it[DeckTable.updatedAt],
            it[DeckTable.createdBy],
            it[DeckTable.updatedBy]
        )
    )

}