package de.westnordost.osmfeatures

import kotlinx.io.Source

interface ResourceAccessAdapter {
    fun exists(name: String): Boolean

    fun open(name: String): Source
}