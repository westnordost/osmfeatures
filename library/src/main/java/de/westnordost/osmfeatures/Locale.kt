package de.westnordost.osmfeatures

import io.fluidsonic.locale.LanguageTag

class Locale(

    val language: String,
    private val region: String?,
    val script: String?) {
        companion object {


            @JvmField
            val ENGLISH: Locale = Locale("en")
            @JvmField
            val UK: Locale = Locale("en","UK")
            @JvmField
            val US: Locale = Locale("en","US")
            @JvmField
            val FRENCH: Locale = Locale("fr")
            @JvmField
            val ITALIAN: Locale = Locale("it")
            @JvmField
            val GERMAN: Locale = Locale("de")
            @JvmField
            val GERMANY: Locale = Locale("de", "DE")
            @JvmField
            val CHINESE: Locale = Locale("zh")

            @JvmStatic
            val default: Locale? = null

        }



    val country : String
        get() = this.region.orEmpty()

    val languageTag : String? by lazy {
        LanguageTag.forLanguage(language, script, region).toString()
    }

    constructor(lang: String) : this(lang,"", "")

    constructor(lang: String, region: String) : this(lang, region, "")



    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other is Locale) {
            return other.languageTag == this.languageTag
        }
        return false

    }


    class Builder {
        private var language: String? = null
        fun setLanguage(language: String) : Builder {
            this.language = language
            return this
        }

        private var region: String? = null

        fun setRegion(region: String?) : Builder {
            this.region = region.orEmpty()
            return this
        }

        private var script: String? = null
        fun setScript(script: String?) : Builder {

            this.script = script.orEmpty()
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