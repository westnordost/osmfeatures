package de.westnordost.osmnames;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Dictionary to translate names or keywords to OSM-tag-combinations or the other way round. */
public class NamesDictionary
{
	/** Whether to do a case insensitive search */
	public static final int CASE_INSENSITIVE = 1;
	/** Whether to do a diacritics insensitive search */
	public static final int DIACRITICS_INSENSITIVE = 2;

	/** Whether the search term can be anywhere in the name */
	public static final int ANYWHERE = 0;
	/** Whether the search term must match the start of a name */
	public static final int STARTS_WITH = 4;
	private static final int ENDS_WITH = 8;
	/** Whether the search term must match a name exactly */
	public static final int EXACT_MATCH = 12;

	private final Collection<Entry> allEntries;

	private final Map<String, List<Entry>> entriesByCategory;

	public NamesDictionary(Collection<Entry> allEntries)
	{
		this.allEntries = allEntries;
		
		this.entriesByCategory = new HashMap<>();
		for (Entry entry : allEntries)
		{
			List<String> categories = findCategories(entry.getTags());
			if(categories.isEmpty()) categories.add(null);
			for (String category : categories)
			{
				putToMultiMap(entriesByCategory, category, entry);
			}
		}
	}

	public List<Entry> getAll() { return new ArrayList<>(allEntries); }

	/** Find entries by a set of tags. The results are returned in this order:
	 * <ol>
	 * <li>by number of matching tags</li>
	 * <li>by current usage of the tag(s) of the entry</li>
	 * </ol>
	 *
	 *  @param tags The tags to search with
	 *  @return a list of dictionary entries that match the given tags. Returns an empty list if
	 *  nothing is found.
	 * */
	public List<Entry> get(Map<String, String> tags)
	{
		List<Entry> result = new ArrayList<>();

		List<String> categories = findCategories(tags);
		categories.add(null); // always search in the "rest" category
		for (String category : categories)
		{
			List<Entry> entriesInCategory = entriesByCategory.get(category);
			if(entriesInCategory != null)
			{
				for (Entry entry : entriesInCategory)
				{
					if (mapContainsAllEntries(tags, entry.getTags().entrySet()))
					{
						result.add(entry);
					}
				}
			}
		}
		
		Collections.sort(result, (a,b) -> {
			// 1. sort matches with more tags before matches with less
			int orderByTagCount = b.getTags().size() - a.getTags().size();
			if(orderByTagCount != 0) return orderByTagCount;
			// 2. sort by count
			return b.getCount() - a.getCount();
		});
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

	/**
	 * Find entries by given term. The results are returned in this order:
	 * <ol>
	 * <li>exact matches with the term</li>
	 * <li>matches where the name starts with the term</li>
	 * <li>matches with the primary name of an entry instead of just a synonym or keyword</li>
	 * <li>by current usage of the tag(s) of the entry</li>
	 * </ol>
	 *
	 * @param term The term (name or keywords) to find the entries for
	 * @param flags Search options: {@link #CASE_INSENSITIVE}, {@link #DIACRITICS_INSENSITIVE}
	 * @return The ordered list of findings. Empty if nothing has been found.
	 */
	public List<Match> find(String term, int flags)
	{
		boolean isCaseInsensitive = (flags & CASE_INSENSITIVE) == CASE_INSENSITIVE;
		boolean isDiacriticsInsensitive = (flags & DIACRITICS_INSENSITIVE) == DIACRITICS_INSENSITIVE;
		boolean isStartsWith = (flags & STARTS_WITH) == STARTS_WITH;
		boolean isEndsWith = (flags & ENDS_WITH) == ENDS_WITH;

		Pattern pattern = createPattern(term, isCaseInsensitive, isDiacriticsInsensitive, isStartsWith, isEndsWith);
		Pattern exactPattern = createPattern(term, isCaseInsensitive, isDiacriticsInsensitive, true, true);

		Comparator<Match> comparator = (a, b) -> {
			// 1. matches with names (not keywords) first
			if((a.getWordIndex() < 0) != (b.getWordIndex() < 0))
				return a.getWordIndex() < 0 ? 1 : -1;
			// 2. those where the search word is in the beginning of the matched word
			if((a.getWordIndex() == 0) != (b.getWordIndex() == 0))
				return a.getWordIndex() == 0 ? -1 : 1;
			// 3. exact matches first
			boolean aIsExactMatch = createMatcher(exactPattern, a.getWord(), isDiacriticsInsensitive).matches();
			boolean bIsExactMatch = createMatcher(exactPattern, b.getWord(), isDiacriticsInsensitive).matches();
			if(aIsExactMatch != bIsExactMatch)
				return aIsExactMatch ? -1 : 1;
			// 4. primary names before synonyms
			if(a.isPrimaryNameMatch() != b.isPrimaryNameMatch())
				return a.isPrimaryNameMatch() ? -1 : 1;
			// 5. sort by count
			return b.getEntry().getCount() - a.getEntry().getCount();
		};

		List<Match> result = new ArrayList<>();
		List<Match> matchesForEntry = new ArrayList<>(16);
		for (Entry entry : allEntries)
		{
			for (String name : entry.getNames())
			{
				Matcher matcher = createMatcher(pattern, name, isDiacriticsInsensitive);
				if(matcher.find())
				{
					int wordIndex = matcher.start();
					matchesForEntry.add(new Match(entry, name, wordIndex));
				}
			}
			// only search in keywords if no name found
			if(matchesForEntry.isEmpty())
			{
				for (String keyword : entry.getKeywords())
				{
					Matcher matcher = createMatcher(pattern, keyword, isDiacriticsInsensitive);
					if(matcher.find())
					{
						matchesForEntry.add(new Match(entry, keyword, -1));
					}
				}
			}
			if(!matchesForEntry.isEmpty())
			{
				Collections.sort(matchesForEntry, comparator);
				result.add(matchesForEntry.get(0));
			}
			matchesForEntry.clear();
		}

		Collections.sort(result, comparator);

		return result;
	}

	/** Like {@link #find(String, int)} with flag {@link #EXACT_MATCH} set. */
	public List<Entry> get(String term)
	{
		List<Entry> result = new ArrayList<>();
		for (Match match : find(term, EXACT_MATCH))
		{
			result.add(match.getEntry());
		}
		return result;
	}

	private static Pattern createPattern(
			String term, boolean isCaseInsensitive, boolean isDiacriticsInsensitive,
			boolean isStartsWith, boolean isEndsWith)
	{
		return Pattern.compile(
				(isStartsWith ? "^" : "") +
				Pattern.quote(isDiacriticsInsensitive ? stripDiacritics(term) : term) +
				(isEndsWith ? "$" : ""),
				isCaseInsensitive ? Pattern.CASE_INSENSITIVE : 0
		);
	}

	private static Matcher createMatcher(Pattern pattern, String str, boolean isDiacriticsInsensitive)
	{
		return pattern.matcher(isDiacriticsInsensitive ? stripDiacritics(str) : str);
	}

	private static String stripDiacritics(String str)
	{
		return FIND_DIACRITICS.matcher(Normalizer.normalize(str, Normalizer.Form.NFD)).replaceAll("");
	}

	private static final Pattern FIND_DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	private static Set<String> CATEGORY_KEYS = new HashSet<String>(Arrays.asList(
			"advertising","aerialway","aeroway","allotments","amenity","area","attraction",
			"barrier","boundary","bridge","building","camp_site","circular","club","craft",
			"embankment","emergency","entrance","ford","golf","healthcare","highway",
			"historic","internet_acess","junction","landuse","leisure","line","man_made",
			"manhole","natural","noexit","office","piste","place","playground","power",
			"public_transport","railway","route","seamark","shop","tourism","traffic_calming",
			"traffic_sign","type","waterway"
	));

	private static List<String> findCategories(Map<String,String> tags)
	{
		List<String> result = new ArrayList<>(2);
		for (String key : tags.keySet())
		{
			if(CATEGORY_KEYS.contains(key)) result.add(key + "/" + tags.get(key));
		}
		return result;
	}

	private static <T> void putToMultiMap(Map<String, List<T>> map, String key, T value)
	{
		if(!map.containsKey(key)) map.put(key, new ArrayList<>());
		map.get(key).add(value);
	}
}
