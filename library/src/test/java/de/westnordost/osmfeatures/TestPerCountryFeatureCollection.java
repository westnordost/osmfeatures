package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestPerCountryFeatureCollection implements PerCountryFeatureCollection
{
	private final List<Feature> features;

	public TestPerCountryFeatureCollection(List<Feature> features)
	{
		this.features = features;
	}

	@Override
	public Collection<Feature> getAll(List<String> countryCodes) {
		List<Feature> result = new ArrayList<>();
		for (Feature feature : features) {
			if (feature.getIncludeCountryCodes().isEmpty()) {
				result.add(feature);
				continue;
			}
			for (String countryCode : countryCodes) {
				if (feature.getIncludeCountryCodes().contains(countryCode)) {
					result.add(feature);
				}
			}
		}
		return result;
	}

	@Override
	public Feature get(String id, List<String> countryCodes) {
		for (Feature feature : features) {
			if (!feature.getId().equals(id)) continue;
			if (feature.getIncludeCountryCodes().isEmpty()) return feature;
			for (String countryCode : countryCodes) {
				if (feature.getIncludeCountryCodes().contains(countryCode)) return feature;
			}
		}
		return null;
	}
}
