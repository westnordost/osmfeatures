package de.westnordost.osmfeatures

object CollectionUtils {
    /** For the given map, get the value of the entry at the given key and if there is no
     * entry yet, create it using the given create function thread-safely  */
    fun <K, V> synchronizedGetOrCreate(map: MutableMap<K, V>, key: K, createFn: (K) -> V): V? {
        synchronized(map) {
            if (!map.containsKey(key)) {
                map[key] = createFn(key)
            }
        }
        return map[key]
    }
}