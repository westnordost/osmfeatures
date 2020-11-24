package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Subset of a feature as defined in the iD editor
 *  https://github.com/openstreetmap/iD/blob/master/data/presets/README.md
 *  with only the fields helpful for the dictionary */
class BaseFeature implements Feature
{
	private final String id;
	private final Map<String,String> tags;
	private final List<GeometryType> geometry;
	private final String name;
	private final List<String> terms;
	private final List<String> countryCodes;
	private final boolean searchable;
	private final double matchScore;
	private final boolean suggestion;
	private final Map<String,String> addTags;

	private final String canonicalName;
	private final List<String> canonicalTerms;

	public BaseFeature(String id, Map<String, String> tags, List<GeometryType> geometry, String name,
				List<String> terms, List<String> countryCodes, boolean searchable,
				double matchScore, boolean suggestion, Map<String, String> addTags)
	{
		this.id = id;
		this.tags = Collections.unmodifiableMap(tags);
		this.geometry = Collections.unmodifiableList(geometry);
		this.name = name;
		this.terms = Collections.unmodifiableList(terms);
		this.countryCodes = Collections.unmodifiableList(countryCodes);
		this.searchable = searchable;
		this.matchScore = matchScore;
		this.suggestion = suggestion;
		this.addTags = addTags != null ? Collections.unmodifiableMap(addTags) : Collections.emptyMap();

		this.canonicalName = StringUtils.canonicalize(name);
		List<String> canonicalTerms = new ArrayList<>(terms.size());
		for (String term : terms)
		{
			canonicalTerms.add(StringUtils.canonicalize(term));
		}
		this.canonicalTerms = Collections.unmodifiableList(canonicalTerms);
	}

	@Override public String getParentId()
	{
		int lastSlashIndex = getId().lastIndexOf("/");
		if(lastSlashIndex == -1) return null;
		return getId().substring(0, lastSlashIndex);
	}

	@Override public String getId() { return id; }
	@Override public Map<String, String> getTags() { return tags; }
	@Override public List<GeometryType> getGeometry() { return geometry; }
	@Override public String getName() { return name; }
	@Override public List<String> getTerms() { return terms; }
	@Override public List<String> getCountryCodes() { return countryCodes; }
	@Override public boolean isSearchable() { return searchable; }
	@Override public double getMatchScore() { return matchScore; }
	@Override public boolean isSuggestion() { return suggestion; }
	@Override public Map<String, String> getAddTags() { return addTags; }
	@Override public String getCanonicalName() { return canonicalName; }
	@Override public List<String> getCanonicalTerms() { return canonicalTerms; }
}
