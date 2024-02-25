package de.westnordost.osmfeatures

import doist.x.normalize.Form
import doist.x.normalize.normalize

class StringUtils {

    companion object {
        private val FIND_DIACRITICS = "\\p{InCombiningDiacriticalMarks}+".toRegex()

        fun canonicalize(str: String): String {
            return stripDiacritics(str).lowercase()
        }

        private fun stripDiacritics(str: String): String {
            return FIND_DIACRITICS.replace(str.normalize(Form.NFD),"")
        }
    }
}