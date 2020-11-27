package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class BaseFeature implements Feature
{
	private final String id;
	private final Map<String,String> tags;
	private final List<GeometryType> geometry;
	private final String name;
	private final List<String> terms;
	private final List<String> countryCodes;
	private final List<String> notCountryCodes;
	private final boolean searchable;
	private final double matchScore;
	private final boolean suggestion;
	private final Map<String,String> addTags;
	private final Map<String,String> removeTags;

	private final String canonicalName;
	private final List<String> canonicalTerms;

	public BaseFeature(String id, Map<String, String> tags, List<GeometryType> geometry, String name,
					   List<String> terms, List<String> countryCodes, List<String> notCountryCodes,
					   boolean searchable, double matchScore, boolean suggestion,
					   Map<String, String> addTags, Map<String, String> removeTags)
	{
		this.id = id;
		this.tags = tags;
		this.geometry = geometry;
		this.name = name;
		this.terms = terms;
		this.countryCodes = countryCodes;
		this.notCountryCodes = notCountryCodes;
		this.searchable = searchable;
		this.matchScore = matchScore;
		this.suggestion = suggestion;
		this.addTags = addTags;
		this.removeTags = removeTags;

		this.canonicalName = StringUtils.canonicalize(name);
		List<String> canonicalTerms = new ArrayList<>(terms.size());
		for (String term : terms)
		{
			canonicalTerms.add(StringUtils.canonicalize(term));
		}
		this.canonicalTerms = Collections.unmodifiableList(canonicalTerms);
	}

	@Override public String getId() { return id; }
	@Override public Map<String, String> getTags() { return tags; }
	@Override public List<GeometryType> getGeometry() { return geometry; }
	@Override public String getName() { return name; }
	@Override public List<String> getTerms() { return terms; }
	@Override public List<String> getCountryCodes() { return countryCodes; }
	@Override public List<String> getNotCountryCodes() { return notCountryCodes; }
	@Override public boolean isSearchable() { return searchable; }
	@Override public double getMatchScore() { return matchScore; }
	@Override public boolean isSuggestion() { return suggestion; }
	@Override public Map<String, String> getAddTags() { return addTags; }
	@Override public Map<String, String> getRemoveTags() { return removeTags; }
	@Override public String getCanonicalName() { return canonicalName; }
	@Override public List<String> getCanonicalTerms() { return canonicalTerms; }
	@Override public Locale getLocale() { return suggestion ? null : Locale.ENGLISH; }
}
