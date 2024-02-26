package de.westnordost.osmfeatures

import okio.Source
import okio.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class IDPresetsTranslationJsonParserTest {
    @Test
    fun load_features_and_localization() {
        val features: List<LocalizedFeature> = parse("one_preset_min.json", "localizations.json")
        assertEquals(1, features.size)
        val feature: Feature = features[0]
        assertEquals("some/id", feature.id)
        assertEquals(mapOf("a" to "b", "c" to "d"), feature.tags)
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
        assertEquals(mapOf("a" to "b", "c" to "d"), feature.tags)
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
        assertEquals(mapOf("a" to "b", "c" to "d"), feature?.tags)
        assertEquals(listOf(GeometryType.POINT), feature?.geometry)
        assertEquals("bar", feature?.name)
        assertEquals(listOf("bar", "one", "two", "three"), feature?.names)
        assertEquals(listOf("a", "b"), feature?.terms)
    }

    private fun parse(presetsFile: String, translationsFile: String): List<LocalizedFeature> {
        return try {
            val baseFeatures: List<BaseFeature> =
                IDPresetsJsonParser().parse(getSource(presetsFile))
            val featureMap = HashMap(baseFeatures.associateBy { it.id })
            IDPresetsTranslationJsonParser().parse(
                getSource(translationsFile),
                Locale("en"),
                featureMap
            )
        } catch (e: IOException) {
            throw RuntimeException()
        }
    }

    @Throws(IOException::class)
    private fun getSource(file: String): Source {
        val fileSystemAccess = FileSystemAccess("src/commonTest/resources")
        return fileSystemAccess.open(file)
    }
}
