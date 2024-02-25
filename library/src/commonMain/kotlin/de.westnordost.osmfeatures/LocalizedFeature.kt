package de.westnordost.osmfeatures

/** Data class associated with the Feature interface. Represents a localized feature.
 *
 * I.e. the name and terms are specified in the given locale.  */
class LocalizedFeature(
    private val p: BaseFeature,
    override val locale: Locale?,
    override val names: List<String>,
    override val terms: List<String>
) : Feature {
    override val canonicalNames: List<String> = names.map { name -> StringUtils.canonicalize(name) }
    override val canonicalTerms: List<String> = terms.map { term -> StringUtils.canonicalize(term) }

    override val id: String
        get() = p.id
    override val tags: Map<String, String>
        get() = p.tags
    override val geometry: List<GeometryType>
        get() = p.geometry
    override val name: String
        get() = names[0]
    override val icon: String
        get() = p.icon
    override val imageURL: String
        get() = p.imageURL
    override val includeCountryCodes: List<String>
        get() = p.includeCountryCodes
    override val excludeCountryCodes: List<String>
        get() = p.excludeCountryCodes
    override val isSearchable: Boolean
        get() = p.isSearchable
    override val matchScore: Float
        get() = p.matchScore
    override val addTags: Map<String, String>
        get() = p.addTags
    override val removeTags: Map<String, String>
        get() = p.removeTags
    override val isSuggestion: Boolean
        get() = p.isSuggestion

    override fun toString(): String {
        return id
    }
}
