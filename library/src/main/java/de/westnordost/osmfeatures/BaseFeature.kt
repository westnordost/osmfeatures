package de.westnordost.osmfeatures

import java.util.*
import java.util.stream.Collectors

/** Data class associated with the Feature interface. Represents a non-localized feature.  */
open class BaseFeature(
        override val id: String, override val tags: Map<String, String>, geometry: List<GeometryType>?,
        icon: String?, imageURL: String?, names: List<String>, terms: List<String>?,
        includeCountryCodes: List<String>, excludeCountryCodes: List<String>,
        searchable: Boolean, matchScore: Double, isSuggestion: Boolean,
        addTags: Map<String, String>, removeTags: Map<String, String>): Feature {
        override val geometry: List<GeometryType>?
        override val icon: String?
        override val imageURL: String?
        override val names: List<String>
        override val terms: List<String>?
        override val includeCountryCodes: List<String>
        override val excludeCountryCodes: List<String>
        override val isSearchable: Boolean
        override val matchScore: Double
        override val addTags: Map<String, String>
        override val removeTags: Map<String, String>
        override val isSuggestion: Boolean
        override val canonicalNames: List<String>
        override var canonicalTerms: List<String>? = null

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
                this.canonicalNames = names.stream().map { name -> StringUtils.canonicalize(name)}.collect(Collectors.toList())
                if (terms != null) {
                        this.canonicalTerms = terms.stream().map { term -> StringUtils.canonicalize(term)}.collect(Collectors.toList())
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
