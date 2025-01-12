package de.westnordost.osmfeatures

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive

private val ISO3166_2 = Regex("[A-Z]{2}(-[A-Z0-9]{1,3})?")

internal fun JsonArray.parseCountryCodes(): List<String>? {
    // for example a lat,lon pair to denote a location with radius. Not supported.
    if (any { it is JsonArray }) return null

    val list = map { it.jsonPrimitive.content }
    val result = ArrayList<String>(list.size)
    for (item in list) {
        val cc = item.uppercase()
        // don't need this, 001 stands for "whole world"
        if (cc == "001") continue
        // ISO-3166-2 codes are supported but not m49 code such as "150" or geojsons like "city_national_bank_fl.geojson"
        if (!cc.matches(ISO3166_2)) return null
        result.add(cc)
    }
    return result
}