package de.westnordost.osmfeatures

import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.json.*

internal object JsonUtils {

    fun <T> parseList(array: JsonArray?, t: (JsonElement) -> T): List<T> {
        return array?.mapNotNull { item -> t(item) }.orEmpty()
    }

    fun parseStringMap(map: JsonObject?): Map<String, String> {
        if (map == null) return HashMap(1)
        return map.map { (key, value) -> key to value.jsonPrimitive.content}.toMap().toMutableMap()
    }
}

// TODO This can hopefully be replaced with a function from kotlinx-serialization soon
@OptIn(ExperimentalStdlibApi::class)
internal inline fun <reified T> Json.decodeFromSource(source: Source): T =
    decodeFromString(source.use { it.readString() })