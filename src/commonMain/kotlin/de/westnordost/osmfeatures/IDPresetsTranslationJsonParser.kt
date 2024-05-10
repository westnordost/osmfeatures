package de.westnordost.osmfeatures

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.Source

/** Parses a file from
 * https://github.com/openstreetmap/id-tagging-schema/tree/main/dist/translations
 * , given the base features are already parsed.
 */
class IDPresetsTranslationJsonParser {

    fun parse(
        source: Source, locale: String?, baseFeatures: Map<String, BaseFeature>
    ): List<LocalizedFeature> {
        val content = JsonUtils.getContent(source)
        return parse(content, locale, baseFeatures)
    }
    fun parse(
        content: String, locale: String?, baseFeatures: Map<String, BaseFeature>
    ): List<LocalizedFeature> {
        val decodedObject = Json.decodeFromString<JsonObject>(content)
        val languageKey = decodedObject.entries.iterator().next().key
        val languageObject = decodedObject[languageKey]
            ?: return emptyList()
        val presetsContainerObject = languageObject.jsonObject["presets"]
            ?: return emptyList()
        val presetsObject = presetsContainerObject.jsonObject["presets"]?.jsonObject
            ?: return emptyList()
        val localizedFeatures = HashMap<String, LocalizedFeature>(presetsObject.size)
        presetsObject.entries.forEach { (key, value) ->
            val f = parseFeature(baseFeatures[key], locale, value.jsonObject)
            if (f != null) localizedFeatures[key] = f
        }
        for (baseFeature in baseFeatures.values) {
            val names = baseFeature.names
            if (names.isEmpty()) continue
            val name = names[0]
            val isPlaceholder = name.startsWith("{") && name.endsWith("}")
            if (!isPlaceholder) continue
            val placeholderId = name.substring(1, name.length - 1)
            val localizedFeature = localizedFeatures[placeholderId] ?: continue
            localizedFeatures[baseFeature.id] = LocalizedFeature(
                baseFeature,
                locale,
                localizedFeature.names,
                localizedFeature.terms
            )
        }

        return ArrayList(localizedFeatures.values)
    }

    private fun parseFeature(feature: BaseFeature?, locale: String?, localization: JsonObject): LocalizedFeature? {
        if (feature == null) return null

        val name = localization["name"]?.jsonPrimitive?.content
        if (name.isNullOrEmpty()) return null

        val namesArray = parseNewlineSeparatedList(localization["aliases"]?.jsonPrimitive?.content)
        val names: MutableList<String> = ArrayList(namesArray.size + 1)
        names.addAll(namesArray)
        names.remove(name)
        names.add(0, name)

        val termsArray = parseCommaSeparatedList(localization["terms"]?.jsonPrimitive?.content)
        val terms: MutableList<String> = ArrayList(termsArray.size)
        terms.addAll(termsArray)
        terms.removeAll(names)

        return LocalizedFeature(
            p = feature,
            locale = locale,
            names = names,
            terms = terms
        )
    }
}

private fun parseCommaSeparatedList(str: String?): Array<String> {
    if (str.isNullOrEmpty()) return emptyArray()
    return str.split("\\s*,+\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
}

private fun parseNewlineSeparatedList(str: String?): Array<String> {
    if (str.isNullOrEmpty()) return emptyArray()
    return str.split("\\s*[\\r\\n]+\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
}