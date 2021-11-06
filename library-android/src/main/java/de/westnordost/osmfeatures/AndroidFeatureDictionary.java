package de.westnordost.osmfeatures;

import android.content.res.AssetManager;

import java.util.Arrays;

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
		FeatureCollection featureCollection =
				new IDFeatureCollection(new AssetManagerAccess(assetManager, presetsBasePath));

		FeatureCollection brandsFeatureCollection = brandPresetsBasePath != null
				? new IDBaseFeatureCollection(new AssetManagerAccess(assetManager, brandPresetsBasePath))
				: null;

		return new FeatureDictionary(Arrays.asList(featureCollection), Arrays.asList(brandsFeatureCollection));
	}
}
