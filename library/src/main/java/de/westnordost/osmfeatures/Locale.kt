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
            fun toTitleString(s: String): String {
                var len: Int
                if (s.length.also { len = it } == 0) {
                    return s
                }
                var idx = 0
                if (!s[idx].isLowerCase()) {
                    idx = 1
                    while (idx < len) {
                        if (s[idx].isUpperCase()) {
                            break
                        }
                        idx++
                    }
                }
                if (idx == len) {
                    return s
                }
                val buf = CharArray(len)
                for (i in 0 until len) {
                    val c = s[i]
                    if (i == 0 && idx == 0) {
                        buf[i] = c.uppercaseChar()
                    } else if (i < idx) {
                        buf[i] = c
                    } else {
                        buf[i] = c.lowercaseChar()
                    }
                }
                return String(buf)
            }

        }



    val country : String
        get() = this.region.orEmpty()

    private var languageTag : String? = null

    constructor(lang: String) : this(lang,"", "")

    constructor(lang: String, region: String) : this(lang, region, "")

    fun toLanguageTag(): String {
        val lTag: String? = this.languageTag
        if (lTag != null) {
            return lTag
        }

        this.languageTag = LanguageTag.forLanguage(language, script, region).toString()
        this.languageTag?.let{ return it}
        throw NullPointerException("LanguageTag could not be parsed")

    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other is Locale) {
            return other.language == this.language && other.region == this.region && other.script == this.script
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