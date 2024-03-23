package de.westnordost.osmfeatures

import okio.FileSystem

actual fun fileSystem(): FileSystem {
    return FileSystem.SYSTEM
}