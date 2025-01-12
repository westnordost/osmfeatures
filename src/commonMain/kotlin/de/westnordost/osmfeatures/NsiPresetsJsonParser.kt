package de.westnordost.osmfeatures

import kotlinx.io.Source
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Parses this
 * [file](https://raw.githubusercontent.com/osmlab/name-suggestion-index/refs/heads/main/dist/nsi.json)
 * into list of Features.  */
class NsiPresetsJsonParser {
    @OptIn(ExperimentalSerializationApi::class)
    fun parse(source: Source): List<NsiFeature> =
        parse(Json.decodeFromSource<JsonObject>(source))

    fun parse(content: String): List<NsiFeature> =
        parse(Json.decodeFromString<JsonObject>(content))

    private fun parse(json: JsonObject): List<NsiFeature> {
        val nsi = json["nsi"]?.jsonObject
        return nsi?.flatMap { nsiEntry ->
            val category = nsiEntry.value.jsonObject
            val properties = category["properties"]!!.jsonObject
            val path = properties["path"]!!.jsonPrimitive.content
            val preserveTags = properties["preserveTags"]?.jsonArray?.map {
                Regex(it.jsonPrimitive.content)
            }.orEmpty()
            val parent = TODO()

            val items =  category["items"]?.jsonArray
            items?.mapNotNull { arrayItem ->
                val item = arrayItem.jsonObject
                parseFeature(parent, preserveTags, item)
            }.orEmpty()
        }.orEmpty()
    }

    private fun parseFeature(parent: Feature, preserveTags: List<Regex>, p: JsonObject): NsiFeature? {
        val id = p["id"]?.jsonPrimitive?.content ?: return null
        val name = p["displayName"]?.jsonPrimitive?.content ?: return null
        val aliases = p["matchNames"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty()
        val names = buildList {
            add(name)
            addAll(aliases)
        }

        val locationSet = p["locationSet"]?.jsonObject
        val include = locationSet?.get("include")?.jsonArray
        val exclude = locationSet?.get("exclude")?.jsonArray
        val includeCountryCodes =
            if (include != null) include.parseCountryCodes() ?: return null
            else emptyList()
        val excludeCountryCodes =
            if (exclude != null) exclude.parseCountryCodes() ?: return null
            else emptyList()

        val preserveTags2 = p["preserveTags"]?.jsonArray?.map {
            Regex(it.jsonPrimitive.content)
        }.orEmpty()

        val tags = p["tags"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content }.orEmpty()

        return NsiFeature(
            parent = TODO(),
            id = "${parent.id}/$id",
            names = names,
            includeCountryCodes = includeCountryCodes,
            excludeCountryCodes = excludeCountryCodes,
            tags = tags,
            preserveTags = preserveTags + preserveTags2,
        )
    }
}