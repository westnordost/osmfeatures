package de.westnordost.osmfeatures

import kotlin.jvm.JvmOverloads

data class Locale @JvmOverloads constructor(
    val language: String,
    val region: String? = null,
    val script: String? = null
) {
        companion object {
            val ENGLISH: Locale = Locale("en")

            val UK: Locale = Locale("en","UK")

            val US: Locale = Locale("en","US")

            val FRENCH: Locale = Locale("fr")

            val ITALIAN: Locale = Locale("it")

            val GERMAN: Locale = Locale("de")

            val GERMANY: Locale = Locale("de", "DE")

            val CHINESE: Locale = Locale("zh")

            val default: Locale? = null
        }

    /** IETF language tag */
    val languageTag: String
        get() = listOfNotNull(language, script, region).joinToString("-")
}