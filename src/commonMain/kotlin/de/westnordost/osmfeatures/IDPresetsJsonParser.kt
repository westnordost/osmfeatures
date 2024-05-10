package de.westnordost.osmfeatures

import kotlinx.io.Source
import kotlinx.serialization.json.*

/** Parses this file
 * [...](https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json)
 * into map of id -> Feature.  */
internal class IDPresetsJsonParser(private val isSuggestion: Boolean = false) {

    fun parse(source: Source): List<BaseFeature> =
        parse(Json.decodeFromSource<JsonObject>(source))

    fun parse(content: String): List<BaseFeature> =
        parse(Json.decodeFromString<JsonObject>(content))

    private fun parse(json: JsonObject): List<BaseFeature> =
        json.mapNotNull { (key, value) ->  parseFeature(key, value.jsonObject) }

    private fun parseFeature(id: String, p: JsonObject): BaseFeature? {
        val tags = p["tags"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content }.orEmpty()
        // drop features with * in key or value of tags (for now), because they never describe
        // a concrete thing, but some category of things.
        // TODO maybe drop this limitation
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
        val includeCountryCodes: List<String>?
        val excludeCountryCodes: List<String>?
        if (locationSet != null) {
            includeCountryCodes = locationSet["include"]?.jsonArray?.parseCountryCodes() ?: return null
            excludeCountryCodes = locationSet["exclude"]?.jsonArray?.parseCountryCodes() ?: return null
        } else {
            includeCountryCodes = emptyList()
            excludeCountryCodes = emptyList()
        }

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
            isSuggestion = isSuggestion,
            addTags = addTags,
            removeTags = removeTags
        )
    }
}

private fun JsonArray.parseCountryCodes(): List<String>? {
    // for example a lat,lon pair to denote a location with radius. Not supported.
    if (any { it is JsonArray }) return null

    val list = map { it.jsonPrimitive.content }
    val result = ArrayList<String>(list.size)
    for (item in list) {
        val cc = item.uppercase()
        // don't need this, 001 stands for "whole world"
        if (cc == "001") continue
        // ISO-3166-2 codes are supported but not m49 code such as "150" or geojsons like "city_national_bank_fl.geojson"
        if (!cc.matches("[A-Z]{2}(-[A-Z0-9]{1,3})?".toRegex())) return null
        result.add(cc)
    }
    return result
}

private fun Map<String, String>.anyKeyOrValueContainsWildcard(): Boolean =
    any { (key, value) ->  key.contains("*") || value.contains("*") }