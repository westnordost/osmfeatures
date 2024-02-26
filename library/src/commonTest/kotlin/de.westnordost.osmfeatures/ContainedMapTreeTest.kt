package de.westnordost.osmfeatures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContainedMapTreeTest {
    @Test
    fun copes_with_empty_feature_collection() {
        val t = tree(emptyList())
        assertTrue(t.getAll(mapOf("a" to "b")).isEmpty())
    }

    @Test
    fun find_single_map() {
        val f1: Map<String, String> = mapOf("a" to "b")
        val t: ContainedMapTree<String, String> = tree(listOf(f1))
        assertEquals(listOf(f1), t.getAll(mapOf("a" to "b", "c" to "d")))
    }

    @Test
    fun dont_find_single_map() {
        val tree: ContainedMapTree<String, String> = tree(listOf(mapOf("a" to "b")))
        assertTrue(tree.getAll(mapOf()).isEmpty())
        assertTrue(tree.getAll(mapOf("c" to "d")).isEmpty())
        assertTrue(tree.getAll(mapOf("a" to "c")).isEmpty())
    }

    @Test
    fun find_only_generic_map() {
        val f1: Map<String, String> = mapOf("a" to "b")
        val f2: Map<String, String> = mapOf("a" to "b", "c" to "d")
        val tree: ContainedMapTree<String, String> = tree(listOf(f1, f2))
        assertEquals(listOf(f1), tree.getAll(mapOf("a" to "b")))
    }

    @Test
    fun find_map_with_one_match_and_with_several_matches() {
        val f1: Map<String, String> = mapOf("a" to "b")
        val f2: Map<String, String> = mapOf("a" to "b", "c" to "d")
        val f3: Map<String, String> = mapOf("a" to "b", "c" to "e")
        val f4: Map<String, String> = mapOf("a" to "b", "d" to "d")
        val tree: ContainedMapTree<String, String> = tree(listOf(f1, f2, f3, f4))
        assertEqualsIgnoreOrder(listOf(f1, f2), tree.getAll(mapOf("a" to "b", "c" to "d")))
    }

    companion object {
        private fun tree(items: Collection<Map<String, String>>): ContainedMapTree<String, String> {
            return ContainedMapTree(items)
        }
    }
}
