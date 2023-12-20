package osmfeatures

import de.westnordost.osmfeatures.BaseFeature
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.osmfeatures.IDPresetsJsonParser
import de.westnordost.osmfeatures.IDPresetsTranslationJsonParser
import de.westnordost.osmfeatures.Locale
import de.westnordost.osmfeatures.LocalizedFeature
import okio.FileSystem
import okio.Okio
import okio.Path
import okio.Source
import org.junit.Test
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import osmfeatures.MapEntry.Companion.tag
import osmfeatures.MapEntry.Companion.mapOf
import de.westnordost.osmfeatures.TestUtils.listOf
import okio.source
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class IDPresetsTranslationJsonParserTest {
    @Test
    fun load_features_and_localization() {
        val features: List<LocalizedFeature> = parse("one_preset_min.json", "localizations.json")
        assertEquals(1, features.size)
        val feature: Feature = features[0]
        assertEquals("some/id", feature.id)
        assertEquals(mapOf(tag("a", "b"), tag("c", "d")), feature.tags)
        assertEquals(listOf(GeometryType.POINT), feature.geometry)
        assertEquals("bar", feature.name)
        assertEquals(listOf("bar", "one", "two", "three"), feature.names)
        assertEquals(listOf("a", "b"), feature.terms)
    }

    @Test
    fun load_features_and_localization_defaults() {
        val features: List<LocalizedFeature> =
            parse("one_preset_min.json", "localizations_min.json")
        assertEquals(1, features.size)
        val feature: Feature = features[0]
        assertEquals("some/id", feature.id)
        assertEquals(mapOf(tag("a", "b"), tag("c", "d")), feature.tags)
        assertEquals(listOf(GeometryType.POINT), feature.geometry)
        assertEquals("bar", feature.name)
        assertTrue(feature.terms.isEmpty())
    }

    @Test
    fun load_features_and_localization_with_placeholder_name() {
        val features: List<LocalizedFeature> =
            parse("one_preset_with_placeholder_name.json", "localizations.json")
        val featuresById = HashMap(features.associateBy { it.id })
        assertEquals(2, features.size)
        val feature: Feature? = featuresById["some/id-dingsdongs"]
        assertEquals("some/id-dingsdongs", feature?.id)
        assertEquals(mapOf(tag("a", "b"), tag("c", "d")), feature?.tags)
        assertEquals(listOf(GeometryType.POINT), feature?.geometry)
        assertEquals("bar", feature?.name)
        assertEquals(listOf("bar", "one", "two", "three"), feature?.names)
        assertEquals(listOf("a", "b"), feature?.terms)
    }

    @Test
    @kotlin.Throws(IOException::class, URISyntaxException::class)
    fun parse_some_real_data() {
        val url =
            URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json")
        val features: List<BaseFeature> =
            IDPresetsJsonParser().parse(url.openConnection().getInputStream().source())
        val featureMap = HashMap(features.associateBy { it.id })
        val rawTranslationsURL =
            URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/translations/de.json")
        val translatedFeatures: List<LocalizedFeature> = IDPresetsTranslationJsonParser().parse(
            rawTranslationsURL.openStream().source(),
            Locale.GERMAN,
            featureMap
        )

        // should not crash etc
        assertTrue(translatedFeatures.size > 1000)
    }

    private fun parse(presetsFile: String, translationsFile: String): List<LocalizedFeature> {
        return try {
            val baseFeatures: List<BaseFeature> =
                IDPresetsJsonParser().parse(getSource(presetsFile))
            val featureMap = HashMap(baseFeatures.associateBy { it.id })
            IDPresetsTranslationJsonParser().parse(
                getSource(translationsFile),
                Locale.ENGLISH,
                featureMap
            )
        } catch (e: IOException) {
            throw RuntimeException()
        }
    }

    @kotlin.Throws(IOException::class)
    private fun getSource(file: String): Source {
        val resourceStream = this.javaClass.getResourceAsStream(file)
            ?: throw IOException("Could not retrieve file $file in resource assets")
        return resourceStream.source()
    }
}
