package de.westnordost.osmfeatures

/**
 * Index that makes finding Features whose name/term/... starts with a given string very efficient.
 *
 * Based on the StartsWithStringTree data structure, see that class.  */
internal class FeatureTermIndex(features: Collection<Feature>, getStrings: (Feature) -> List<String>) {
    // name/term/... -> features
    private val featureMap = HashMap<String, MutableList<Feature>>(features.size)
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
            val fs = featureMap[string]
            if (fs != null) result.addAll(fs)
        }
        return result
    }
}
