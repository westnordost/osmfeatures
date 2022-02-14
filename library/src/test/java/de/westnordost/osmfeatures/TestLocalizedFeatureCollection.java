package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class TestLocalizedFeatureCollection implements LocalizedFeatureCollection
{
	private final List<Feature> features;

	public TestLocalizedFeatureCollection(List<Feature> features)
	{
		this.features = features;
	}

	@Override public Collection<Feature> getAll(List<Locale> locales)
	{
		List<Feature> result = new ArrayList<>();
		for (Feature feature : features) {
			if (locales.contains(feature.getLocale())) {
				result.add(feature);
			}
		}
		return result;
	}

	@Override public Feature get(String id, List<Locale> locales)
	{
		for (Feature feature : features) {
			if (!feature.getId().equals(id)) continue;
			if (!locales.contains(feature.getLocale())) return null;
			return feature;
		}
		return null;
	}
}
