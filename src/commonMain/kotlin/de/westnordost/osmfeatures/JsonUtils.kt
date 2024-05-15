package de.westnordost.osmfeatures

import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.json.*

// TODO This can hopefully be replaced with a function from kotlinx-serialization soon that streams the data
// monitor https://github.com/Kotlin/kotlinx.serialization/issues/253
@OptIn(ExperimentalStdlibApi::class)
internal inline fun <reified T> Json.decodeFromSource(source: Source): T =
    decodeFromString(source.use { it.readString() })