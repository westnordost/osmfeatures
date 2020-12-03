package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

class MultiFeatureCollection implements FeatureCollection
{
    private final FeatureCollection[] featureCollections;

    public MultiFeatureCollection(FeatureCollection ...featureCollections)
    {
        this.featureCollections = featureCollections;
    }

    @Override
    public Collection<Feature> getAllSuggestions()
    {
        ArrayList<Feature> result = new ArrayList<>();
        for (FeatureCollection featureCollection : featureCollections)
        {
            result.addAll(featureCollection.getAllSuggestions());
        }
        return result;
    }

    @Override
    public Collection<Feature> getAllLocalized(List<Locale> locale)
    {
        ArrayList<Feature> result = new ArrayList<>();
        for (FeatureCollection featureCollection : featureCollections)
        {
            result.addAll(featureCollection.getAllLocalized(locale));
        }
        return result;
    }

    @Override
    public Feature get(String id, List<Locale> locale)
    {
        for (FeatureCollection featureCollection : featureCollections)
        {
            Feature f = featureCollection.get(id, locale);
            if (f != null) return f;
        }
        return null;
    }
}
