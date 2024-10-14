package com.vocabutor.repository

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> dbTransaction(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }