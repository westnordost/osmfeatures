package de.westnordost.osmfeatures;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.westnordost.osmfeatures.MapEntry.mapOf;
import static de.westnordost.osmfeatures.MapEntry.tag;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IDPresetsTranslationJsonParserTest {

    @Test public void load_features_and_localization()
    {
        List<LocalizedFeature> features = parse("one_preset_min.json", "localizations.json");

        assertEquals(1, features.size());
        Feature feature = features.get(0);

        assertEquals("some/id", feature.getId());
        assertEquals(mapOf(tag("a","b"), tag("c","d")), feature.getTags());
        assertEquals(listOf(GeometryType.POINT), feature.getGeometry());
        assertEquals("bar", feature.getName());
        assertEquals(listOf("bar", "one", "two", "three"), feature.getNames());
        assertEquals(listOf("a", "b"), feature.getTerms());
    }

    @Test public void load_features_and_localization_defaults()
    {
        List<LocalizedFeature> features = parse("one_preset_min.json", "localizations_min.json");

        assertEquals(1, features.size());
        Feature feature = features.get(0);

        assertEquals("some/id", feature.getId());
        assertEquals(mapOf(tag("a","b"), tag("c","d")), feature.getTags());
        assertEquals(listOf(GeometryType.POINT), feature.getGeometry());
        assertEquals("bar", feature.getName());
        assertTrue(feature.getTerms().isEmpty());
    }

    @Test public void parse_some_real_data() throws IOException
    {
        URL url = new URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json");
        List<BaseFeature> features = new IDPresetsJsonParser().parse(url.openStream());
        Map<String, BaseFeature> featureMap = new HashMap<>();
        for (BaseFeature feature : features)
        {
            featureMap.put(feature.getId(), feature);
        }

        URL rawTranslationsURL = new URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/translations/de.json");
        List<LocalizedFeature> translatedFeatures = new IDPresetsTranslationJsonParser().parse(rawTranslationsURL.openStream(), Locale.GERMAN, featureMap);

        // should not crash etc
        assertTrue(translatedFeatures.size() > 1000);
    }

    private List<LocalizedFeature> parse(String presetsFile, String translationsFile)
    {
        try
        {
            List<BaseFeature> baseFeatures = new IDPresetsJsonParser().parse(getStream(presetsFile));
            Map<String, BaseFeature> featureMap = new HashMap<>();
            for (BaseFeature feature : baseFeatures)
            {
                featureMap.put(feature.getId(), feature);
            }
            return new IDPresetsTranslationJsonParser().parse(getStream(translationsFile), Locale.ENGLISH, featureMap);
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
