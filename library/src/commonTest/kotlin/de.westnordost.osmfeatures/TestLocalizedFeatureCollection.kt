package de.westnordost.osmfeatures


class TestLocalizedFeatureCollection(features: List<Feature>) : LocalizedFeatureCollection {
    private val features: List<Feature>

    init {
        this.features = features
    }

    override fun getAll(locales: List<Locale?>): Collection<Feature> {
        return features.filter { locales.contains(it.locale) }
    }

    override operator fun get(id: String, locales: List<Locale?>): Feature? {
        val feature = features.find { it.id == id }
        return if (feature == null || (!locales.contains(feature.locale))) null else feature
    }
}