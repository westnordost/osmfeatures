package de.westnordost.osmfeatures

import de.westnordost.osmfeatures.Locale

/** Data class associated with the Feature interface. Represents a localized feature.
 *
 * I.e. the name and terms are specified in the given locale.  */
class LocalizedFeature(
    private val p: BaseFeature,
    override val locale: Locale?,
    override val names: List<String>,
    override val terms: List<String>
) :
    Feature {
    override val canonicalNames: List<String>
    override val canonicalTerms: List<String>

    init {
        val canonicalNames: MutableList<String> = ArrayList(names.size)
        for (name in names) {
            canonicalNames.add(StringUtils.canonicalize(name))
        }
        this.canonicalNames = canonicalNames.toList()
        val canonicalTerms: MutableList<String> = ArrayList(terms.size)
        for (term in terms) {
            canonicalTerms.add(StringUtils.canonicalize(term))
        }
        this.canonicalTerms = canonicalTerms.toList()
    }

    override val id: String
        get() = p.id
    override val tags: Map<String, String>
        get() = p.tags
    override val geometry: List<GeometryType>
        get() = p.geometry
    override val name: String
        get() = names[0]
    override val icon: String?
        get() = p.icon
    override val imageURL: String?
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
