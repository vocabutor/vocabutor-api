package com.vocabutor.repository

import com.vocabutor.dto.request.QuestionSetCreationRequest
import com.vocabutor.entity.Audit
import com.vocabutor.entity.QuestionSet
import com.vocabutor.entity.QuestionSetStatus
import com.vocabutor.repository.DeckRepository.DeckTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Clock
import java.time.Instant
import java.util.*

class QuestionSetRepository {

    object QuestionSetTable : Table("app_question_set") {
        val id = varchar("id", length = 100)
        val userId = long("user_id")
            .references(UserRepository.Users.id)
        val languageId = long("language_id")
            .references(LanguageRepository.LanguageTable.id)
        val deckId = varchar("deck_id", length = 100)
            .references(DeckTable.id)
            .nullable()
        val count = long("count")
        val status = varchar("status", length = 50)
        val progressIndex = long("progress_index")
        val startedAt = timestamp("started_at").nullable()
        val finishedAt = timestamp("finished_at").nullable()
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")
        val createdBy = varchar("created_by", length = 50)
        val updatedBy = varchar("updated_by", length = 50)

        override val primaryKey = PrimaryKey(id)

        init {
            index(false, userId)
            index(false, languageId)
            index(false, userId, languageId)
            index(false, status)
            index(false, userId, languageId, status)
        }
    }

    suspend fun insert(req: QuestionSetCreationRequest,
                       currentUsername: String,
                       userId: Long,
                       progressIndex: Long,
                       status: QuestionSetStatus): String = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        QuestionSetTable.insert {
            it[id] = UUID.randomUUID().toString()
            it[QuestionSetTable.userId] = userId
            it[languageId] = req.languageId
            it[deckId] = req.deckId
            it[count] = req.count
            it[QuestionSetTable.status] = status.name
            it[QuestionSetTable.progressIndex] = progressIndex
            it[createdAt] = now
            it[updatedAt] = now
            it[createdBy] = currentUsername
            it[updatedBy] = currentUsername
        }[QuestionSetTable.id]
    }

    suspend fun start(id: String, currentUsername: String) = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        QuestionSetTable.update({ QuestionSetTable.id eq id }) {
            it[status] = QuestionSetStatus.IN_PROGRESS.name
            it[startedAt] = now
            it[updatedAt] = now
            it[updatedBy] = currentUsername
        }
    }

    suspend fun updateProgress(id: String, currentUsername: String, newProgressIndex: Long) = dbTransaction {
        QuestionSetTable.update({ QuestionSetTable.id eq id }) {
            it[progressIndex] = newProgressIndex
            it[updatedAt] = Instant.now(Clock.systemUTC())
            it[updatedBy] = currentUsername
        }
    }

    suspend fun updateStatus(id: String, username: String, status: QuestionSetStatus) = dbTransaction {
        QuestionSetTable.update({ QuestionSetTable.id eq id }) {
            it[QuestionSetTable.status] = status.name
            it[updatedAt] = Instant.now(Clock.systemUTC())
            it[updatedBy] = username
        }
    }

    suspend fun finish(id: String, currentUsername: String) = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        QuestionSetTable.update({ QuestionSetTable.id eq id }) {
            it[status] = QuestionSetStatus.FINISHED.name
            it[finishedAt] = now
            it[updatedAt] = now
            it[updatedBy] = currentUsername
        }
    }

    suspend fun findByUserIdAndLanguageIdAndStatus(userId: Long, languageId: Long, status: QuestionSetStatus): List<QuestionSet>
        = dbTransaction {
            QuestionSetTable.selectAll().where{
                (QuestionSetTable.userId eq userId) and (QuestionSetTable.languageId eq languageId) and (QuestionSetTable.status eq status.name)
            }.map { rowMapper(it) }
    }

    suspend fun findById(id: String): QuestionSet? = dbTransaction {
        QuestionSetTable.selectAll().where{ QuestionSetTable.id eq id }
            .map { rowMapper(it) }
            .singleOrNull()
    }

    private fun rowMapper(it: ResultRow) = QuestionSet(
        it[QuestionSetTable.id],
        it[QuestionSetTable.userId],
        it[QuestionSetTable.languageId],
        it[QuestionSetTable.deckId],
        it[QuestionSetTable.count],
        QuestionSetStatus.valueOf(it[QuestionSetTable.status]),
        it[QuestionSetTable.progressIndex],
        it[QuestionSetTable.startedAt],
        it[QuestionSetTable.finishedAt],
        Audit(
            it[QuestionSetTable.createdAt],
            it[QuestionSetTable.updatedAt],
            it[QuestionSetTable.createdBy],
            it[QuestionSetTable.updatedBy]
        )
    )

}