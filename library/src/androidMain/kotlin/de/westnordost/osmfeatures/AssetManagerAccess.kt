package de.westnordost.osmfeatures

import android.content.res.AssetManager
import okio.Source
import okio.source
import java.io.File

class AssetManagerAccess(private val assetManager: AssetManager, private val basePath: String): FileAccessAdapter {
    override fun exists(name: String): Boolean {
        val files: Array<String> = assetManager.list(basePath) ?: return false
        return files.contains(name)
    }

    override fun open(name: String): Source {
        return assetManager.open(basePath + File.separator + name).source()
    }
}