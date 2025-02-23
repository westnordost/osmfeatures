package de.westnordost.osmfeatures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureTagsIndexTest {
    @Test
    fun copes_with_empty_collection() {
        val index = index()
        assertTrue(index.getAll(mapOf("a" to "b")).isEmpty())
    }

    @Test
    fun get_two_features_with_same_tags() {
        val f1 = feature(mapOf("a" to "b"))
        val f2 = feature(mapOf("a" to "b"))
        val index = index(f1, f2)
        assertEquals(
            listOf(f1, f2),
            index.getAll(mapOf("a" to "b", "c" to "d"))
        )
    }

    @Test
    fun get_two_features_with_different_tags() {
        val f1 = feature(mapOf("a" to "b"))
        val f2 = feature(mapOf("c" to "d"))
        val index = index(f1, f2)
        assertEquals(
            listOf(f1, f2),
            index.getAll(mapOf("a" to "b", "c" to "d"))
        )
    }

    @Test
    fun get_feature_with_wildcard_value() {
        val f1 = feature(mapOf("a" to "b"), setOf("c"))
        val f2 = feature(mapOf("a" to "b"), setOf("d"))
        val index = index(f1, f2)

        assertEquals(
            listOf(),
            index.getAll(mapOf("a" to "b"))
        )

        assertEquals(
            listOf(f1),
            index.getAll(mapOf("a" to "b", "c" to "anything"))
        )

        assertEquals(
            listOf(f1, f2),
            index.getAll(mapOf("a" to "b", "c" to "anything", "d" to "x"))
        )
    }
}

private fun index(vararg features: Feature) = FeatureTagsIndex(features.toList())

private fun feature(
    tags: Map<String, String>,
    keys: Set<String> = setOf()
): Feature = BaseFeature(
    id = "id",
    tags = tags,
    geometry = listOf(GeometryType.POINT),
    icon = null,
    imageURL = null,
    names = listOf("name"),
    terms = listOf(),
    includeCountryCodes = listOf(),
    excludeCountryCodes = listOf(),
    isSearchable = true,
    matchScore = 1.0f,
    isSuggestion = false,
    addTags = mapOf(),
    removeTags = mapOf(),
    preserveTags = listOf(),
    tagKeys = keys,
    addTagKeys = setOf(),
    removeTagKeys = setOf()
)