package de.westnordost.osmfeatures;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/** Non-localized feature collection sourcing from (NSI) iD presets defined in JSON.
 *
 *  The base path is defined via the given FileAccessAdapter. In the base path, it is expected that
 *  there is a json with the given name which includes all the features. */
public class IDBaseFeatureCollection implements FeatureCollection
{
    private static final String FEATURES_FILE = "presets.json";

    private final FileAccessAdapter fileAccess;

    private final LinkedHashMap<String, Feature> featuresById = new LinkedHashMap<>();

    IDBaseFeatureCollection(FileAccessAdapter fileAccess) {
        this.fileAccess = fileAccess;
        List<BaseFeature> features = loadFeatures();
        for (BaseFeature feature : features) {
            this.featuresById.put(feature.getId(), feature);
        }
    }

    private List<BaseFeature> loadFeatures()
    {
        try(InputStream is = fileAccess.open(FEATURES_FILE))
        {
            return new IDPresetsJsonParser().parse(is);
        }
        catch (IOException | JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override public Collection<Feature> getAll(List<Locale> locales) {
        return featuresById.values();
    }

    @Override public Feature get(String id, List<Locale> locales) {
        return featuresById.get(id);
    }
}
