package de.westnordost.osmfeatures;

import okio.Okio;
import okio.Source;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static de.westnordost.osmfeatures.MapEntry.mapOf;
import static de.westnordost.osmfeatures.MapEntry.tag;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

        assertEquals(listOf("DE", "GB"), feature.getIncludeCountryCodes());
        assertEquals(listOf("IT"), feature.getExcludeCountryCodes());
        assertEquals("foo", feature.getName());
        assertEquals("abc", feature.getIcon());
        assertEquals("someurl", feature.getImageURL());
        assertEquals(listOf("foo", "one","two"), feature.getNames());
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

        assertTrue(feature.getIncludeCountryCodes().isEmpty());
        assertTrue(feature.getExcludeCountryCodes().isEmpty());
        assertEquals("", feature.getName());
        assertEquals("",feature.getIcon());
        assertEquals("",feature.getImageURL());
        assertEquals(1, feature.getNames().size());
        assertTrue(feature.getTerms().isEmpty());
        assertEquals(1.0f, feature.getMatchScore(), 0.001f);
        assertTrue(feature.isSearchable());
        assertEquals(feature.getAddTags(), feature.getTags());
        assertEquals(feature.getAddTags(), feature.getRemoveTags());
    }

    @Test public void load_features_unsupported_location_set()
    {
        List<BaseFeature> features = parse("one_preset_unsupported_location_set.json");
        assertEquals(2, features.size());
        assertEquals("some/ok", features.get(0).getId());
        assertEquals("another/ok", features.get(1).getId());
    }

    @Test public void load_features_no_wildcards()
    {
        List<BaseFeature> features = parse("one_preset_wildcard.json");
        assertTrue(features.isEmpty());
    }

    @Test public void parse_some_real_data() throws IOException
    {
        URL url = new URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json");

        List<BaseFeature> features = new IDPresetsJsonParser().parse(Okio.source(url.openStream()));
        // should not crash etc
        assertTrue(features.size() > 1000);
    }

    private List<BaseFeature> parse(String file)
    {
        try
        {
            return new IDPresetsJsonParser().parse(getSource(file));
        } catch (IOException e)
        {
            throw new RuntimeException();
        }
    }

    private Source getSource(String file) throws IOException {
        return Okio.source(getClass().getClassLoader().getResource(file).openConnection().getInputStream());
    }
}
