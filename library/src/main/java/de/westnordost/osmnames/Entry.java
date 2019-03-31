package de.westnordost.osmnames;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** A dictionary entry, linking names (name + synonyms) and keywords with tags */
public class Entry
{
	private final List<String> names;
	private final List<String> keywords;
	private final Map<String, String> tags;
	private final int count;

	public Entry(List<String> names, Map<String,String> tags, List<String> keywords, int count)
	{
		this.names = names;
		this.keywords = keywords;
		this.tags = tags;
		this.count = count;
	}

	public List<String> getNames() { return Collections.unmodifiableList(names); }

	public List<String> getKeywords()
	{
		return keywords != null ? Collections.unmodifiableList(keywords) : Collections.emptyList();
	}

	public Map<String, String> getTags()
	{
		return Collections.unmodifiableMap(tags);
	}

	public int getCount()
	{
		return count;
	}

	/** @return primary name (first in names list) */
	public String getPrimaryName() { return names.get(0); }
}
