package de.westnordost.osmfeatures;

import org.junit.Test;

import java.util.Collection;
import java.util.Map;

import static de.westnordost.osmfeatures.MapEntry.mapOf;
import static de.westnordost.osmfeatures.MapEntry.tag;
import static de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContainedMapTreeTest
{
    @Test public void copes_with_empty_feature_collection()
    {
        ContainedMapTree<String, String> t = tree(listOf());
        assertTrue(t.getAll(mapOf(tag("a", "b"))).isEmpty());
    }

    @Test public void find_single_map()
    {
        Map<String, String> f1 = mapOf(tag("a", "b"));
        ContainedMapTree<String, String> t = tree(listOf(f1));

        assertEquals(listOf(f1), t.getAll(mapOf(tag("a", "b"), tag("c", "d"))));
    }

    @Test public void dont_find_single_map()
    {
        ContainedMapTree<String, String> tree = tree(listOf(mapOf(tag("a", "b"))));

        assertTrue(tree.getAll(mapOf()).isEmpty());
        assertTrue(tree.getAll(mapOf(tag("c", "d"))).isEmpty());
        assertTrue(tree.getAll(mapOf(tag("a", "c"))).isEmpty());
    }

    @Test public void find_only_generic_map()
    {
        Map<String, String> f1 = mapOf(tag("a", "b"));
        Map<String, String> f2 = mapOf(tag("a", "b"), tag("c", "d"));
        ContainedMapTree<String, String> tree = tree(listOf(f1, f2));

        assertEquals(listOf(f1), tree.getAll(mapOf(tag("a", "b"))));
    }

    @Test public void find_map_with_one_match_and_with_several_matches()
    {
        Map<String, String> f1 = mapOf(tag("a", "b"));
        Map<String, String> f2 = mapOf(tag("a", "b"), tag("c", "d"));
        Map<String, String> f3 = mapOf(tag("a", "b"), tag("c", "e"));
        Map<String, String> f4 = mapOf(tag("a", "b"), tag("d", "d"));
        ContainedMapTree<String, String> tree = tree(listOf(f1, f2, f3, f4));

        assertEqualsIgnoreOrder(listOf(f1, f2), tree.getAll(mapOf(tag("a", "b"), tag("c", "d"))));
    }

    private static ContainedMapTree<String, String> tree(Collection<Map<String, String>> items)
    {
        return new ContainedMapTree<>(items);
    }
}
