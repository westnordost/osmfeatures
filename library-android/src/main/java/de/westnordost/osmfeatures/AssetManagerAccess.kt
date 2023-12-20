package de.westnordost.osmfeatures

import android.content.res.AssetManager
import java.io.File

internal class AssetManagerAccess(assetManager: AssetManager, private val basePath: String) : FileAccessAdapter {
    private val assetManager: AssetManager = assetManager

    override fun exists(name: String): Boolean {
        val files: Array<String> = assetManager.list(basePath) ?: return false
        for (file in files) {
            if (file == name) return true
        }
        return false
    }

    override fun open(name: String): okio.Source {
        return assetManager.open(basePath + File.separator + name)
    }
}