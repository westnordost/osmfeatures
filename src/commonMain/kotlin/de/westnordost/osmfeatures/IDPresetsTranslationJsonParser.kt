package de.westnordost.osmfeatures

import kotlinx.io.Source
import kotlinx.serialization.json.*

/** Parses a file from
 * https://github.com/openstreetmap/id-tagging-schema/tree/main/dist/translations
 * , given the base features are already parsed.
 */
internal class IDPresetsTranslationJsonParser {
    // TODO locale nullable does not make sense?!
    fun parse(
        content: String, locale: String?, baseFeatures: Map<String, BaseFeature>
    ): List<LocalizedFeature> =
        parse(Json.decodeFromString<JsonObject>(content), locale, baseFeatures)

    fun parse(
        source: Source, locale: String?, baseFeatures: Map<String, BaseFeature>
    ): List<LocalizedFeature> =
        parse(Json.decodeFromSource<JsonObject>(source), locale, baseFeatures)

    private fun parse(
        json: JsonObject, locale: String?, baseFeatures: Map<String, BaseFeature>
    ): List<LocalizedFeature> {
        val translations = json.jsonObject.entries.firstOrNull()?.value?.jsonObject
        val presetsObject = translations
            ?.get("presets")?.jsonObject
            ?.get("presets")?.jsonObject
            ?: return emptyList()

        val localizedFeatures = HashMap<String, LocalizedFeature>(presetsObject.size)
        presetsObject.entries.forEach { (key, value) ->
            val f = parseFeature(baseFeatures[key], locale, value.jsonObject)
            if (f != null) localizedFeatures[key] = f
        }

        for (baseFeature in baseFeatures.values) {
            val names = baseFeature.names
            if (names.isEmpty()) continue
            val name = names.first()
            val isPlaceholder = name.startsWith("{") && name.endsWith("}")
            if (!isPlaceholder) continue
            val placeholderId = name.substring(1, name.length - 1)
            val localizedFeature = localizedFeatures[placeholderId] ?: continue
            localizedFeatures[baseFeature.id] = LocalizedFeature(
                p = baseFeature,
                locale = locale,
                names = localizedFeature.names,
                terms = localizedFeature.terms
            )
        }

        return localizedFeatures.values.toList()
    }

    private fun parseFeature(feature: BaseFeature?, locale: String?, localization: JsonObject): LocalizedFeature? {
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
            locale = locale,
            names = names,
            terms = terms
        )
    }
}
