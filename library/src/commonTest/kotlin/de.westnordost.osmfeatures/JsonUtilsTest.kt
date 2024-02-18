package de.westnordost.osmfeatures

import kotlinx.serialization.json.JsonArray
import org.junit.Test
import de.westnordost.osmfeatures.JsonUtils.parseList
import de.westnordost.osmfeatures.JsonUtils.parseStringMap
import org.junit.Assert.assertEquals

class JsonUtilsTest {
    @Test
    fun parseList_with_null_json_array() {
        assertEquals(0, parseList(null) { obj -> obj }.size)
    }

    @Test
    fun parseList_with_empty_json_array() {
        assertEquals(0, parseList(JsonArray(listOf())) { obj -> obj }.size)
    }
    @Test
    fun parseStringMap_with_null_json_map() {
        assertEquals(0, parseStringMap(null).size)
    }
}
