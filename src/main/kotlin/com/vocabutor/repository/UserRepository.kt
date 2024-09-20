package com.vocabutor.repository

import com.vocabutor.entity.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction


class UserRepository(database: Database) {
    object Users : Table() {
        val id = long("id").autoIncrement()
        val name = varchar("name", length = 50)
        val age = integer("age")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun insert(user: User): Long = dbQuery {
        Users.insert {
            it[name] = user.name
            it[age] = user.age
        }[Users.id]
    }

    suspend fun findById(id: Long): User? {
        return dbQuery {
            Users.selectAll().where { Users.id eq id }
                .map { User(it[Users.id], it[Users.name], it[Users.age]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Long, user: User) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[age] = user.age
            }
        }
    }

    suspend fun delete(id: Long) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}