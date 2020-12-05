package de.westnordost.osmfeatures;

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
import static de.westnordost.osmfeatures.JsonUtils.parseCommaSeparatedList;

/** Parses a file from
 *  https://github.com/openstreetmap/id-tagging-schema/tree/main/dist/translations
 *  , given the base features are already parsed.
 */
class IDPresetsTranslationJsonParser {

    public List<LocalizedFeature> parse(
            InputStream is, Locale locale, Map<String, BaseFeature> baseFeatures
    ) throws JSONException, IOException
    {
        JSONObject object = createFromInputStream(is);
        String languageKey = object.keys().next();
        JSONObject languageObject = object.optJSONObject(languageKey);
        if (languageObject == null) return Collections.emptyList();
        JSONObject presetsContainerObject = languageObject.optJSONObject("presets");
        if (presetsContainerObject == null) return Collections.emptyList();
        JSONObject presetsObject = presetsContainerObject.optJSONObject("presets");
        if (presetsObject == null) return Collections.emptyList();
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
        return new LocalizedFeature(feature, locale, name, Collections.unmodifiableList(terms));
    }
}
