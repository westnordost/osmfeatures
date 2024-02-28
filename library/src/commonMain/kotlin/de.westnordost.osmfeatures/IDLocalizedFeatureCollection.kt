package de.westnordost.osmfeatures

import okio.use

/** Localized feature collection sourcing from iD presets defined in JSON.
 *
 * The base path is defined via the given FileAccessAdapter. In the base path, it is expected that
 * there is a presets.json which includes all the features. The translations are expected to be
 * located in the same directory named like e.g. de.json, pt-BR.json etc.  */
class IDLocalizedFeatureCollection(
    private val fileAccess: FileAccessAdapter
) : LocalizedFeatureCollection {
    // featureId -> Feature
    private val featuresById: LinkedHashMap<String, BaseFeature>

    // locale -> localized feature
    private val localizedFeaturesList: MutableMap<String?, List<LocalizedFeature>> = HashMap()

    // locales -> featureId -> Feature
    private val localizedFeatures: MutableMap<List<String?>, LinkedHashMap<String, Feature>> = HashMap()

    init {
        featuresById = loadFeatures().associateByTo(LinkedHashMap()) { it.id }
    }

    override fun getAll(locales: List<String?>): Collection<Feature> {
        return getOrLoadLocalizedFeatures(locales).values
    }

    override fun get(id: String, locales: List<String?>): Feature? {
        return getOrLoadLocalizedFeatures(locales)[id]
    }

    private fun loadFeatures(): List<BaseFeature> {
        return fileAccess.open(FEATURES_FILE).use { source ->
            IDPresetsJsonParser().parse(source)
        }
    }

    private fun getOrLoadLocalizedFeatures(locales: List<String?>): LinkedHashMap<String, Feature> {
        return localizedFeatures.synchronizedGetOrCreate(locales, ::loadLocalizedFeatures)
    }

    private fun loadLocalizedFeatures(locales: List<String?>): LinkedHashMap<String, Feature> {
        val result = LinkedHashMap<String, Feature>(featuresById.size)
        for (locale in locales.asReversed()) {
            if (locale != null) {
                for (localeComponent in locale.getLocaleComponents()) {
                    val features = getOrLoadLocalizedFeaturesList(localeComponent)
                    for (feature in features) {
                        result[feature.id] = feature
                    }
                }
            } else {
                result.putAll(featuresById)
            }
}
        return result
    }

    private fun getOrLoadLocalizedFeaturesList(locale: String): List<LocalizedFeature> {
        return localizedFeaturesList.synchronizedGetOrCreate(locale, ::loadLocalizedFeaturesList)
    }

    private fun loadLocalizedFeaturesList(locale: String?): List<LocalizedFeature> {
        val filename = if (locale != null) getLocalizationFilename(locale) else "en.json"
        if (!fileAccess.exists(filename)) return emptyList()
        fileAccess.open(filename).use { source ->
            return IDPresetsTranslationJsonParser().parse(source, locale, featuresById)
        }
    }

    companion object {
        private const val FEATURES_FILE = "presets.json"

        private fun getLocalizationFilename(locale: String): String = "$locale.json"
    }
}

private fun String.getLocaleComponents(): Sequence<String> = sequence {
    val components = split('-')
    val language = components.first()
    yield(language)
    if (components.size == 1) return@sequence

    val others = components.subList(1, components.size)
    for (other in others) {
        yield(listOf(language, other).joinToString("-"))
    }
    yield(this@getLocaleComponents)
}