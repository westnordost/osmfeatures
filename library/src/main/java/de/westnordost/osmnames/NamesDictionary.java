package de.westnordost.osmnames;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NamesDictionary
{
	private final PresetCollection presetCollection;
	private final Set<String> keys;

	NamesDictionary(PresetCollection presetCollection)
	{
		this.presetCollection = presetCollection;
		keys = new HashSet<>();
		for (Preset preset : presetCollection.getAll(null))
		{
			keys.addAll(preset.tags.keySet());
		}
	}

	/** Create a new NamesDictionary which gets it's data from the given directory. */
	public static NamesDictionary create(String path)
	{
		return new NamesDictionary(new iDPresetCollection(new FileSystemAccess(new File(path))));
	}

	/** Find matches by a set of tags */
	public QueryByTagBuilder byTags(Map<String, String> tags)
	{
		return new QueryByTagBuilder(tags);
	}

	private List<Match> get(Map<String, String> tags, GeometryType geometry, Locale locale)
	{
		// TODO possible performance improvement: group presets by countryCodes (50% less iterations)
		// TODO possible performance improvement: sort presets into a tree of tags (lookup in almost O(1))

		// little performance improvement: no use to look for tags that are not used for any preset
		Map<String, String> relevantTags = createMapWithOnlyRelevantTagsRetained(tags);
		List<Preset> foundPresets = new ArrayList<>();
		if(!relevantTags.isEmpty())
		{
			Set<String> removeIds = new HashSet<>();

			for (Preset preset : presetCollection.getAll(locale))
			{
				if (geometry != null)
					if (!preset.geometry.contains(geometry))
						continue;

				if (mapContainsAllEntries(relevantTags, preset.tags.entrySet()))
				{
					foundPresets.add(preset);
					removeIds.addAll(getParentCategoryIds(preset.id));
				}
			}
			Collections.sort(foundPresets, (a, b) -> {
				// 1. presets with more matching tags first
				int tagOrder = b.tags.size() - a.tags.size();
				if(tagOrder != 0) return tagOrder;
				// 2. presets with higher matchScore first
				return (int) (100 * b.matchScore - 100 * a.matchScore);
			});

			// only return of each category the most specific thing. I.e. will return
			// McDonalds only instead of McDonalds,Fast-Food Restaurant,Amenity
			Iterator<Preset> it = foundPresets.iterator();
			while(it.hasNext())
			{
				if(removeIds.contains(it.next().id)) it.remove();
			}
		}
		return createMatches(foundPresets, -1, locale);
	}

	private static Collection<String> getParentCategoryIds(String id)
	{
		List<String> result = new ArrayList<>();
		do
		{
			id = getParentId(id);
			if(id != null) result.add(id);
		}
		while(id != null);
		return result;
	}

	private static String getParentId(String id)
	{
		int lastSlashIndex = id.lastIndexOf("/");
		if(lastSlashIndex == -1) return null;
		return id.substring(0, lastSlashIndex);
	}

	private Map<String, String> createMapWithOnlyRelevantTagsRetained(Map<String, String> tags)
	{
		Map<String, String> result = new HashMap<>();
		for (Map.Entry<String, String> e : tags.entrySet())
		{
			if(keys.contains(e.getKey())) result.put(e.getKey(), e.getValue());
		}
		return result;
	}

	private static <K,V> boolean mapContainsAllEntries(Map<K,V> map, Collection<Map.Entry<K,V>> entries)
	{
		for (Map.Entry<K, V> entry : entries)
		{
			if(!mapContainsEntry(map, entry)) return false;
		}
		return true;
	}

	private static <K,V> boolean mapContainsEntry(Map<K,V> map, Map.Entry<K,V> entry)
	{
		V mapValue = map.get(entry.getKey());
		V value = entry.getValue();
		return mapValue == value || value != null && value.equals(mapValue);
	}

	/** Find matches by given search word */
	public QueryByTermBuilder byTerm(String term)
	{
		return new QueryByTermBuilder(term);
	}

	private List<Match> get(String search, GeometryType geometry, String countryCode, int limit, Locale locale)
	{
		// TODO: possible performance improvement: Map of name -> preset for fast exact-matches
		String canonicalSearch = StringUtils.canonicalize(search);

		List<Preset> searchable = filter(presetCollection.getAll(locale), preset ->
			preset.searchable &&
			(geometry == null || preset.geometry.contains(geometry)) &&
			(preset.countryCodes.isEmpty() || (countryCode != null && preset.countryCodes.contains(countryCode)))
		);

		Comparator<Preset> sortNames = (a, b) -> {
			// 1. exact matches first
			int exactMatchOrder = (b.name.equals(search)?1:0) - (a.name.equals(search)?1:0);
			if(exactMatchOrder != 0) return exactMatchOrder;
			// 2. exact matches case and diacritics insensitive first
			int cExactMatchOrder = (b.canonicalName.equals(canonicalSearch)?1:0) - (a.canonicalName.equals(canonicalSearch)?1:0);
			if(cExactMatchOrder != 0) return cExactMatchOrder;
			// 3. earlier matches in string first
			int indexOfOrder = a.canonicalName.indexOf(canonicalSearch) - b.canonicalName.indexOf(canonicalSearch);
			if(indexOfOrder != 0) return indexOfOrder;
			// 4. presets with higher matchScore first
			int matchScoreOrder = (int) (100 * b.matchScore - 100 * a.matchScore);
			if(matchScoreOrder != 0) return matchScoreOrder;
			// 5. shorter names first
			return a.name.length() - b.name.length();
		};

		Comparator<Preset> sortTerms = (a, b) -> {
			// 3. presets with higher matchScore first
			return (int) (100 * b.matchScore - 100 * a.matchScore);
		};

		Set<Preset> foundPresets = new HashSet<>();
		List<Preset> result = new ArrayList<>();

		List<Preset> nameMatches = filter(searchable, preset ->
				!preset.suggestion && startsWordWith(preset.canonicalName, canonicalSearch)
		);
		Collections.sort(nameMatches, sortNames);
		result.addAll(nameMatches);
		foundPresets.addAll(nameMatches);

		// if limit is reached, can return earlier (performance improvement)
		if(limit > 0 && result.size() >= limit) return createMatches(result, limit, locale);

		List<Preset> brandNameMatches = filter(searchable, preset ->
				preset.suggestion && preset.canonicalName.startsWith(canonicalSearch)
		);
		Collections.sort(brandNameMatches, sortNames);
		result.addAll(brandNameMatches);
		foundPresets.addAll(brandNameMatches);

		// if limit is reached, can return earlier (performance improvement)
		if(limit > 0 && result.size() >= limit) return createMatches(result, limit, locale);

		List<Preset> termsMatches = filter(searchable, preset -> {
			if(foundPresets.contains(preset)) return false;
			for (String s : preset.canonicalTerms)
			{
				if (s.startsWith(canonicalSearch)) return true;
			}
			return false;
		});
		Collections.sort(termsMatches, sortTerms);
		result.addAll(termsMatches);

		return createMatches(result, limit, locale);
	}

	// TODO: interface: get(String name) for exact-matches only?

	private static boolean startsWordWith(String haystack, String needle)
	{
		int indexOf = haystack.indexOf(needle);
		return indexOf == 0 || indexOf > 0 && haystack.charAt(indexOf-1) == ' ';
	}

	private interface Predicate<T> { boolean fn(T v); }
	private static <T> List<T> filter(Iterable<T> collection, Predicate<T> predicate)
	{
		List<T> result = new ArrayList<>();
		for (T v : collection) if(predicate.fn(v)) result.add(v);
		return result;
	}

	private List<Match> createMatches(List<Preset> presetsList, int limit, Locale locale)
	{
		int size = limit > 0 ? Math.min(presetsList.size(), limit) : presetsList.size();
		List<Match> result = new ArrayList<>(size);
		for (Preset preset : presetsList)
		{
			result.add(createMatch(preset, locale));
			if(--limit == 0) return result;
		}
		return result;
	}

	private Match createMatch(Preset preset, Locale locale)
	{
		String name = preset.name;
		Map<String, String> tags = new HashMap<>(preset.tags);
		tags.putAll(preset.addTags);
		String parentName = null;
		if(preset.suggestion)
		{
			String parentId = preset.getParentId();
			if(parentId != null)
			{
				Preset parentPreset = presetCollection.get(parentId, locale);
				if(parentPreset != null)
				{
					parentName = parentPreset.name;
				}
			}
		}
		return new Match(name, tags, parentName);
	}

	public class QueryByTagBuilder
	{
		private final Map<String, String> tags;
		private GeometryType geometryType = null;
		private Locale locale = Locale.getDefault();

		private QueryByTagBuilder(Map<String, String> tags) { this.tags = tags; }

		/** Sets for which geometry type to look. If not set or <tt>null</tt>, any will match. */
		public QueryByTagBuilder forGeometry(GeometryType geometryType)
		{
			this.geometryType = geometryType;
			return this;
		}

		/** Sets the locale in which to present the results. If none is specified, the default
		 *  locale is used. */
		public QueryByTagBuilder forLocale(Locale locale)
		{
			this.locale = locale;
			return this;
		}

		/** Returns a list of dictionary entries that match or an empty list if nothing is
		 *  found. <br>In rare cases, a set of tags may match multiple primary features, such as for
		 *  tag combinations like <tt>shop=deli</tt> + <tt>amenity=cafe</tt>, so, this is why
		 *  it is a list. */
		public List<Match> find()
		{
			return get(tags, geometryType, locale);
		}
	}

	public class QueryByTermBuilder
	{
		private final String term;
		private GeometryType geometryType = null;
		private Locale locale = Locale.getDefault();
		private int limit = 50;
		private String countryCode = null;

		private QueryByTermBuilder(String term) { this.term = term; }

		/** Sets for which geometry type to look. If not set or <tt>null</tt>, any will match. */
		public QueryByTermBuilder forGeometry(GeometryType geometryType)
		{
			this.geometryType = geometryType;
			return this;
		}

		/** Sets the locale in which to present the results. If none is specified, the default
		 *  locale is used. */
		public QueryByTermBuilder forLocale(Locale locale)
		{
			this.locale = locale;
			return this;
		}

		/** the ISO 3166-1 alpha-2 country code of the country the element is in. If not specified,
		 *  will only return matches that are not county-specific. */
		public QueryByTermBuilder inCountry(String countryCode)
		{
			this.countryCode = countryCode;
			return this;
		}

		/** limit how many results to return at most. Default is 50, -1 for unlimited. */
		public QueryByTermBuilder limit(int limit)
		{
			this.limit = limit;
			return this;
		}

		/** Returns a list of dictionary entries that match or an empty list if nothing is
		 *  found. <br>
		 *  Results are sorted mainly in this order: Matches with names, with brand names, then
		 *  matches with terms (keywords). */
		public List<Match> find()
		{
			return get(term, geometryType, countryCode, limit, locale);
		}
	}

	private static class FileSystemAccess implements iDPresetCollection.FileAccessAdapter
	{
		private final File basePath;

		FileSystemAccess(File basePath)
		{
			if(!basePath.isDirectory()) throw new IllegalArgumentException("basePath must be a directory");
			this.basePath = basePath;
		}

		@Override public boolean exists(String name) { return new File(basePath, name).exists(); }
		@Override public InputStream open(String name) throws IOException
		{
			return new FileInputStream(new File(basePath, name));
		}
	}
}
