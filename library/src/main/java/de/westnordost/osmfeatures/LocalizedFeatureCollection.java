package de.westnordost.osmfeatures;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/** A localized collection of features */
public interface LocalizedFeatureCollection
{
	/** Returns all features in the given locale(s). */
	Collection<Feature> getAll(List<Locale> locales);
	/** Returns the feature with the given id in the given locale(s) or null if it has not been
	 *  found (for the given locale(s)) */
	Feature get(String id, List<Locale> locales);
}
