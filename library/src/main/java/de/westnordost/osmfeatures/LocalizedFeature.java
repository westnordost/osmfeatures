package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class LocalizedFeature implements Feature {

    private final BaseFeature p;
    private final String name;
    private final List<String> terms;
    private final String canonicalName;
    private final List<String> canonicalTerms;
    private final Locale locale;

    public LocalizedFeature(BaseFeature p, Locale locale, String name, List<String> terms)
    {
        this.p = p;
        this.name = name;
        this.terms = terms;
        this.locale = locale;

        this.canonicalName = StringUtils.canonicalize(name);
        List<String> canonicalTerms = new ArrayList<>(terms.size());
        for (String term : terms)
        {
            canonicalTerms.add(StringUtils.canonicalize(term));
        }
        this.canonicalTerms = Collections.unmodifiableList(canonicalTerms);
    }

    @Override public String getId() { return p.getId(); }
    @Override public Map<String, String> getTags() { return p.getTags(); }
    @Override public List<GeometryType> getGeometry() { return p.getGeometry(); }
    @Override public String getName() { return name; }
    @Override public String getIcon() { return p.getIcon(); }
    @Override public String getImageURL() { return p.getImageURL(); }
    @Override public List<String> getTerms() { return terms; }
    @Override public List<String> getIncludeCountryCodes() { return p.getIncludeCountryCodes(); }
    @Override public List<String> getExcludeCountryCodes() { return p.getExcludeCountryCodes(); }
    @Override public boolean isSearchable() { return p.isSearchable(); }
    @Override public double getMatchScore() { return p.getMatchScore(); }
    @Override public boolean isSuggestion() { return p.isSuggestion(); }
    @Override public Map<String, String> getAddTags() { return p.getAddTags(); }
    @Override public Map<String, String> getRemoveTags() { return p.getRemoveTags(); }
    @Override public String getCanonicalName() { return canonicalName; }
    @Override public List<String> getCanonicalTerms() { return canonicalTerms; }
    @Override public Locale getLocale() { return locale; }
}
