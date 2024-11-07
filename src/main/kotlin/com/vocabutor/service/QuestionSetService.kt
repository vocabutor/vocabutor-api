package com.vocabutor.service

import com.vocabutor.dto.QuestionSetDto
import com.vocabutor.dto.QuestionSetWithCurrentQuestionDto
import com.vocabutor.dto.of
import com.vocabutor.dto.request.QuestionFeedbackDto
import com.vocabutor.dto.request.QuestionSetCreationRequest
import com.vocabutor.entity.QuestionSetStatus
import com.vocabutor.entity.QuestionStatus
import com.vocabutor.entity.toDto
import com.vocabutor.exception.*
import com.vocabutor.repository.CardRepository
import com.vocabutor.repository.QuestionRepository
import com.vocabutor.repository.QuestionSetRepository
import com.vocabutor.repository.dbTransaction

class QuestionSetService(
    private val questionSetRepository: QuestionSetRepository,
    private val questionRepository: QuestionRepository,
    private val cardRepository: CardRepository
) {

    suspend fun createSet(userId: Long, currentUsername: String, req: QuestionSetCreationRequest): QuestionSetDto =
        dbTransaction {
            val newQuestionSets = questionSetRepository.findByUserIdAndLanguageIdAndStatus(
                userId, req.languageId, QuestionSetStatus.NEW
            )
            if (newQuestionSets.size > 0) {
                throw UnfinishedQuestionSetFoundException()
            }
            val inProgressSets = questionSetRepository.findByUserIdAndLanguageIdAndStatus(
                userId, req.languageId, QuestionSetStatus.IN_PROGRESS
            )
            if (inProgressSets.size > 0) {
                throw UnfinishedQuestionSetFoundException()
            }

            val languageAvailableCardsCount = cardRepository.countByUserIdAndLanguageId(userId, req.languageId)
            if (languageAvailableCardsCount < req.count) {
                throw NotEnoughCardsForSetCreationException()
            }

            val cards = cardRepository.findByUserIdAndLanguageId(userId, req.languageId)
            val selectedCards = cards.shuffled().take(req.count.toInt())
            val setId = questionSetRepository.insert(req, currentUsername, userId, 0, QuestionSetStatus.NEW)

            val mappedQuestions = selectedCards.mapIndexed { index, card ->
                QuestionRepository.QuestionInsert(
                    userId,
                    currentUsername,
                    req.languageId,
                    setId,
                    (index + 1).toLong(),
                    card.id,
                    QuestionStatus.NEW,
                    card.phrase,
                    card.answer
                )
            }

            for (q in mappedQuestions) {
                questionRepository.insert(q)
            }

            questionSetRepository.findById(setId)?.toDto()
                ?: throw InternalServerError("could not fetch created question set")
        }

    suspend fun viewCurrentQuestion(
        setId: String, userId: Long, currentUsername: String, currentIndex: Long
    ): QuestionSetWithCurrentQuestionDto = dbTransaction {
        val questionSet = questionSetRepository.findById(setId)?.takeIf { it.userId == userId }
            ?: throw NotFoundError("question set $setId not found")

        if (currentIndex != questionSet.progressIndex) {
            throw UnmatchedQuestionSetIndexException()
        }

        if (questionSet.status == QuestionSetStatus.CANCELLED || questionSet.status == QuestionSetStatus.FINISHED) {
            throw InvalidStateForQuestionSetException()
        }

        if (questionSet.status == QuestionSetStatus.NEW) {
            if (currentIndex == 0L) {
                questionSetRepository.start(questionSet.id, currentUsername)
            } else {
                throw InvalidStateForQuestionSetException()
            }
        }

        val question = questionRepository.findBySetIdAndOrder(setId, currentIndex + 1)
            ?: throw InternalServerError("failed to fetch current question in the set from db")

        if (question.status != QuestionStatus.NEW) {
            throw InvalidStateForQuestionException()
        }

        questionRepository.updateViewedAt(question.id, currentUsername)

        val updatedQuestion = questionRepository.findById(question.id)
            ?: throw InternalServerError("failed to fetch question after setting viewedAt")

        QuestionSetWithCurrentQuestionDto.of(questionSet.toDto(), updatedQuestion.toDto())
    }

    suspend fun saveFeedbackAndProceedToNext(
        setId: String, userId: Long, currentUsername: String, currentIndex: Long, body: QuestionFeedbackDto
    ): QuestionSetDto = dbTransaction {
        val questionSet = questionSetRepository.findById(setId)?.takeIf { it.userId == userId }
            ?: throw NotFoundError("question set $setId not found")

        if (currentIndex != questionSet.progressIndex) {
            throw UnmatchedQuestionSetIndexException()
        }

        if (questionSet.status == QuestionSetStatus.FINISHED || questionSet.status == QuestionSetStatus.CANCELLED || questionSet.status == QuestionSetStatus.NEW) {
            throw InvalidStateForQuestionSetException()
        }

        val question = questionRepository.findBySetIdAndOrder(setId, currentIndex + 1)
            ?: throw InternalServerError("failed to fetch current question in the set from db")

        if (question.status != QuestionStatus.NEW) {
            throw InvalidStateForQuestionException()
        }

        if (question.viewedAt == null) {
            throw InvalidStateForQuestionException()
        }

        questionRepository.feedbackUpdate(question.id, currentUsername, body.difficulty)

        if (questionSet.progressIndex + 1 == questionSet.count) {
            questionSetRepository.finish(questionSet.id, currentUsername)
        } else {
            questionSetRepository.updateProgress(questionSet.id, currentUsername, currentIndex + 1)
        }

        questionSetRepository.findById(questionSet.id)?.toDto()
            ?: throw InternalServerError("failed to fetched updated question set")
    }

    suspend fun cancel(setId: String, userId: Long, currentUsername: String): QuestionSetDto = dbTransaction {
        val questionSet = questionSetRepository.findById(setId)?.takeIf { it.userId == userId }
            ?: throw NotFoundError("question set $setId not found")

        if (questionSet.status == QuestionSetStatus.FINISHED) {
            throw InvalidStateForQuestionSetException()
        }
        questionSetRepository.updateStatus(questionSet.id, currentUsername, QuestionSetStatus.CANCELLED)
        questionSetRepository.findById(questionSet.id)?.toDto()
            ?: throw InternalServerError("failed to fetch updated question set")
    }

}