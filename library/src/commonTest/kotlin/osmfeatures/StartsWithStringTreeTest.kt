package de.westnordost.osmfeatures

import org.junit.Test
import osmfeatures.TestUtils.assertEqualsIgnoreOrder
import osmfeatures.TestUtils.listOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class StartsWithStringTreeTest {
    @Test
    fun copes_with_empty_collection() {
        val t = StartsWithStringTree(listOf())
        assertTrue(t.getAll("any").isEmpty())
    }

    @Test
    fun find_single_string() {
        val t = StartsWithStringTree(listOf("anything"))
        assertEquals(listOf("anything"), t.getAll("a"))
        assertEquals(listOf("anything"), t.getAll("any"))
        assertEquals(listOf("anything"), t.getAll("anything"))
    }

    @Test
    fun dont_find_single_string() {
        val t = StartsWithStringTree(listOf("anything", "more", "etc"))
        assertTrue(t.getAll("").isEmpty())
        assertTrue(t.getAll("nything").isEmpty())
        assertTrue(t.getAll("anything else").isEmpty())
    }

    @Test
    fun find_several_strings() {
        val t = StartsWithStringTree(listOf("anything", "anybody", "anytime"))
        assertEqualsIgnoreOrder(listOf("anything", "anybody", "anytime"), t.getAll("any"))
    }
}
