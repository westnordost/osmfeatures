package de.westnordost.osmfeatures

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking

class IDPresetsTranslationJsonParserTest {
    @Test
    fun load_features_and_localization() {
        val features = parseResource("one_preset_min.json", "localizations.json")
        assertEquals(1, features.size)

        val feature = features[0]
        assertEquals("some/id", feature.id)
        assertEquals(mapOf("a" to "b", "c" to "d"), feature.tags)
        assertEquals(listOf(GeometryType.POINT), feature.geometry)
        assertEquals("bar", feature.name)
        assertEquals(listOf("bar", "one", "two", "three"), feature.names)
        assertEquals(listOf("a", "b"), feature.terms)
    }

    @Test
    fun load_features_and_localization_defaults() {
        val features = parseResource("one_preset_min.json", "localizations_min.json")
        assertEquals(1, features.size)

        val feature = features[0]
        assertEquals("some/id", feature.id)
        assertEquals(mapOf("a" to "b", "c" to "d"), feature.tags)
        assertEquals(listOf(GeometryType.POINT), feature.geometry)
        assertEquals("bar", feature.name)
        assertTrue(feature.terms.isEmpty())
    }

    @Test
    fun load_features_and_localization_with_placeholder_name() {
        val features = parseResource("one_preset_with_placeholder_name.json", "localizations.json")
        val featuresById = features.associateBy { it.id }
        assertEquals(2, features.size)

        val feature = featuresById["some/id-dingsdongs"]
        assertEquals("some/id-dingsdongs", feature?.id)
        assertEquals(mapOf("a" to "b", "c" to "d"), feature?.tags)
        assertEquals(listOf(GeometryType.POINT), feature?.geometry)
        assertEquals("bar", feature?.name)
        assertEquals(listOf("bar", "one", "two", "three"), feature?.names)
        assertEquals(listOf("a", "b"), feature?.terms)
    }

    @Test
    fun parse_some_real_data() = runBlocking {
        val client = HttpClient(CIO) { expectSuccess = true }

        val presets = client
            .get("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json")
            .bodyAsText()

        val features = IDPresetsJsonParser()
            .parse(presets)
            .associateBy { it.id }

        val translations = client
            .get("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/translations/de.json")
            .bodyAsText()

        val translatedFeatures = IDPresetsTranslationJsonParser().parse(translations, "de-DE", features)
        // should not crash etc
        assertTrue(translatedFeatures.size > 1000)
    }

    private fun parseResource(presetsFile: String, translationsFile: String): List<LocalizedFeature> {
        val baseFeatures = useResource(presetsFile) {
            IDPresetsJsonParser().parse(it)
        }.associateBy { it.id }

        return useResource(translationsFile) {
            IDPresetsTranslationJsonParser().parse(it, "en", baseFeatures)
        }
    }
}
