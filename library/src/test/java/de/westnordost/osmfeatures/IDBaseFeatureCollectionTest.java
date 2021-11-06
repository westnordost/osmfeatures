package de.westnordost.osmfeatures;

import static org.junit.Assert.assertEquals;
import static de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder;
import static de.westnordost.osmfeatures.TestUtils.listOf;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class IDBaseFeatureCollectionTest {

    @Test public void load_brands()
    {
        IDBaseFeatureCollection c = new IDBaseFeatureCollection(new FileAccessAdapter()
        {
            @Override public boolean exists(String name)
            {
                return name.equals("presets.json");
            }

            @Override public InputStream open(String name) throws IOException
            {
                if (name.equals("presets.json")) return getStream("with_brand_presets_min.json");
                throw new IOException("File not found");
            }
        });

        assertEqualsIgnoreOrder(listOf("Duckworths", "Megamall"), getNames(c.getAll(listOf(Locale.ITALIAN))));
        assertEquals("Duckworths", c.get("a/brand", listOf(Locale.ITALIAN)).getName());
        assertEquals("Megamall", c.get("another/brand", listOf(Locale.ITALIAN)).getName());
    }

    private InputStream getStream(String file)
    {
        return getClass().getClassLoader().getResourceAsStream(file);
    }

    private static Collection<String> getNames(Collection<Feature> features)
    {
        List<String> result = new ArrayList<>();
        for (Feature feature : features) {
            result.add(feature.getName());
        }
        return result;
    }
}
