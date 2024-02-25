package de.westnordost.osmfeatures

/** Subset of a feature as defined in the iD editor
 *  https://github.com/ideditor/schema-builder#preset-schema
 *  with only the fields helpful for the dictionary */
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
