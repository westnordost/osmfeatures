package de.westnordost.osmfeatures

/** A collection of features grouped by country code  */
interface PerCountryFeatureCollection {
    /** Returns all features with the given country code  */
    fun getAll(countryCodes: List<String?>): Collection<Feature>

    /** Returns the feature with the given id with the given country code or null if it has not been
     * found   */
    operator fun get(id: String, countryCodes: List<String?>): Feature?
}
