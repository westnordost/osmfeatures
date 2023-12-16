package de.westnordost.osmfeatures;

import okio.BufferedSource;
import okio.FileHandle;
import okio.Okio;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static de.westnordost.osmfeatures.TestUtils.listOf;

public class LivePresetDataAccessAdapter implements FileAccessAdapter
{
    @Override public boolean exists(String name)
    {
        return listOf("presets.json", "de.json", "en.json", "en-GB.json").contains(name);
    }

    @Override public okio.Source open(String name) throws IOException
    {
        URL url;
        if(name.equals("presets.json"))
        {
            url = new URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json");
        } else
            {
            url = new URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/translations/"+name);
        }
        return Okio.source(url.openStream());
    }
}
