package de.westnordost.osmfeatures


class TestLocalizedFeatureCollection(private val features: List<Feature>) : LocalizedFeatureCollection {

    override fun getAll(languages: List<String?>): Collection<Feature> =
        features.filter { languages.contains(it.language) }

    override fun get(id: String, languages: List<String?>): Feature? {
        val feature = features.find { it.id == id } ?: return null
        if (!languages.contains(feature.language)) return null
        return feature
    }
}