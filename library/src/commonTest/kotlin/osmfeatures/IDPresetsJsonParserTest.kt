package de.westnordost.osmfeatures

import okio.FileSystem
import okio.Source
import org.junit.Test
import osmfeatures.MapEntry.Companion.tag
import osmfeatures.MapEntry.Companion.mapOf
import osmfeatures.TestUtils.listOf
import okio.IOException
import okio.Path.Companion.toPath
import okio.source
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.net.URL

class IDPresetsJsonParserTest {
    @Test
    fun load_features_only() {
        val features: List<BaseFeature> = parse("one_preset_full.json")
        assertEquals(1, features.size)
        val feature: Feature = features[0]
        assertEquals("some/id", feature.id)
        assertEquals(mapOf(tag("a", "b"), tag("c", "d")), feature.tags)
        assertEquals(
            listOf(
                GeometryType.POINT,
                GeometryType.VERTEX,
                GeometryType.LINE,
                GeometryType.AREA,
                GeometryType.RELATION
            ), feature.geometry
        )
        assertEquals(listOf("DE", "GB"), feature.includeCountryCodes)
        assertEquals(listOf("IT"), feature.excludeCountryCodes)
        assertEquals("foo", feature.name)
        assertEquals("abc", feature.icon)
        assertEquals("someurl", feature.imageURL)
        assertEquals(listOf("foo", "one", "two"), feature.names)
        assertEquals(listOf("1", "2"), feature.terms)
        assertEquals(0.5f, feature.matchScore, 0.001f)
        assertFalse(feature.isSearchable)
        assertEquals(mapOf(tag("e", "f")), feature.addTags)
        assertEquals(mapOf(tag("d", "g")), feature.removeTags)
    }

    @Test
    fun load_features_only_defaults() {
        val features: List<BaseFeature> = parse("one_preset_min.json")
        assertEquals(1, features.size)
        val feature: Feature = features[0]
        assertEquals("some/id", feature.id)
        assertEquals(mapOf(tag("a", "b"), tag("c", "d")), feature.tags)
        assertEquals(listOf(GeometryType.POINT), feature.geometry)
        assertTrue(feature.includeCountryCodes.isEmpty())
        assertTrue(feature.excludeCountryCodes.isEmpty())
        assertEquals("", feature.name)
        assertEquals("", feature.icon)
        assertEquals("", feature.imageURL)
        assertEquals(1, feature.names.size)
        assertTrue(feature.terms.isEmpty())
        assertEquals(1.0f, feature.matchScore, 0.001f)
        assertTrue(feature.isSearchable)
        assertEquals(feature.addTags, feature.tags)
        assertEquals(feature.addTags, feature.removeTags)
    }

    @Test
    fun load_features_unsupported_location_set() {
        val features: List<BaseFeature> = parse("one_preset_unsupported_location_set.json")
        assertEquals(2, features.size)
        assertEquals("some/ok", features[0].id)
        assertEquals("another/ok", features[1].id)
    }

    @Test
    fun load_features_no_wildcards() {
        val features: List<BaseFeature> = parse("one_preset_wildcard.json")
        assertTrue(features.isEmpty())
    }

    @Test
    @kotlin.Throws(IOException::class)
    fun parse_some_real_data() {
        val url =
            URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json")
        val features: List<BaseFeature> = IDPresetsJsonParser().parse(url.openStream().source())
        // should not crash etc
        assertTrue(features.size > 1000)
    }

    private fun parse(file: String): List<BaseFeature> {
        return try {
            IDPresetsJsonParser().parse(getSource(file))
        } catch (e: IOException) {
            throw RuntimeException()
        }
    }

    @kotlin.Throws(IOException::class)
    private fun getSource(file: String): Source {
        val resourcePath = "src/commonTest/resources/${file}".toPath()
        return FileSystem.SYSTEM.source(resourcePath)
    }
}
