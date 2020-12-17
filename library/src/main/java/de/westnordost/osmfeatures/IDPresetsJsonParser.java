package de.westnordost.osmfeatures;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.westnordost.osmfeatures.JsonUtils.createFromInputStream;
import static de.westnordost.osmfeatures.JsonUtils.parseList;
import static de.westnordost.osmfeatures.JsonUtils.parseStringMap;

/** Parses this file
 *  https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json
 *  into map of id -> Feature. */
class IDPresetsJsonParser {

    public List<BaseFeature> parse(InputStream is) throws JSONException, IOException
    {
        JSONObject object = createFromInputStream(is);
        List<BaseFeature> result = new ArrayList<>();
        for (Iterator<String> it = object.keys(); it.hasNext(); )
        {
            String id = it.next().intern();
            BaseFeature f = parseFeature(id, object.getJSONObject(id));
            if (f != null) result.add(f);
        }
        return result;
    }

    private BaseFeature parseFeature(String id, JSONObject p)
    {
        Map<String,String> tags = parseStringMap(p.getJSONObject("tags"));
        // drop features with * in key or value of tags (for now), because they never describe
        // a concrete thing, but some category of things.
        // TODO maybe drop this limitation
        if(anyKeyOrValueContainsWildcard(tags)) return null;
        // also dropping features with empty tags (generic point, line, relation)
        if(tags.isEmpty()) return null;

        List<GeometryType> geometry = parseList(p.getJSONArray("geometry"),
                item -> GeometryType.valueOf(((String)item).toUpperCase(Locale.US)));
        boolean suggestion = p.optBoolean("suggestion", false);
        String name = p.optString("name");
        String icon = p.optString("icon");
        String imageURL = p.optString("imageURL");
        List<String> terms = parseList(p.optJSONArray("terms"), item -> (String)item);

        JSONObject locationSet = p.optJSONObject("locationSet");
        List<String> includeCountryCodes;
        List<String> excludeCountryCodes;
        if (locationSet != null) {
            includeCountryCodes = parseCountryCodes(locationSet.optJSONArray("include"));
            if (includeCountryCodes == null) return null;
            excludeCountryCodes = parseCountryCodes(locationSet.optJSONArray("exclude"));
            if (excludeCountryCodes == null) return null;
        } else {
            includeCountryCodes = new ArrayList<>(0);
            excludeCountryCodes = new ArrayList<>(0);
        }

        boolean searchable = p.optBoolean("searchable", true);
        double matchScore = p.optDouble("matchScore", 1.0);
        Map<String,String> addTags =
                p.has("addTags") ? parseStringMap(p.optJSONObject("addTags")) : tags;
        Map<String,String> removeTags =
                p.has("removeTags") ? parseStringMap(p.optJSONObject("removeTags")) : addTags;

        return new BaseFeature(
                id,
                Collections.unmodifiableMap(tags),
                Collections.unmodifiableList(geometry),
                name, icon, imageURL,
                Collections.unmodifiableList(terms),
                Collections.unmodifiableList(includeCountryCodes),
                Collections.unmodifiableList(excludeCountryCodes),
                searchable, matchScore, suggestion,
                Collections.unmodifiableMap(addTags),
                Collections.unmodifiableMap(removeTags)
        );
    }

    private static List<String> parseCountryCodes(JSONArray jsonList)
    {
        List<Object> list = parseList(jsonList, item -> item);
        List<String> result = new ArrayList<>(list.size());
        for (Object item : list)
        {
            // for example a lat,lon pair to denote a location with radius. Not supported.
            if (!(item instanceof String)) return null;
            String cc = ((String)item).toUpperCase(Locale.US).intern();
            // don't need this, 001 stands for "whole world"
            if (cc.equals("001")) continue;
            // some unsupported code, such as "150" or "city_national_bank_fl.geojson"
            if (cc.length() > 2) return null;
            result.add(cc);
        }
        return result;
    }

    private static boolean anyKeyOrValueContainsWildcard(Map<String,String> map)
    {
        for (Map.Entry<String, String> e : map.entrySet())
        {
            if(e.getKey().contains("*") || e.getValue().contains("*")) return true;
        }
        return false;
    }
}
