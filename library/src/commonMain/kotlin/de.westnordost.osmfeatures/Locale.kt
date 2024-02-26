package de.westnordost.osmfeatures

import kotlin.jvm.JvmOverloads

data class Locale @JvmOverloads constructor(
    val language: String,
    val region: String? = null,
    val script: String? = null
) {
        companion object {
            val default: Locale? = null
        }

    /** IETF language tag */
    val languageTag: String
        get() = listOfNotNull(language, script, region).joinToString("-")
}