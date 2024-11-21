package com.vocabutor.service

import com.vocabutor.dto.DeckDto
import com.vocabutor.dto.PageDto
import com.vocabutor.dto.request.AddDeckRequest
import com.vocabutor.dto.request.UpdateDeckRequest
import com.vocabutor.entity.DeckStatus
import com.vocabutor.entity.toDto
import com.vocabutor.exception.InternalServerError
import com.vocabutor.exception.NotFoundError
import com.vocabutor.repository.CardDeckRelRepository
import com.vocabutor.repository.CardRepository
import com.vocabutor.repository.DeckRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.sql.SortOrder

class DeckService(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val cardDeckRelRepository: CardDeckRelRepository) {

    suspend fun create(req: AddDeckRequest, userId: Long, currentUsername: String): DeckDto {
        val deckId = deckRepository.insert(req, userId, DeckStatus.ACTIVE, currentUsername)
        return deckRepository.findById(deckId)?.toDto()
            ?: throw InternalServerError("Failed to fetch inserted deck row by id $deckId")
    }

    suspend fun getByIdOrNotFound(id: String, userId: Long): DeckDto =
        deckRepository.findById(id)
            ?.takeIf { it.userId == userId }
            ?.toDto()
            ?: throw NotFoundError("deck with id $id not found")

    suspend fun getByIdWithCardsOrNotFound(id: String, userId: Long): DeckDto =
        deckRepository.findByIdWithCards(id)
            ?.takeIf { it.userId == userId }
            ?.toDto()
            ?: throw NotFoundError("deck with id $id not found")

    suspend fun pageAll(
        userId: Long,
        search: String,
        page: Int,
        size: Int,
        deckSort: DeckRepository.DeckSort,
        sortOrder: SortOrder
    ): PageDto<DeckDto> =
        coroutineScope {
            val offset = page.toLong() * size
            val countDeferred = async {
                deckRepository.countByUserIdAndSearchQuery(userId, search)
            }
            val decksDeferred = async {
                deckRepository.pageByUserIdAndSearchQuery(userId, offset, size, search, deckSort, sortOrder)
            }
            val count = countDeferred.await()
            val decks = decksDeferred.await()
            val deckDtos = decks.map { it.toDto() }
           PageDto(
                page,
                size,
                deckDtos,
                count,
                decks.size == size && offset + size != count
            )
        }

    suspend fun update(id: String, req: UpdateDeckRequest, currentUsername: String, userId: Long): DeckDto {
        getByIdOrNotFound(id, userId)
        deckRepository.update(id, req, currentUsername)
        return deckRepository.findById(id)?.toDto()
            ?: throw InternalServerError("Failed to fetch updated deck row by id $id")
    }

    suspend fun updateStatus(id: String, status: DeckStatus, currentUsername: String, userId: Long) {
        getByIdOrNotFound(id, userId)
        deckRepository.updateStatus(id, status, currentUsername)
    }

    suspend fun addCardToDeck(userId: Long, currentUsername: String, cardId: String, deckId: String) {
        cardRepository.findById(cardId)?.takeIf { it.userId == userId }
            ?: throw NotFoundError("card $cardId not found for user $userId")
        deckRepository.findById(deckId)?.takeIf { it.userId == userId }
            ?: throw NotFoundError("deck $deckId not found for user $userId")
        cardDeckRelRepository.upsert(cardId, deckId, currentUsername)
    }

    suspend fun removeCardFromDeck(userId: Long, cardId: String, deckId: String) {
        cardRepository.findById(cardId)?.takeIf { it.userId == userId }
            ?: throw NotFoundError("card $cardId not found for user $userId")
        deckRepository.findById(deckId)?.takeIf { it.userId == userId }
            ?: throw NotFoundError("deck $deckId not found for user $userId")
        cardDeckRelRepository.delete(cardId, deckId)
    }

}