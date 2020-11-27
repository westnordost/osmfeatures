package de.westnordost.osmfeatures;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TestFeatureCollection implements FeatureCollection
{
	private final Map<String, Feature> features;
	private final Map<String, Feature> brandFeatures;
	private final Locale locale;

	public TestFeatureCollection(Locale locale, Feature...features)
	{
		this.locale = locale;
		this.features = new HashMap<>(features.length);
		this.brandFeatures = new HashMap<>(features.length);
		for (Feature feature : features)
		{
			if (feature.isSuggestion()) brandFeatures.put(feature.getId(), feature);
			else this.features.put(feature.getId(), feature);
		}
	}

	public TestFeatureCollection(Feature...features)
	{
		this(null, features);
	}

	@Override public Collection<Feature> getAllSuggestions() {
		return brandFeatures.values();
	}

	@Override public Collection<Feature> getAllLocalized(List<Locale> locales)
	{
		if (locales.contains(locale)) return features.values();
		return Collections.emptyList();
	}

	@Override public Feature get(String id, List<Locale> locales)
	{
		Feature f = locales.contains(locale) ? features.get(id) : null;
		return f != null ? f : brandFeatures.get(id);
	}

}
