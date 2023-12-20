package osmfeatures

import de.westnordost.osmfeatures.ContainedMapTree
import org.junit.Test
import de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder
import de.westnordost.osmfeatures.TestUtils.listOf
import osmfeatures.MapEntry.Companion.tag
import osmfeatures.MapEntry.Companion.mapOf
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContainedMapTreeTest {
    @Test
    fun copes_with_empty_feature_collection() {
        val t = tree(emptyList<Map<String,String>>())
        assertTrue(t.getAll(mapOf(tag("a", "b"))).isEmpty())
    }

    @Test
    fun find_single_map() {
        val f1: Map<String, String> = mapOf(tag("a", "b"))
        val t: ContainedMapTree<String, String> = tree(listOf(f1))
        assertEquals(listOf(f1), t.getAll(mapOf(tag("a", "b"), tag("c", "d"))))
    }

    @Test
    fun dont_find_single_map() {
        val tree: ContainedMapTree<String, String> = tree(listOf(mapOf(tag("a", "b"))))
        assertTrue(tree.getAll(mapOf()).isEmpty())
        assertTrue(tree.getAll(mapOf(tag("c", "d"))).isEmpty())
        assertTrue(tree.getAll(mapOf(tag("a", "c"))).isEmpty())
    }

    @Test
    fun find_only_generic_map() {
        val f1: Map<String, String> = mapOf(tag("a", "b"))
        val f2: Map<String, String> = mapOf(tag("a", "b"), tag("c", "d"))
        val tree: ContainedMapTree<String, String> = tree(listOf(f1, f2))
        assertEquals(listOf(f1), tree.getAll(mapOf(tag("a", "b"))))
    }

    @Test
    fun find_map_with_one_match_and_with_several_matches() {
        val f1: Map<String, String> = mapOf(tag("a", "b"))
        val f2: Map<String, String> = mapOf(tag("a", "b"), tag("c", "d"))
        val f3: Map<String, String> = mapOf(tag("a", "b"), tag("c", "e"))
        val f4: Map<String, String> = mapOf(tag("a", "b"), tag("d", "d"))
        val tree: ContainedMapTree<String, String> = tree(listOf(f1, f2, f3, f4))
        assertEqualsIgnoreOrder(listOf(f1, f2), tree.getAll(mapOf(tag("a", "b"), tag("c", "d"))))
    }

    companion object {
        private fun tree(items: Collection<Map<String, String>>): ContainedMapTree<String, String> {
            return ContainedMapTree(items)
        }
    }
}
