package de.westnordost.osmfeatures

/**
 * Index that makes finding Features whose name/term/... starts with a given string very efficient.
 *
 * Based on the StartsWithStringTree data structure, see that class.  */
class FeatureTermIndex(features: Collection<Feature>, selector: Selector?) {
    private val featureMap: MutableMap<String, MutableList<Feature>>
    private val tree: StartsWithStringTree

    init {
        featureMap = HashMap(features.size)
        for (feature in features) {
            val strings = selector?.getStrings(feature) ?: emptyList()
            for (string in strings) {
                val map = featureMap.getOrPut(string) { ArrayList(1) }
                map.add(feature)
            }
        }
        tree = StartsWithStringTree(featureMap.keys)
    }

    fun getAll(startsWith: String): List<Feature> {
        val result = HashSet<Feature>()
        for (string in tree.getAll(startsWith)) {
            val fs: List<Feature>? = featureMap[string]
            if (fs != null) result.addAll(fs)
        }
        return result.toList()
    }

    fun interface Selector {
        fun getStrings(feature: Feature): List<String>
    }
}
