package osmfeatures

import de.westnordost.osmfeatures.CollectionUtils
import osmfeatures.MapEntry.Companion.tag
import osmfeatures.MapEntry.Companion.mapOf
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class CollectionUtilsTest {
    @Test
    fun mapContainsEntry() {
        val ab = mapOf(tag("a", "b")).entries.iterator().next()
        val cd = mapOf(tag("c", "d")).entries.iterator().next()
        val ef = mapOf(tag("e", "f")).entries.iterator().next()
        val map = mapOf(tag("a", "b"), tag("c", "d"))
        assertTrue(CollectionUtils.mapContainsEntry(map, ab))
        assertTrue(CollectionUtils.mapContainsEntry(map, cd))
        assertFalse(CollectionUtils.mapContainsEntry(map, ef))
    }

    @Test
    fun numberOfContainedEntriesInMap() {
        val ab = mapOf(tag("a", "b"))
        val abcdef = mapOf(tag("a", "b"), tag("c", "d"), tag("e", "f"))
        val map = mapOf(tag("a", "b"), tag("c", "d"))
        assertEquals(0, CollectionUtils.numberOfContainedEntriesInMap(map, emptyMap<String, String>().entries))
        assertEquals(1, CollectionUtils.numberOfContainedEntriesInMap(map, ab.entries))
        assertEquals(2, CollectionUtils.numberOfContainedEntriesInMap(map, map.entries))
        assertEquals(2, CollectionUtils.numberOfContainedEntriesInMap(map, abcdef.entries))
    }

    @Test
    fun mapContainsAllEntries() {
        val ab = mapOf(tag("a", "b"))
        val abcdef = mapOf(tag("a", "b"), tag("c", "d"), tag("e", "f"))
        val map = mapOf(tag("a", "b"), tag("c", "d"))
        assertTrue(CollectionUtils.mapContainsAllEntries(map, emptyMap<String, String>().entries))
        assertTrue(CollectionUtils.mapContainsAllEntries(map, ab.entries))
        assertTrue(CollectionUtils.mapContainsAllEntries(map, map.entries))
        assertFalse(CollectionUtils.mapContainsAllEntries(map, abcdef.entries))
    }
}
