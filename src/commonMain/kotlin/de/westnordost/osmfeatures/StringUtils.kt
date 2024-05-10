package de.westnordost.osmfeatures

import doist.x.normalize.Form
import doist.x.normalize.normalize

private val FIND_DIACRITICS = "\\p{InCombiningDiacriticalMarks}+".toRegex()

internal fun String.canonicalize(): String =
    stripDiacritics().lowercase()

private fun String.stripDiacritics(): String {
    return FIND_DIACRITICS.replace(normalize(Form.NFD),"")
}