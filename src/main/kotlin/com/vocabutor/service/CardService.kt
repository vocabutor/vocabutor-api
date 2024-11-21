package com.vocabutor.service

import com.vocabutor.dto.CardDto
import com.vocabutor.dto.PageDto
import com.vocabutor.dto.request.AddCardRequest
import com.vocabutor.dto.request.UpdateCardRequest
import com.vocabutor.entity.CardStatus
import com.vocabutor.entity.toDto
import com.vocabutor.exception.InternalServerError
import com.vocabutor.exception.NotFoundError
import com.vocabutor.repository.CardRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CardService(private val cardRepository: CardRepository) {

    suspend fun create(req: AddCardRequest, userId: Long, currentUsername: String): CardDto {
        //TODO: add revision
        val cardId = cardRepository.insert(req, userId, CardStatus.ACTIVE, currentUsername)
        return cardRepository.findById(cardId)?.toDto()
            ?: throw InternalServerError("Failed to fetch inserted card row by id $cardId")
    }

    suspend fun getByIdOrNotFound(id: String, userId: Long): CardDto =
        cardRepository.findById(id)
            ?.takeIf { it.userId == userId }
            ?.toDto()
            ?: throw NotFoundError("card with id $id not found")

    suspend fun pageAll(userId: Long, search: String, page: Int, size: Int): PageDto<CardDto> =
        coroutineScope {
            val offset = page.toLong() * size
            val countDeferred = async {
                cardRepository.countByUserIdAndSearchQuery(userId, search)
            }
            val cardsDeferred = async {
                cardRepository.pageByUserIdAndSearchQuery(userId, offset, size, search)
            }
            val count = countDeferred.await()
            val cards = cardsDeferred.await()
            val cardDtos = cards.map { it.toDto() }
            PageDto(page, size, cardDtos, count, cards.size == size && offset + size != count)
        }

    suspend fun update(id: String, req: UpdateCardRequest, currentUsername: String, userId: Long): CardDto {
        getByIdOrNotFound(id, userId)
        cardRepository.update(id, req, currentUsername)
        return cardRepository.findById(id)?.toDto()
            ?: throw InternalServerError("Failed to fetch updated card row by id $id")
    }

    suspend fun updateStatus(id: String, status: CardStatus, currentUsername: String, userId: Long) {
        getByIdOrNotFound(id, userId)
        cardRepository.updateStatus(id, status, currentUsername)
    }

}