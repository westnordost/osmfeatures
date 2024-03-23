package de.westnordost.osmfeatures
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Source

expect fun fileSystem(): FileSystem

class FileSystemAccess(private val basePath: String) : FileAccessAdapter {
    private val fs: FileSystem = fileSystem()
    init {
        fs.metadataOrNull(basePath.toPath())?.let { require(it.isDirectory) { "$basePath is not a directory" } }
    }

    override fun exists(name: String): Boolean {

        return fs.exists(("$basePath/$name").toPath())
    }
    override fun open(name: String): Source {
        return fs.source(("$basePath/$name".toPath()))
    }
}
