package de.westnordost.osmnames;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TestFeatureCollection implements FeatureCollection
{
	private final Map<String, Feature> features;

	public TestFeatureCollection(Feature...features)
	{
		this.features = new HashMap<>(features.length);
		for (Feature feature : features)
		{
			this.features.put(feature.id, feature);
		}
	}

	@Override public Collection<Feature> getAll(Locale locale)
	{
		return features.values();
	}

	@Override public Feature get(String id, Locale locale)
	{
		return features.get(id);
	}

}
