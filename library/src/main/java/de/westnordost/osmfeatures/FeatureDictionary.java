package de.westnordost.osmfeatures;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static de.westnordost.osmfeatures.CollectionUtils.numberOfContainedEntriesInMap;
import static de.westnordost.osmfeatures.CollectionUtils.removeIf;
import static de.westnordost.osmfeatures.CollectionUtils.synchronizedGetOrCreate;

public class FeatureDictionary
{
	private final FeatureCollection featureCollection;

	private final FeatureTermIndex brandNameIndex;
	private final FeatureTagsIndex brandTagsIndex;

	private final Map<List<Locale>, FeatureTagsIndex> tagsIndexes;

	private final Map<List<Locale>, FeatureTermIndex> nameIndexes;
	private final Map<List<Locale>, FeatureTermIndex> termsIndexes;

	FeatureDictionary(FeatureCollection featureCollection)
	{
		this.featureCollection = featureCollection;
		tagsIndexes = new HashMap<>();
		nameIndexes = new HashMap<>();
		termsIndexes = new HashMap<>();
		Collection<Feature> brandFeatures = featureCollection.getAllSuggestions();
		brandNameIndex = new FeatureTermIndex(brandFeatures, feature ->
		{
			if (!feature.isSearchable()) return Collections.emptyList();
			return Collections.singletonList(feature.getCanonicalName());
		});
		brandTagsIndex = new FeatureTagsIndex(brandFeatures);
		// build indices for default locale
		getTagsIndex(Arrays.asList(Locale.getDefault(), null));
		getNameIndex(Arrays.asList(Locale.getDefault()));
		getTermsIndex(Arrays.asList(Locale.getDefault()));
	}

	/** Create a new FeatureDictionary which gets its data from the given directory. */
	public static FeatureDictionary create(String path)
	{
		return new FeatureDictionary(new IDFeatureCollection(new FileSystemAccess(new File(path))));
	}

	/** Find matches by a set of tags */
	public QueryByTagBuilder byTags(Map<String, String> tags)
	{
		return new QueryByTagBuilder(tags);
	}

	private List<Feature> get(Map<String, String> tags, GeometryType geometry, Boolean isSuggestion, List<Locale> locales)
	{
		if(tags.isEmpty()) return Collections.emptyList();

		List<Feature> foundFeatures = new ArrayList<>();

		if (isSuggestion == null || !isSuggestion)
		{
			foundFeatures.addAll(getTagsIndex(locales).getAll(tags));
		}
		if (isSuggestion == null || isSuggestion)
		{
			foundFeatures.addAll(brandTagsIndex.getAll(tags));
		}

		removeIf(foundFeatures, feature -> !(geometry == null || feature.getGeometry().contains(geometry)));

		if (foundFeatures.size() > 1)
		{
			// only return of each category the most specific thing. I.e. will return
			// McDonalds only instead of McDonalds,Fast-Food Restaurant,Amenity
			Set<String> removeIds = new HashSet<>();
			for (Feature feature : foundFeatures)
			{
				removeIds.addAll(getParentCategoryIds(feature.getId()));
			}
			if (!removeIds.isEmpty())
			{
				removeIf(foundFeatures, feature -> removeIds.contains(feature.getId()));
			}
		}

		Collections.sort(foundFeatures, (a, b) ->
		{
			// 1. features with more matching tags first
			int tagOrder = b.getTags().size() - a.getTags().size();
			if(tagOrder != 0) return tagOrder;

			// 2. if search is not limited by locale, return matches not limited by locale first
			if(locales.size() == 1 && locales.get(0) == null)
			{
				int localeOrder =
					(b.getIncludeCountryCodes().isEmpty() && b.getExcludeCountryCodes().isEmpty() ? 1 : 0)
					- (a.getIncludeCountryCodes().isEmpty() && a.getExcludeCountryCodes().isEmpty()? 1 : 0);
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

		return foundFeatures;
	}

	/** Find matches by given search word */
	public QueryByTermBuilder byTerm(String term)
	{
		return new QueryByTermBuilder(term);
	}

	private List<Feature> get(String search, GeometryType geometry, String countryCode, Boolean isSuggestion, int limit, List<Locale> locales)
	{
		String canonicalSearch = StringUtils.canonicalize(search);

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

		List<Feature> result = new ArrayList<>();

		if (isSuggestion == null || !isSuggestion)
		{
			// a. matches with presets first
			List<Feature> foundFeaturesByName = getNameIndex(locales).getAll(canonicalSearch);
			removeIf(foundFeaturesByName, feature -> !isFeatureMatchingParameters(feature, geometry, countryCode));
			Collections.sort(foundFeaturesByName, sortNames);

			result.addAll(foundFeaturesByName);

			// if limit is reached, can return earlier (performance improvement)
			if(limit > 0 && result.size() >= limit) return result.subList(0, Math.min(limit, result.size()));

		}
		if (isSuggestion == null || isSuggestion)
		{
			// b. matches with brand names second
			List<Feature> foundBrandFeatures = brandNameIndex.getAll(canonicalSearch);
			removeIf(foundBrandFeatures, feature -> !isFeatureMatchingParameters(feature, geometry, countryCode));
			Collections.sort(foundBrandFeatures, sortNames);

			result.addAll(foundBrandFeatures);

			// if limit is reached, can return earlier (performance improvement)
			if(limit > 0 && result.size() >= limit) return result.subList(0, Math.min(limit, result.size()));
		}
		if (isSuggestion == null || !isSuggestion)
		{
			// c. matches with terms third
			List<Feature> foundFeaturesByTerm = getTermsIndex(locales).getAll(canonicalSearch);
			removeIf(foundFeaturesByTerm, feature -> !isFeatureMatchingParameters(feature, geometry, countryCode));

			if (!foundFeaturesByTerm.isEmpty())
			{
				final Set<Feature> alreadyFoundFeatures = new HashSet<>(result);
				removeIf(foundFeaturesByTerm, feature -> alreadyFoundFeatures.contains(feature));
			}

			Collections.sort(foundFeaturesByTerm, (a, b) ->
			{
				// 1. features with higher matchScore first
				return (int) (100 * b.getMatchScore() - 100 * a.getMatchScore());
			});
			result.addAll(foundFeaturesByTerm);
		}
		return result.subList(0, Math.min(limit, result.size()));
	}

	/** lazily get or create tags index for given locale(s) */
	private FeatureTagsIndex getTagsIndex(List<Locale> locales)
	{
		return synchronizedGetOrCreate(tagsIndexes, locales, this::createTagsIndex);
	}

	private FeatureTagsIndex createTagsIndex(List<Locale> locales)
	{
		return new FeatureTagsIndex(featureCollection.getAllLocalized(locales));
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

	/** lazily get or create name index for given locale(s) */
	private FeatureTermIndex getNameIndex(List<Locale> locales)
	{
		return synchronizedGetOrCreate(nameIndexes, locales, this::createNameIndex);
	}

	private FeatureTermIndex createNameIndex(List<Locale> locales)
	{
		return new FeatureTermIndex(featureCollection.getAllLocalized(locales), feature -> {
			if (!feature.isSearchable()) return Collections.emptyList();
			return Arrays.asList(feature.getCanonicalName().split(" "));
		});
	}

	/** lazily get or create terms index for given locale(s) */
	private FeatureTermIndex getTermsIndex(List<Locale> locales)
	{
		return synchronizedGetOrCreate(termsIndexes, locales, this::createTermsIndex);
	}

	private FeatureTermIndex createTermsIndex(List<Locale> locales)
	{
		return new FeatureTermIndex(featureCollection.getAllLocalized(locales), feature -> {
			if (!feature.isSearchable()) return Collections.emptyList();
			return feature.getCanonicalTerms();
		});
	}

	private boolean isFeatureMatchingParameters(Feature feature, GeometryType geometry, String countryCode)
	{
		return
			(geometry == null || feature.getGeometry().contains(geometry)) &&
			(
				(
					countryCode != null &&
					feature.getIncludeCountryCodes().contains(countryCode) &&
					!feature.getExcludeCountryCodes().contains(countryCode)
				) || (
					feature.getIncludeCountryCodes().isEmpty() &&
					feature.getExcludeCountryCodes().isEmpty()
				)
			);
	}

	public class QueryByTagBuilder
	{
		private final Map<String, String> tags;
		private GeometryType geometryType = null;
		private Locale[] locale = new Locale[]{Locale.getDefault(), null};
		private Boolean suggestion = null;

		private QueryByTagBuilder(Map<String, String> tags) { this.tags = tags; }

		/** Sets for which geometry type to look. If not set or <tt>null</tt>, any will match. */
		public QueryByTagBuilder forGeometry(GeometryType geometryType)
		{
			this.geometryType = geometryType;
			return this;
		}

		/** <p>Sets the locale(s) in which to present the results.</p>
		 *  <p>You can specify several locales in
		 *  a row to each fall back to if a translation does not exist in the locale before that.
		 *  For example <code>[new Locale("ca", "ES"), new Locale("es","ES")]</code> if you
		 *  wanted results preferredly in Catalan, but Spanish is also fine.</p>
		 *
		 *  <p><code>null</code> means to include unlocalized results.</p>
		 *
		 *  <p>If nothing is specified, it defaults to <code>[Locale.getDefault(), null]</code>,
		 *  i.e. unlocalized results are included by default.</p>
		 *   */
		public QueryByTagBuilder forLocale(Locale... locale)
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
		public List<Feature> find()
		{
			return get(tags, geometryType, suggestion, Arrays.asList(locale));
		}
	}

	public class QueryByTermBuilder
	{
		private final String term;
		private GeometryType geometryType = null;
		private Locale[] locale = new Locale[]{Locale.getDefault()};
		private Boolean suggestion = null;
		private int limit = 50;
		private String countryCode = null;

		private QueryByTermBuilder(String term) { this.term = term; }

		/** Sets for which geometry type to look. If not set or <tt>null</tt>, any will match. */
		public QueryByTermBuilder forGeometry(GeometryType geometryType)
		{
			this.geometryType = geometryType;
			return this;
		}

		/** <p>Sets the locale(s) in which to present the results.</p>
		 *  <p>You can specify several locales in
		 *  a row to each fall back to if a translation does not exist in the locale before that.
		 *  For example <code>[new Locale("ca", "ES"), new Locale("es","ES")]</code> if you
		 *  wanted results preferredly in Catalan, but Spanish is also fine.</p>
		 *
		 *  <p><code>null</code> means to include unlocalized results.</p>
		 *
		 *  <p>If nothing is specified, it defaults to <code>[Locale.getDefault()]</code>, i.e.
		 *  unlocalized results are excluded by default.</p>
		 *   */
		public QueryByTermBuilder forLocale(Locale ...locale)
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

		/** Set whether to only include suggestions (=true) or to not include suggestions (=false).
		 *  Suggestions are brands, like 7-Eleven. */
		public QueryByTermBuilder isSuggestion(Boolean suggestion)
		{
			this.suggestion = suggestion;
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
		public List<Feature> find()
		{
			return get(term, geometryType, countryCode, suggestion, limit, Arrays.asList(locale));
		}
	}

	private static class FileSystemAccess implements IDFeatureCollection.FileAccessAdapter
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
