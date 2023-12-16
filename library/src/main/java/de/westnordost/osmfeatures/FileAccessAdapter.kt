package de.westnordost.osmfeatures

import okio.FileHandle
import okio.IOException
import okio.Source
import kotlin.jvm.Throws

interface FileAccessAdapter {

    fun exists(name: String): Boolean
    @Throws(IOException::class)
    fun open(name: String): Source
}