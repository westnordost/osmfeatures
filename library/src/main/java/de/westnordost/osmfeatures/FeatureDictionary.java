package de.westnordost.osmfeatures;

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

import static de.westnordost.osmfeatures.CollectionUtils.filter;
import static de.westnordost.osmfeatures.CollectionUtils.numberOfContainedEntriesInMap;
import static de.westnordost.osmfeatures.StringUtils.startsWordWith;

public class FeatureDictionary
{
	private final FeatureCollection featureCollection;
	private final Map<Locale, FeatureTagTree> tagsIndices;

	FeatureDictionary(FeatureCollection featureCollection)
	{
		this.featureCollection = featureCollection;
		tagsIndices = new HashMap<>();
	}

	/** Create a new FeatureDictionary which gets its data from the given directory. */
	public static FeatureDictionary create(String path)
	{
		return new FeatureDictionary(new iDFeatureCollection(new FileSystemAccess(new File(path))));
	}

	/** Find matches by a set of tags */
	public QueryByTagBuilder byTags(Map<String, String> tags)
	{
		return new QueryByTagBuilder(tags);
	}

	private List<Match> get(Map<String, String> tags, GeometryType geometry, Boolean isSuggestion, Locale locale)
	{
		if(tags.isEmpty()) return Collections.emptyList();

		List<Feature> foundFeatures = filter(getTagsIndex(locale).getAll(tags), feature ->
				(geometry == null || feature.getGeometry().contains(geometry)) &&
				(isSuggestion == null || feature.isSuggestion() == isSuggestion)
		);

		if (foundFeatures.size() > 1)
		{
			Set<String> removeIds = new HashSet<>();
			for (Feature feature : foundFeatures)
			{
				removeIds.addAll(getParentCategoryIds(feature.getId()));
			}
			if (!removeIds.isEmpty())
			{
				// only return of each category the most specific thing. I.e. will return
				// McDonalds only instead of McDonalds,Fast-Food Restaurant,Amenity
				Iterator<Feature> it = foundFeatures.iterator();
				while (it.hasNext())
				{
					if (removeIds.contains(it.next().getId())) it.remove();
				}
			}

			Collections.sort(foundFeatures, (a, b) ->
			{
				// 1. features with more matching tags first
				int tagOrder = b.getTags().size() - a.getTags().size();
				if(tagOrder != 0) return tagOrder;

				// 2. if search is not limited by locale, return matches not limited by locale first
				if(locale == null)
				{
					int localeOrder = (b.getCountryCodes().isEmpty() ? 1 : 0) - (a.getCountryCodes().isEmpty() ? 1 : 0);
					if (localeOrder != 0) return localeOrder;
				}

				// 3. features with more matching tags in addTags first
				// https://github.com/openstreetmap/iD/issues/7927
				int numberOfMatchedAddTags =
						numberOfContainedEntriesInMap(b.getAddTags(), tags.entrySet())
						- numberOfContainedEntriesInMap(a.getAddTags(), tags.entrySet());
				if(numberOfMatchedAddTags != 0) return numberOfMatchedAddTags;

				// 4. features with higher matchScore first
				return (int) (100 * b.getMatchScore() - 100 * a.getMatchScore());
			});
		}

		return createMatches(foundFeatures, -1, locale);
	}

	private FeatureTagTree getTagsIndex(Locale locale)
	{
		if (!tagsIndices.containsKey(locale))
		{
			synchronized (tagsIndices)
			{
				if (!tagsIndices.containsKey(locale))
				{
					tagsIndices.put(locale, new FeatureTagTree(featureCollection.getAll(locale)));
				}
			}
		}
		return tagsIndices.get(locale);
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

	/** Find matches by given search word */
	public QueryByTermBuilder byTerm(String term)
	{
		return new QueryByTermBuilder(term);
	}

	private List<Match> get(String search, GeometryType geometry, String countryCode, int limit, Locale locale)
	{
		// TODO: possible performance improvement: Map of name -> feature for fast exact-matches
		String canonicalSearch = StringUtils.canonicalize(search);

		List<Feature> searchable = filter(featureCollection.getAll(locale), feature ->
			feature.isSearchable() &&
			(geometry == null || feature.getGeometry().contains(geometry)) &&
			(feature.getCountryCodes().isEmpty() || (countryCode != null && feature.getCountryCodes().contains(countryCode)))
		);

		Comparator<Feature> sortNames = (a, b) ->
		{
			// 1. exact matches first
			int exactMatchOrder = (b.getName().equals(search)?1:0) - (a.getName().equals(search)?1:0);
			if(exactMatchOrder != 0) return exactMatchOrder;
			// 2. exact matches case and diacritics insensitive first
			int cExactMatchOrder = (b.getCanonicalName().equals(canonicalSearch)?1:0) - (a.getCanonicalName().equals(canonicalSearch)?1:0);
			if(cExactMatchOrder != 0) return cExactMatchOrder;
			// 3. earlier matches in string first
			int indexOfOrder = a.getCanonicalName().indexOf(canonicalSearch) - b.getCanonicalName().indexOf(canonicalSearch);
			if(indexOfOrder != 0) return indexOfOrder;
			// 4. features with higher matchScore first
			int matchScoreOrder = (int) (100 * b.getMatchScore() - 100 * a.getMatchScore());
			if(matchScoreOrder != 0) return matchScoreOrder;
			// 5. shorter names first
			return a.getName().length() - b.getName().length();
		};

		Comparator<Feature> sortTerms = (a, b) ->
		{
			// 3. features with higher matchScore first
			return (int) (100 * b.getMatchScore() - 100 * a.getMatchScore());
		};

		Set<Feature> foundFeatures = new HashSet<>();
		List<Feature> result = new ArrayList<>();

		List<Feature> nameMatches = filter(searchable, feature ->
				!feature.isSuggestion() && startsWordWith(feature.getCanonicalName(), canonicalSearch)
		);
		Collections.sort(nameMatches, sortNames);
		result.addAll(nameMatches);
		foundFeatures.addAll(nameMatches);

		// if limit is reached, can return earlier (performance improvement)
		if(limit > 0 && result.size() >= limit) return createMatches(result, limit, locale);

		List<Feature> brandNameMatches = filter(searchable, feature ->
				feature.isSuggestion() && feature.getCanonicalName().startsWith(canonicalSearch)
		);
		Collections.sort(brandNameMatches, sortNames);
		result.addAll(brandNameMatches);
		foundFeatures.addAll(brandNameMatches);

		// if limit is reached, can return earlier (performance improvement)
		if(limit > 0 && result.size() >= limit) return createMatches(result, limit, locale);

		List<Feature> termsMatches = filter(searchable, feature ->
		{
			if(foundFeatures.contains(feature)) return false;
			for (String s : feature.getCanonicalTerms())
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

	private List<Match> createMatches(List<Feature> featuresList, int limit, Locale locale)
	{
		int size = limit > 0 ? Math.min(featuresList.size(), limit) : featuresList.size();
		List<Match> result = new ArrayList<>(size);
		for (Feature feature : featuresList)
		{
			result.add(createMatch(feature, locale));
			if(--limit == 0) return result;
		}
		return result;
	}

	private Match createMatch(Feature feature, Locale locale)
	{
		String name = feature.getName();
		Map<String, String> tags = new HashMap<>(feature.getTags());
		tags.putAll(feature.getAddTags());
		String parentName = null;
		if(feature.isSuggestion())
		{
			String parentId = feature.getParentId();
			if(parentId != null)
			{
				Feature parentFeature = featureCollection.get(parentId, locale);
				if(parentFeature != null)
				{
					parentName = parentFeature.getName();
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
		private Boolean suggestion = null;

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

		/** Set whether to only include suggestions (=true) or to not include suggestions (=false).
		 *  Suggestions are brands, like 7-Eleven. */
		public QueryByTagBuilder isSuggestion(Boolean suggestion)
		{
			this.suggestion = suggestion;
			return this;
		}

		/** Returns a list of dictionary entries that match or an empty list if nothing is
		 *  found. <br>In rare cases, a set of tags may match multiple primary features, such as for
		 *  tag combinations like <tt>shop=deli</tt> + <tt>amenity=cafe</tt>, so, this is why
		 *  it is a list. */
		public List<Match> find()
		{
			return get(tags, geometryType, suggestion, locale);
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

	private static class FileSystemAccess implements iDFeatureCollection.FileAccessAdapter
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
