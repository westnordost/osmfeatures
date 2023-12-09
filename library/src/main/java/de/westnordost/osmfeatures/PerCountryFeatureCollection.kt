package de.westnordost.osmfeatures;

import java.util.Collection;
import java.util.List;

/** A collection of features grouped by country code */
public interface PerCountryFeatureCollection {

    /** Returns all features with the given country code */
    Collection<Feature> getAll(List<String> countryCodes);

    /** Returns the feature with the given id with the given country code or null if it has not been
     *  found  */
    Feature get(String id, List<String> countryCodes);
}
