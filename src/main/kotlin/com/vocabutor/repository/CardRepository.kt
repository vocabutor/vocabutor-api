package com.vocabutor.repository

import com.vocabutor.dto.request.AddCardRequest
import com.vocabutor.dto.request.UpdateCardRequest
import com.vocabutor.entity.Audit
import com.vocabutor.entity.Card
import com.vocabutor.entity.CardStatus
import com.vocabutor.repository.CardRepository.CardTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Clock
import java.time.Instant
import java.util.UUID

class CardRepository {

    object CardTable : Table("app_card") {
        val id = varchar("id", length = 100)
        val userId = long("user_id")
            .references(UserRepository.Users.id)
        val languageId = long("language_id")
            .references(LanguageRepository.LanguageTable.id)
        val phrase = varchar("phrase", length = 500)
        val answer = varchar("answer", length = 500)
        val status = varchar("status", length = 50)
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")
        val createdBy = varchar("created_by", length = 50)
        val updatedBy = varchar("updated_by", length = 50)

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun insert(req: AddCardRequest, userId: Long, status: CardStatus, currentUsername: String): String =
        dbTransaction {
            val now = Instant.now(Clock.systemUTC())
            CardTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[CardTable.userId] = userId
                it[languageId] = req.languageId
                it[phrase] = req.phrase
                it[answer] = req.answer
                it[CardTable.status] = status.name
                it[createdAt] = now
                it[updatedAt] = now
                it[createdBy] = currentUsername
                it[updatedBy] = currentUsername
            }[CardTable.id]
        }
    
    suspend fun findById(id: String): Card? = dbTransaction { 
        CardTable.selectAll().where{ CardTable.id eq id and (CardTable.status eq CardStatus.ACTIVE.name) }
            .map { cardRowMapper(it) }
            .singleOrNull()
    }
    
    suspend fun update(id: String, req: UpdateCardRequest, currentUsername: String) = dbTransaction {
        CardTable.update({ CardTable.id eq id }) {
            it[phrase] = req.phrase
            it[answer] = req.answer
            it[updatedAt] = Instant.now(Clock.systemUTC())
            it[updatedBy] = currentUsername
        }
    }

    suspend fun updateStatus(id: String, status: CardStatus, currentUsername: String) = dbTransaction {
        CardTable.update({ CardTable.id eq id }) {
            it[CardTable.status] = status.name
            it[updatedAt] = Instant.now(Clock.systemUTC())
            it[updatedBy] = currentUsername
        }
    }

    suspend fun pageByUserIdAndSearchQuery(userId: Long, offset: Long, limit: Int, search: String): List<Card> =
        dbTransaction {
            CardTable.selectAll().where {
                    (CardTable.userId eq userId) and ((CardTable.phrase ilike '%' + search + '%') or (CardTable.answer ilike '%' + search + '%')) and (CardTable.status eq CardStatus.ACTIVE.name)
                }
                .limit(limit, offset = offset)
                .map {
                    cardRowMapper(it)
                }
        }

    suspend fun countByUserIdAndSearchQuery(userId: Long, search: String): Long =
        dbTransaction {
            CardTable.selectAll().where {
                (CardTable.userId eq userId) and ((CardTable.phrase ilike '%' + search + '%') or (CardTable.answer ilike '%' + search + '%')) and (CardTable.status eq CardStatus.ACTIVE.name)
            }.count()
        }

}

fun cardRowMapper(it: ResultRow) = Card(
    it[CardTable.id],
    it[CardTable.userId],
    it[CardTable.languageId],
    it[CardTable.phrase],
    it[CardTable.answer],
    CardStatus.valueOf(it[CardTable.status]),
    Audit(
        it[CardTable.createdAt],
        it[CardTable.updatedAt],
        it[CardTable.createdBy],
        it[CardTable.updatedBy]
    )
)