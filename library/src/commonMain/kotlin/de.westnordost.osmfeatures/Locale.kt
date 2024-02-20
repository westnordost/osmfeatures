package de.westnordost.osmfeatures

data class Locale(

    val language: String,
    val region: String?,
    val script: String?) {
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

    constructor(lang: String) : this(lang,null, null)

    constructor(lang: String, region: String) : this(lang, region, null)



    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other is Locale) {
            return other.languageTag == this.languageTag
        }
        return false

    }

    override fun hashCode(): Int {
        var result = language.hashCode()
        result = 31 * result + (region?.hashCode() ?: 0)
        result = 31 * result + (script?.hashCode() ?: 0)
        return result
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