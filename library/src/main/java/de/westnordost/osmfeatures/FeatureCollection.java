package de.westnordost.osmfeatures;

import java.util.Collection;
import java.util.Locale;

public interface FeatureCollection
{
	Collection<Feature> getAll(Locale locale);
	Feature get(String id, Locale locale);
}
