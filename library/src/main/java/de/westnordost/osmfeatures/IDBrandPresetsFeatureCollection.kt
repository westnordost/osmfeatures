package de.westnordost.osmfeatures;

import static de.westnordost.osmfeatures.CollectionUtils.synchronizedGetOrCreate;

import okio.FileHandle;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Non-localized feature collection sourcing from (NSI) iD presets defined in JSON.
 *
 *  The base path is defined via the given FileAccessAdapter. In the base path, it is expected that
 *  there is a presets.json which includes all the features. Additionally, it is possible to place
 *  more files like e.g. presets-DE.json, presets-US-NY.json into the directory which will be loaded
 *  lazily on demand */
public class IDBrandPresetsFeatureCollection implements PerCountryFeatureCollection
{
    private final FileAccessAdapter fileAccess;

    private final HashMap<String, LinkedHashMap<String, Feature>> featuresByIdByCountryCode = new LinkedHashMap<>(320);

    IDBrandPresetsFeatureCollection(FileAccessAdapter fileAccess) {
        this.fileAccess = fileAccess;
        getOrLoadPerCountryFeatures(null);
    }

    @Override public Collection<Feature> getAll(List<String> countryCodes)
    {
        Map<String, Feature> result = new HashMap<>();
        for (String cc : countryCodes) {
            result.putAll(getOrLoadPerCountryFeatures(cc));
        }
        return result.values();
    }

    @Override
    public Feature get(String id, List<String> countryCodes)
    {
        for (String cc : countryCodes) {
            Feature result = getOrLoadPerCountryFeatures(cc).get(id);
            if (result != null) return result;
        }
        return null;
    }

    private LinkedHashMap<String, Feature> getOrLoadPerCountryFeatures(String countryCode)
    {
        return synchronizedGetOrCreate(featuresByIdByCountryCode, countryCode, this::loadPerCountryFeatures);
    }

    private LinkedHashMap<String, Feature> loadPerCountryFeatures(String countryCode)
    {
        List<BaseFeature> features = loadFeatures(countryCode);
        LinkedHashMap<String, Feature> featuresById = new LinkedHashMap<>(features.size());
        for (BaseFeature feature : features) {
            featuresById.put(feature.getId(), feature);
        }
        return featuresById;
    }

    private List<BaseFeature> loadFeatures(String countryCode) {
        String filename = getPresetsFileName(countryCode);
        try {
            if (!fileAccess.exists(filename)) return Collections.emptyList();
            try (FileHandle is = fileAccess.open(filename)) {
                return new IDPresetsJsonParser(true).parse(is);
            }
        }
        catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getPresetsFileName(String countryCode)
    {
        if (countryCode == null) {
            return "presets.json";
        } else {
            return "presets-" + countryCode + ".json";
        }
    }
}
