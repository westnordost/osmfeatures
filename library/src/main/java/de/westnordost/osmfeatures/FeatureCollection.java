package de.westnordost.osmfeatures;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/** A non-localized collection of features */
public interface FeatureCollection
{
	/** Returns all features in the given locale(s). */
	Collection<Feature> getAll();
	/** Returns the feature with the given id in the given locale(s) or null if it has not been
	 *  found (for the given locale(s)) */
	Feature get(String id);
	/** Whether the features in this collection are suggestions, i.e. brands */
	Boolean isSuggestions();
}
