package de.westnordost.osmfeatures

/** Data class associated with the Feature interface. Represents a non-localized feature.  */
open class BaseFeature(
        override val id: String,
        override val tags: Map<String, String>,
        final override var geometry: List<GeometryType>,
        final override val icon: String?,
        final override val imageURL: String?,
        final override val names: List<String>,
        final override val terms: List<String>?,
        final override val includeCountryCodes: List<String>,
        final override val excludeCountryCodes: List<String>,
        final override val isSearchable: Boolean,
        final override val matchScore: Double,
        final override val isSuggestion: Boolean,
        final override val addTags: Map<String, String>,
        final override val removeTags: Map<String, String>
): Feature {
        final override val canonicalNames: List<String> = names.map { name -> StringUtils.canonicalize(name)}
        final override var canonicalTerms: List<String>? = null

        init {
                if (terms != null) {
                        this.canonicalTerms = terms.map { term -> StringUtils.canonicalize(term)}
                }
        }

        override val name: String
                get() = names[0]
        override val locale: Locale?
                get() = null

        override fun toString(): String {
                return id
        }
}
