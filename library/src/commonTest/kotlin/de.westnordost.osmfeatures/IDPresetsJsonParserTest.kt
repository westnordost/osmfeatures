package de.westnordost.osmfeatures

import okio.Source
import kotlin.test.Test
import okio.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IDPresetsJsonParserTest {
    @Test
    fun load_features_only() {
        val features: List<BaseFeature> = parse("one_preset_full.json")
        assertEquals(1, features.size)
        val feature: Feature = features[0]
        assertEquals("some/id", feature.id)
        assertEquals(mapOf("a" to "b", "c" to "d"), feature.tags)
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
        assertEquals(mapOf("e" to "f"), feature.addTags)
        assertEquals(mapOf("d" to "g"), feature.removeTags)
    }

    @Test
    fun load_features_only_defaults() {
        val features: List<BaseFeature> = parse("one_preset_min.json")
        assertEquals(1, features.size)
        val feature: Feature = features[0]
        assertEquals("some/id", feature.id)
        assertEquals(mapOf("a" to "b", "c" to "d"), feature.tags)
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

    private fun parse(file: String): List<BaseFeature> {
        return try {
            IDPresetsJsonParser().parse(getSource(file))
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
