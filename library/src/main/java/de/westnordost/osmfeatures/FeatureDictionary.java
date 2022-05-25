package de.westnordost.osmfeatures;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.westnordost.osmfeatures.CollectionUtils.numberOfContainedEntriesInMap;
import static de.westnordost.osmfeatures.CollectionUtils.removeIf;
import static de.westnordost.osmfeatures.CollectionUtils.synchronizedGetOrCreate;

public class FeatureDictionary
{
	private static final Pattern VALID_COUNTRY_CODE_REGEX =  Pattern.compile("([A-Z]{2})(?:-([A-Z0-9]{1,3}))?");

	private final LocalizedFeatureCollection featureCollection;
	private final PerCountryFeatureCollection brandFeatureCollection;

	private final Map<List<String>, FeatureTermIndex> brandNamesIndexes;
	private final Map<List<String>, FeatureTagsIndex> brandTagsIndexes;

	private final Map<List<Locale>, FeatureTagsIndex> tagsIndexes;
	private final Map<List<Locale>, FeatureTermIndex> namesIndexes;
	private final Map<List<Locale>, FeatureTermIndex> termsIndexes;

	FeatureDictionary(LocalizedFeatureCollection featureCollection, PerCountryFeatureCollection brandFeatureCollection)
	{
		this.featureCollection = featureCollection;
		this.brandFeatureCollection = brandFeatureCollection;

		tagsIndexes = new HashMap<>();
		namesIndexes = new HashMap<>();
		termsIndexes = new HashMap<>();
		brandNamesIndexes = new HashMap<>();
		brandTagsIndexes = new HashMap<>();
		// build indices for default locale
		getTagsIndex(Arrays.asList(Locale.getDefault(), null));
		getNamesIndex(Collections.singletonList(Locale.getDefault()));
		getTermsIndex(Collections.singletonList(Locale.getDefault()));
	}

	/** Create a new FeatureDictionary which gets its data from the given directory. */
	public static FeatureDictionary create(String presetsBasePath)	{
		return create(presetsBasePath, null);
	}

	/** Create a new FeatureDictionary which gets its data from the given directory. Optionally,
	 *  a path to brand presets can be specified. */
	public static FeatureDictionary create(String presetsBasePath, String brandPresetsBasePath) {
		LocalizedFeatureCollection featureCollection =
				new IDLocalizedFeatureCollection(new FileSystemAccess(new File(presetsBasePath)));

		PerCountryFeatureCollection brandsFeatureCollection = brandPresetsBasePath != null
				? new IDBrandPresetsFeatureCollection(new FileSystemAccess(new File(brandPresetsBasePath)))
				: null;

		return new FeatureDictionary(featureCollection, brandsFeatureCollection);
	}

	//region Query by tags

	/** Find matches by a set of tags */
	public QueryByTagBuilder byTags(Map<String, String> tags)
	{
		return new QueryByTagBuilder(tags);
	}

	private List<Feature> get(
			Map<String, String> tags,
			GeometryType geometry,
			String countryCode,
			Boolean isSuggestion,
			List<Locale> locales
	) {
		if(tags.isEmpty()) return Collections.emptyList();

		List<Feature> foundFeatures = new ArrayList<>();

		if (isSuggestion == null || !isSuggestion)
		{
			foundFeatures.addAll(getTagsIndex(locales).getAll(tags));
		}
		if (isSuggestion == null || isSuggestion)
		{
			List<String> countryCodes = dissectCountryCode(countryCode);
			foundFeatures.addAll(getBrandTagsIndex(countryCodes).getAll(tags));
		}

		removeIf(foundFeatures, feature -> !isFeatureMatchingParameters(feature, geometry, countryCode));

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

	//endregion

	//region Query by term

	/** Find matches by given search word */
	public QueryByTermBuilder byTerm(String term)
	{
		return new QueryByTermBuilder(term);
	}

	private List<Feature> get(
			String search,
			GeometryType geometry,
			String countryCode,
			Boolean isSuggestion,
			int limit,
			List<Locale> locales
	) {
		String canonicalSearch = StringUtils.canonicalize(search);

		Comparator<Feature> sortNames = (a, b) ->
		{
			// 1. exact matches first
			int exactMatchOrder =
				(CollectionUtils.find(b.getNames(), n -> n.equals(search)) != null ? 1 : 0)
				- (CollectionUtils.find(a.getNames(), n -> n.equals(search)) != null ? 1 : 0);
			if(exactMatchOrder != 0) return exactMatchOrder;

			// 2. exact matches case and diacritics insensitive first
			int cExactMatchOrder =
					(CollectionUtils.find(b.getCanonicalNames(), n -> n.equals(canonicalSearch)) != null ? 1 : 0)
					- (CollectionUtils.find(a.getCanonicalNames(), n -> n.equals(canonicalSearch)) != null ? 1 : 0);
			if(cExactMatchOrder != 0) return cExactMatchOrder;

			// 3. starts-with matches in string first
			int startsWithOrder =
					(CollectionUtils.find(b.getCanonicalNames(), n -> n.startsWith(canonicalSearch)) != null ? 1 : 0)
					- (CollectionUtils.find(a.getCanonicalNames(), n -> n.startsWith(canonicalSearch)) != null ? 1 : 0);
			if(startsWithOrder != 0) return startsWithOrder;

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
			List<Feature> foundFeaturesByName = getNamesIndex(locales).getAll(canonicalSearch);
			removeIf(foundFeaturesByName, feature -> !isFeatureMatchingParameters(feature, geometry, countryCode));
			Collections.sort(foundFeaturesByName, sortNames);

			result.addAll(foundFeaturesByName);

			// if limit is reached, can return earlier (performance improvement)
			if(limit > 0 && result.size() >= limit) return result.subList(0, Math.min(limit, result.size()));

		}
		if (isSuggestion == null || isSuggestion)
		{
			// b. matches with brand names second
			List<String> countryCodes = dissectCountryCode(countryCode);
			List<Feature> foundBrandFeatures = getBrandNamesIndex(countryCodes).getAll(canonicalSearch);
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

	//endregion

	//region Utility / Filter functions

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

	private static boolean isFeatureMatchingParameters(Feature feature, GeometryType geometry, String countryCode)
	{
		if (geometry != null && !feature.getGeometry().contains(geometry)) return false;
		List<String> include = feature.getIncludeCountryCodes();
		List<String> exclude = feature.getExcludeCountryCodes();
		if (!include.isEmpty() || !exclude.isEmpty())
		{
			if (countryCode == null) return false;
			if (!include.isEmpty() && !matchesAnyCountryCode(countryCode, include)) return false;
			if (matchesAnyCountryCode(countryCode, exclude)) return false;
		}
		return true;
	}

	private static List<String> dissectCountryCode(String countryCode) {
		List<String> result = new ArrayList<>();
		// add default / international
		result.add(null);
		if (countryCode != null) {
			Matcher matcher = VALID_COUNTRY_CODE_REGEX.matcher(countryCode);
			if (matcher.matches()) {
				// add ISO 3166-1 alpha2 (e.g. "US")
				result.add(matcher.group(1));
				if (matcher.groupCount() == 2 && matcher.group(2) != null) {
					// add ISO 3166-2 (e.g. "US-NY")
					result.add(countryCode);
				}
			}
		}
		return result;
	}

	private static boolean matchesAnyCountryCode(String showOnly, List<String> featureCountryCodes)
	{
		for (String featureCountryCode : featureCountryCodes) {
			if (matchesCountryCode(showOnly, featureCountryCode)) return true;
		}
		return false;
	}

	private static boolean matchesCountryCode(String showOnly, String featureCountryCode)
	{
		return showOnly.equals(featureCountryCode)
				// e.g. US-NY is in US
				|| showOnly.substring(0,2).equals(featureCountryCode);
	}

	//endregion

	//region Lazily get or create Indexes

	/** lazily get or create tags index for given locale(s) */
	private FeatureTagsIndex getTagsIndex(List<Locale> locales)
	{
		return synchronizedGetOrCreate(tagsIndexes, locales, this::createTagsIndex);
	}

	private FeatureTagsIndex createTagsIndex(List<Locale> locales)
	{
		return new FeatureTagsIndex(featureCollection.getAll(locales));
	}

	/** lazily get or create names index for given locale(s) */
	private FeatureTermIndex getNamesIndex(List<Locale> locales)
	{
		return synchronizedGetOrCreate(namesIndexes, locales, this::createNamesIndex);
	}

	private FeatureTermIndex createNamesIndex(List<Locale> locales)
	{
		return new FeatureTermIndex(featureCollection.getAll(locales), feature -> {
			if (!feature.isSearchable()) return Collections.emptyList();
			List<String> names = feature.getCanonicalNames();
			List<String> result = new ArrayList<>(names);
			for (String name : names) {
				if (name.contains(" ")) {
					Collections.addAll(result, name.split(" "));
				}
			}
			return result;
		});
	}

	/** lazily get or create terms index for given locale(s) */
	private FeatureTermIndex getTermsIndex(List<Locale> locales)
	{
		return synchronizedGetOrCreate(termsIndexes, locales, this::createTermsIndex);
	}

	private FeatureTermIndex createTermsIndex(List<Locale> locales)
	{
		return new FeatureTermIndex(featureCollection.getAll(locales), feature -> {
			if (!feature.isSearchable()) return Collections.emptyList();
			return feature.getCanonicalTerms();
		});
	}

	/** lazily get or create brand names index for country */
	private FeatureTermIndex getBrandNamesIndex(List<String> countryCodes)
	{
		return synchronizedGetOrCreate(brandNamesIndexes, countryCodes, this::createBrandNamesIndex);
	}

	private FeatureTermIndex createBrandNamesIndex(List<String> countryCodes)
	{
		if (brandFeatureCollection == null) {
			return new FeatureTermIndex(Collections.emptyList(), null);
		}

		return new FeatureTermIndex(brandFeatureCollection.getAll(countryCodes), feature -> {
			if (!feature.isSearchable()) return Collections.emptyList();
			return feature.getCanonicalNames();
		});
	}

	/** lazily get or create tags index for the given countries */
	private FeatureTagsIndex getBrandTagsIndex(List<String> countryCodes)
	{
		return synchronizedGetOrCreate(brandTagsIndexes, countryCodes, this::createBrandTagsIndex);
	}

	private FeatureTagsIndex createBrandTagsIndex(List<String> countryCodes)
	{
		if (brandFeatureCollection == null) {
			return new FeatureTagsIndex(Collections.emptyList());
		}

		return new FeatureTagsIndex(brandFeatureCollection.getAll(countryCodes));
	}

	//endregion

	//region Query builders

	public class QueryByTagBuilder
	{
		private final Map<String, String> tags;
		private GeometryType geometryType = null;
		private Locale[] locale = new Locale[]{Locale.getDefault(), null};
		private Boolean suggestion = null;
		private String countryCode = null;

		private QueryByTagBuilder(Map<String, String> tags) { this.tags = tags; }

		/** Sets for which geometry type to look. If not set or <code>null</code>, any will match. */
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

		/** the ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of the
		 *  country/state the element is in. If not specified, will only return matches that are not
		 *  county-specific. */
		public QueryByTagBuilder inCountry(String countryCode)
		{
			this.countryCode = countryCode;
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
		 *  tag combinations like <code>shop=deli</code> + <code>amenity=cafe</code>, so, this is why
		 *  it is a list. */
		public List<Feature> find()
		{
			return get(tags, geometryType, countryCode, suggestion, Arrays.asList(locale));
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

		/** Sets for which geometry type to look. If not set or <code>null</code>, any will match. */
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

		/** the ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of the
		 *  country/state the element is in. If not specified, will only return matches that are not
		 *  county-specific. */
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

	//endregion
}
