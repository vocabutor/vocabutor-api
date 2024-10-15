package com.vocabutor.exception

open class AppException(val statusCode: Int, open val reason: String?) : RuntimeException()

class InternalServerError(override val reason: String? = "Unknown server error") : AppException(500, reason)

class BadRequestError(override val reason: String? = "Invalid request from client") : AppException(400, reason)

class NotFoundError(override val reason: String? = "Not found") : AppException(404, reason)
