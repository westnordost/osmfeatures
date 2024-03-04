package de.westnordost.osmfeatures

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

internal actual fun defaultLocale(): String =
    NSLocale.currentLocale.localeIdentifier