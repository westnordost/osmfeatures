package de.westnordost.osmfeatures

/**
 * Index that makes finding Features whose name/term/... starts with a given string very efficient.
 *
 * Based on the StartsWithStringTree data structure, see that class.  */
class FeatureTermIndex(features: Collection<Feature>, getStrings: (Feature) -> List<String>) {
    private val featureMap: MutableMap<String, MutableList<Feature>> = HashMap(features.size)
    private val tree: StartsWithStringTree

    init {
        for (feature in features) {
            for (string in getStrings(feature)) {
                val map = featureMap.getOrPut(string) { ArrayList(1) }
                map.add(feature)
            }
        }
        tree = StartsWithStringTree(featureMap.keys)
    }

    fun getAll(startsWith: String): Set<Feature> {
        val result = HashSet<Feature>()
        for (string in tree.getAll(startsWith)) {
            val fs: List<Feature>? = featureMap[string]
            if (fs != null) result.addAll(fs)
        }
        return result
    }
}
