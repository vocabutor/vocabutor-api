package com.vocabutor.repository

import org.jetbrains.exposed.sql.*

class InsensitiveLikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")

infix fun <T : String?> ExpressionWithColumnType<T>.ilike(pattern: String): Op<Boolean> {
    val castPattern = pattern as? T ?: throw IllegalArgumentException("Pattern cannot be cast to the expected type.")
    return InsensitiveLikeOp(this, QueryParameter(castPattern, columnType))
}