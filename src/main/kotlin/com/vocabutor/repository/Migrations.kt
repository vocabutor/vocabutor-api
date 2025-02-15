package com.vocabutor.repository

import com.vocabutor.repository.UserRepository.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class Migrations(database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
            SchemaUtils.create(UserGoogleAuthRepository.UserGoogleAuthTable)
            SchemaUtils.create(LanguageRepository.LanguageTable)
            SchemaUtils.create(CardRepository.CardTable)
            SchemaUtils.create(CardRepository.CardRevisionTable)
            SchemaUtils.create(DeckRepository.DeckTable)
            SchemaUtils.create(CardDeckRelRepository.CardDeckRelTable)
            SchemaUtils.create(QuestionSetRepository.QuestionSetTable)
            SchemaUtils.create(QuestionRepository.QuestionTable)
        }
    }
}