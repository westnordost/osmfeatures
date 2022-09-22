package de.westnordost.osmfeatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder;
import static de.westnordost.osmfeatures.TestUtils.listOf;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IDBrandPresetsFeatureCollectionTest {

    @Test public void load_brands()
    {
        IDBrandPresetsFeatureCollection c = new IDBrandPresetsFeatureCollection(new FileAccessAdapter()
        {
            @Override public boolean exists(String name)
            {
                return name.equals("presets.json");
            }

            @Override public InputStream open(String name) throws IOException
            {
                if (name.equals("presets.json")) return getStream("brand_presets_min.json");
                throw new IOException("File not found");
            }
        });

        assertEqualsIgnoreOrder(listOf("Duckworths", "Megamall"), getNames(c.getAll(listOf((String)null))));
        assertEquals("Duckworths", c.get("a/brand", listOf((String)null)).getName());
        assertEquals("Megamall", c.get("another/brand", listOf((String)null)).getName());
    }

    @Test public void load_brands_by_country()
    {
        IDBrandPresetsFeatureCollection c = new IDBrandPresetsFeatureCollection(new FileAccessAdapter()
        {
            @Override public boolean exists(String name)
            {
                return name.equals("presets-DE.json");
            }

            @Override public InputStream open(String name) throws IOException
            {
                if (name.equals("presets-DE.json")) return getStream("brand_presets_min2.json");
                throw new IOException("File not found");
            }
        });

        assertEqualsIgnoreOrder(listOf("Talespin"), getNames(c.getAll(listOf("DE"))));
        assertEquals("Talespin", c.get("yet_another/brand", listOf("DE")).getName());
        assertTrue(c.get("yet_another/brand", listOf("DE")).isSuggestion());
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
