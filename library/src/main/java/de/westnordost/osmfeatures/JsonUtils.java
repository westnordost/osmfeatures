package de.westnordost.osmfeatures;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class JsonUtils {
    public interface Transformer<T> { T apply(Object item); }

    public static <T> List<T> parseList(JSONArray array, Transformer<T> t) throws JSONException
    {
        if(array == null) return Collections.emptyList();
        List<T> result = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++)
        {
            T item = t.apply(array.get(i));
            if(item != null) result.add(item);
        }
        return Collections.unmodifiableList(result);
    }

    public static Map<String, String> parseStringMap(JSONObject map) throws JSONException
    {
        if(map == null) return Collections.emptyMap();
        Map<String, String> result = new HashMap<>(map.length());
        for (Iterator<String> it = map.keys(); it.hasNext(); )
        {
            String key = it.next().intern();
            result.put(key, map.getString(key));
        }
        return Collections.unmodifiableMap(result);
    }

    // this is only necessary because Android uses some old version of org.json where
    // new JSONObject(new JSONTokener(inputStream)) is not defined...
    public static JSONObject createFromInputStream(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1)
        {
            result.write(buffer, 0, length);
        }
        String jsonString = result.toString("UTF-8");
        return new JSONObject(jsonString);
    }
}
