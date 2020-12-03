package de.westnordost.osmfeatures;

import android.content.res.AssetManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

class AssetManagerAccess implements IDFeatureCollection.FileAccessAdapter
{
    private final AssetManager assetManager;
    private final String basePath;

    AssetManagerAccess(AssetManager assetManager, String basePath)
    {
        this.assetManager = assetManager;
        this.basePath = basePath;
    }

    @Override public boolean exists(String name) throws IOException
    {
        String[] files = assetManager.list(basePath);
        if(files == null) return false;
        for (String file : files)
        {
            if(file.equals(name)) return true;
        }
        return false;
    }

    @Override public InputStream open(String name) throws IOException
    {
        return assetManager.open(basePath + File.separator + name);
    }
}
