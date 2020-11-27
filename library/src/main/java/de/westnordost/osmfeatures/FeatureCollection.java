package de.westnordost.osmfeatures;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public interface FeatureCollection
{
	Collection<Feature> getAllSuggestions();
	Collection<Feature> getAllLocalized(List<Locale> locale);
	Feature get(String id, List<Locale> locale);
}
