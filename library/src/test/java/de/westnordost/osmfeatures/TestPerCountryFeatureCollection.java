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
			for (String countryCode : countryCodes) {
				List<String> includeCountryCodes = feature.getIncludeCountryCodes();
				if (includeCountryCodes.contains(countryCode) || countryCode == null && includeCountryCodes.isEmpty()) {
					result.add(feature);
					break;
				}
			}
		}
		return result;
	}

	@Override
	public Feature get(String id, List<String> countryCodes) {
		for (Feature feature : features) {
			if (!feature.getId().equals(id)) continue;
			List<String> includeCountryCodes = feature.getIncludeCountryCodes();
			for (String countryCode : countryCodes) {
				if (includeCountryCodes.contains(countryCode) || countryCode == null && includeCountryCodes.isEmpty()) {
					return feature;
				}
			}
		}
		return null;
	}
}
