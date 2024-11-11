package com.vocabutor.routes

import com.vocabutor.dictionary.job.OrdbokeneScraper
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.scraperRoutes() {
    val ordbokeneScraper = OrdbokeneScraper()
    route("/v1/scrapers") {
        post {
            ordbokeneScraper.runJob()
            call.respond("started")
        }
    }
}