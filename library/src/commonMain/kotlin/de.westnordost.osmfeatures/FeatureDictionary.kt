package de.westnordost.osmfeatures

import de.westnordost.osmfeatures.Locale.Companion.default

class FeatureDictionary internal constructor(
    private val featureCollection: LocalizedFeatureCollection,
    private val brandFeatureCollection: PerCountryFeatureCollection?
) {
    private val brandNamesIndexes: MutableMap<List<String?>, FeatureTermIndex> = HashMap()
    private val brandTagsIndexes: MutableMap<List<String?>, FeatureTagsIndex> = HashMap()
    private val tagsIndexes: MutableMap<List<Locale?>, FeatureTagsIndex> = HashMap()
    private val namesIndexes: MutableMap<List<Locale?>, FeatureTermIndex> = HashMap()
    private val termsIndexes: MutableMap<List<Locale?>, FeatureTermIndex> = HashMap()
    private val tagValuesIndexes: MutableMap<List<Locale?>, FeatureTermIndex> = HashMap()

    init {
        // build indices for default locale
        getTagsIndex(listOf(default, null))
        getNamesIndex(listOf(default))
        getTermsIndex(listOf(default))
    }

    //region Get by id

    /** Find feature by id */
    fun byId(id: String) = QueryByIdBuilder(id)

    private fun getById(
        id: String,
        locales: List<Locale?> = listOf(default),
        countryCode: String? = null
    ): Feature? {
        return featureCollection.get(id, locales)
            ?: brandFeatureCollection?.get(id, dissectCountryCode(countryCode))
    }

    //endregion

    //region Query by tags

    /** Find matches by a set of tags  */
    fun byTags(tags: Map<String, String>) = QueryByTagBuilder(tags)

    private fun getByTags(
        tags: Map<String, String>,
        geometry: GeometryType? = null,
        locales: List<Locale?> = listOf(default),
        countryCode: String? = null,
        isSuggestion: Boolean? = null
    ): List<Feature> {
        if (tags.isEmpty()) return emptyList()

        val foundFeatures: MutableList<Feature> = mutableListOf()
        if (isSuggestion == null || !isSuggestion) {
            foundFeatures.addAll(getTagsIndex(locales).getAll(tags))
        }
        if (isSuggestion == null || isSuggestion) {
            val countryCodes = dissectCountryCode(countryCode)
            foundFeatures.addAll(getBrandTagsIndex(countryCodes).getAll(tags))
        }
        foundFeatures.removeAll { !it.matches(geometry, countryCode) }

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
            if (locales.size == 1 && locales[0] == null) {
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

    /** Find matches by given search word  */
    fun byTerm(term: String) = QueryByTermBuilder(term)

    private fun getByTerm(
        search: String,
        geometry: GeometryType?,
        locales: List<Locale?>,
        countryCode: String?,
        isSuggestion: Boolean?
    ): Sequence<Feature> {
        val canonicalSearch = search.canonicalize()

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
                    getNamesIndex(locales).getAll(canonicalSearch).sortedWith(sortNames)
                )
            }
            if (isSuggestion == null || isSuggestion) {
                // b. matches with brand names second
                val countryCodes = dissectCountryCode(countryCode)
                yieldAll(
                    getBrandNamesIndex(countryCodes).getAll(canonicalSearch).sortedWith(sortNames)
                )
            }
            if (isSuggestion == null || !isSuggestion) {
                // c. matches with terms third
                yieldAll(
                    getTermsIndex(locales).getAll(canonicalSearch).sortedWith(sortMatchScore)
                )
            }
            if (isSuggestion == null || !isSuggestion) {
                // d. matches with tag values fourth
                yieldAll(
                    getTagValuesIndex(locales).getAll(canonicalSearch).sortedWith(sortMatchScore)
                )
            }
        }
        .distinct()
        .filter { it.matches(geometry, countryCode) }
    }

    //endregion

    //region Lazily get or create Indexes

    /** lazily get or create tags index for given locale(s)  */
    private fun getTagsIndex(locales: List<Locale?>): FeatureTagsIndex {
        return tagsIndexes.synchronizedGetOrCreate(locales, ::createTagsIndex)
    }

    private fun createTagsIndex(locales: List<Locale?>): FeatureTagsIndex {
        return FeatureTagsIndex(featureCollection.getAll(locales))
    }

    /** lazily get or create names index for given locale(s)  */
    private fun getNamesIndex(locales: List<Locale?>): FeatureTermIndex =
        namesIndexes.synchronizedGetOrCreate(locales, ::createNamesIndex)

    private fun createNamesIndex(locales: List<Locale?>): FeatureTermIndex {
        val features = featureCollection.getAll(locales)
        return FeatureTermIndex(features) { feature ->
            feature.getSearchableNames().toList()
        }
    }

    /** lazily get or create terms index for given locale(s)  */
    private fun getTermsIndex(locales: List<Locale?>): FeatureTermIndex {
        return termsIndexes.synchronizedGetOrCreate(locales, ::createTermsIndex)
    }

    private fun createTermsIndex(locales: List<Locale?>): FeatureTermIndex {
        return FeatureTermIndex(featureCollection.getAll(locales)) { feature ->
            if (!feature.isSearchable) emptyList() else feature.canonicalTerms
        }
    }

    /** lazily get or create tag values index  */
    private fun getTagValuesIndex(locales: List<Locale?>): FeatureTermIndex {
        return tagValuesIndexes.synchronizedGetOrCreate(locales, ::createTagValuesIndex)
    }

    private fun createTagValuesIndex(locales: List<Locale?>): FeatureTermIndex {
        return FeatureTermIndex(featureCollection.getAll(locales)) { feature ->
            if (!feature.isSearchable) return@FeatureTermIndex emptyList<String>()
            return@FeatureTermIndex feature.tags.values.filter { it != "*" }
        }
    }

    /** lazily get or create brand names index for country  */
    private fun getBrandNamesIndex(countryCodes: List<String?>): FeatureTermIndex {
        return brandNamesIndexes.synchronizedGetOrCreate(countryCodes, ::createBrandNamesIndex)
    }

    private fun createBrandNamesIndex(countryCodes: List<String?>): FeatureTermIndex {
        return if (brandFeatureCollection == null) {
            FeatureTermIndex(emptyList()) { emptyList() }
        } else {
            FeatureTermIndex(brandFeatureCollection.getAll(countryCodes)) { feature ->
                if (!feature.isSearchable) emptyList() else feature.canonicalNames
            }
        }
    }

    /** lazily get or create tags index for the given countries  */
    private fun getBrandTagsIndex(countryCodes: List<String?>): FeatureTagsIndex {
        return brandTagsIndexes.synchronizedGetOrCreate(countryCodes, ::createBrandTagsIndex)
    }

    private fun createBrandTagsIndex(countryCodes: List<String?>): FeatureTagsIndex {
        return if (brandFeatureCollection == null) {
            FeatureTagsIndex(emptyList())
        } else {
            FeatureTagsIndex(brandFeatureCollection.getAll(countryCodes))
        }
    }

    //endregion

    //region Query builders

    inner class QueryByIdBuilder(private val id: String) {
        private var locale: List<Locale?> = listOf(default)
        private var countryCode: String? = null

        /**
         * Sets the locale(s) in which to present the results.
         *
         * You can specify several locales in a row to each fall back to if a translation does not
         * exist in the locale before that. For example
         * `[new Locale("ca", "ES"), new Locale("es","ES")]`
         * if you prefer results in Catalan, but Spanish is also fine.
         *
         * `null` means to include unlocalized results.
         *
         * If nothing is specified, it defaults to `[Locale.getDefault(), null]`,
         * i.e. unlocalized results are included by default.
         */
        fun forLocale(vararg locales: Locale?): QueryByIdBuilder {
            this.locale = locales.toList()
            return this
        }

        /** the ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of the
         * country/state the element is in. If not specified, will only return matches that are not
         * county-specific.  */
        fun inCountry(countryCode: String?): QueryByIdBuilder {
            this.countryCode = countryCode
            return this
        }

        /** Returns the feature associated with the given id or `null` if it does not exist  */
        fun get(): Feature? = getById(id, locale, countryCode)
    }

    inner class QueryByTagBuilder(private val tags: Map<String, String>) {
        private var geometryType: GeometryType? = null
        private var locale: List<Locale?> = listOf(default)
        private var suggestion: Boolean? = null
        private var countryCode: String? = null

        /** Sets for which geometry type to look. If not set or `null`, any will match.  */
        fun forGeometry(geometryType: GeometryType): QueryByTagBuilder {
            this.geometryType = geometryType
            return this
        }

        /**
         * Sets the locale(s) in which to present the results.
         *
         * You can specify several locales in a row to each fall back to if a translation does not
         * exist in the locale before that. For example
         * `[new Locale("ca", "ES"), new Locale("es","ES")]`
         * if you prefer results in Catalan, but Spanish is also fine.
         *
         * `null` means to include unlocalized results.
         *
         * If nothing is specified, it defaults to `[Locale.getDefault(), null]`,
         * i.e. unlocalized results are included by default.
         */
        fun forLocale(vararg locales: Locale?): QueryByTagBuilder {
            this.locale = locales.toList()
            return this
        }

        /** the ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of the
         * country/state the element is in. If not specified, will only return matches that are not
         * county-specific.  */
        fun inCountry(countryCode: String?): QueryByTagBuilder {
            this.countryCode = countryCode
            return this
        }

        /** Set whether to only include suggestions (=true) or to not include suggestions (=false).
         * Suggestions are brands, like 7-Eleven.  */
        fun isSuggestion(suggestion: Boolean?): QueryByTagBuilder {
            this.suggestion = suggestion
            return this
        }

        /** Returns a list of dictionary entries that match or an empty list if nothing is
         * found. <br></br>In rare cases, a set of tags may match multiple primary features, such as for
         * tag combinations like `shop=deli` + `amenity=cafe`, so, this is why
         * it is a list.  */
        fun find(): List<Feature> = getByTags(tags, geometryType, locale, countryCode, suggestion)
    }

    inner class QueryByTermBuilder(private val term: String) {
        private var geometryType: GeometryType? = null
        private var locale: List<Locale?> = listOf(default)
        private var suggestion: Boolean? = null
        private var limit = 50
        private var countryCode: String? = null

        /** Sets for which geometry type to look. If not set or `null`, any will match.  */
        fun forGeometry(geometryType: GeometryType): QueryByTermBuilder {
            this.geometryType = geometryType
            return this
        }

        /**
         * Sets the locale(s) in which to present the results.
         *
         * You can specify several locales in a row to each fall back to if a translation does not
         * exist in the locale before that. For example
         * `[new Locale("ca", "ES"), new Locale("es","ES")]`
         * if you prefer results in Catalan, but Spanish is also fine.
         *
         * `null` means to include unlocalized results.
         *
         * If nothing is specified, it defaults to `[Locale.getDefault(), null]`,
         * i.e. unlocalized results are included by default.
         */
        fun forLocale(vararg locales: Locale?): QueryByTermBuilder {
            this.locale = locales.toList()
            return this
        }

        /** the ISO 3166-1 alpha-2 country code (e.g. "US") or the ISO 3166-2 (e.g. "US-NY") of the
         * country/state the element is in. If not specified, will only return matches that are not
         * county-specific.  */
        fun inCountry(countryCode: String?): QueryByTermBuilder {
            this.countryCode = countryCode
            return this
        }

        /** Set whether to only include suggestions (=true) or to not include suggestions (=false).
         * Suggestions are brands, like 7-Eleven.  */
        fun isSuggestion(suggestion: Boolean?): QueryByTermBuilder {
            this.suggestion = suggestion
            return this
        }

        /** limit how many results to return at most. Default is 50, -1 for unlimited.  */
        fun limit(limit: Int): QueryByTermBuilder {
            this.limit = limit
            return this
        }

        /** Returns a list of dictionary entries that match or an empty list if nothing is
         * found. <br></br>
         * Results are sorted mainly in this order: Matches with names, with brand names, then
         * matches with terms (keywords).  */
        fun find(): List<Feature> =
            getByTerm(term, geometryType, locale, countryCode, suggestion).take(limit).toList()
    }
    //endregion

    companion object {

        /** Create a new FeatureDictionary which gets its data from the given directory. Optionally,
         * a path to brand presets can be specified.  */
        /** Create a new FeatureDictionary which gets its data from the given directory.  */
        fun create(presetsBasePath: String, brandPresetsBasePath: String? = null) =
            FeatureDictionary(
                featureCollection = IDLocalizedFeatureCollection(FileSystemAccess(presetsBasePath)),
                brandFeatureCollection = brandPresetsBasePath?.let {
                    IDBrandPresetsFeatureCollection(FileSystemAccess(brandPresetsBasePath))
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
    countryCode in countryCodes || countryCode.substringBefore('-') in countryCodes.map { it }

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