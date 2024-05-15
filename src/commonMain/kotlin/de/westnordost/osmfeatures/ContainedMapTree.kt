package de.westnordost.osmfeatures

/** Index that makes finding which maps are completely contained by a given map very efficient.
 * It sorts the maps into a tree structure with configurable depth.
 *
 * It is threadsafe because it is immutable.
 *
 * For example for the string maps...
 * ```
 * [
 *   #1 (amenity -> bicycle_parking),
 *   #2 (amenity -> bicycle_parking, bicycle_parking -> shed),
 *   #3 (amenity -> bicycle_parking, bicycle_parking -> lockers),
 *   #4 (amenity -> taxi),
 *   #5 (shop -> supermarket),
 * ]
 * ```
 * ...the tree internally looks like this:
 * ```
 * amenity ->
 *   bicycle_parking ->
 *     #1
 *     bicycle_parking ->
 *       shed ->
 *         #2
 *       lockers ->
 *         #3
 *   taxi ->
 *     #4
 * shop ->
 *   supermarket ->
 *     #5
 * ...
* ```
 */
internal class ContainedMapTree<K, V>
/** Create this index with the given maps.
 *
 *  The generated tree will have a max depth of maxDepth and another depth is not added to the
 *  tree if there are less than minContainerSize maps in one tree node.
 */
 constructor(
    maps: Collection<Map<K, V>>,
    maxDepth: Int = 4,
    minContainerSize: Int = 4
) {
    private val root: Node<K, V> =
        buildTree(maps, emptyList(), maxDepth.coerceAtLeast(0), minContainerSize)

    /** Get all maps whose entries are completely contained by the given map  */
    fun getAll(map: Map<K, V>): List<Map<K, V>> =
        root.getAll(map)

    private class Node<K, V>(
        /** key -> (value -> Node)  */
        val children: Map<K, Map<V, Node<K, V>>>,
        val maps: Collection<Map<K, V>>
    ) {
        /** Get all maps whose entries are all contained by the given map  */
        fun getAll(map: Map<K, V>): List<Map<K, V>> {
            val result = ArrayList<Map<K, V>>()
            for ((key, nodesByValue) in children) {
                if (map.containsKey(key)) {
                    for ((value, node) in nodesByValue) {
                        if (value == map[key]) {
                            result.addAll(node.getAll(map))
                        }
                    }
                }
            }
            for (m in maps) {
                if (m.all { entry -> map[entry.key] == entry.value } ) {
                    result.add(m)
                }
            }
            return result
        }
    }

    companion object {
        private fun <K, V> buildTree(
            maps: Collection<Map<K, V>>,
            previousKeys: Collection<K>,
            maxDepth: Int,
            minContainerSize: Int
        ): Node<K, V> {
            if (previousKeys.size == maxDepth || maps.size < minContainerSize) {
                return Node(emptyMap(), maps)
            }

            val unsortedMaps = HashSet(maps)

            val mapsByKey = maps.groupByEachKey(previousKeys)

            /* the map should be categorized by frequent keys first and least frequent keys last. */
            val sortedByCountDesc = ArrayList(mapsByKey.entries).sortedByDescending { it.value.size }

            val result = HashMap<K, Map<V, Node<K, V>>>(mapsByKey.size)

            for ((key, mapsForKey) in sortedByCountDesc) {
                // a map already sorted in a certain node should not be sorted into another too
                (mapsForKey as MutableList).retainAll(unsortedMaps)
                if (mapsForKey.isEmpty()) continue

                val mapsByKeyValue = mapsForKey.groupByKeyValue(key)

                val nodesByValue = HashMap<V, Node<K, V>>(mapsByKeyValue.size)
                for ((value, nodes) in mapsByKeyValue) {
                    val previousKeysNow = ArrayList(previousKeys)
                    previousKeysNow.add(key)
                    nodesByValue[value] = buildTree(nodes, previousKeysNow, maxDepth, minContainerSize)
                }

                result[key] = nodesByValue

                for (map in mapsForKey) {
                    unsortedMaps.remove(map)
                }
            }

            return Node(result, ArrayList(unsortedMaps))
        }

        /** returns these maps grouped by the map entry value of the given key.  */
        private fun <K, V> Collection<Map<K, V>>.groupByKeyValue(
            key: K
        ): Map<V, List<Map<K, V>>> {
            val result = HashMap<V, MutableList<Map<K, V>>>()
            for (map in this) {
                val value = map[key] ?: continue
                val group = result.getOrPut(value) { ArrayList() }
                group.add(map)
            }
            return result
        }

        /** returns these maps grouped by each of their keys (except the given ones).  */
        private fun <K, V> Collection<Map<K, V>>.groupByEachKey(
            excludeKeys: Collection<K>
        ): Map<K, List<Map<K, V>>> {
            val result = HashMap<K, MutableList<Map<K, V>>>()
            for (map in this) {
                for (key in map.keys) {
                    if (excludeKeys.contains(key)) continue
                    val group = result.getOrPut(key) { ArrayList() }
                    group.add(map)
                }
            }
            return result
        }
    }
}