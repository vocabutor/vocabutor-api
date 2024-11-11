package com.vocabutor.dictionary.job

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.*;
import kotlinx.serialization.json.Json
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class OrdbokeneScraper {

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            engine {
                https {
                    trustManager = object : X509TrustManager {
                        override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}

                        override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}

                        override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    }
                }
            }
            json(
                json = Json {
                    classDiscriminator = "type" // Ensure this matches your JSON structure
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    suspend fun runJob() {
        GlobalScope.launch {
            for (i in 880..40_000) {
                val article = getArticle(i)
                if (article != null) {
                    saveToElastic(article, i)
                }
                if (i % 50 == 0) {
                    println("successfully ingested $i document")
                }
                delay(100)
            }
        }
    }

    suspend fun getArticle(id: Int): Article? {
        try {
            val url = "https://ord.uib.no/bm/article/$id.json"
            return client.get(url).body()
        } catch (e: NoTransformationFoundException) {
            println("index $id not found")
        }
        return null
    }

    suspend fun saveToElastic(article: Article, id: Int) {

        val response: HttpResponse = client.put("http://localhost:9200/ordbokene/_doc/$id") {
            contentType(ContentType.Application.Json)
            setBody(article)
        }
        if (!response.status.isSuccess()) {
            throw Exception(response.bodyAsText())
        }

    }

}

@Serializable
data class Article(
    val article_id: Int,
    val submitted: String? = null,
    val suggest: List<String>? = null,
    val lemmas: List<Lemma>? = null,
    val body: Body? = null,
    val to_index: List<String>? = null,
    val author: String? = null,
    val edit_state: String? = null,
    val referers: List<Referer>? = null,
    val status: Int? = null,
    val updated: String? = null
)

@Serializable
data class Referer(
    val article_id: Int? = null,
    val hgno: Int? = null,
    val lemma: String? = null
)

@Serializable
data class Lemma(
    val type_: String? = null,
    val final_lexeme: String? = null,
    val hgno: Int? = null,
    val id: Int? = null,
    val inflection_class: String? = null,
    val lemma: String? = null,
    val paradigm_info: List<ParadigmInfo>? = null,
    val split_inf: Boolean? = null
)

@Serializable
data class ParadigmInfo(
    val from: String?,
    val inflection: List<Inflection>?,
    val inflection_group: String?,
    val paradigm_id: Int?,
    val standardisation: String?,
    val tags: List<String>?,
    val to: String?
)

@Serializable
data class Inflection(
    val tags: List<String>?,
    val word_form: String?
)

@Serializable
data class Body(
    val pronunciation: List<Pronunciation>? = null,
    val definitions: List<Definition>? = null
)

@Serializable
data class Pronunciation(
    val type_: String? = null,
    val content: String? = null,
//    val items: List<String>? = null
)

@Serializable
data class Definition(
    val type_: String? = null,
    val elements: List<Element>? = null,
    val id: Int? = null
)

@Serializable
data class Element(
    val type_: String? = null,
    val content: String? = null,
    val items: List<Item> = emptyList(),
    val quote: Quote? = null,
    val explanation: Explanation? = null
)

@Serializable
data class Item(
    val type_: String? = null,
    val article_id: Int? = null,
    val lemmas: List<LemmaRef>? = null,
    val definition_id: Int? = null
)

@Serializable
data class LemmaRef(
    val type_: String? = null,
    val hgno: Int? = null,
    val id: Int? = null,
    val lemma: String? = null
)

@Serializable
data class Quote(
    val content: String? = null,
    val items: List<Usage>? = null
)

@Serializable
data class Usage(
    val type_: String? = null,
    val text: String? = null
)

@Serializable
data class Explanation(
    val content: String? = null,
    val items: List<ExplanationItem>? = null
)

@Serializable
data class ExplanationItem(
    val type_: String? = null,
    val text: String? = null,
    val id: String? = null,
    val article_id: Int? = null,
    val lemmas: List<Lemma>? = emptyList()
)