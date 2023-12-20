package de.westnordost.osmfeatures

import kotlinx.serialization.json.JsonArray
import org.junit.Test
import de.westnordost.osmfeatures.JsonUtils.parseList
import de.westnordost.osmfeatures.JsonUtils.parseStringMap
import de.westnordost.osmfeatures.TestUtils.listOf
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

    //    @Test public void parseList_with_array()
    //    {
    //        String[] array = new String[]{"a","b","c"};
    //        JsonPrimitive prim = new JsonElement() {
    //        }
    //        assertEquals(listOf(array), parseList(new JsonArray(null)., obj -> obj));
    //    }
    //    @Test public void parseList_with_array_and_transformer()
    //    {
    //        int[] array = new int[]{1,2,3};
    //        assertEquals(listOf(2,4,6), parseList(new JSONArray(array), i -> (int)i*2));
    //    }
    @Test
    fun parseStringMap_with_null_json_map() {
        assertEquals(0, parseStringMap(null).size)
    } //    @Test public void parseStringMap_with_empty_json_map()
    //    {
    //        assertEquals(0, parseStringMap(new JSONObject()).size());
    //    }
    //    @Test public void parseStringMap_with_one_entry()
    //    {
    //        Map<String, String> m = mapOf(tag("a","b"));
    //        assertEquals(m, parseStringMap(new JSONObject(m)));
    //    }
    //
    //    @Test public void parseStringMap_with_several_entries()
    //    {
    //        Map<String, String> m = mapOf(tag("a","b"), tag("c", "d"));
    //        assertEquals(m, parseStringMap(new JSONObject(m)));
    //    }
}
