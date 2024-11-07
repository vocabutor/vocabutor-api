package com.vocabutor.repository

import com.vocabutor.entity.Audit
import com.vocabutor.entity.Question
import com.vocabutor.entity.QuestionStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Clock
import java.time.Instant
import java.util.*

class QuestionRepository {

    object QuestionTable : Table("app_question") {
        val id = varchar("id", length = 100)
        val userId = long("user_id")
            .references(UserRepository.Users.id)
        val languageId = long("language_id")
            .references(LanguageRepository.LanguageTable.id)
        val questionSetId = varchar("question_set_id", length = 100)
            .references(QuestionSetRepository.QuestionSetTable.id)
        val orderInSet = long("order_in_set")
        val cardId = varchar("card_id", length = 100)
            .references(CardRepository.CardTable.id)
        val status = varchar("status", length = 100)
        val viewedAt = timestamp("viewed_at").nullable()
        val answeredAt = timestamp("answered_at").nullable()
        val difficulty = integer("difficulty").nullable()
        val phrase = varchar("phrase", length = 500)
        val expectedAnswer = varchar("expected_answer", length = 500)
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
            index(false, questionSetId)
            index(true, questionSetId, orderInSet)
            index(false, cardId)
        }
    }

    data class QuestionInsert(
        val userId: Long,
        val username: String,
        val languageId: Long,
        val setId: String,
        val order: Long,
        val cardId: String,
        val status: QuestionStatus,
        val phrase: String,
        val expectedAnswer: String
    )

    suspend fun insert(q: QuestionInsert): String = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        QuestionTable.insert {
            it[id] = UUID.randomUUID().toString()
            it[userId] = q.userId
            it[languageId] = q.languageId
            it[questionSetId] = q.setId
            it[orderInSet] = q.order
            it[cardId] = q.cardId
            it[status] = q.status.name
            it[phrase] = q.phrase
            it[expectedAnswer] = q.expectedAnswer
            it[createdAt] = now
            it[updatedAt] = now
            it[createdBy] = q.username
            it[updatedBy] = q.username
        }[QuestionTable.id]
    }

    suspend fun findBySetIdAndOrder(setId: String, order: Long): Question? = dbTransaction {
        QuestionTable.selectAll()
            .where{ (QuestionTable.questionSetId eq setId) and (QuestionTable.orderInSet eq order ) }
            .map { rowMapper(it) }
            .singleOrNull()
    }

    suspend fun findById(id: String): Question? = dbTransaction {
        QuestionTable.selectAll()
            .where{ QuestionTable.id eq id }
            .map { rowMapper(it) }
            .singleOrNull()
    }

    suspend fun updateViewedAt(id: String, username: String) = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        QuestionTable.update({ QuestionTable.id eq id }){
            it[viewedAt] = now
            it[updatedAt] = now
            it[updatedBy] = username
        }
    }

    suspend fun feedbackUpdate(id: String, username: String, difficulty: Int) = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        QuestionTable.update({ QuestionTable.id eq id }) {
            it[answeredAt] = now
            it[status] = QuestionStatus.ANSWERED.name
            it[QuestionTable.difficulty] = difficulty
            it[updatedAt] = now
            it[updatedBy] = username
        }
    }

    private fun rowMapper(it: ResultRow) = Question(
        it[QuestionTable.id],
        it[QuestionTable.userId],
        it[QuestionTable.languageId],
        it[QuestionTable.questionSetId],
        it[QuestionTable.orderInSet],
        it[QuestionTable.cardId],
        QuestionStatus.valueOf(it[QuestionTable.status]),
        it[QuestionTable.viewedAt],
        it[QuestionTable.answeredAt],
        it[QuestionTable.difficulty],
        it[QuestionTable.phrase],
        it[QuestionTable.expectedAnswer],
        Audit(
            it[QuestionTable.createdAt],
            it[QuestionTable.updatedAt],
            it[QuestionTable.createdBy],
            it[QuestionTable.updatedBy]
        )
    )

}