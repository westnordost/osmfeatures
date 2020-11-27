package de.westnordost.osmfeatures;

import org.junit.Test;

import static de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StartsWithStringTreeTest
{
    @Test public void copes_with_empty_collection()
    {
        StartsWithStringTree t = new StartsWithStringTree(listOf());
        assertTrue(t.getAll("any").isEmpty());
    }

    @Test public void find_single_string()
    {
        StartsWithStringTree t = new StartsWithStringTree(listOf("anything"));
        assertEquals(listOf("anything"), t.getAll("a"));
        assertEquals(listOf("anything"), t.getAll("any"));
        assertEquals(listOf("anything"), t.getAll("anything"));
    }

    @Test public void dont_find_single_string()
    {
        StartsWithStringTree t = new StartsWithStringTree(listOf("anything", "more", "etc"));

        assertTrue(t.getAll("").isEmpty());
        assertTrue(t.getAll("nything").isEmpty());
        assertTrue(t.getAll("anything else").isEmpty());
    }

    @Test public void find_several_strings()
    {
        StartsWithStringTree t = new StartsWithStringTree(listOf("anything", "anybody", "anytime"));

        assertEqualsIgnoreOrder(listOf("anything", "anybody", "anytime"), t.getAll("any"));
    }
}
