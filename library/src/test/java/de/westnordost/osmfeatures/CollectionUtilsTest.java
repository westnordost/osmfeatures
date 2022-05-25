package de.westnordost.osmfeatures;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static de.westnordost.osmfeatures.MapEntry.mapOf;
import static de.westnordost.osmfeatures.MapEntry.tag;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CollectionUtilsTest
{
    @Test public void removeIf()
    {
         List<Integer> ints = new ArrayList<>(listOf(1,2,3,4,5,6,7,8,9,10));
         CollectionUtils.removeIf(ints, i -> i % 2 == 0);
         assertEquals(listOf(1,3,5,7,9), ints);
    }

    @Test public void find()
    {
        List<String> strs = new ArrayList<>(listOf("one", "two", "three"));
        assertEquals("two", CollectionUtils.find(strs, str -> str.equals("two")));
        assertNull(CollectionUtils.find(strs, str -> str.equals("four")));
    }

    @Test public void mapContainsEntry()
    {
        Map.Entry<String, String> ab = mapOf(tag("a", "b")).entrySet().iterator().next();
        Map.Entry<String, String> cd = mapOf(tag("c", "d")).entrySet().iterator().next();
        Map.Entry<String, String> ef = mapOf(tag("e", "f")).entrySet().iterator().next();
        Map<String, String> map = mapOf(tag("a", "b"), tag("c", "d"));

        assertTrue(CollectionUtils.mapContainsEntry(map, ab));
        assertTrue(CollectionUtils.mapContainsEntry(map, cd));
        assertFalse(CollectionUtils.mapContainsEntry(map, ef));
    }

    @Test public void numberOfContainedEntriesInMap()
    {
        Map<String, String> ab = mapOf(tag("a", "b"));
        Map<String, String> abcdef = mapOf(tag("a", "b"), tag("c", "d"), tag("e", "f"));

        Map<String, String> map = mapOf(tag("a", "b"), tag("c", "d"));

        assertEquals(0, CollectionUtils.numberOfContainedEntriesInMap(map, mapOf().entrySet()));
        assertEquals(1, CollectionUtils.numberOfContainedEntriesInMap(map, ab.entrySet()));
        assertEquals(2, CollectionUtils.numberOfContainedEntriesInMap(map, map.entrySet()));
        assertEquals(2, CollectionUtils.numberOfContainedEntriesInMap(map, abcdef.entrySet()));
    }

    @Test public void mapContainsAllEntries()
    {
        Map<String, String> ab = mapOf(tag("a", "b"));
        Map<String, String> abcdef = mapOf(tag("a", "b"), tag("c", "d"), tag("e", "f"));

        Map<String, String> map = mapOf(tag("a", "b"), tag("c", "d"));

        assertTrue(CollectionUtils.mapContainsAllEntries(map, mapOf().entrySet()));
        assertTrue(CollectionUtils.mapContainsAllEntries(map, ab.entrySet()));
        assertTrue(CollectionUtils.mapContainsAllEntries(map, map.entrySet()));
        assertFalse(CollectionUtils.mapContainsAllEntries(map, abcdef.entrySet()));
    }
}
