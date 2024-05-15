package de.westnordost.osmfeatures

import java.util.Locale

internal actual fun defaultLanguage(): String =
    Locale.getDefault().toLanguageTag()