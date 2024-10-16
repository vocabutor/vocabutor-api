package com.vocabutor.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.connectToPostgres(): Database {
    return Database.connect(
        url = environment.config.property("postgres.url").getString(),
        user = environment.config.property("postgres.user").getString(),
        driver = "org.postgresql.Driver",
        password = environment.config.property("postgres.pass").getString(),
    )
}
