package de.westnordost.osmfeatures

class TestPerCountryFeatureCollection(features: List<Feature>) : PerCountryFeatureCollection {
    private val features: List<Feature>

    init {
        this.features = features
    }

    override fun getAll(countryCodes: List<String?>): Collection<Feature> {
        return features.filter { feature ->
            !countryCodes.any { feature.excludeCountryCodes.contains(it) }
                    && countryCodes
                .any { countryCode ->
                    feature.includeCountryCodes.contains(countryCode) || countryCode == null && feature.includeCountryCodes.isEmpty()
                }
        }
    }

    override operator fun get(id: String, countryCodes: List<String?>): Feature? {
        return features.find { feature ->
            feature.id == id
                    && !countryCodes.any { feature.excludeCountryCodes.contains(it) }
                    && countryCodes.any { countryCode ->
                        feature.includeCountryCodes.contains(countryCode) || countryCode == null && feature.includeCountryCodes.isEmpty()
                    }
        }

    }
}
