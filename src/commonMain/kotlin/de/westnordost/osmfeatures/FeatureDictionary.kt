package de.westnordost.osmfeatures

import kotlinx.io.files.FileSystem

class FeatureDictionary internal constructor(
    private val featureCollection: LocalizedFeatureCollection,
    private val brandFeatureCollection: PerCountryFeatureCollection?
) {
    private val brandNamesIndexes = HashMap<List<String?>, Lazy<FeatureTermIndex>>()
    private val brandTermsIndexes = HashMap<List<String?>, Lazy<FeatureTermIndex>>()
    private val brandTagsIndexes = HashMap<List<String?>, Lazy<FeatureTagsIndex>>()

    // language list -> index
    private val tagsIndexes = HashMap<List<String?>, Lazy<FeatureTagsIndex>>()
    private val namesIndexes = HashMap<List<String?>, Lazy<FeatureTermIndex>>()
    private val termsIndexes = HashMap<List<String?>, Lazy<FeatureTermIndex>>()
    private val tagValuesIndexes = HashMap<List<String?>, Lazy<FeatureTermIndex>>()

    init {
        // build indices for default language
        getTagsIndex(listOf(defaultLanguage(), null))
        getNamesIndex(listOf(defaultLanguage(), null))
        getTermsIndex(listOf(defaultLanguage(), null))
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
     *  @param languages
     *  Optional. List of IETF language tags of languages in which the result should be localized.
     *
     *  Several languages can be specified to each fall back to if a translation does not exist in
     *  the language before that. For example, specify `listOf("ca-ES","es", null)` if results in
     *  Catalan are preferred, Spanish is also fine or otherwise use unlocalized results (`null`).
     *
     *  Defaults to `listOf(<default system language>, null)`, i.e. unlocalized results are
     *  included by default. (Brand features are usually not localized.)
     *
     *  @param country
     *  Optional. ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of
     *  the country/state the element is in.
     *  If `null`, will only return matches that are *not* country-specific.
     * */
    fun getById(
        id: String,
        languages: List<String?>? = null,
        country: String? = null
    ): Feature? =
        featureCollection.get(id, languages ?: listOf(defaultLanguage(), null))
            ?.takeIf { it.matches(country) }
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
     *  @param languages
     *  Optional. List of IETF language tags of languages in which the result should be localized.
     *
     *  Several languages can be specified to each fall back to if a translation does not exist in
     *  the language before that. For example, specify `listOf("ca-ES","es", null)` if results in
     *  Catalan are preferred, Spanish is also fine or otherwise use unlocalized results (`null`).
     *
     *  Defaults to `listOf(<default system language>, null)`, i.e. unlocalized results are
     *  included by default. (Brand features are usually not localized.)
     *
     *  @param country
     *  Optional. ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of
     *  the country/state the element is in.
     *  If `null`, will only return matches that are *not* country-specific.
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
        languages: List<String?>? = null,
        country: String? = null,
        geometry: GeometryType? = null,
        isSuggestion: Boolean? = null
    ): List<Feature> {
        if (tags.isEmpty()) return emptyList()

        val languagesOrDefault = languages ?: listOf(defaultLanguage(), null)

        val foundFeatures = mutableListOf<Feature>()
        if (isSuggestion == null || !isSuggestion) {
            foundFeatures.addAll(getTagsIndex(languagesOrDefault).getAll(tags))
        }
        if (isSuggestion == null || isSuggestion) {
            val countryCodes = dissectCountryCode(country)
            foundFeatures.addAll(getBrandTagsIndex(countryCodes).getAll(tags))
        }
        foundFeatures.removeAll { !it.matches(geometry) || !it.matches(country) }

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
            val tagOrder = (b.tags.size + b.tagKeys.size) - (a.tags.size + a.tagKeys.size)
            if (tagOrder != 0) {
                return@Comparator tagOrder
            }

            // 2. if search is not limited by language, return matches not limited by language first
            if (languagesOrDefault.size == 1 && languagesOrDefault[0] == null) {
                val languageOrder = (
                    (b.includeCountryCodes.isEmpty() && b.excludeCountryCodes.isEmpty()).toInt()
                    - (a.includeCountryCodes.isEmpty() && a.excludeCountryCodes.isEmpty()).toInt()
                )
                if (languageOrder != 0) return@Comparator languageOrder
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
     *  @param languages
     *  Optional. List of IETF language tags of languages in which the result should be localized.
     *
     *  Several languages can be specified to each fall back to if a translation does not exist in
     *  the language before that. For example, specify `listOf("ca-ES","es", null)` if results in
     *  Catalan are preferred, Spanish is also fine or otherwise use unlocalized results (`null`).
     *
     *  Defaults to `listOf(<default system language>, null)`, i.e. unlocalized results are
     *  included by default. (Brand features are usually not localized.)
     *
     *  @param country
     *  Optional. ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of
     *  the country/state the element is in.
     *  If `null`, will only return matches that are *not* country-specific.
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
        languages: List<String?>? = null,
        country: String? = null,
        geometry: GeometryType? = null,
        isSuggestion: Boolean? = null
    ): Sequence<Feature> {
        val canonicalSearch = search.canonicalize()

        val languagesOrDefault = languages ?: listOf(defaultLanguage(), null)

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
                    getNamesIndex(languagesOrDefault)
                        .getAll(canonicalSearch)
                        .sortedWith(sortNames)
                )
            }
            if (isSuggestion == null || isSuggestion) {
                // b. matches with brand names second
                val countryCodes = dissectCountryCode(country)
                yieldAll(
                    getBrandNamesIndex(countryCodes)
                        .getAll(canonicalSearch)
                        .sortedWith(sortNames)
                )
            }
            if (isSuggestion == null || !isSuggestion) {
                // c. matches with terms third
                yieldAll(
                    getTermsIndex(languagesOrDefault)
                        .getAll(canonicalSearch)
                        .sortedWith(sortMatchScore)
                )
            }
            if (isSuggestion == null || isSuggestion) {
                // d. matches with terms of brands fourth
                val countryCodes = dissectCountryCode(country)
                yieldAll(
                    getBrandTermsIndex(countryCodes)
                        .getAll(canonicalSearch)
                        .sortedWith(sortMatchScore)
                )
            }
            if (isSuggestion == null || !isSuggestion) {
                // e. matches with tag values fifth
                yieldAll(
                    getTagValuesIndex(languagesOrDefault)
                        .getAll(canonicalSearch)
                        .sortedWith(sortMatchScore)
                )
            }
        }
        .distinct()
        .filter { it.matches(geometry) && it.matches(country) }
    }

    //endregion

    //region Lazily get or create Indexes

    /** lazily get or create tags index for given language(s)  */
    private fun getTagsIndex(languages: List<String?>): FeatureTagsIndex =
        tagsIndexes.getOrPut(languages) { lazy { createTagsIndex(languages) } }.value

    private fun createTagsIndex(languages: List<String?>): FeatureTagsIndex =
        FeatureTagsIndex(featureCollection.getAll(languages))

    /** lazily get or create names index for given language(s)  */
    private fun getNamesIndex(languages: List<String?>): FeatureTermIndex =
        namesIndexes.getOrPut(languages) { lazy { createNamesIndex(languages) } }.value

    private fun createNamesIndex(languages: List<String?>): FeatureTermIndex =
        FeatureTermIndex(featureCollection.getAll(languages)) { feature ->
            feature.getSearchableNames().toList()
        }

    /** lazily get or create terms index for given language(s)  */
    private fun getTermsIndex(languages: List<String?>): FeatureTermIndex =
        termsIndexes.getOrPut(languages) { lazy { createTermsIndex(languages) } }.value

    private fun createTermsIndex(languages: List<String?>): FeatureTermIndex =
        FeatureTermIndex(featureCollection.getAll(languages)) { feature ->
            if (!feature.isSearchable) emptyList() else feature.canonicalTerms
        }

    /** lazily get or create tag values index  */
    private fun getTagValuesIndex(languages: List<String?>): FeatureTermIndex =
        tagValuesIndexes.getOrPut(languages) { lazy { createTagValuesIndex(languages) } }.value

    private fun createTagValuesIndex(languages: List<String?>): FeatureTermIndex =
        FeatureTermIndex(featureCollection.getAll(languages)) { feature ->
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

    /** lazily get or create brand terms index for country  */
    private fun getBrandTermsIndex(countryCodes: List<String?>): FeatureTermIndex =
        brandTermsIndexes.getOrPut(countryCodes) { lazy { createBrandTermsIndex(countryCodes) } }.value

    private fun createBrandTermsIndex(countryCodes: List<String?>): FeatureTermIndex =
        if (brandFeatureCollection == null) {
            FeatureTermIndex(emptyList()) { emptyList() }
        } else {
            FeatureTermIndex(brandFeatureCollection.getAll(countryCodes)) { feature ->
                if (!feature.isSearchable) emptyList() else feature.canonicalTerms
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

    inner class QueryByIdBuilder internal constructor(private val id: String) {
        private var languages: List<String?>? = null
        private var country: String? = null

        fun inLanguage(vararg languages: String?): QueryByIdBuilder =
            apply { this.languages = languages.toList() }

        fun inCountry(country: String?): QueryByIdBuilder =
            apply { this.country = country }

        fun get(): Feature? = getById(id, languages, country)
    }

    inner class QueryByTagBuilder internal constructor(private val tags: Map<String, String>) {
        private var geometry: GeometryType? = null
        private var languages: List<String?>? = null
        private var isSuggestion: Boolean? = null
        private var country: String? = null

        fun forGeometry(geometry: GeometryType): QueryByTagBuilder =
            apply { this.geometry = geometry }

        fun inLanguage(vararg languages: String?): QueryByTagBuilder =
            apply { this.languages = languages.toList() }

        fun inCountry(country: String?): QueryByTagBuilder =
            apply { this.country = country }

        fun isSuggestion(isSuggestion: Boolean?): QueryByTagBuilder =
            apply { this.isSuggestion = isSuggestion }

        fun find(): List<Feature> = getByTags(tags, languages, country, geometry, isSuggestion)
    }

    inner class QueryByTermBuilder internal constructor(private val term: String) {
        private var geometry: GeometryType? = null
        private var languages: List<String?>? = null
        private var isSuggestion: Boolean? = null
        private var country: String? = null

        fun forGeometry(geometryType: GeometryType): QueryByTermBuilder =
            apply { this.geometry = geometryType }

        fun inLanguage(vararg languages: String?): QueryByTermBuilder =
            apply { this.languages = languages.toList() }

        fun inCountry(countryCode: String?): QueryByTermBuilder =
            apply { this.country = countryCode }

        fun isSuggestion(suggestion: Boolean?): QueryByTermBuilder =
            apply { this.isSuggestion = suggestion }

        fun find(): Sequence<Feature> =
            getByTerm(term, languages, country, geometry, isSuggestion)
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

private fun Feature.matches(countryCode: String?): Boolean {
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

private fun Feature.matches(geometry: GeometryType?): Boolean =
    !(geometry != null && !this.geometry.contains(geometry))

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