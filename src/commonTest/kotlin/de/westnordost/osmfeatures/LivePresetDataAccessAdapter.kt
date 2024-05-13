package de.westnordost.osmfeatures

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.Source

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

        // TODO will be able to use Source directly in ktor v3
        val jsonBytes = runBlocking { client.get(url).readBytes() }
        val buffer = Buffer()
        buffer.write(jsonBytes)
        return buffer
    }
}
