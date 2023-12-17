package de.westnordost.osmfeatures

/** Index that makes finding Features whose name/term/... starts with a given string very efficient.
 *
 * Based on the StartsWithStringTree data structure, see that class.  */
class FeatureTermIndex(features: Collection<Feature>, selector: Selector?) {
    private val featureMap: MutableMap<String, MutableList<Feature>> = HashMap()
    private val tree: StartsWithStringTree

    init {
            for (feature in features) {
                val strings: Collection<String> = selector?.getStrings(feature) ?: emptyList()
                for (string in strings) {
                    if (!featureMap.containsKey(string)) featureMap[string] = ArrayList(1)
                    featureMap[string]?.add(feature)
                }
            }
        tree = StartsWithStringTree(featureMap.keys)
    }

    fun getAll(startsWith: String): List<Feature> {
        val result: MutableSet<Feature> = HashSet()
        for (string in tree.getAll(startsWith)) {
            val fs: List<Feature>? = featureMap[string]
            if (fs != null) result.addAll(fs)
        }
        return ArrayList(result)
    }

    fun interface Selector {
        fun getStrings(feature: Feature): List<String>
    }
}
