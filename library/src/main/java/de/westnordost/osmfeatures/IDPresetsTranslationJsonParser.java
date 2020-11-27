package de.westnordost.osmfeatures;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.westnordost.osmfeatures.JsonUtils.createFromInputStream;
import static de.westnordost.osmfeatures.JsonUtils.parseCommaSeparatedList;

/** Parses a file from
 *  https://github.com/openstreetmap/id-tagging-schema/tree/main/dist/translations
 *  , given the base features are already parsed.
 *
 *  Note: The translation files contain more than just the translations for the presets. To be
 *  precise, the parser assumes the translation files to be reduced to everything in
 *  [language code] -> presets -> presets
 *  see README.md */
class IDPresetsTranslationJsonParser {

    public List<LocalizedFeature> parse(
            InputStream is, Locale locale, Map<String, BaseFeature> baseFeatures
    ) throws JSONException, IOException
    {
        JSONObject object = createFromInputStream(is);
        JSONObject presetsObject = object.getJSONObject("presets");
        List<LocalizedFeature> result = new ArrayList<>(presetsObject.length());
        for (Iterator<String> it = presetsObject.keys(); it.hasNext(); )
        {
            String id = it.next().intern();
            LocalizedFeature f = parseFeature(baseFeatures.get(id), locale, presetsObject.getJSONObject(id));
            if (f != null) result.add(f);
        }
        return result;
    }

    private LocalizedFeature parseFeature(BaseFeature feature, Locale locale, JSONObject localization)
    {
        if (feature == null) return null;
        String name = localization.optString("name");
        if(name == null || name.isEmpty()) return null;
        List<String> terms = parseCommaSeparatedList(localization.optString("terms"), name);
        return new LocalizedFeature(feature, locale, name, terms);
    }
}
