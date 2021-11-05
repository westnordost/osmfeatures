package de.westnordost.osmfeatures;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/** Non-localized feature collection sourcing from (NSI) iD presets defined in JSON.
 *
 *  The base path is defined via the given FileAccessAdapter. In the base path, it is expected that
 *  there is a json with the given name which includes all the features. */
public class IDBaseFeatureCollection implements FeatureCollection
{
    private final FileAccessAdapter fileAccess;

    private final LinkedHashMap<String, Feature> featuresById = new LinkedHashMap<>();

    private final String featuresFileName;
    private final boolean isSuggestions;

    IDBaseFeatureCollection(FileAccessAdapter fileAccess, boolean isSuggestions, String featuresFileName) {
        this.featuresFileName = featuresFileName;
        this.isSuggestions = isSuggestions;
        this.fileAccess = fileAccess;
        List<BaseFeature> features = loadFeatures();
        for (BaseFeature feature : features) {
            this.featuresById.put(feature.getId(), feature);
        }
    }

    private List<BaseFeature> loadFeatures()
    {
        try(InputStream is = fileAccess.open(featuresFileName))
        {
            return new IDPresetsJsonParser().parse(is);
        }
        catch (IOException | JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override public Collection<Feature> getAll() {
        return featuresById.values();
    }

    @Override public Feature get(String id) {
        return featuresById.get(id);
    }

    @Override public Boolean isSuggestions() {
        return isSuggestions;
    }
}
