package de.westnordost.osmfeatures

import okio.Source

interface FileAccessAdapter {

    fun exists(name: String): Boolean

    fun open(name: String): Source
}