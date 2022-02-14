package de.westnordost.osmfeatures;

import android.content.res.AssetManager;

public class AndroidFeatureDictionary
{
	private AndroidFeatureDictionary() {} // cannot be instantiated

	/** Create a new FeatureDictionary which gets its data from the given directory in the app's asset folder. */
	public static FeatureDictionary create(AssetManager assetManager, String presetsBasePath) {
		return create(assetManager, presetsBasePath, null);
	}

	/** Create a new FeatureDictionary which gets its data from the given directory in the app's
	 *  asset folder. Optionally, the path to the brand presets can be specified. */
	public static FeatureDictionary create(AssetManager assetManager, String presetsBasePath, String brandPresetsBasePath) {
		LocalizedFeatureCollection featureCollection =
				new IDLocalizedFeatureCollection(new AssetManagerAccess(assetManager, presetsBasePath));

		PerCountryFeatureCollection brandsFeatureCollection = brandPresetsBasePath != null
				? new IDBrandPresetsFeatureCollection(new AssetManagerAccess(assetManager, brandPresetsBasePath))
				: null;

		return new FeatureDictionary(featureCollection, brandsFeatureCollection);
	}
}
