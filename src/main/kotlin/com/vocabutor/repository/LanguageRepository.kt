package com.vocabutor.repository

import com.vocabutor.dto.request.AddLanguageRequest
import com.vocabutor.dto.request.UpdateLanguageRequest
import com.vocabutor.entity.Audit
import com.vocabutor.entity.Language
import com.vocabutor.entity.LanguageStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Clock
import java.time.Instant

class LanguageRepository {

    object LanguageTable : Table("app_language") {
        val id = long("id").autoIncrement()
        val name = varchar("name", length = 50)
        val shortName = varchar("short_name", length = 50)
        val displayOrder = float("display_order")
        val description = varchar("description", length = 500)
            .nullable()
        val iconUrl = varchar("icon_url", length = 1000)
        val languageStatus = varchar("language_status", length = 200)
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")
        val createdBy = varchar("created_by", length = 50)
        val updatedBy = varchar("updated_by", length = 50)

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun insert(req: AddLanguageRequest, currentUserName: String): Long = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        LanguageTable.insert {
            it[name] = req.name
            it[shortName] = req.shortName
            it[displayOrder] = req.displayOrder
            it[description] = req.description
            it[iconUrl] = req.iconUrl
            it[languageStatus] = req.languageStatus.name
            it[createdAt] = now
            it[updatedAt] = now
            it[createdBy] = currentUserName
            it[updatedBy] = currentUserName
        }[LanguageTable.id]
    }

    suspend fun update(id: Long, req: UpdateLanguageRequest, currentUserName: String) = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        LanguageTable.update({ LanguageTable.id eq id }) {
            it[name] = req.name
            it[shortName] = req.shortName
            it[description] = req.description
            it[iconUrl] = req.iconUrl
            it[updatedAt] = now
            it[updatedBy] = currentUserName
        }
    }

    suspend fun updateStatus(id: Long, status: LanguageStatus, currentUserName: String) = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        LanguageTable.update({ LanguageTable.id eq id }) {
            it[languageStatus] = status.name
            it[updatedAt] = now
            it[updatedBy] = currentUserName
        }
    }

    suspend fun updateOrder(id: Long, order: Float, currentUserName: String) = dbTransaction {
        val now = Instant.now(Clock.systemUTC())
        LanguageTable.update({ LanguageTable.id eq id }) {
            it[displayOrder] = order
            it[updatedAt] = now
            it[updatedBy] = currentUserName
        }
    }

    suspend fun findById(id: Long): Language? = dbTransaction {
        LanguageTable.selectAll().where{ LanguageTable.id eq id }
            .map {
                resultRowMapper(it)
            }
            .singleOrNull()
    }

    suspend fun findAll(): List<Language> = dbTransaction {
        LanguageTable.selectAll()
            .map {
                resultRowMapper(it)
            }
    }

    private fun resultRowMapper(it: ResultRow) = Language(
        it[LanguageTable.id],
        it[LanguageTable.name],
        it[LanguageTable.shortName],
        it[LanguageTable.displayOrder],
        it[LanguageTable.description],
        it[LanguageTable.iconUrl],
        LanguageStatus.valueOf(it[LanguageTable.languageStatus]),
        Audit(
            it[LanguageTable.createdAt],
            it[LanguageTable.updatedAt],
            it[LanguageTable.createdBy],
            it[LanguageTable.updatedBy]
        )
    )

}