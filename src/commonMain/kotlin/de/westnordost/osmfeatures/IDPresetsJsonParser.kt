package de.westnordost.osmfeatures

import kotlinx.io.Source
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.json.io.decodeFromSource

/** Parses this
 * [file](https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json)
 * into list of Features.  */
internal class IDPresetsJsonParser() {

    @OptIn(ExperimentalSerializationApi::class)
    fun parse(source: Source): List<BaseFeature> =
        parse(Json.decodeFromSource<JsonObject>(source))

    fun parse(content: String): List<BaseFeature> =
        parse(Json.decodeFromString<JsonObject>(content))

    private fun parse(json: JsonObject): List<BaseFeature> {
        // the presets in the nsi presets are one level down (in preset object)
        val root = json["presets"]?.jsonObject ?: json
        return root.mapNotNull { (key, value) -> parseFeature(key, value.jsonObject) }
    }

    private fun parseFeature(id: String, p: JsonObject): BaseFeature? {
        val tags = p["tags"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content }.orEmpty()
        // drop features with * in key or value of tags (for now), because they never describe
        // a concrete thing, but some category of things.
        if (tags.anyKeyOrValueContainsWildcard()) return null
        // also dropping features with empty tags (generic point, line, relation)
        if (tags.isEmpty()) return null

        val geometry = p["geometry"]?.jsonArray?.map {
            GeometryType.valueOf(it.jsonPrimitive.content.uppercase())
        }.orEmpty()

        val name = p["name"]?.jsonPrimitive?.contentOrNull ?: ""
        val icon = p["icon"]?.jsonPrimitive?.contentOrNull
        val imageURL = p["imageURL"]?.jsonPrimitive?.contentOrNull
        val names = buildList {
            add(name)
            addAll(p["aliases"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty())
        }
        val terms = p["terms"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty()

        val locationSet = p["locationSet"]?.jsonObject
        val include = locationSet?.get("include")?.jsonArray
        val exclude = locationSet?.get("exclude")?.jsonArray
        val includeCountryCodes =
            if (include != null) include.parseCountryCodes() ?: return null
            else emptyList()
        val excludeCountryCodes =
            if (exclude != null) exclude.parseCountryCodes() ?: return null
            else emptyList()

        val searchable = p["searchable"]?.jsonPrimitive?.booleanOrNull ?: true
        val matchScore = p["matchScore"]?.jsonPrimitive?.floatOrNull ?: 1.0f
        val addTags = p["addTags"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: tags
        val removeTags = p["removeTags"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: addTags

        return BaseFeature(
            id = id,
            tags = tags,
            geometry = geometry,
            icon = icon,
            imageURL = imageURL,
            names = names,
            terms = terms,
            includeCountryCodes = includeCountryCodes,
            excludeCountryCodes = excludeCountryCodes,
            isSearchable = searchable,
            matchScore = matchScore,
            addTags = addTags,
            removeTags = removeTags,
        )
    }
}

private fun Map<String, String>.anyKeyOrValueContainsWildcard(): Boolean =
    any { (key, value) ->  key.contains("*") || value.contains("*") }