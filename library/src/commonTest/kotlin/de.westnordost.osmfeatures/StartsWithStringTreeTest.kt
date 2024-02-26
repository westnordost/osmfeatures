package de.westnordost.osmfeatures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StartsWithStringTreeTest {

    @Test
    fun copes_with_empty_collection() {
        assertTrue(tree().getAll("any").isEmpty())
    }

    @Test
    fun find_single_string() {
        val t = tree("anything")
        assertEquals(listOf("anything"), t.getAll("a"))
        assertEquals(listOf("anything"), t.getAll("any"))
        assertEquals(listOf("anything"), t.getAll("anything"))
    }

    @Test
    fun do_not_find_single_string() {
        val t = tree("anything", "more", "etc")
        assertTrue(t.getAll("").isEmpty())
        assertTrue(t.getAll("nything").isEmpty())
        assertTrue(t.getAll("anything else").isEmpty())
    }

    @Test
    fun find_several_strings() {
        val t = tree("anything", "anybody", "anytime")
        assertEqualsIgnoreOrder(listOf("anything", "anybody", "anytime"), t.getAll("any"))
    }
}

private fun tree(vararg strings: String) = StartsWithStringTree(strings.toList())