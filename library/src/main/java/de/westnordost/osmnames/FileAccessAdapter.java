package de.westnordost.osmnames;

import java.io.IOException;
import java.io.InputStream;

public interface FileAccessAdapter
{
	boolean exists(String name) throws IOException;
	InputStream open(String name) throws IOException;
}
