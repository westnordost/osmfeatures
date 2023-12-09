package de.westnordost.osmfeatures;

import java.io.IOException;
import java.io.InputStream;

interface FileAccessAdapter
{
    boolean exists(String name) throws IOException;
    InputStream open(String name) throws IOException;
}