package de.westnordost.osmfeatures

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.Buffer
import okio.Source
import okio.buffer
internal object JsonUtils {

    @JvmStatic
    fun <T> parseList(array: JsonArray?, t: Transformer<T>): List<T> {
        return array?.mapNotNull { item -> t.apply(item) }.orEmpty()
    }

    @JvmStatic
    fun parseStringMap(map: JsonObject?): Map<String, String> {
        if (map == null) return HashMap(1)
        return map.map { (key, value) -> key.intern() to value.jsonPrimitive.toString()}.toMap().toMutableMap()
    }

    // this is only necessary because Android uses some old version of org.json where
    // new JSONObject(new JSONTokener(inputStream)) is not defined...
    @JvmStatic
    fun createFromSource(source: Source): JsonObject {
        val sink = Buffer()
        source.buffer().readAll(sink)

        return Json.decodeFromString<JsonObject>(sink.readUtf8())
    }

    fun interface Transformer<T> {
        fun apply(item: Any?): T
    }
}
