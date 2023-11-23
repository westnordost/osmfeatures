package de.westnordost.osmfeatures

import io.fluidsonic.locale.LanguageTag

class Locale(

        val language: String,
        val region: String = "",
        val script: String = "",
        val variant: String? = null) {
        companion object {


            @JvmField
            val ENGLISH: Locale = Locale("en")
            @JvmField
            val UK: Locale = Locale("en","UK")
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

            const val SEP = "-"
            const val PRIVATEUSE = "x"

            @kotlin.jvm.JvmStatic
            val default: Locale = ENGLISH
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
        get() = this.region

    private var languageTag : String? = null

    constructor(lang: String) : this(lang,"", "", null) {

    }

    constructor(lang: String, region: String) : this(lang, region, "", null) {

    }

    fun toLanguageTag(): String {
        val lTag: String? = this.languageTag
        if (lTag != null) {
            return lTag
        }

        this.languageTag = LanguageTag.forLanguage(language, script, region).toString()
        return this.languageTag!!
//        val buf = StringBuilder()
//
//        var subtag = tag.language
//        if (subtag.isNotEmpty()) {
//            buf.append(subtag.lowercase())
//        }
//
//        subtag = tag.script
//        if (subtag.isNotEmpty()) {
//            buf.append(SEP)
//            buf.append(toTitleString(subtag))
//        }
//
//        subtag = tag.region
//        if (subtag.isNotEmpty()) {
//            buf.append(SEP)
//            buf.append(subtag.uppercase())
//        }
//
//        var subtags = tag.variants
//        for (s in subtags) {
//            buf.append(SEP)
//            // preserve casing
//            buf.append(s)
//        }
//
//        subtags = tag.getExtensions()
//        for (s in subtags) {
//            buf.append(SEP)
//            buf.append(subtag.lowercase())
//        }
//
//        subtag = tag.privateuse
//        if (subtag.isNotEmpty()) {
//            if (buf.isNotEmpty()) {
//                buf.append(SEP)
//            }
//            buf.append(PRIVATEUSE).append(SEP)
//            // preserve casing
//            buf.append(subtag)
//        }
//
//        val langTag = buf.toString()
//        synchronized(this) {
//            if (this.languageTag == null) {
//                this.languageTag = langTag
//            }
//        }
//        return langTag
    }


    class Builder {
        private var language: String? = null
        fun setLanguage(language: String) : Builder {
            this.language = language
            return this
        }

        private var region: String = ""

        fun setRegion(region: String) : Builder {
            this.region = region
            return this
        }

        private var script: String = ""
        fun setScript(script: String) : Builder {
            this.script = script
            return this
        }

        fun build(): Locale {
            if(language == null) {
                throw NullPointerException("Builder language is null")
            }
            return Locale(language!!, region, script)
        }

    }


}