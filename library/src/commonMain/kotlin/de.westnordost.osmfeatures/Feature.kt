package de.westnordost.osmfeatures

interface Feature {
    val id: String
    val tags: Map<String, String>
    val geometry: List<GeometryType>
    val name: String
    val icon: String?
    val imageURL: String?

    /** name + aliases  */
    val names: List<String>
    val terms: List<String>
    val includeCountryCodes: List<String>
    val excludeCountryCodes: List<String>
    val isSearchable: Boolean
    val matchScore: Float
    val addTags: Map<String, String>
    val removeTags: Map<String, String>
    val canonicalNames: List<String>
    val canonicalTerms: List<String>?
    val isSuggestion: Boolean
    val locale: Locale?
}
