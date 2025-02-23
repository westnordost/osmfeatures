package de.westnordost.osmfeatures

/**
 * Index that makes finding Features whose tags are completely contained by a given set of tags
 * very efficient.
 *
 * Based on ContainedMapTree data structure, see that class.  */
internal class FeatureTagsIndex(features: Collection<Feature>) {
    // tags -> list of features
    private val featureMap = HashMap<Map<String, String>, MutableList<Feature>>(features.size)
    private val tree: ContainedMapTree<String, String>

    init {
        for (feature in features) {
            val map = featureMap.getOrPut(feature.tags) { ArrayList(1) }
            map.add(feature)
        }
        tree = ContainedMapTree(featureMap.keys)
    }

    fun getAll(tags: Map<String, String>): List<Feature> {
        val result = ArrayList<Feature>()
        for (map in tree.getAll(tags)) {
            val features = featureMap[map].orEmpty()
            for (feature in features) {
                if (feature.tagKeys.all { tags.containsKey(it) }) {
                    result.add(feature)
                }
            }
        }
        return result
    }
}
