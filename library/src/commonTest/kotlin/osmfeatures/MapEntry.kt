package osmfeatures

internal class MapEntry private constructor(private val key: String, private val value: String) {
    companion object {
        fun tag(key: String, value: String): MapEntry {
            return MapEntry(key, value)
        }

        fun mapOf(vararg items: MapEntry): Map<String, String> {
            val result = hashMapOf<String, String>()
            for (item in items) result[item.key] = item.value
            return result
        }
    }
}
