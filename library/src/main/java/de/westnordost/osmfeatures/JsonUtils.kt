package de.westnordost.osmfeatures

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
internal object JsonUtils {
    @Throws(JSONException::class)
    @JvmStatic
    fun <T> parseList(array: JSONArray?, t: Transformer<T>): List<T> {
        if (array == null) return ArrayList(0)
        val result: MutableList<T> = ArrayList(array.length())
        for (i in 0 until array.length()) {
            val item: T? = t.apply(array[i])
            if (item != null) result.add(item)
        }
        return result
    }

    @Throws(JSONException::class)
    @JvmStatic
    fun parseStringMap(map: JSONObject?): Map<String, String> {
        if (map == null) return HashMap(1)
        val result: MutableMap<String, String> = HashMap(map.length())
        val it = map.keys()
        while (it.hasNext()) {
            val key = it.next().intern()
            result[key] = map.getString(key)
        }
        return result
    }

    // this is only necessary because Android uses some old version of org.json where
    // new JSONObject(new JSONTokener(inputStream)) is not defined...
    @Throws(IOException::class)
    @JvmStatic
    fun createFromInputStream(inputStream: InputStream): JSONObject {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } != -1) {
            result.write(buffer, 0, length)
        }
        val jsonString = result.toString("UTF-8")
        return JSONObject(jsonString)
    }

    interface Transformer<T> {
        fun apply(item: Any?): T
    }
}
