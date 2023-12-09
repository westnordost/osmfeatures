package de.westnordost.osmfeatures

import okio.FileHandle

internal interface FileAccessAdapter {

    fun exists(name: String): Boolean
    fun open(name: String): FileHandle
}