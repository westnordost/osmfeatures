package de.westnordost.osmfeatures;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
            URL url = new URL("https://raw.githubusercontent.com/openstreetmap/iD/develop/dist/locales/"+name);
            String localeString = name.split("\\.")[0];
            try(InputStream is = url.openStream())
            {
                JSONObject localizationJson = new JSONObject(new JSONTokener(is));
                JSONObject featuresJson = localizationJson.getJSONObject(localeString).getJSONObject("presets").getJSONObject("presets");
                String localizationJsonString = "{\"presets\": " + featuresJson.toString() +"}";
                return new ByteArrayInputStream(localizationJsonString.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
