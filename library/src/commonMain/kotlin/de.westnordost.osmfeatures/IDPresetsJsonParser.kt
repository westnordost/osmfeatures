package de.westnordost.osmfeatures

import de.westnordost.osmfeatures.JsonUtils.parseList
import de.westnordost.osmfeatures.JsonUtils.parseStringMap
import kotlinx.serialization.json.*
import okio.Buffer
import okio.Source
import okio.buffer

/** Parses this file
 * [...](https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json)
 * into map of id -> Feature.  */
class IDPresetsJsonParser {
    private var isSuggestion = false

    constructor()

    constructor(isSuggestion: Boolean) {
        this.isSuggestion = isSuggestion
    }
    fun parse(source: Source): List<BaseFeature> {
        val sink = Buffer()
        source.buffer().readAll(sink)

        val decodedObject = Json.decodeFromString<JsonObject>(sink.readUtf8())
        return decodedObject.mapNotNull { (key, value) ->  parseFeature(key, value.jsonObject)}
    }
    fun parse(content: String): List<BaseFeature> {
        val decodedObject = Json.decodeFromString<JsonObject>(content)
        return decodedObject.mapNotNull { (key, value) ->  parseFeature(key, value.jsonObject)}
    }

    private fun parseFeature(id: String, p: JsonObject): BaseFeature? {
        val tags = parseStringMap(p["tags"]?.jsonObject)
        // drop features with * in key or value of tags (for now), because they never describe
        // a concrete thing, but some category of things.
        // TODO maybe drop this limitation
        if (anyKeyOrValueContainsWildcard(tags)) return null
        // also dropping features with empty tags (generic point, line, relation)
        if (tags.isEmpty()) return null

        val geometry = parseList(
            p["geometry"]?.jsonArray
        ) { GeometryType.valueOf(((it as JsonPrimitive).content).uppercase())
        }

        val name = p["name"]?.jsonPrimitive?.content ?: ""
        val icon = p["icon"]?.jsonPrimitive?.content
        val imageURL = p["imageURL"]?.jsonPrimitive?.content
        val names = parseList(p["aliases"]?.jsonArray) { it.jsonPrimitive.content }.toMutableList()
        names.add(0, name)
        val terms = parseList(p["terms"]?.jsonArray) { it.jsonPrimitive.content }

        val locationSet = p["locationSet"]?.jsonObject
        val includeCountryCodes: List<String>?
        val excludeCountryCodes: List<String>?
        if (locationSet != null) {
            includeCountryCodes = parseCountryCodes(locationSet["include"]?.jsonArray)
            if (includeCountryCodes == null) return null
            excludeCountryCodes = parseCountryCodes(locationSet["exclude"]?.jsonArray)
            if (excludeCountryCodes == null) return null
        } else {
            includeCountryCodes = ArrayList(0)
            excludeCountryCodes = ArrayList(0)
        }

        val searchable = p["searchable"]?.jsonPrimitive?.booleanOrNull?: true
        val matchScore = p["matchScore"]?.jsonPrimitive?.floatOrNull?: 1.0f
        val addTags = p["addTags"]?.let { parseStringMap(it.jsonObject)}?: tags
        val removeTags = p["removeTags"]?.let { parseStringMap(it.jsonObject)}?: addTags

        return BaseFeature(
                id,
                tags,
                geometry,
                icon, imageURL,
                names,
                terms,
                includeCountryCodes,
                excludeCountryCodes,
                searchable, matchScore, isSuggestion,
                addTags,
                removeTags
        )
    }

    companion object {
        private fun parseCountryCodes(jsonList: JsonArray?): List<String>? {
            if(jsonList?.any { it is JsonArray } == true) {
                return null
            }
            val list = parseList(jsonList) { it.jsonPrimitive.content }
            val result: MutableList<String> = ArrayList(list.size)
            for (item in list) {
                // for example a lat,lon pair to denote a location with radius. Not supported.
                val cc = item.uppercase()
                // don't need this, 001 stands for "whole world"
                if (cc == "001") continue
                // ISO-3166-2 codes are supported but not m49 code such as "150" or geojsons like "city_national_bank_fl.geojson"
                if (!cc.matches("[A-Z]{2}(-[A-Z0-9]{1,3})?".toRegex())) return null
                result.add(cc)
            }
            return result
        }

        private fun anyKeyOrValueContainsWildcard(map: Map<String, String>): Boolean {
            return map.any { (key, value) ->  key.contains("*") || value.contains("*")}
        }
    }
}


