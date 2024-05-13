package de.westnordost.osmfeatures

import android.content.res.AssetManager
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.File

internal class AssetManagerAccess(
    private val assetManager: AssetManager,
    private val basePath: String
): ResourceAccessAdapter {

    override fun exists(name: String): Boolean =
        assetManager.list(basePath)?.contains(name) ?: false

    @Throws(IOException::class)
    override fun open(name: String): Source =
        assetManager.open(basePath + File.separator + name).asSource().buffered()
}