package de.westnordost.osmfeatures

import java.util.regex.Pattern

import kotlin.text.Regex

class StringUtils {

    private val FIND_DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")


    companion object {
        @JvmStatic
        fun canonicalize(str: String): String {
            return stripDiacritics(str).lowercase()
        }

        private fun stripDiacritics(str: String): String {
            val reg = Regex("\\p{InCombiningDiacriticalMarks}+")
            return reg.replace("", str)
        }
    }




}