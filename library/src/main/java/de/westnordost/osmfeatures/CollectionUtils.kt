package de.westnordost.osmfeatures

object CollectionUtils {
    /** For the given map, get the value of the entry at the given key and if there is no
     * entry yet, create it using the given create function thread-safely  */
    fun <K, V> synchronizedGetOrCreate(map: MutableMap<K?, V>, key: K, createFn: (K) -> V): V? {
        synchronized(map) {
            if (!map.containsKey(key)) {
                map[key] = createFn(key)
            }
        }
        return map[key]
    }

    @JvmStatic
    /** Whether the given map contains all the given entries  */
    fun <K, V> mapContainsAllEntries(map: Map<K, V>, entries: Iterable<Map.Entry<K, V>>): Boolean {
        for (entry in entries) {
            if (!mapContainsEntry(map, entry)) return false
        }
        return true
    }

    @JvmStatic
    /** Number of entries contained in the given map  */
    fun <K, V> numberOfContainedEntriesInMap(map: Map<K, V>, entries: Iterable<Map.Entry<K, V>>): Int {
        var found = 0
        for (entry in entries) {
            if (mapContainsEntry(map, entry)) found++
        }
        return found
    }

    @JvmStatic
    /** Whether the given map contains the given entry  */
    fun <K, V> mapContainsEntry(map: Map<K, V>, entry: Map.Entry<K, V>): Boolean {
        val mapValue = map[entry.key]
        val value = entry.value
        return value == mapValue
    }

    /** Backport of Collection.removeIf  */
    fun <T> removeIf(list: MutableList<T>, predicate: (T) -> Boolean) {
        list.removeIf(predicate)
    }
}