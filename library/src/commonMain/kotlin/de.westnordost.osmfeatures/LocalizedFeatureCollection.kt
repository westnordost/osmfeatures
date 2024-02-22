package de.westnordost.osmfeatures

/** A localized collection of features  */
interface LocalizedFeatureCollection {
    /** Returns all features in the given locale(s).  */
    fun getAll(locales: List<Locale?>): Collection<Feature>

    /** Returns the feature with the given id in the given locale(s) or null if it has not been
     * found (for the given locale(s))  */
    operator fun get(id: String, locales: List<Locale?>): Feature?
}