package de.westnordost.osmfeatures
import okio.FileHandle
import okio.FileSystem
import okio.Path.Companion.toPath

internal class FileSystemAccess(val basePath: String) : FileAccessAdapter {
    private val fs = FileSystem.SYSTEM
    init {
        fs.metadataOrNull(basePath.toPath())?.let { require(it.isDirectory) { "basePath must be a directory" } }
    }

    override fun exists(name: String): Boolean {

        return fs.exists(("$basePath/$name").toPath())
    }
    override fun open(name: String): FileHandle {
        return fs.openReadOnly(("$basePath/$name".toPath()))
    }
}
