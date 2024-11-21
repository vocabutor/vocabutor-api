package com.vocabutor.repository

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> dbTransaction(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }

fun String?.toSortOrder(): SortOrder? {
    for (order in SortOrder.entries) {
        if (this == order.name) {
            return order
        }
    }
    return null
}