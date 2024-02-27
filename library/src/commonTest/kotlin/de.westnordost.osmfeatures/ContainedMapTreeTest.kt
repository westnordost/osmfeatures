package de.westnordost.osmfeatures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContainedMapTreeTest {
    @Test
    fun copes_with_empty_feature_collection() {
        assertTrue(tree().getAll(mapOf("a" to "b")).isEmpty())
    }

    @Test
    fun find_single_map() {
        val f1 = mapOf("a" to "b")
        val t = tree(f1)
        assertEquals(listOf(f1), t.getAll(mapOf("a" to "b", "c" to "d")))
    }

    @Test
    fun do_not_find_single_map() {
        val tree = tree(mapOf("a" to "b"))
        assertTrue(tree.getAll(mapOf()).isEmpty())
        assertTrue(tree.getAll(mapOf("c" to "d")).isEmpty())
        assertTrue(tree.getAll(mapOf("a" to "c")).isEmpty())
    }

    @Test
    fun find_only_generic_map() {
        val f1 = mapOf("a" to "b")
        val f2 = mapOf("a" to "b", "c" to "d")
        val tree = tree(f1, f2)
        assertEquals(listOf(f1), tree.getAll(mapOf("a" to "b")))
    }

    @Test
    fun find_map_with_one_match_and_with_several_matches() {
        val f1 = mapOf("a" to "b")
        val f2 = mapOf("a" to "b", "c" to "d")
        val f3 = mapOf("a" to "b", "c" to "e")
        val f4 = mapOf("a" to "b", "d" to "d")
        val tree = tree(f1, f2, f3, f4)
        assertEquals(setOf(f1, f2), tree.getAll(mapOf("a" to "b", "c" to "d")).toSet())
    }
}

private fun tree(vararg items: Map<String, String>) = ContainedMapTree(items.toList())
