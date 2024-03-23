package de.westnordost.osmfeatures

import kotlinx.serialization.json.*
import okio.Buffer
import okio.Source
import okio.buffer
internal object JsonUtils {

    fun <T> parseList(array: JsonArray?, t: (JsonElement) -> T): List<T> {
        return array?.mapNotNull { item -> t(item) }.orEmpty()
    }

    fun parseStringMap(map: JsonObject?): Map<String, String> {
        if (map == null) return HashMap(1)
        return map.map { (key, value) -> key to value.jsonPrimitive.content}.toMap().toMutableMap()
    }

    // this is only necessary because Android uses some old version of org.json where
    // new JSONObject(new JSONTokener(inputStream)) is not defined...

    fun getContent(source: Source): String {
        val sink = Buffer()
        source.buffer().readAll(sink)

        return sink.readUtf8()
    }
}
