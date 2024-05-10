package de.westnordost.osmfeatures

/** Localized feature collection sourcing from iD presets defined in JSON.
 *
 * The base path is defined via the given FileAccessAdapter. In the base path, it is expected that
 * there is a presets.json which includes all the features. The translations are expected to be
 * located in the same directory named like e.g. de.json, pt-BR.json etc.  */
internal class IDLocalizedFeatureCollection(
    private val fileAccess: ResourceAccessAdapter
) : LocalizedFeatureCollection {
    // featureId -> Feature
    private val featuresById: LinkedHashMap<String, BaseFeature>

    // locale -> lazy { localized features }
    private val localizedFeaturesList = HashMap<String?, Lazy<List<LocalizedFeature>>>()

    // locales -> lazy { featureId -> Feature }
    private val localizedFeatures = HashMap<List<String?>, Lazy<LinkedHashMap<String, Feature>>>()

    init {
        featuresById = loadFeatures().associateByTo(LinkedHashMap()) { it.id }
    }

    override fun getAll(locales: List<String?>): Collection<Feature> {
        return getOrLoadLocalizedFeatures(locales).values
    }

    override fun get(id: String, locales: List<String?>): Feature? {
        return getOrLoadLocalizedFeatures(locales)[id]
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun loadFeatures(): List<BaseFeature> =
        fileAccess.open(FEATURES_FILE).use { IDPresetsJsonParser().parse(it) }

    private fun getOrLoadLocalizedFeatures(locales: List<String?>): LinkedHashMap<String, Feature> =
        localizedFeatures.getOrPut(locales) { lazy { loadLocalizedFeatures(locales) } }.value

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

    private fun getOrLoadLocalizedFeaturesList(locale: String): List<LocalizedFeature> =
        localizedFeaturesList.getOrPut(locale) { lazy { loadLocalizedFeaturesList(locale) } }.value

    @OptIn(ExperimentalStdlibApi::class)
    private fun loadLocalizedFeaturesList(locale: String?): List<LocalizedFeature> {
        val filename = if (locale != null) getLocalizationFilename(locale) else "en.json"
        if (!fileAccess.exists(filename)) return emptyList()
        return fileAccess.open(filename).use { source ->
            IDPresetsTranslationJsonParser().parse(source, locale, featuresById)
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