package de.westnordost.osmfeatures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureTermIndexTest {
    @Test
    fun copes_with_empty_collection() {
        val index = index()
        assertTrue(index.getAll("a").isEmpty())
    }

    @Test
    fun get_one_features_with_same_term() {
        val f1 = feature("a", "b")
        val f2 = feature("c")
        val index = index(f1, f2)
        assertEquals(setOf(f1), index.getAll("b").toSet())
    }

    @Test
    fun get_two_features_with_same_term() {
        val f1 = feature("a", "b")
        val f2 = feature("a", "c")
        val index = index(f1, f2)
        assertEquals(setOf(f1, f2), index.getAll("a").toSet())
    }

    @Test
    fun get_two_features_with_different_terms() {
        val f1 = feature("anything")
        val f2 = feature("anybody")
        val index = index(f1, f2)
        assertEquals(setOf(f1, f2), index.getAll("any").toSet())
        assertEquals(setOf(f1), index.getAll("anyt").toSet())
    }

    @Test
    fun do_not_get_one_feature_twice() {
        val f1 = feature("something", "someone")
        val index = index(f1)
        assertEquals(listOf(f1), index.getAll("some"))
    }
}

private fun index(vararg features: Feature) = FeatureTermIndex(features.toList()) { it.terms }

private fun feature(vararg terms: String): Feature = BaseFeature(
    "id",
    mapOf(),
    listOf(GeometryType.POINT),
    null,
    null,
    listOf("name"),
    terms.toList(),
    listOf(),
    listOf(),
    true,
    1.0f,
    false,
    mapOf(),
    mapOf()
)