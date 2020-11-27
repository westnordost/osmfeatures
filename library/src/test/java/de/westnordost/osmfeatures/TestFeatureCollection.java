package de.westnordost.osmfeatures;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TestFeatureCollection implements FeatureCollection
{
	private final Map<String, Feature> features;
	private final Map<String, Feature> brandFeatures;

	public TestFeatureCollection(Feature...features)
	{
		this.features = new HashMap<>(features.length);
		this.brandFeatures = new HashMap<>(features.length);
		for (Feature feature : features)
		{
			if (feature.isSuggestion()) brandFeatures.put(feature.getId(), feature);
			else this.features.put(feature.getId(), feature);
		}
	}

	@Override public Collection<Feature> getAllSuggestions() {
		return brandFeatures.values();
	}

	@Override public Collection<Feature> getAllLocalized(List<Locale> locale)
	{
		return features.values();
	}

	@Override public Feature get(String id, List<Locale> locale)
	{
		Feature f = features.get(id);
		return f != null ? f : brandFeatures.get(id);
	}

}
