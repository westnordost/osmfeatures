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
    private val localizedFeaturesList: MutableMap<Locale?, List<LocalizedFeature>> = HashMap()

    // locales -> featureId -> Feature
    private val localizedFeatures: MutableMap<List<Locale?>, LinkedHashMap<String, Feature>> = HashMap()

    init {
        featuresById = loadFeatures().associateByTo(LinkedHashMap()) { it.id }
    }

    override fun getAll(locales: List<Locale?>): Collection<Feature> {
        return getOrLoadLocalizedFeatures(locales).values
    }

    override fun get(id: String, locales: List<Locale?>): Feature? {
        return getOrLoadLocalizedFeatures(locales)[id]
    }

    private fun loadFeatures(): List<BaseFeature> {
        return fileAccess.open(FEATURES_FILE).use { source ->
            IDPresetsJsonParser().parse(source)
        }
    }

    private fun getOrLoadLocalizedFeatures(locales: List<Locale?>): LinkedHashMap<String, Feature> {
        return localizedFeatures.synchronizedGetOrCreate(locales, ::loadLocalizedFeatures)
    }

    private fun loadLocalizedFeatures(locales: List<Locale?>): LinkedHashMap<String, Feature> {
        val result = LinkedHashMap<String, Feature>(featuresById.size)
        for (locale in locales.asReversed()) {
            if (locale != null) {
                for (localeComponent in locale.getComponents()) {
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

    private fun getOrLoadLocalizedFeaturesList(locale: Locale): List<LocalizedFeature> {
        return localizedFeaturesList.synchronizedGetOrCreate(locale, ::loadLocalizedFeaturesList)
    }

    private fun loadLocalizedFeaturesList(locale: Locale?): List<LocalizedFeature> {
        val filename = if (locale != null) getLocalizationFilename(locale) else "en.json"
        if (!fileAccess.exists(filename)) return emptyList()
        fileAccess.open(filename).use { source ->
            return IDPresetsTranslationJsonParser().parse(source, locale, featuresById)
        }
    }

    companion object {
        private const val FEATURES_FILE = "presets.json"

        private fun getLocalizationFilename(locale: Locale): String =
            locale.languageTag + ".json"

        private fun Locale.getComponents(): List<Locale> {
            val result = ArrayList<Locale>(4)
            result.add(Locale(language))
            if (region != null) result.add(Locale(language, region = region))
            if (script != null) result.add(Locale(language, script = script))
            if (region != null && script != null) result.add(Locale(language, region = region, script = script))
            return result
        }
    }
}
