package osmfeatures

import de.westnordost.osmfeatures.BaseFeature
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import org.junit.Test
import osmfeatures.MapEntry.Companion.tag
import osmfeatures.MapEntry.Companion.mapOf
import de.westnordost.osmfeatures.TestUtils.listOf
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureTagsIndexTest {
    @Test
    fun copes_with_empty_collection() {
        val index: FeatureTagsIndex = index()
        assertTrue(index.getAll(mapOf(tag("a", "b"))).isEmpty())
    }

    @Test
    fun get_two_features_with_same_tags() {
        val f1: Feature = feature(tag("a", "b"))
        val f2: Feature = feature(tag("a", "b"))
        val index: FeatureTagsIndex = index(f1, f2)
        assertEquals(
            listOf(f1, f2),
            index.getAll(mapOf(tag("a", "b"), tag("c", "d")))
        )
    }

    @Test
    fun get_two_features_with_different_tags() {
        val f1: Feature = feature(tag("a", "b"))
        val f2: Feature = feature(tag("c", "d"))
        val index: FeatureTagsIndex = index(f1, f2)
        assertEquals(
            listOf(f1, f2),
            index.getAll(mapOf(tag("a", "b"), tag("c", "d")))
        )
    }

    companion object {
        private fun index(vararg features: Feature): FeatureTagsIndex {
            return FeatureTagsIndex(features.toList())
        }

        private fun feature(vararg mapEntries: MapEntry): Feature {
            return BaseFeature(
                "id",
                mapOf(*mapEntries),
                listOf(GeometryType.POINT),
                null, null,
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
        }
    }
}
