package de.westnordost.osmfeatures;

import android.content.res.AssetManager;

public class AndroidFeatureDictionary
{
	private AndroidFeatureDictionary() {} // cannot be instantiated

	/** Create a new NamesDictionary which gets it's data from the given directory in the app's asset folder. */
	public static FeatureDictionary create(AssetManager assetManager, String ...basePaths)
	{
		IDFeatureCollection[] collections = new IDFeatureCollection[basePaths.length];
		for (int i = 0; i < basePaths.length; i++) {
			collections[i] = new IDFeatureCollection(new AssetManagerAccess(assetManager, basePaths[i]));
		}
		return new FeatureDictionary(collections);
	}
}
