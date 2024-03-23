package de.westnordost.osmfeatures

import okio.Source
import okio.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlin.test.fail

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

    @Test
    fun parse_some_real_data() {
        val client = HttpClient(CIO)
        runBlocking {
            val httpResponse: HttpResponse = client.get("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json")
            if (httpResponse.status.value in 200..299) {
                val body = httpResponse.bodyAsText()
                val features = IDPresetsJsonParser().parse(body)
                val featureMap = HashMap(features.associateBy { it.id })
                val translationHttpResponse: HttpResponse = client.get("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/translations/de.json")
                if (translationHttpResponse.status.value in 200..299) {
                    val translatedFeatures = IDPresetsTranslationJsonParser().parse(
                        translationHttpResponse.bodyAsText(),
                        "de-DE",
                        featureMap
                    )
                    // should not crash etc
                    assertTrue(translatedFeatures.size > 1000)
                }
                else {
                    fail("Unable to retrieve translation Http response")
                }
            }
            else {
                fail("Unable to retrieve response")
            }
        }




    }

    private fun parse(presetsFile: String, translationsFile: String): List<LocalizedFeature> {
        return try {
            val baseFeatures: List<BaseFeature> =
                IDPresetsJsonParser().parse(getSource(presetsFile))
            val featureMap = HashMap(baseFeatures.associateBy { it.id })
            IDPresetsTranslationJsonParser().parse(
                getSource(translationsFile),
                "en",
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
