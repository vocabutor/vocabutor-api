package com.vocabutor.exception

open class AppException(val statusCode: Int, open val reason: String?) : RuntimeException("$statusCode: $reason")

open class InternalServerError(override val reason: String? = "Unknown server error") : AppException(500, reason)

open class BadRequestError(override val reason: String? = "Invalid request from client") : AppException(400, reason)

open class NotFoundError(override val reason: String? = "Not found") : AppException(404, reason)

class UnfinishedQuestionSetFoundException(override val reason: String? = "Unfinished question set found") : BadRequestError(reason = reason)

class NotEnoughCardsForSetCreationException() : BadRequestError(reason = "Not enough cards to create question set")

class UnmatchedQuestionSetIndexException() : BadRequestError(reason = "Unmatched index on question set")

class InvalidStateForQuestionException() : InternalServerError(reason = "Invalid state for question")

class InvalidStateForQuestionSetException() : InternalServerError(reason = "Invalid state for question set")
