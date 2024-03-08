package de.westnordost.osmfeatures

import java.util.Locale

internal actual fun defaultLocale(): String = Locale.getDefault().toLanguageTag()