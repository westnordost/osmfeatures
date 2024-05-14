package de.westnordost.osmfeatures

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

internal actual fun defaultLanguage(): String =
    NSLocale.currentLocale.localeIdentifier