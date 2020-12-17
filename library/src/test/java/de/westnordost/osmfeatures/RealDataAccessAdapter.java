package de.westnordost.osmfeatures;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static de.westnordost.osmfeatures.TestUtils.listOf;

public class RealDataAccessAdapter implements IDFeatureCollection.FileAccessAdapter
{
    @Override public boolean exists(String name)
    {
        return listOf("presets.json", "de.json", "en.json", "en-GB.json").contains(name);
    }

    @Override public InputStream open(String name) throws IOException
    {
        if(name.equals("presets.json"))
        {
            return new URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json").openStream();
        } else
            {
            return new URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/translations/"+name).openStream();
        }
    }
}
