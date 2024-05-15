package de.westnordost.osmfeatures

import kotlinx.io.IOException
import kotlinx.io.Source

interface ResourceAccessAdapter {
    fun exists(name: String): Boolean

    @Throws(IOException::class)
    fun open(name: String): Source
}