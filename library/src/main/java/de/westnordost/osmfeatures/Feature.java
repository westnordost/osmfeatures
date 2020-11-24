package de.westnordost.osmfeatures;

import java.util.List;
import java.util.Map;

interface Feature {
    String getId();
    Map<String,String> getTags();
    List<GeometryType> getGeometry();
    String getName();
    List<String> getTerms();
    List<String> getCountryCodes();
    boolean isSearchable();
    double getMatchScore();
    boolean isSuggestion();
    Map<String,String> getAddTags();

    String getCanonicalName();
    List<String> getCanonicalTerms();

    String getParentId();
}

// not included:
// - fields
// - moreFields
// - icon
// - imageURL