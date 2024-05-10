package de.westnordost.osmfeatures

/** For the given map, get the value of the entry at the given key and if there is no
 * entry yet, create it using the given create function thread-safely  */
internal fun <K, V> MutableMap<K, V>.synchronizedGetOrCreate(key: K, createFn: (K) -> V): V {
    val value by lazy {
        val value = get(key)
        if (value == null) {
            val answer = createFn(key)
            put(key, answer)
            answer
        } else {
            value
        }
    }
    return value
}
