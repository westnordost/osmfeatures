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

    // language -> lazy { localized features }
    private val localizedFeaturesList = HashMap<String?, Lazy<List<LocalizedFeature>>>()

    // languages -> lazy { featureId -> Feature }
    private val localizedFeatures = HashMap<List<String?>, Lazy<LinkedHashMap<String, Feature>>>()

    init {
        featuresById = loadFeatures().associateByTo(LinkedHashMap()) { it.id }
    }

    override fun getAll(languages: List<String?>): Collection<Feature> {
        return getOrLoadLocalizedFeatures(languages).values
    }

    override fun get(id: String, languages: List<String?>): Feature? {
        return getOrLoadLocalizedFeatures(languages)[id]
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun loadFeatures(): List<BaseFeature> =
        fileAccess.open(FEATURES_FILE).use { IDPresetsJsonParser().parse(it) }

    private fun getOrLoadLocalizedFeatures(languages: List<String?>): LinkedHashMap<String, Feature> =
        localizedFeatures.getOrPut(languages) { lazy { loadLocalizedFeatures(languages) } }.value

    private fun loadLocalizedFeatures(languages: List<String?>): LinkedHashMap<String, Feature> {
        val result = LinkedHashMap<String, Feature>(featuresById.size)
        for (language in languages.asReversed()) {
            if (language != null) {
                for (languageComponent in language.getLanguageComponents()) {
                    val features = getOrLoadLocalizedFeaturesList(languageComponent)
                    for (feature in features) {
                        result[feature.id] = feature.withFallback(result[feature.id])
                    }
                }
            } else {
                result.putAll(featuresById)
            }
        }
        return result
    }

    private fun getOrLoadLocalizedFeaturesList(language: String): List<LocalizedFeature> =
        localizedFeaturesList.getOrPut(language) { lazy { loadLocalizedFeaturesList(language) } }.value

    @OptIn(ExperimentalStdlibApi::class)
    private fun loadLocalizedFeaturesList(language: String?): List<LocalizedFeature> {
        val filename = if (language != null) getLocalizationFilename(language) else "en.json"
        if (!fileAccess.exists(filename)) return emptyList()
        return fileAccess.open(filename).use { source ->
            IDPresetsTranslationJsonParser().parse(source, language, featuresById)
        }
    }

    companion object {
        private const val FEATURES_FILE = "presets.json"

        private fun getLocalizationFilename(language: String): String = "$language.json"
    }
}

private fun String.getLanguageComponents(): Sequence<String> = sequence {
    val components = split('-')
    val language = components.first()
    yield(language)
    if (components.size == 1) return@sequence

    val others = components.subList(1, components.size)
    for (other in others) {
        yield(listOf(language, other).joinToString("-"))
    }
    yield(this@getLanguageComponents)
}

/** If a localized feature has no names or no terms and fallbacks are available, apply those. */
private fun LocalizedFeature.withFallback(fallback: Feature?) =
    if (fallback == null) this
    else copy(
        names = names.ifEmpty { fallback.names },
        terms = terms.ifEmpty { fallback.terms },
    )