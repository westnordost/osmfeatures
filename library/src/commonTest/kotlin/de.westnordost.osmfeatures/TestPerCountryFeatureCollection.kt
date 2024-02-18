package de.westnordost.osmfeatures

import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.PerCountryFeatureCollection

class TestPerCountryFeatureCollection(features: List<Feature>) : PerCountryFeatureCollection {
    private val features: List<Feature>

    init {
        this.features = features
    }

    @Override
    override fun getAll(countryCodes: List<String?>): Collection<Feature> {
        return features.filter {
                countryCodes
                    .find { countryCode ->
                        it.includeCountryCodes.contains(countryCode) || countryCode == null && it.includeCountryCodes.isEmpty()
                    } != null
        }
    }

    @Override
    override operator fun get(id: String, countryCodes: List<String?>): Feature? {
        return features.find { feature ->
            feature.id == id
                    && countryCodes.find {
                        feature.includeCountryCodes.contains(it) || it == null && feature.includeCountryCodes.isEmpty()
                    } != null
        }

    }
}
