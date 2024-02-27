package de.westnordost.osmfeatures

class TestPerCountryFeatureCollection(private val features: List<Feature>) : PerCountryFeatureCollection {

    override fun getAll(countryCodes: List<String?>): Collection<Feature> {
        return features.filter { it.isAvailableIn(countryCodes) }
    }

    override fun get(id: String, countryCodes: List<String?>): Feature? {
        return features.find { it.isAvailableIn(countryCodes) }
    }

    private fun Feature.isAvailableIn(countryCodes: List<String?>): Boolean =
        countryCodes.none { excludeCountryCodes.contains(it) } &&
        countryCodes.any { includeCountryCodes.contains(it) || it == null && includeCountryCodes.isEmpty() }
}