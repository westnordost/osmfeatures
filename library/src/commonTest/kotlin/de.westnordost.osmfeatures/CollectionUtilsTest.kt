package de.westnordost.osmfeatures

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class CollectionUtilsTest {
    @Test
    fun mapContainsEntry() {
        val ab = mapOf("a" to "b").entries.iterator().next()
        val cd = mapOf("c" to "d").entries.iterator().next()
        val ef = mapOf("e" to "f").entries.iterator().next()
        val map = mapOf("a" to "b", "c" to "d")
        assertTrue(CollectionUtils.mapContainsEntry(map, ab))
        assertTrue(CollectionUtils.mapContainsEntry(map, cd))
        assertFalse(CollectionUtils.mapContainsEntry(map, ef))
    }

    @Test
    fun numberOfContainedEntriesInMap() {
        val ab = mapOf("a" to "b")
        val abcdef = mapOf("a" to "b", "c" to "d", "e" to "f")
        val map = mapOf("a" to "b", "c" to "d")
        assertEquals(0, CollectionUtils.numberOfContainedEntriesInMap(map, emptyMap<String, String>().entries))
        assertEquals(1, CollectionUtils.numberOfContainedEntriesInMap(map, ab.entries))
        assertEquals(2, CollectionUtils.numberOfContainedEntriesInMap(map, map.entries))
        assertEquals(2, CollectionUtils.numberOfContainedEntriesInMap(map, abcdef.entries))
    }

    @Test
    fun mapContainsAllEntries() {
        val ab = mapOf("a" to "b")
        val abcdef = mapOf("a" to "b", "c" to "d", "e" to "f")
        val map = mapOf("a" to "b", "c" to "d")
        assertTrue(CollectionUtils.mapContainsAllEntries(map, emptyMap<String, String>().entries))
        assertTrue(CollectionUtils.mapContainsAllEntries(map, ab.entries))
        assertTrue(CollectionUtils.mapContainsAllEntries(map, map.entries))
        assertFalse(CollectionUtils.mapContainsAllEntries(map, abcdef.entries))
    }
}
