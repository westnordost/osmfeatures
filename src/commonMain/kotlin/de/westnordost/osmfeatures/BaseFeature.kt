package de.westnordost.osmfeatures

/** Data class associated with the Feature interface. Represents a non-localized feature.  */
data class BaseFeature(
    override val id: String,
    override val tags: Map<String, String>,
    override val geometry: List<GeometryType>,
    override val icon: String?,
    override val imageURL: String?,
    override val names: List<String>,
    override val terms: List<String>,
    override val includeCountryCodes: List<String>,
    override val excludeCountryCodes: List<String>,
    override val isSearchable: Boolean,
    override val matchScore: Float,
    override val isSuggestion: Boolean,
    override val addTags: Map<String, String>,
    override val removeTags: Map<String, String>,
    override val preserveTags: List<Regex>,
    override val tagKeys: Set<String>,
    override val addTagKeys: Set<String>,
    override val removeTagKeys: Set<String>
): Feature {
    override val canonicalNames: List<String> = names.map { it.canonicalize() }
    override val canonicalTerms: List<String> = terms.map { it.canonicalize() }

    override val language: String? get() = null
    override fun toString(): String = id
}