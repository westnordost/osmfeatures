package de.westnordost.osmfeatures

import de.westnordost.osmfeatures.Locale.Companion.default
import kotlin.math.min
import kotlin.text.Regex

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
    /** Find feature by id  */
    fun byId(id: String): QueryByIdBuilder {
        return QueryByIdBuilder(id)
    }

    private operator fun get(id: String, locales: List<Locale?>, countryCode: String?): Feature? {
        val feature = featureCollection[id, locales]
        if (feature != null) return feature
        val countryCodes = dissectCountryCode(countryCode)
        brandFeatureCollection?.let {
            return brandFeatureCollection[id, countryCodes]
        }
        throw NullPointerException("brandFeatureCollection is null")
    }
    //endregion
    //region Query by tags
    /** Find matches by a set of tags  */
    fun byTags(tags: Map<String, String>): QueryByTagBuilder {
        return QueryByTagBuilder(tags)
    }

    private operator fun get(
        tags: Map<String, String>,
        geometry: GeometryType?,
        countryCode: String?,
        isSuggestion: Boolean?,
        locales: List<Locale?>
    ): List<Feature> {
        if (tags.isEmpty()) return emptyList()
        val foundFeatures: MutableList<Feature> = mutableListOf()
        if (isSuggestion == null || !isSuggestion) {
            foundFeatures.addAll(getTagsIndex(locales)?.getAll(tags).orEmpty())
        }
        if (isSuggestion == null || isSuggestion) {
            val countryCodes = dissectCountryCode(countryCode)
            foundFeatures.addAll(getBrandTagsIndex(countryCodes)?.getAll(tags).orEmpty())
        }
        foundFeatures.removeAll { feature: Feature ->
            !isFeatureMatchingParameters(
                feature,
                geometry,
                countryCode
            )
        }
        if (foundFeatures.size > 1) {
            // only return of each category the most specific thing. I.e. will return
            // McDonalds only instead of McDonalds,Fast-Food Restaurant,Amenity
            val removeIds: MutableSet<String> = HashSet()
            for (feature in foundFeatures) {
                removeIds.addAll(getParentCategoryIds(feature.id))
            }
            if (removeIds.isNotEmpty()) {
                foundFeatures.removeAll { feature: Feature ->
                    removeIds.contains(
                        feature.id
                    )
                }
            }
        }
        return foundFeatures.sortedWith(object : Comparator<Feature> {
            override fun compare(a: Feature, b: Feature): Int {
                // 1. features with more matching tags first
                val tagOrder: Int = b.tags.size - a.tags.size
                if (tagOrder != 0) {
                    return tagOrder
                }

                // 2. if search is not limited by locale, return matches not limited by locale first
                if (locales.size == 1 && locales[0] == null) {
                    val localeOrder =
                        ((if (b.includeCountryCodes.isEmpty() && b.excludeCountryCodes.isEmpty()) 1 else 0)
                                - if (a.includeCountryCodes.isEmpty() && a.excludeCountryCodes
                                .isEmpty()
                        ) 1 else 0)
                    if (localeOrder != 0) return localeOrder
                }

                // 3. features with more matching tags in addTags first
                // https://github.com/openstreetmap/iD/issues/7927
                val numberOfMatchedAddTags =
                    (tags.entries.count { b.addTags.entries.contains(it) }
                            - tags.entries.count { a.addTags.entries.contains(it) })
                if (numberOfMatchedAddTags != 0) return numberOfMatchedAddTags
                return (100 * b.matchScore - 100 * a.matchScore).toInt()
            }
        })
    }
    //endregion
    //region Query by term
    /** Find matches by given search word  */
    fun byTerm(term: String): QueryByTermBuilder {
        return QueryByTermBuilder(term)
    }

    private operator fun get(
        search: String,
        geometry: GeometryType?,
        countryCode: String?,
        isSuggestion: Boolean?,
        limit: Int,
        locales: List<Locale?>
    ): MutableList<Feature> {
        val canonicalSearch = search.canonicalize()
        val sortNames = Comparator { a: Feature, b: Feature ->
            // 1. exact matches first
            val exactMatchOrder =
                ((if (b.names.find { n: String? -> n == search } != null
                ) 1 else 0)
                        - if (a.names.find { n: String? -> n == search } != null
                ) 1 else 0)
            if (exactMatchOrder != 0) return@Comparator exactMatchOrder

            // 2. exact matches case and diacritics insensitive first
            val cExactMatchOrder =
                ((if (b.canonicalNames.find { n: String? -> n == canonicalSearch } != null
                ) 1 else 0)
                        - if (a.canonicalNames.find { n: String? -> n == canonicalSearch } != null
                ) 1 else 0)
            if (cExactMatchOrder != 0) return@Comparator cExactMatchOrder

            // 3. starts-with matches in string first
            val startsWithOrder =
                ((if (b.canonicalNames.find { n: String? ->
                        n!!.startsWith(
                            canonicalSearch
                        )
                    } != null
                ) 1 else 0)
                        - if (a.canonicalNames.find { n: String? ->
                        n!!.startsWith(
                            canonicalSearch
                        )
                    } != null
                ) 1 else 0)
            if (startsWithOrder != 0) return@Comparator startsWithOrder

            // 4. features with higher matchScore first
            val matchScoreOrder: Int = (100 * b.matchScore - 100 * a.matchScore).toInt()
            if (matchScoreOrder != 0) return@Comparator matchScoreOrder
            a.name.length - b.name.length
        }
        val result: MutableList<Feature> = ArrayList()
        if (isSuggestion == null || !isSuggestion) {
            // a. matches with presets first
            val foundFeaturesByName: MutableList<Feature> =
                getNamesIndex(locales)?.getAll(canonicalSearch).orEmpty().toMutableList()
            foundFeaturesByName.removeAll { feature: Feature ->
                !isFeatureMatchingParameters(
                    feature,
                    geometry,
                    countryCode
                )
            }
            result.addAll(foundFeaturesByName.sortedWith(sortNames))

            // if limit is reached, can return earlier (performance improvement)
            if (limit > 0 && result.size >= limit) return result.subList(
                0,
                min(limit.toDouble(), result.size.toDouble()).toInt()
            )
        }
        if (isSuggestion == null || isSuggestion) {
            // b. matches with brand names second
            val countryCodes = dissectCountryCode(countryCode)
            val foundBrandFeatures = getBrandNamesIndex(countryCodes)?.getAll(canonicalSearch).orEmpty().toMutableList()
            foundBrandFeatures.removeAll { feature: Feature ->
                !isFeatureMatchingParameters(
                    feature,
                    geometry,
                    countryCode
                )
            }

            result.addAll(foundBrandFeatures.sortedWith(sortNames))

            // if limit is reached, can return earlier (performance improvement)
            if (limit > 0 && result.size >= limit) return result.subList(
                0,
                min(limit.toDouble(), result.size.toDouble()).toInt()
            )
        }
        if (isSuggestion == null || !isSuggestion) {
            // c. matches with terms third
            val foundFeaturesByTerm = getTermsIndex(locales)?.getAll(canonicalSearch).orEmpty().toMutableList()
            foundFeaturesByTerm.removeAll { feature: Feature ->
                !isFeatureMatchingParameters(
                    feature,
                    geometry,
                    countryCode
                )
            }
            if (foundFeaturesByTerm.isNotEmpty()) {
                val alreadyFoundFeatures: Set<Feature> = HashSet(result)
                foundFeaturesByTerm.removeAll { feature: Feature ->
                    alreadyFoundFeatures.contains(
                        feature
                    )
                }

            }
            result.addAll(foundFeaturesByTerm.sortedWith { a: Feature, b: Feature -> (100 * b.matchScore - 100 * a.matchScore).toInt() })

            // if limit is reached, can return earlier (performance improvement)
            if (limit > 0 && result.size >= limit) return result.subList(
                0,
                min(limit.toDouble(), result.size.toDouble()).toInt()
            )
        }
        if (isSuggestion == null || !isSuggestion) {
            // d. matches with tag values fourth
            val foundFeaturesByTagValue = getTagValuesIndex(locales)?.getAll(canonicalSearch).orEmpty().toMutableList()
            foundFeaturesByTagValue.removeAll { feature: Feature ->
                !isFeatureMatchingParameters(
                    feature,
                    geometry,
                    countryCode
                )
            }
            if (foundFeaturesByTagValue.isNotEmpty()) {
                val alreadyFoundFeatures: Set<Feature> = HashSet(result)
                foundFeaturesByTagValue.removeAll { feature: Feature ->
                    alreadyFoundFeatures.contains(
                        feature
                    )
                }
                result.addAll(foundFeaturesByTagValue)
            }
        }
            return result.subList(0, min(limit.toDouble(), result.size.toDouble()).toInt())

    }
        //endregion
        //region Lazily get or create Indexes
        /** lazily get or create tags index for given locale(s)  */
        private fun getTagsIndex(locales: List<Locale?>): FeatureTagsIndex? {
            return CollectionUtils.synchronizedGetOrCreate(
                tagsIndexes,
                locales,
                ::createTagsIndex
            )
        }

        private fun createTagsIndex(locales: List<Locale?>): FeatureTagsIndex {
            return FeatureTagsIndex(featureCollection.getAll(locales))
        }

        /** lazily get or create names index for given locale(s)  */
        private fun getNamesIndex(locales: List<Locale?>): FeatureTermIndex? {
            return CollectionUtils.synchronizedGetOrCreate(
                namesIndexes,
                locales,
                ::createNamesIndex
            )
        }

        private fun createNamesIndex(locales: List<Locale?>): FeatureTermIndex {
            val features = featureCollection.getAll(locales)
            return FeatureTermIndex(features, FeatureTermIndex.Selector { feature: Feature ->
                if (!feature.isSearchable) return@Selector emptyList<String>()
                val names: List<String> = feature.canonicalNames
                val result = ArrayList(names)
                    for (name in names) {
                        if (name.contains(" ")) {
                            result.addAll(
                                name.replace("[()]", "").split(" ")
                            )
                        }
                    }
                result
            })
        }

        /** lazily get or create terms index for given locale(s)  */
        private fun getTermsIndex(locales: List<Locale?>): FeatureTermIndex? {
            return CollectionUtils.synchronizedGetOrCreate(
                termsIndexes,
                locales,
                ::createTermsIndex
            )
        }

        private fun createTermsIndex(locales: List<Locale?>): FeatureTermIndex {
            return FeatureTermIndex(featureCollection.getAll(locales), FeatureTermIndex.Selector { feature: Feature ->
                if (!feature.isSearchable) return@Selector emptyList<String>()
                feature.canonicalTerms
            })
        }

        /** lazily get or create tag values index  */
        private fun getTagValuesIndex(locales: List<Locale?>): FeatureTermIndex? {
            return CollectionUtils.synchronizedGetOrCreate(
                tagValuesIndexes,
                locales,
                ::createTagValuesIndex
            )
        }

        private fun createTagValuesIndex(locales: List<Locale?>): FeatureTermIndex {
            return FeatureTermIndex(featureCollection.getAll(locales), FeatureTermIndex.Selector { feature: Feature ->
                if (!feature.isSearchable) return@Selector emptyList<String>()

                val result: ArrayList<String> = ArrayList(feature.tags.size)
                for (tagValue in feature.tags.values) {
                    if (tagValue != "*") result.add(tagValue)
                }
                return@Selector result
            })
        }

        /** lazily get or create brand names index for country  */
        private fun getBrandNamesIndex(countryCodes: List<String?>): FeatureTermIndex? {
            return CollectionUtils.synchronizedGetOrCreate(
                brandNamesIndexes,
                countryCodes,
                ::createBrandNamesIndex
            )
        }

        private fun createBrandNamesIndex(countryCodes: List<String?>): FeatureTermIndex {
            return if (brandFeatureCollection == null) {
                FeatureTermIndex(emptyList(), null)
            } else FeatureTermIndex(
                brandFeatureCollection.getAll(countryCodes)
            ) {
                if (!it.isSearchable) emptyList() else it.canonicalNames
            }
        }

        /** lazily get or create tags index for the given countries  */
        private fun getBrandTagsIndex(countryCodes: List<String?>): FeatureTagsIndex? {
            return CollectionUtils.synchronizedGetOrCreate(
                brandTagsIndexes,
                countryCodes,
                ::createBrandTagsIndex
            )
        }

        private fun createBrandTagsIndex(countryCodes: List<String?>): FeatureTagsIndex {
            return if (brandFeatureCollection == null) {
                FeatureTagsIndex(emptyList())
            } else FeatureTagsIndex(brandFeatureCollection.getAll(countryCodes))
        }

        //endregion
        //region Query builders
        inner class QueryByIdBuilder(private val id: String) {
            private var locale: List<Locale?> = listOf(default)
            private var countryCode: String? = null

            private operator fun get(id: String, locales: List<Locale?>, countryCode: String?): Feature? {
                return this@FeatureDictionary[id, locales, countryCode]
            }

            /**
             *
             *Sets the locale(s) in which to present the results.
             *
             * You can specify several locales in
             * a row to each fall back to if a translation does not exist in the locale before that.
             * For example `[new Locale("ca", "ES"), new Locale("es","ES")]` if you
             * wanted results preferredly in Catalan, but Spanish is also fine.
             *
             *
             * `null` means to include unlocalized results.
             *
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

            /** Returns the feature associated with the given id or `null` if it does not
             * exist  */
            fun get(): Feature? {
                return this[id, locale, countryCode]
            }
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
             *
             *Sets the locale(s) in which to present the results.
             *
             * You can specify several locales in
             * a row to each fall back to if a translation does not exist in the locale before that.
             * For example `[new Locale("ca", "ES"), new Locale("es","ES")]` if you
             * wanted results preferredly in Catalan, but Spanish is also fine.
             *
             *
             * `null` means to include unlocalized results.
             *
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
            fun find(): List<Feature> {
                return get(tags, geometryType, countryCode, suggestion, locale)
            }
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
             *
             *Sets the locale(s) in which to present the results.
             *
             * You can specify several locales in
             * a row to each fall back to if a translation does not exist in the locale before that.
             * For example `[new Locale("ca", "ES"), new Locale("es","ES")]` if you
             * wanted results preferredly in Catalan, but Spanish is also fine.
             *
             *
             * `null` means to include unlocalized results.
             *
             *
             * If nothing is specified, it defaults to `[Locale.getDefault()]`, i.e.
             * unlocalized results are excluded by default.
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
            fun find(): List<Feature> {
                return get(term, geometryType, countryCode, suggestion, limit, locale)
            }
        } //endregion

        companion object {
            private val VALID_COUNTRY_CODE_REGEX = Regex("([A-Z]{2})(?:-([A-Z0-9]{1,3}))?")
            /** Create a new FeatureDictionary which gets its data from the given directory. Optionally,
             * a path to brand presets can be specified.  */
            /** Create a new FeatureDictionary which gets its data from the given directory.  */

            fun create(presetsBasePath: String, brandPresetsBasePath: String? = null): FeatureDictionary {
                val featureCollection: LocalizedFeatureCollection =
                    IDLocalizedFeatureCollection(FileSystemAccess(presetsBasePath))
                val brandsFeatureCollection: PerCountryFeatureCollection? =
                    if (brandPresetsBasePath != null) IDBrandPresetsFeatureCollection(
                        FileSystemAccess(brandPresetsBasePath)
                    ) else null
                return FeatureDictionary(featureCollection, brandsFeatureCollection)
            }

            //endregion
            //region Utility / Filter functions
            private fun getParentCategoryIds(id: String): Collection<String> {
                var currentId: String? = id
                val result: MutableList<String> = ArrayList()
                do {
                    currentId = getParentId(currentId)
                    if (currentId != null) result.add(currentId)
                } while (currentId != null)
                return result
            }

            private fun getParentId(id: String?): String? {
                val lastSlashIndex = id!!.lastIndexOf("/")
                return if (lastSlashIndex == -1) null else id.substring(0, lastSlashIndex)
            }

            private fun isFeatureMatchingParameters(
                feature: Feature,
                geometry: GeometryType?,
                countryCode: String?
            ): Boolean {
                if (geometry != null && !feature.geometry.contains(geometry)) return false
                val include: List<String> = feature.includeCountryCodes
                val exclude: List<String> = feature.excludeCountryCodes
                if (include.isNotEmpty() || exclude.isNotEmpty()) {
                    if (countryCode == null) return false
                    if (include.isNotEmpty() && !matchesAnyCountryCode(countryCode, include)) return false
                    if (matchesAnyCountryCode(countryCode, exclude)) return false
                }
                return true
            }

            private fun dissectCountryCode(countryCode: String?): List<String?> {
                val result: MutableList<String?> = ArrayList()
                // add default / international
                result.add(null)
                countryCode?.let {
                    val matcher = VALID_COUNTRY_CODE_REGEX.find(it)
                    if(matcher?.groups?.get(1) != null) {
                        result.add(matcher.groups[1]?.value)

                        // add ISO 3166-1 alpha2 (e.g. "US")
                        if (matcher.groups.size != 2 && matcher.groups[2] != null) {
                            // add ISO 3166-2 (e.g. "US-NY")
                            result.add(countryCode)
                        }
                    }


                }
                return result
            }

            private fun matchesAnyCountryCode(showOnly: String, featureCountryCodes: List<String>): Boolean {
                return featureCountryCodes.any { matchesCountryCode(showOnly, it) }
            }

            private fun matchesCountryCode(showOnly: String, featureCountryCode: String): Boolean {
                return showOnly == featureCountryCode || showOnly.substring(0, 2) == featureCountryCode
            }
        }
    }


