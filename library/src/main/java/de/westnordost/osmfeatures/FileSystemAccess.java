package de.westnordost.osmfeatures;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class FileSystemAccess implements IDFeatureCollection.FileAccessAdapter
{
    private final File basePath;

    FileSystemAccess(File basePath)
    {
        if(!basePath.isDirectory()) throw new IllegalArgumentException("basePath must be a directory");
        this.basePath = basePath;
    }

    @Override public boolean exists(String name) { return new File(basePath, name).exists(); }
    @Override public InputStream open(String name) throws IOException
    {
        return new FileInputStream(new File(basePath, name));
    }
}
