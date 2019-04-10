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

	public static NamesDictionary create(String path)
	{
		return new NamesDictionary(new IDPresetCollection(new FileSystemAccess(new File(path))));
	}

	public List<Match> get(Map<String, String> tags, GeometryType geometry)
	{
		return get(tags, geometry, null);
	}

	/** Find entries by a set of tags. Returns a list because in rare cases, a set of tags may match
	 *  multiple primary features, such as for tag combinations like
	 *  <tt>shop=deli</tt> + <tt>amenity=cafe</tt>.
	 *
	 *  @param tags The tags the element has
	 *  @param geometry the type of geometry the element has. Optional.
	 *  @param locale the Locale in which to present the results. Uses the current default locale if
	 *                none is specified.
	 *  @return a list of dictionary entries that match. Returns an empty list if nothing is found.
	 * */
	public List<Match> get(Map<String, String> tags, GeometryType geometry, Locale locale)
	{
		if(locale == null) locale = Locale.getDefault();

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

	public List<Match> find(String search, GeometryType geometry, String countryCode, int limit)
	{
		return find(search, geometry, countryCode, limit, null);
	}

	/**
	 * Find entries by given search word.<br>
	 * Results are sorted mainly in this order: Matches with names, with brand names, then matches
	 * with terms (keywords).
	 *
	 * @param search The term (name or keywords) to find the entries for
	 * @param geometry the type of geometry the element has. Optional.
	 * @param countryCode the ISO 3166-1 alpha-2 country code of the country the element is in.
	 *                    Optional. Will only return non-country-specific results if none is
	 *                    specified.
	 * @param limit limit how many results to return at most. Default (if 0) 50, -1 for unlimited.
	 * @param locale the Locale in which to present the results. Uses the current default locale if
	 *               none is specified.
	 *
	 * @return The ordered list of findings. Empty if nothing has been found.
	 */
	public List<Match> find(String search, GeometryType geometry, String countryCode, int limit, Locale locale)
	{
		// TODO: possible performance improvement: Map of name -> preset for fast exact-matches

		if(limit == 0) limit = 50;
		if(locale == null) locale = Locale.getDefault();

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

	private static class FileSystemAccess implements IDPresetCollection.FileAccessAdapter
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
