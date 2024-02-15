package de.westnordost.osmfeatures

import org.junit.Test
import osmfeatures.TestUtils.assertEqualsIgnoreOrder
import osmfeatures.TestUtils.listOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import osmfeatures.FeatureTermIndex

class FeatureTermIndexTest {
    @Test
    fun copes_with_empty_collection() {
        val index: FeatureTermIndex = index()
        assertTrue(index.getAll("a").isEmpty())
    }

    @Test
    fun get_one_features_with_same_term() {
        val f1: Feature = feature("a", "b")
        val f2: Feature = feature("c")
        val index: FeatureTermIndex = index(f1, f2)
        assertEqualsIgnoreOrder(
            listOf(f1),
            index.getAll("b")
        )
    }

    @Test
    fun get_two_features_with_same_term() {
        val f1: Feature = feature("a", "b")
        val f2: Feature = feature("a", "c")
        val index: FeatureTermIndex = index(f1, f2)
        assertEqualsIgnoreOrder(
            listOf(f1, f2),
            index.getAll("a")
        )
    }

    @Test
    fun get_two_features_with_different_terms() {
        val f1: Feature = feature("anything")
        val f2: Feature = feature("anybody")
        val index: FeatureTermIndex = index(f1, f2)
        assertEqualsIgnoreOrder(
            listOf(f1, f2),
            index.getAll("any")
        )
        assertEqualsIgnoreOrder(
            listOf(f1),
            index.getAll("anyt")
        )
    }

    @Test
    fun dont_get_one_feature_twice() {
        val f1: Feature = feature("something", "someone")
        val index: FeatureTermIndex = index(f1)
        assertEquals(listOf(f1), index.getAll("some"))
    }

    companion object {
        private fun index(vararg features: Feature): FeatureTermIndex {
            return FeatureTermIndex(features.toList()) { feature: Feature -> feature.terms }
        }

        private fun feature(vararg terms: String): Feature {
            return BaseFeature(
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
        }
    }
}
