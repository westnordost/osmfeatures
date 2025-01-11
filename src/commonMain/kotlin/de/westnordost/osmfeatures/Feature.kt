package de.westnordost.osmfeatures

/** Subset of a feature as defined in the iD editor
 *  https://github.com/ideditor/schema-builder#preset-schema
 *  with only the fields helpful for the dictionary */
interface Feature {
    /** unique identifier for this feature */
    val id: String
    /** tags a feature must have to match this feature */
    val tags: Map<String, String>
    /** a list of possible geometry types for this feature */
    val geometry: List<GeometryType>
    /** primary name */
    val name: String get() = names[0]
    /** icon representing the feature */
    val icon: String?
    /** url to image representing the feature. Usually used for brands features */
    val imageURL: String?

    /** primary name + aliases  */
    val names: List<String>
    /** Additional search terms or keywords to find this feature */
    val terms: List<String>
    /** A list of ISO 3166-1 2 or 3 letter country codes country codes in which this feature is
     *  available. Empty if it is available everywhere. */
    val includeCountryCodes: List<String>
    /** A list of ISO 3166-1 2 or 3 letter country codes country codes in which this feature is
     *  not available. Empty if it available everywhere. */
    val excludeCountryCodes: List<String>
    /** Whether this feature should be searchable. E.g. deprecated or generic features are not
     *  searchable. */
    val isSearchable: Boolean
    /** A number that ranks this preset against others that match the feature. */
    val matchScore: Float
    /** tags that are added when selecting this feature. This can differ from [tags], as those are
     *  just the minimum tags necessary to match this feature */
    val addTags: Map<String, String>
    /** tags that are removed from the feature when deselecting this feature. */
    val removeTags: Map<String, String>
    /** Regexes for keys of tags which should not be overwritten by [addTags] when selecting a
     *  feature. */
    val preserveTags: List<Regex>
    /** primary names + aliases in all lowercase with stripped diacritics */
    val canonicalNames: List<String>
    /** Additional search terms or keywords in all lowercase with stripped diacritics */
    val canonicalTerms: List<String>

    /** Whether this feature is a brand feature i.e. from the NSI */
    val isSuggestion: Boolean
    /** ISO 639 language code of this feature. `null` if it isn't localized. */
    val language: String?
}
