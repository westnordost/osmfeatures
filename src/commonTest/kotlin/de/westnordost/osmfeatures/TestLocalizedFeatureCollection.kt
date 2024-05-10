package de.westnordost.osmfeatures


class TestLocalizedFeatureCollection(private val features: List<Feature>) : LocalizedFeatureCollection {

    override fun getAll(locales: List<String?>): Collection<Feature> =
        features.filter { locales.contains(it.locale) }

    override fun get(id: String, locales: List<String?>): Feature? {
        val feature = features.find { it.id == id } ?: return null
        if (!locales.contains(feature.locale)) return null
        return feature
    }
}