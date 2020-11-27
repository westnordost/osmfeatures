package de.westnordost.osmfeatures;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static de.westnordost.osmfeatures.MapEntry.mapOf;
import static de.westnordost.osmfeatures.MapEntry.tag;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class IDPresetsJsonParserTest {

    @Test public void load_features_only()
    {
        List<BaseFeature> features = parse("one_preset_full.json");

        assertEquals(1, features.size());
        Feature feature = features.get(0);
        assertEquals("some/id", feature.getId());
        assertEquals(mapOf(tag("a","b"), tag("c","d")), feature.getTags());
        assertEquals(listOf(GeometryType.POINT, GeometryType.VERTEX, GeometryType.LINE, GeometryType.AREA, GeometryType.RELATION), feature.getGeometry());

        assertEquals(listOf("DE", "GB"), feature.getCountryCodes());
        assertEquals(listOf("IT"), feature.getNotCountryCodes());
        assertEquals("foo", feature.getName());
        assertTrue(feature.isSuggestion());
        assertEquals(listOf("1","2"), feature.getTerms());
        assertEquals(0.5f, feature.getMatchScore(), 0.001f);
        assertFalse(feature.isSearchable());
        assertEquals(mapOf(tag("e","f")), feature.getAddTags());
        assertEquals(mapOf(tag("d","g")), feature.getRemoveTags());
    }

    @Test public void load_features_only_defaults()
    {
        List<BaseFeature> features = parse("one_preset_min.json");

        assertEquals(1, features.size());
        Feature feature = features.get(0);

        assertEquals("some/id", feature.getId());
        assertEquals(mapOf(tag("a","b"), tag("c","d")), feature.getTags());
        assertEquals(listOf(GeometryType.POINT), feature.getGeometry());

        assertTrue(feature.getCountryCodes().isEmpty());
        assertTrue(feature.getNotCountryCodes().isEmpty());
        assertEquals("test",feature.getName());
        assertFalse(feature.isSuggestion());
        assertTrue(feature.getTerms().isEmpty());
        assertEquals(1.0f, feature.getMatchScore(), 0.001f);
        assertTrue(feature.isSearchable());
        assertTrue(feature.getAddTags().isEmpty());
        assertTrue(feature.getRemoveTags().isEmpty());
        assertSame(feature.getAddTags(), feature.getRemoveTags());
    }

    @Test public void load_features_no_wildcards()
    {
        List<BaseFeature> features = parse("one_preset_wildcard.json");
        assertTrue(features.isEmpty());
    }

    @Test public void parse_some_real_data() throws IOException
    {
        URL url = new URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json");
        List<BaseFeature> features = new IDPresetsJsonParser().parse(url.openStream());
        // should not crash etc
        assertTrue(features.size() > 1000);
    }

    private List<BaseFeature> parse(String file)
    {
        try
        {
            return new IDPresetsJsonParser().parse(getStream(file));
        } catch (IOException e)
        {
            throw new RuntimeException();
        }
    }

    private InputStream getStream(String file)
    {
        return getClass().getClassLoader().getResourceAsStream(file);
    }
}
