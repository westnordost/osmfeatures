package de.westnordost.osmfeatures;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Map;

import static de.westnordost.osmfeatures.JsonUtils.parseList;
import static de.westnordost.osmfeatures.JsonUtils.parseStringMap;
import static de.westnordost.osmfeatures.MapEntry.mapOf;
import static de.westnordost.osmfeatures.MapEntry.tag;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.assertEquals;

public class JsonUtilsTest
{
    @Test public void parseList_with_null_json_array()
    {
        assertEquals(0, parseList(null, obj -> obj).size());
    }

    @Test public void parseList_with_empty_json_array()
    {
        assertEquals(0, parseList(new JSONArray(), obj -> obj).size());
    }

    @Test public void parseList_with_array()
    {
        String[] array = new String[]{"a","b","c"};

        assertEquals(listOf(array), parseList(new JSONArray(array), obj -> obj));
    }

    @Test public void parseList_with_array_and_transformer()
    {
        int[] array = new int[]{1,2,3};
        assertEquals(listOf(2,4,6), parseList(new JSONArray(array), i -> (int)i*2));
    }

    @Test public void parseStringMap_with_null_json_map()
    {
        assertEquals(0, parseStringMap(null).size());
    }

    @Test public void parseStringMap_with_empty_json_map()
    {
        assertEquals(0, parseStringMap(new JSONObject()).size());
    }

    @Test public void parseStringMap_with_one_entry()
    {
        Map<String, String> m = mapOf(tag("a","b"));
        assertEquals(m, parseStringMap(new JSONObject(m)));
    }

    @Test public void parseStringMap_with_several_entries()
    {
        Map<String, String> m = mapOf(tag("a","b"), tag("c", "d"));
        assertEquals(m, parseStringMap(new JSONObject(m)));
    }
}
