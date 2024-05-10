package de.westnordost.osmfeatures

import android.content.res.AssetManager
import okio.Source
import okio.source
import java.io.File

internal class AssetManagerAccess(
    private val assetManager: AssetManager,
    private val basePath: String
): FileAccessAdapter {

    override fun exists(name: String): Boolean =
        assetManager.list(basePath)?.contains(name) ?: false

    override fun open(name: String): Source =
        assetManager.open(basePath + File.separator + name).source()
}