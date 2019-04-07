package de.westnordost.osmnames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Subset of a preset as defined in the iD editor
 *  https://github.com/openstreetmap/iD/blob/master/data/presets/README.md
 *  with only the fields helpful for the dictionary */
class Preset
{
	// TODO make nullsafe?

	final String id;
	final Map<String,String> tags;
	final List<GeometryType> geometry;
	final String name;
	final List<String> terms;
	final List<String> countryCodes;
	final boolean searchable;
	final float matchScore;
	final boolean suggestion;
	final Map<String,String> addTags;

	final String canonicalName;
	final List<String> canonicalTerms;

	Preset(String id, Map<String, String> tags, List<GeometryType> geometry, String name,
		   List<String> terms, List<String> countryCodes, boolean searchable,
		   float matchScore, boolean suggestion, Map<String, String> addTags)
	{
		this.id = id;
		this.tags = tags;
		this.geometry = geometry != null ? Collections.unmodifiableList(geometry) : null;
		this.name = name;
		this.terms = terms != null ? Collections.unmodifiableList(terms) : null;
		this.countryCodes = countryCodes != null ? Collections.unmodifiableList(countryCodes) : null;
		this.searchable = searchable;
		this.matchScore = matchScore;
		this.suggestion = suggestion;
		this.addTags = addTags != null ? Collections.unmodifiableMap(addTags) : null;

		this.canonicalName = StringUtils.canonicalize(name);
		if(terms != null)
		{
			List<String> canonicalTerms = new ArrayList<>(terms.size());
			for (String term : terms)
			{
				canonicalTerms.add(StringUtils.canonicalize(term));
			}
			this.canonicalTerms = Collections.unmodifiableList(canonicalTerms);
		}
		else
		{
			this.canonicalTerms = null;
		}
	}

	Preset(Preset p, String name, List<String> terms)
	{
		this(p.id, p.tags, p.geometry, name, terms, p.countryCodes, p.searchable, p.matchScore,
				p.suggestion, p.addTags);
	}

	String getParentId()
	{
		int lastSlashIndex = id.lastIndexOf("/");
		if(lastSlashIndex == -1) return null;
		return id.substring(0, lastSlashIndex);
	}

	// not defined:
	// - fields
	// - moreFields
	// - icon
	// - imageURL
	// - replacements
}
