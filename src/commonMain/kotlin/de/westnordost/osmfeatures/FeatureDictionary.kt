package de.westnordost.osmfeatures

import kotlinx.io.files.FileSystem

class FeatureDictionary internal constructor(
    private val featureCollection: LocalizedFeatureCollection,
    private val brandFeatureCollection: PerCountryFeatureCollection?
) {
    private val brandNamesIndexes = HashMap<List<String?>, Lazy<FeatureTermIndex>>()
    private val brandTagsIndexes = HashMap<List<String?>, Lazy<FeatureTagsIndex>>()

    // locale list -> index
    private val tagsIndexes = HashMap<List<String?>, Lazy<FeatureTagsIndex>>()
    private val namesIndexes = HashMap<List<String?>, Lazy<FeatureTermIndex>>()
    private val termsIndexes = HashMap<List<String?>, Lazy<FeatureTermIndex>>()
    private val tagValuesIndexes = HashMap<List<String?>, Lazy<FeatureTermIndex>>()

    init {
        // build indices for default locale
        getTagsIndex(listOf(defaultLocale(), null))
        getNamesIndex(listOf(defaultLocale()))
        getTermsIndex(listOf(defaultLocale()))
    }

    //region Get by id

    /** Builder to find a feature by id. See [getById] */
    fun byId(id: String) = QueryByIdBuilder(id)

    /**
     *  Returns the feature associated with the given id or `null` if it does not exist
     *
     *  @param id
     *  feature id
     *
     *  @param locales
     *  Optional. List of IETF language tags of languages in which the result should be localized.
     *
     *  Several languages can be specified to each fall back to if a translation does not exist in
     *  the locale before that. For example, specify `listOf("ca-ES","es", null)` if results in
     *  Catalan are preferred, Spanish is also fine or otherwise use unlocalized results (`null`).
     *
     *  Defaults to `listOf(<default system locale>, null)`, i.e. unlocalized results are
     *  included by default. (Brand features are usually not localized.)
     *
     *  @param country
     *  Optional. ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of
     *  the country/state the element is in.
     *  If `null`, will only return matches that are *not* county-specific.
     * */
    fun getById(
        id: String,
        locales: List<String?>? = null,
        country: String? = null
    ): Feature? =
        featureCollection.get(id, locales ?: listOf(defaultLocale(), null))
            ?: brandFeatureCollection?.get(id, dissectCountryCode(country))

    //endregion

    //region Query by tags

    /** Builder to find matches by a set of tags. See [getByTags] */
    fun byTags(tags: Map<String, String>) = QueryByTagBuilder(tags)

    /**
     *  Search for features by a set of tags.
     *
     *  @param tags feature tags
     *
     *  @param geometry
     *  Optional. If not `null`, only returns features that match the given geometry type.
     *
     *  @param locales
     *  Optional. List of IETF language tags of languages in which the result should be localized.
     *
     *  Several languages can be specified to each fall back to if a translation does not exist in
     *  the locale before that. For example, specify `listOf("ca-ES","es", null)` if results in
     *  Catalan are preferred, Spanish is also fine or otherwise use unlocalized results (`null`).
     *
     *  Defaults to `listOf(<default system locale>, null)`, i.e. unlocalized results are
     *  included by default. (Brand features are usually not localized.)
     *
     *  @param country
     *  Optional. ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of
     *  the country/state the element is in.
     *  If `null`, will only return matches that are *not* county-specific.
     *
     *  @param isSuggestion
     *  Optional. `true` to *only* include suggestions, `false` to *not* include suggestions
     *  or `null` to include any in the result.
     *  Suggestions are brands, like 7-Eleven, Santander etc.
     *
     *  @return
     *  A list of dictionary entries that match the given tags or an empty list if nothing is found.
     *
     *  For a set of tags that match a less specific feature and a more specific feature, only the
     *  more specific feature is returned.
     *  E.g. `amenity=doctors` + `healthcare:speciality=cardiology` matches *only* a Cardiologist,
     *  not a Doctor's Office in general.
     *
     *  In rare cases, a set of tags may match multiple primary features, such as for
     *  tag combinations like `shop=deli` + `amenity=cafe`. This somewhat frowned upon tagging
     *  practice is the only reason why this method returns a list.
     * */
    fun getByTags(
        tags: Map<String, String>,
        locales: List<String?>? = null,
        country: String? = null,
        geometry: GeometryType? = null,
        isSuggestion: Boolean? = null
    ): List<Feature> {
        if (tags.isEmpty()) return emptyList()

        val localesOrDefault = locales ?: listOf(defaultLocale(), null)

        val foundFeatures = mutableListOf<Feature>()
        if (isSuggestion == null || !isSuggestion) {
            foundFeatures.addAll(getTagsIndex(localesOrDefault).getAll(tags))
        }
        if (isSuggestion == null || isSuggestion) {
            val countryCodes = dissectCountryCode(country)
            foundFeatures.addAll(getBrandTagsIndex(countryCodes).getAll(tags))
        }
        foundFeatures.removeAll { !it.matches(geometry, country) }

        if (foundFeatures.size > 1) {
            // only return of each category the most specific thing. I.e. will return
            // McDonald's only instead of McDonald's,Fast-Food Restaurant,Amenity
            val removeIds = HashSet<String>()
            for (feature in foundFeatures) {
                removeIds.addAll(getParentCategoryIds(feature.id))
            }
            if (removeIds.isNotEmpty()) {
                foundFeatures.removeAll { it.id in removeIds }
            }
        }
        return foundFeatures.sortedWith(Comparator { a: Feature, b: Feature ->
            // 1. features with more matching tags first
            val tagOrder = b.tags.size - a.tags.size
            if (tagOrder != 0) {
                return@Comparator tagOrder
            }

            // 2. if search is not limited by locale, return matches not limited by locale first
            if (localesOrDefault.size == 1 && localesOrDefault[0] == null) {
                val localeOrder = (
                    (b.includeCountryCodes.isEmpty() && b.excludeCountryCodes.isEmpty()).toInt()
                    - (a.includeCountryCodes.isEmpty() && a.excludeCountryCodes.isEmpty()).toInt()
                )
                if (localeOrder != 0) return@Comparator localeOrder
            }

            // 3. features with more matching tags in addTags first
            // https://github.com/openstreetmap/iD/issues/7927
            val numberOfMatchedAddTags = (
                tags.entries.count { it in b.addTags.entries }
                - tags.entries.count { it in a.addTags.entries }
            )
            if (numberOfMatchedAddTags != 0) return@Comparator numberOfMatchedAddTags

            // 4. features with higher matchScore first
            return@Comparator (100 * b.matchScore - 100 * a.matchScore).toInt()
        })
    }

    //endregion

    //region Query by term

    /** Builder to find matches by given search word. See [getByTerm] */
    fun byTerm(term: String) = QueryByTermBuilder(term)

    /**
     *  Search for features by a search term.
     *
     *  @param search The search term
     *
     *  @param geometry
     *  Optional. If not `null`, only returns features that match the given geometry type.
     *
     *  @param locales
     *  Optional. List of IETF language tags of languages in which the result should be localized.
     *
     *  Several languages can be specified to each fall back to if a translation does not exist in
     *  the locale before that. For example, specify `listOf("ca-ES","es", null)` if results in
     *  Catalan are preferred, Spanish is also fine or otherwise use unlocalized results (`null`).
     *
     *  Defaults to `listOf(<default system locale>, null)`, i.e. unlocalized results are
     *  included by default. (Brand features are usually not localized.)
     *
     *  @param country
     *  Optional. ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of
     *  the country/state the element is in.
     *  If `null`, will only return matches that are *not* county-specific.
     *
     *  @param isSuggestion
     *  Optional. `true` to *only* include suggestions, `false` to *not* include suggestions
     *  or `null` to include any in the result.
     *  Suggestions are brands, like 7-Eleven, Santander etc.
     *
     *  @return
     *  A sequence of dictionary entries that match the search, or an empty sequence list if nothing
     *  is found.
     *
     *  Results are broadly sorted in this order: Matches with names, then with brand names, then
     *  with terms (keywords), then with tag values.
     * */
    fun getByTerm(
        search: String,
        locales: List<String?>? = null,
        country: String? = null,
        geometry: GeometryType? = null,
        isSuggestion: Boolean? = null
    ): Sequence<Feature> {
        val canonicalSearch = search.canonicalize()

        val localesOrDefault = locales ?: listOf(defaultLocale(), null)

        val sortNames = Comparator { a: Feature, b: Feature ->
            // 1. exact matches first
            val exactMatchOrder = (
                (b.names.any { it == search }).toInt()
                - (a.names.any { it == search }).toInt()
            )
            if (exactMatchOrder != 0) return@Comparator exactMatchOrder

            // 2. exact matches case and diacritics insensitive first
            val cExactMatchOrder = (
                (b.canonicalNames.any { it == canonicalSearch }).toInt()
                - (a.canonicalNames.any { it == canonicalSearch }).toInt()
            )
            if (cExactMatchOrder != 0) return@Comparator cExactMatchOrder

            // 3. starts-with matches in string first
            val startsWithOrder = (
                (b.canonicalNames.any { it.startsWith(canonicalSearch) }).toInt()
                - (a.canonicalNames.any { it.startsWith(canonicalSearch) }).toInt()
            )
            if (startsWithOrder != 0) return@Comparator startsWithOrder

            // 4. features with higher matchScore first
            val matchScoreOrder = (100 * b.matchScore - 100 * a.matchScore).toInt()
            if (matchScoreOrder != 0) return@Comparator matchScoreOrder

            // 5. shorter names first
            return@Comparator a.name.length - b.name.length
        }

        val sortMatchScore = Comparator { a: Feature, b: Feature ->
            (100 * b.matchScore - 100 * a.matchScore).toInt()
        }

        return sequence {
            if (isSuggestion == null || !isSuggestion) {
                // a. matches with presets first
                yieldAll(
                    getNamesIndex(localesOrDefault).getAll(canonicalSearch).sortedWith(sortNames)
                )
            }
            if (isSuggestion == null || isSuggestion) {
                // b. matches with brand names second
                val countryCodes = dissectCountryCode(country)
                yieldAll(
                    getBrandNamesIndex(countryCodes).getAll(canonicalSearch).sortedWith(sortNames)
                )
            }
            if (isSuggestion == null || !isSuggestion) {
                // c. matches with terms third
                yieldAll(
                    getTermsIndex(localesOrDefault).getAll(canonicalSearch).sortedWith(sortMatchScore)
                )
            }
            if (isSuggestion == null || !isSuggestion) {
                // d. matches with tag values fourth
                yieldAll(
                    getTagValuesIndex(localesOrDefault).getAll(canonicalSearch).sortedWith(sortMatchScore)
                )
            }
        }
        .distinct()
        .filter { it.matches(geometry, country) }
    }

    //endregion

    //region Lazily get or create Indexes

    /** lazily get or create tags index for given locale(s)  */
    private fun getTagsIndex(locales: List<String?>): FeatureTagsIndex =
        tagsIndexes.getOrPut(locales) { lazy { createTagsIndex(locales) } }.value

    private fun createTagsIndex(locales: List<String?>): FeatureTagsIndex =
        FeatureTagsIndex(featureCollection.getAll(locales))

    /** lazily get or create names index for given locale(s)  */
    private fun getNamesIndex(locales: List<String?>): FeatureTermIndex =
        namesIndexes.getOrPut(locales) { lazy { createNamesIndex(locales) } }.value

    private fun createNamesIndex(locales: List<String?>): FeatureTermIndex =
        FeatureTermIndex(featureCollection.getAll(locales)) { feature ->
            feature.getSearchableNames().toList()
        }

    /** lazily get or create terms index for given locale(s)  */
    private fun getTermsIndex(locales: List<String?>): FeatureTermIndex =
        termsIndexes.getOrPut(locales) { lazy { createTermsIndex(locales) } }.value

    private fun createTermsIndex(locales: List<String?>): FeatureTermIndex =
        FeatureTermIndex(featureCollection.getAll(locales)) { feature ->
            if (!feature.isSearchable) emptyList() else feature.canonicalTerms
        }

    /** lazily get or create tag values index  */
    private fun getTagValuesIndex(locales: List<String?>): FeatureTermIndex =
        tagValuesIndexes.getOrPut(locales) { lazy { createTagValuesIndex(locales) } }.value

    private fun createTagValuesIndex(locales: List<String?>): FeatureTermIndex =
        FeatureTermIndex(featureCollection.getAll(locales)) { feature ->
            if (!feature.isSearchable) {
                emptyList()
            } else {
                feature.tags.values.filter { it != "*" }
            }
        }

    /** lazily get or create brand names index for country  */
    private fun getBrandNamesIndex(countryCodes: List<String?>): FeatureTermIndex =
        brandNamesIndexes.getOrPut(countryCodes) { lazy { createBrandNamesIndex(countryCodes) } }.value

    private fun createBrandNamesIndex(countryCodes: List<String?>): FeatureTermIndex =
        if (brandFeatureCollection == null) {
            FeatureTermIndex(emptyList()) { emptyList() }
        } else {
            FeatureTermIndex(brandFeatureCollection.getAll(countryCodes)) { feature ->
                if (!feature.isSearchable) emptyList() else feature.canonicalNames
            }
        }

    /** lazily get or create tags index for the given countries  */
    private fun getBrandTagsIndex(countryCodes: List<String?>): FeatureTagsIndex =
        brandTagsIndexes.getOrPut(countryCodes) { lazy { createBrandTagsIndex(countryCodes) } }.value

    private fun createBrandTagsIndex(countryCodes: List<String?>): FeatureTagsIndex =
        if (brandFeatureCollection == null) {
            FeatureTagsIndex(emptyList())
        } else {
            FeatureTagsIndex(brandFeatureCollection.getAll(countryCodes))
        }

    //endregion

    //region Query builders

    inner class QueryByIdBuilder(private val id: String) {
        private var locales: List<String?>? = null
        private var country: String? = null

        fun forLocale(vararg locales: String?): QueryByIdBuilder =
            apply { this.locales = locales.toList() }

        fun inCountry(country: String?): QueryByIdBuilder =
            apply { this.country = country }

        fun get(): Feature? = getById(id, locales, country)
    }

    inner class QueryByTagBuilder(private val tags: Map<String, String>) {
        private var geometry: GeometryType? = null
        private var locales: List<String?>? = null
        private var isSuggestion: Boolean? = null
        private var country: String? = null

        fun forGeometry(geometry: GeometryType): QueryByTagBuilder =
            apply { this.geometry = geometry }

        fun forLocale(vararg locales: String?): QueryByTagBuilder =
            apply { this.locales = locales.toList() }

        fun inCountry(country: String?): QueryByTagBuilder =
            apply { this.country = country }

        fun isSuggestion(isSuggestion: Boolean?): QueryByTagBuilder =
            apply { this.isSuggestion = isSuggestion }

        fun find(): List<Feature> = getByTags(tags, locales, country, geometry, isSuggestion)
    }

    inner class QueryByTermBuilder(private val term: String) {
        private var geometry: GeometryType? = null
        private var locales: List<String?>? = null
        private var isSuggestion: Boolean? = null
        private var country: String? = null

        fun forGeometry(geometryType: GeometryType): QueryByTermBuilder =
            apply { this.geometry = geometryType }

        fun forLocale(vararg locales: String?): QueryByTermBuilder =
            apply { this.locales = locales.toList() }

        fun inCountry(countryCode: String?): QueryByTermBuilder =
            apply { this.country = countryCode }

        fun isSuggestion(suggestion: Boolean?): QueryByTermBuilder =
            apply { this.isSuggestion = suggestion }

        fun find(): Sequence<Feature> =
            getByTerm(term, locales, country, geometry, isSuggestion)
    }
    //endregion

    companion object {
        /** Create a new FeatureDictionary which gets its data from the given directory.
         *  Optionally, a path to brand presets can be specified.  */
        fun create(
            fileSystem: FileSystem,
            presetsBasePath: String,
            brandPresetsBasePath: String? = null
        ) = FeatureDictionary(
            featureCollection =
                IDLocalizedFeatureCollection(FileSystemAccess(fileSystem, presetsBasePath)),
            brandFeatureCollection =
                brandPresetsBasePath?.let {
                    IDBrandPresetsFeatureCollection(FileSystemAccess(fileSystem, brandPresetsBasePath))
                }
        )
    }
}

//region Utility / Filter functions

private fun Feature.matches(geometry: GeometryType?, countryCode: String?): Boolean {
    if (geometry != null && !this.geometry.contains(geometry)) return false
    if (includeCountryCodes.isNotEmpty() || excludeCountryCodes.isNotEmpty()) {
        if (countryCode == null) return false
        if (
            includeCountryCodes.isNotEmpty() &&
            !isInCountryCodes(countryCode, includeCountryCodes)
        ) return false
        if (isInCountryCodes(countryCode, excludeCountryCodes)) return false
    }
    return true
}

private fun Feature.getSearchableNames(): Sequence<String> = sequence {
    if (!isSearchable) return@sequence
    yieldAll(canonicalNames)
    for (name in canonicalNames) {
        if (name.contains(" ")) {
            yieldAll(name.replace("[()]", "").split(" "))
        }
    }
}

private fun isInCountryCodes(countryCode: String, countryCodes: List<String>): Boolean =
    countryCode in countryCodes ||
    countryCode.substringBefore('-') in countryCodes

private fun getParentCategoryIds(id: String): Sequence<String> = sequence {
    var lastIndex = id.length
    while (true) {
        lastIndex = id.lastIndexOf('/', lastIndex - 1)
        if (lastIndex == -1) break
        yield(id.substring(0, lastIndex))
    }
}

private fun dissectCountryCode(countryCode: String?): List<String?> = buildList {
    // add default / international
    add(null)
    if (countryCode != null) {
        // add ISO 3166-1 alpha2 (e.g. "US")
        val alpha2 = countryCode.substringBefore('-')
        add(alpha2)
        // add ISO 3166-2 (e.g. "US-NY")
        if (alpha2 != countryCode) add(countryCode)
    }
}

private fun Boolean.toInt(): Int =
    if (this) 1 else 0

//endregion