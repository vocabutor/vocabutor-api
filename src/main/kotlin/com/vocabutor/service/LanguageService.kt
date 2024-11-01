package com.vocabutor.service

import com.vocabutor.dto.LanguageDto
import com.vocabutor.dto.request.AddLanguageRequest
import com.vocabutor.dto.request.UpdateLanguageRequest
import com.vocabutor.entity.Language
import com.vocabutor.entity.LanguageStatus
import com.vocabutor.entity.toDto
import com.vocabutor.exception.InternalServerError
import com.vocabutor.exception.NotFoundError
import com.vocabutor.repository.LanguageRepository

class LanguageService(private val languageRepository: LanguageRepository) {

    suspend fun insert(req: AddLanguageRequest, currentUsername: String): LanguageDto {
        val languageId = languageRepository.insert(req, currentUsername)
        return languageRepository.findById(languageId)?.toDto()
            ?: throw InternalServerError("Failed to fetch inserted language row by id $languageId")
    }

    suspend fun getById(id: Long): LanguageDto = languageRepository.findById(id)?.toDto()
        ?: throw NotFoundError("language with id $id not found")

    suspend fun getAll(): List<LanguageDto> = languageRepository.findAll()
        .map { language: Language -> language.toDto() }

    suspend fun update(id: Long, req: UpdateLanguageRequest, currentUsername: String): LanguageDto {
        languageRepository.update(id, req, currentUsername)
        return languageRepository.findById(id)?.toDto()
            ?: throw InternalServerError("Failed to fetch updated language id $id")
    }

    suspend fun updateOrder(id: Long, order: Float, currentUsername: String): LanguageDto {
        languageRepository.updateOrder(id, order, currentUsername)
        return languageRepository.findById(id)?.toDto()
            ?: throw InternalServerError("Failed to fetch updated language order id $id")
    }

    suspend fun updateStatus(id: Long, status: LanguageStatus, currentUsername: String): LanguageDto {
        languageRepository.updateStatus(id, status, currentUsername)
        return languageRepository.findById(id)?.toDto()
            ?: throw InternalServerError("Failed to fetch updated language status id $id")
    }

}