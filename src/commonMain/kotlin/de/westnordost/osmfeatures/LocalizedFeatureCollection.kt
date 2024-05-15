package de.westnordost.osmfeatures

/** A localized collection of features  */
internal interface LocalizedFeatureCollection {
    /** Returns all features in the given IETF language tag(s).  */
    fun getAll(languages: List<String?>): Collection<Feature>

    /** Returns the feature with the given id in the given IETF language tag(s) or null if it has
     * not been found (for the given IETF language tag(s))  */
    fun get(id: String, languages: List<String?>): Feature?
}