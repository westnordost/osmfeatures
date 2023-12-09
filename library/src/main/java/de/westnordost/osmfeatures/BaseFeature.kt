package de.westnordost.osmfeatures

/** Data class associated with the Feature interface. Represents a non-localized feature.  */
open class BaseFeature(
        override val id: String, override val tags: Map<String, String>, geometry: List<GeometryType>,
        icon: String?, imageURL: String?, names: List<String>, terms: List<String>?,
        includeCountryCodes: List<String>, excludeCountryCodes: List<String>,
        searchable: Boolean, matchScore: Double, isSuggestion: Boolean,
        addTags: Map<String, String>, removeTags: Map<String, String>): Feature {
        final override var geometry: List<GeometryType>
        final override val icon: String?
        final override val imageURL: String?
        final override val names: List<String>
        final override val terms: List<String>?
        final override val includeCountryCodes: List<String>
        final override val excludeCountryCodes: List<String>
        final override val isSearchable: Boolean
        final override val matchScore: Double
        final override val addTags: Map<String, String>
        final override val removeTags: Map<String, String>
        final override val isSuggestion: Boolean
        final override val canonicalNames: List<String>
        final override var canonicalTerms: List<String>? = null

        init {
                this.geometry = geometry
                this.icon = icon
                this.imageURL = imageURL
                this.names = names
                this.terms = terms
                this.includeCountryCodes = includeCountryCodes
                this.excludeCountryCodes = excludeCountryCodes
                this.isSearchable = searchable
                this.matchScore = matchScore
                this.isSuggestion = isSuggestion
                this.addTags = addTags
                this.removeTags = removeTags
                this.canonicalNames = names.map { name -> StringUtils.canonicalize(name)}
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
