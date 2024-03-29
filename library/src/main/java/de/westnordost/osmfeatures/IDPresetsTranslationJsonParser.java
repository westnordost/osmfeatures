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
import java.util.HashMap;

import static de.westnordost.osmfeatures.JsonUtils.createFromInputStream;

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
        Map<String, LocalizedFeature> localizedFeatures = new HashMap<>(presetsObject.length());
        for (Iterator<String> it = presetsObject.keys(); it.hasNext(); )
        {
            String id = it.next().intern();
            LocalizedFeature f = parseFeature(baseFeatures.get(id), locale, presetsObject.getJSONObject(id));
            if (f != null) localizedFeatures.put(id, f);
        }
        for (BaseFeature baseFeature : baseFeatures.values())
        {
            List<String> names = baseFeature.getNames();
            if (names.size() < 1) continue;
            String name = names.get(0);
            boolean isPlaceholder = name.startsWith("{") && name.endsWith("}");
            if (!isPlaceholder) continue;
            String placeholderId = name.substring(1, name.length() - 1);
            LocalizedFeature localizedFeature = localizedFeatures.get(placeholderId);
            if (localizedFeature == null) continue;
            localizedFeatures.put(baseFeature.getId(), new LocalizedFeature(
                    baseFeature,
                    locale,
                    localizedFeature.getNames(),
                    localizedFeature.getTerms()
            ));
        }

        return new ArrayList<>(localizedFeatures.values());
    }

    private LocalizedFeature parseFeature(BaseFeature feature, Locale locale, JSONObject localization)
    {
        if (feature == null) return null;

        String name = localization.optString("name");
        if(name == null || name.isEmpty()) return null;

        String[] namesArray = parseNewlineSeparatedList(localization.optString("aliases"));
        List<String> names = new ArrayList<>(namesArray.length + 1);
        Collections.addAll(names, namesArray);
        names.remove(name);
        names.add(0, name);

        String[] termsArray = parseCommaSeparatedList(localization.optString("terms"));
        List<String> terms = new ArrayList<>(termsArray.length);
        Collections.addAll(terms, termsArray);
        terms.removeAll(names);

        return new LocalizedFeature(
                feature,
                locale,
                Collections.unmodifiableList(names),
                Collections.unmodifiableList(terms)
        );
    }


    public static String[] parseCommaSeparatedList(String str)
    {
        if(str == null || str.isEmpty()) return new String[0];
        return str.split("\\s*,+\\s*");
    }

    public static String[] parseNewlineSeparatedList(String str)
    {
        if(str == null || str.isEmpty()) return new String[0];
        return str.split("\\s*[\\r\\n]+\\s*");
    }
}
