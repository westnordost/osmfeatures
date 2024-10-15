package de.westnordost.osmfeatures

import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun <R> useResource(file: String, block: (Source) -> R): R =
    resource(file).use { block(it) }

@Throws(IOException::class)
fun resource(file: String): Source =
    SystemFileSystem.source(Path("src/commonTest/resources", file)).buffered()