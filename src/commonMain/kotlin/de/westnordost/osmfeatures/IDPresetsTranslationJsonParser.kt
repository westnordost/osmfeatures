package de.westnordost.osmfeatures

import kotlinx.io.Source
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.json.io.decodeFromSource

/** Parses a file from
 * https://github.com/openstreetmap/id-tagging-schema/tree/main/dist/translations
 * , given the base features are already parsed.
 */
internal class IDPresetsTranslationJsonParser {
    fun parse(
        content: String, language: String?, baseFeatures: Map<String, BaseFeature>
    ): List<LocalizedFeature> =
        parse(Json.decodeFromString<JsonObject>(content), language, baseFeatures)

    @OptIn(ExperimentalSerializationApi::class)
    fun parse(
        source: Source, language: String?, baseFeatures: Map<String, BaseFeature>
    ): List<LocalizedFeature> =
        parse(Json.decodeFromSource<JsonObject>(source), language, baseFeatures)

    private fun parse(
        json: JsonObject, language: String?, baseFeatures: Map<String, BaseFeature>
    ): List<LocalizedFeature> {
        val translations = json.jsonObject.entries.firstOrNull()?.value?.jsonObject
            ?.get("presets")?.jsonObject
            ?.get("presets")?.jsonObject
            ?: return emptyList()

        val localizedFeatures = HashMap<String, LocalizedFeature>(translations.size)
        translations.entries.forEach { (key, value) ->
            val f = parseFeature(baseFeatures[key], language, value.jsonObject)
            if (f != null) localizedFeatures[key] = f
        }

        for (baseFeature in baseFeatures.values) {
            val name = baseFeature.names.firstOrNull() ?: continue
            val isPlaceholder = name.startsWith("{") && name.endsWith("}")
            if (!isPlaceholder) continue
            val placeholderId = name.substring(1, name.length - 1)
            val localizedFeature = localizedFeatures[placeholderId] ?: continue
            localizedFeatures[baseFeature.id] = LocalizedFeature(
                p = baseFeature,
                language = language,
                names = localizedFeature.names,
                terms = localizedFeature.terms
            )
        }

        return localizedFeatures.values.toList()
    }

    private fun parseFeature(feature: BaseFeature?, language: String?, localization: JsonObject): LocalizedFeature? {
        if (feature == null) return null

        val name = localization["name"]?.jsonPrimitive?.contentOrNull.orEmpty()

        val aliases = localization["aliases"]?.jsonPrimitive?.content
            .orEmpty()
            .lineSequence()

        val names = (sequenceOf(name) + aliases)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val terms = localization["terms"]?.jsonPrimitive?.content
            .orEmpty()
            .splitToSequence(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        return LocalizedFeature(
            p = feature,
            language = language,
            names = names,
            terms = terms
        )
    }
}
