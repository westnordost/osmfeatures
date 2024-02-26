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

    val country : String
        get() = this.region.orEmpty()

    val languageTag : String by lazy {
        when {
            region == null && script == null -> language
            region == null -> "${language}-${script}"
            script == null -> "${language}-${region}"
            else -> "${language}-${script}-${region}"
        }
    }

    class Builder {
        private var language: String? = null
        fun setLanguage(language: String) : Builder {
            this.language = language
            return this
        }

        private var region: String? = null

        fun setRegion(region: String?) : Builder {
            this.region = region
            return this
        }

        private var script: String? = null
        fun setScript(script: String?) : Builder {

            this.script = script
            return this
        }

        fun build(): Locale {
            language?.let {
                return Locale(it, region, script)
            }
            throw IllegalArgumentException("Language should not be empty")
        }

    }
}