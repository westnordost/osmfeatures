package de.westnordost.osmfeatures;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Subset of a feature as defined in the iD editor
 *  https://github.com/ideditor/schema-builder#preset-schema
 *  with only the fields helpful for the dictionary */
public interface Feature {
    String getId();
    Map<String,String> getTags();
    List<GeometryType> getGeometry();
    String getName();
    String getIcon();
    String getImageURL();
    List<String> getTerms();
    List<String> getIncludeCountryCodes();
    List<String> getExcludeCountryCodes();
    boolean isSearchable();
    double getMatchScore();
    Map<String,String> getAddTags();
    Map<String,String> getRemoveTags();

    String getCanonicalName();
    List<String> getCanonicalTerms();

    Locale getLocale();
}

// currently not included:
// - fields
// - moreFields
// - replacement
// - reference