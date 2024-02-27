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
        val f1 = feature("a" to "b")
        val f2 = feature("a" to "b")
        val index = index(f1, f2)
        assertEquals(
            listOf(f1, f2),
            index.getAll(mapOf("a" to "b", "c" to "d"))
        )
    }

    @Test
    fun get_two_features_with_different_tags() {
        val f1 = feature("a" to "b")
        val f2 = feature("c" to "d")
        val index = index(f1, f2)
        assertEquals(
            listOf(f1, f2),
            index.getAll(mapOf("a" to "b", "c" to "d"))
        )
    }
}

private fun index(vararg features: Feature) = FeatureTagsIndex(features.toList())

private fun feature(vararg pairs: Pair<String, String>): Feature = BaseFeature(
    "id",
    mapOf(*pairs),
    listOf(GeometryType.POINT),
    null,
    null,
    listOf("name"),
    listOf(),
    listOf(),
    listOf(),
    true,
    1.0f,
    false,
    mapOf(),
    mapOf()
)