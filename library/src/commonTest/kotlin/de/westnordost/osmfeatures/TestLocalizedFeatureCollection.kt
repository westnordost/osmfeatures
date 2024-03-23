package de.westnordost.osmfeatures


class TestLocalizedFeatureCollection(private val features: List<Feature>) : LocalizedFeatureCollection {

    override fun getAll(locales: List<String?>): Collection<Feature> {
        return features.filter { locales.contains(it.locale) }
    }

    override fun get(id: String, locales: List<String?>): Feature? {
        val feature = features.find { it.id == id }
        return if (feature == null || (!locales.contains(feature.locale))) null else feature
    }
}