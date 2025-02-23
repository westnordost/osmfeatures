package de.westnordost.osmfeatures

/** Data class associated with the Feature interface. Represents a non-localized feature.  */
data class BaseFeature(
    override val id: String,
    override val tags: Map<String, String>,
    override val geometry: List<GeometryType>,
    override val icon: String? = null,
    override val imageURL: String? = null,
    override val names: List<String>,
    override val terms: List<String> = listOf(),
    override val includeCountryCodes: List<String> = listOf(),
    override val excludeCountryCodes: List<String> = listOf(),
    override val isSearchable: Boolean = true,
    override val matchScore: Float = 1f,
    override val isSuggestion: Boolean = false,
    override val addTags: Map<String, String> = tags,
    override val removeTags: Map<String, String> = addTags,
    override val preserveTags: List<Regex> = listOf(),
    override val tagKeys: Set<String> = setOf(),
    override val addTagKeys: Set<String> = tagKeys,
    override val removeTagKeys: Set<String> = addTagKeys
): Feature {
    override val canonicalNames: List<String> = names.map { it.canonicalize() }
    override val canonicalTerms: List<String> = terms.map { it.canonicalize() }

    override val language: String? get() = null
    override fun toString(): String = id
}