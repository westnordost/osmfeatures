package de.westnordost.osmfeatures

/** Index that makes finding Features whose tags are completely contained by a given set of tags
 * very efficient.
 *
 * Based on ContainedMapTree data structure, see that class.  */
internal class FeatureTagsIndex(features: Iterable<Feature>) {
    private val featureMap: MutableMap<Map<String, String>, MutableList<Feature>>
    private val tree: ContainedMapTree<String, String>

    init {
        featureMap = HashMap()
        for (feature in features) {
            val map: Map<String, String> = feature.tags
            if (!featureMap.containsKey(map)) featureMap[map] = ArrayList(1)
            featureMap[map]?.add(feature)
        }
        tree = ContainedMapTree(featureMap.keys)
    }

    fun getAll(tags: Map<String, String>?): List<Feature> {
        val result: MutableList<Feature> = ArrayList()
        for (map in tree.getAll(tags)) {
            val fs: List<Feature>? = featureMap[map]
            if (fs != null) result.addAll(fs)
        }
        return result
    }
}
