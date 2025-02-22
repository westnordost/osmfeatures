package de.westnordost.osmfeatures

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.buffered

class LivePresetDataAccessAdapter : ResourceAccessAdapter {

    private val client = HttpClient(CIO) { expectSuccess = true }
    private val baseUrl = "https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist"

    override fun exists(name: String): Boolean =
        name in listOf("presets.json", "de.json", "en.json", "en-GB.json")

    @Throws(IOException::class)
    override fun open(name: String): Source {
        val url = when(name) {
            "presets.json" -> "$baseUrl/presets.json"
            else -> "$baseUrl/translations/$name"
        }

        return runBlocking { client.get(url).bodyAsChannel().asSource().buffered() }
    }
}
