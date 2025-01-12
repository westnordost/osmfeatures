package de.westnordost.osmfeatures

data class NsiFeature(
    val parent: Feature,
    override val id: String,
    override val names: List<String>,
    override val tags: Map<String, String>,
    override val preserveTags: List<Regex>,
    override val includeCountryCodes: List<String>,
    override val excludeCountryCodes: List<String>
) : Feature {
    override val geometry: List<GeometryType> get() = parent.geometry
    override val icon: String? get() = parent.icon
    override val imageURL: String? get() = null
    override val terms: List<String> get() = emptyList()
    override val isSearchable: Boolean get() = true
    override val matchScore: Float get() = 2f
    override val addTags: Map<String, String> get() = tags
    override val removeTags: Map<String, String> get() = tags
    override val isSuggestion: Boolean get() = true
    override val language: String? get() = null

    override val canonicalNames: List<String> = names.map { it.canonicalize() }
    override val canonicalTerms: List<String> = terms.map { it.canonicalize() }

    override fun toString(): String = id
}
