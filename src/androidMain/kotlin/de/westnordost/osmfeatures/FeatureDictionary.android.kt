package de.westnordost.osmfeatures

import android.content.res.AssetManager

/** Create a new FeatureDictionary which gets its data from the given directory in the app's
 * asset folder. Optionally, the path to the brand presets can be specified.  */
@JvmOverloads
fun FeatureDictionary.Companion.create(
    assetManager: AssetManager,
    presetsBasePath: String,
    brandPresetsBasePath: String? = null
) = FeatureDictionary.create(
    presetsAccess = AssetManagerAccess(assetManager, presetsBasePath),
    brandPresetsAccess = brandPresetsBasePath?.let { AssetManagerAccess(assetManager, it) }
)
