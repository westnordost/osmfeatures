package de.westnordost.osmfeatures

import android.content.res.AssetManager

object AndroidFeatureDictionary {
    /** Create a new FeatureDictionary which gets its data from the given directory in the app's asset folder.  */
    fun create(assetManager: AssetManager, presetsBasePath: String): FeatureDictionary {
        return create(assetManager, presetsBasePath, null)
    }

    /** Create a new FeatureDictionary which gets its data from the given directory in the app's
     * asset folder. Optionally, the path to the brand presets can be specified.  */
    fun create(
        assetManager: AssetManager,
        presetsBasePath: String,
        brandPresetsBasePath: String?
    ): FeatureDictionary {
        val featureCollection: LocalizedFeatureCollection =
            IDLocalizedFeatureCollection(AssetManagerAccess(assetManager, presetsBasePath))

        val brandsFeatureCollection: PerCountryFeatureCollection? = if (brandPresetsBasePath != null
        ) IDBrandPresetsFeatureCollection(AssetManagerAccess(assetManager, brandPresetsBasePath))
        else null

        return FeatureDictionary(featureCollection, brandsFeatureCollection)
    }
}