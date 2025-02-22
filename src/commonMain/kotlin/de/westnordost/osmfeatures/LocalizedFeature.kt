package de.westnordost.osmfeatures

/** Data class associated with the Feature interface. Represents a localized feature.
 *
 * I.e. the name and terms are specified in the given language.  */
data class LocalizedFeature(
    private val p: BaseFeature,
    override val language: String?,
    override val names: List<String>,
    override val terms: List<String>
) : Feature {
    override val canonicalNames: List<String> = names.map { it.canonicalize() }
    override val canonicalTerms: List<String> = terms.map { it.canonicalize() }

    override val id: String get() = p.id
    override val tags: Map<String, String> get() = p.tags
    override val geometry: List<GeometryType> get() = p.geometry
    override val icon: String? get() = p.icon
    override val imageURL: String? get() = p.imageURL
    override val includeCountryCodes: List<String> get() = p.includeCountryCodes
    override val excludeCountryCodes: List<String> get() = p.excludeCountryCodes
    override val isSearchable: Boolean get() = p.isSearchable
    override val matchScore: Float get() = p.matchScore
    override val addTags: Map<String, String> get() = p.addTags
    override val removeTags: Map<String, String> get() = p.removeTags
    override val preserveTags: List<Regex> get() = p.preserveTags
    override val isSuggestion: Boolean get() = p.isSuggestion
    override val keys: Set<String> get() = p.keys
    override val addKeys: Set<String> get() = p.addKeys
    override val removeKeys: Set<String> get() = p.removeKeys

    override fun toString(): String = id
}
