package com.vocabutor.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.upsert
import java.time.Clock
import java.time.Instant

class CardDeckRelRepository {

    object CardDeckRelTable : Table("app_card_deck_rel") {
        val cardId = varchar("card_id", length = 100)
        val deckId = varchar("deck_id", length = 100)
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")
        val createdBy = varchar("created_by", length = 50)
        val updatedBy = varchar("updated_by", length = 50)

        override val primaryKey = PrimaryKey(cardId, deckId)

        init {
            index(false, cardId)
            index(false, deckId)
        }
    }

    suspend fun upsert(cardId: String, deckId: String, currentUsername: String) = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        CardDeckRelTable.upsert(CardDeckRelTable.cardId, CardDeckRelTable.deckId) {
            it[CardDeckRelTable.cardId] = cardId
            it[CardDeckRelTable.deckId] = deckId
            it[createdAt] = now
            it[updatedAt] = now
            it[createdBy] = currentUsername
            it[updatedBy] = currentUsername
        }
    }

    suspend fun delete(cardId: String, deckId: String) = dbTransaction {
        CardDeckRelTable.deleteWhere {
            (CardDeckRelTable.cardId eq cardId) and (CardDeckRelTable.deckId eq deckId) }
    }

}