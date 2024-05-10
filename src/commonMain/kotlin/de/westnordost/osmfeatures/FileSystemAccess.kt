package de.westnordost.osmfeatures

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

class FileSystemAccess(
    private val fileSystem: FileSystem,
    private val basePath: String
) : ResourceAccessAdapter {

    init {
        val metadata = fileSystem.metadataOrNull(Path(basePath))
        require(metadata != null) { "$basePath does not exist" }
        require(metadata.isDirectory) { "$basePath is not a directory" }
    }

    override fun exists(name: String): Boolean =
        fileSystem.exists(Path(basePath, name))

    override fun open(name: String): Source =
        fileSystem.source(Path(basePath, name)).buffered()
}
